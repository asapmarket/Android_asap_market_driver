package com.mhysa.waimai.kuaidi.ui.activities.errandorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.NetworkUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.joey.devilfish.widget.CircularProgress;
import com.joey.devilfish.widget.listview.XListView;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrder;
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrderList;
import com.mhysa.waimai.kuaidi.model.order.OrderListRequest;
import com.mhysa.waimai.kuaidi.ui.adapters.errandorder.ErrandOrderListAdapter;
import com.mhysa.waimai.kuaidi.ui.customerviews.SearchLayout;
import com.mhysa.waimai.kuaidi.ui.fragments.bottommenu.FeedbackPriceBottomFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 文件描述
 * Date: 2018/3/24
 *
 * @author xusheng
 */

public class ErrandOrderListActivity extends BaseActivity {

    private static final String TAG = ErrandOrderListActivity.class.getSimpleName();

    private static final String INTENT_ZIPCODE = "intent_zipcode";

    private static final String INTENT_REQUEST = "intent_request";

    private static final String INTENT_TAG = "intent_tag";

    public static final String TAG_HOME = "tag_home";

    public static final String TAG_ORDER = "tag_order";

    public static final String TAG_ORDER_MANAGER = "tag_order_manager";

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.tv_zip_code)
    TextView mZipCodeTv;

    @Bind(R.id.layout_search)
    SearchLayout mSearchLayout;

    @Bind(R.id.lv_errand_order)
    XListView mOrderLv;

    private OrderListRequest mOrderRequest;

    private String mTag;

    private String mZipCode;

    private int mCurrentPage = FusionCode.PageConstant.INIT_PAGE;

    private View mFooterView;

    private TextView mLoadMoreTv;

    private CircularProgress mLoadMoreProgress;

    private ArrayList<ErrandOrder> mOrders = new ArrayList<ErrandOrder>();

    private ErrandOrderListAdapter mAdapter;

    private int mLastVisibleIndex;

    private boolean mIsLoadMore = false;

    private boolean mIsRefresh = false;

    private boolean mIsInit = false;

    private boolean mHasAddFooterView = false;

    private boolean mIsOffWork = false;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_errand_order_list;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mOrderRequest = (OrderListRequest) getIntent().getSerializableExtra(INTENT_REQUEST);
            mTag = getIntent().getStringExtra(INTENT_TAG);
            mZipCode = getIntent().getStringExtra(INTENT_ZIPCODE);
        }

        if (null == mOrderRequest) {
            mOrderRequest = new OrderListRequest();
        }

        mOrderRequest.zip_code = mZipCode;
        mOrderRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;
        mOrderRequest.page = FusionCode.PageConstant.INIT_PAGE;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        StringUtils.getInstance().setText(mZipCode, mZipCodeTv);

        if (mTag.equals(ErrandOrderListActivity.TAG_HOME)) {
            mOrderRequest.state = FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS;
        } else if (mTag.equals(ErrandOrderListActivity.TAG_ORDER)) {
            mOrderRequest.exp_id = Utils.getUserId(ErrandOrderListActivity.this);
            mOrderRequest.state = FusionCode.OrderStatus.ORDER_STATUS_UNFINISHED;
        } else if (mTag.equals(ErrandOrderListActivity.TAG_ORDER_MANAGER)) {
            mOrderRequest.exp_id = Utils.getUserId(ErrandOrderListActivity.this);
            mOrderRequest.state = FusionCode.OrderStatus.ORDER_STATUS_ALL;
        }

        if (mTag.equals(ErrandOrderListActivity.TAG_HOME)) {
            mSearchLayout.setVisibility(View.GONE);
            mTitleTv.setText(R.string.ungrab_order);
        } else {
            mTitleTv.setText(R.string.order_manager);
            mSearchLayout.setVisibility(View.VISIBLE);

            if (mTag.equals(ErrandOrderListActivity.TAG_ORDER)) {
                mSearchLayout.setChooseVisibility(View.GONE);
            }

            mSearchLayout.setListener(new SearchLayout.OnSearchListener() {
                @Override
                public void onSearch(String search) {
                    mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                    mOrderRequest.user_id = Utils.getUserId(ErrandOrderListActivity.this);
                    mOrderRequest.token = Utils.getToken(ErrandOrderListActivity.this);
                    mOrderRequest.page = mCurrentPage;
                    mOrderRequest.keyword = search;
                    showNetDialog("");
                    getOrderList();
                }

                @Override
                public void onChoose() {
                    ErrandSearchSettingActivity.invoke(ErrandOrderListActivity.this, mOrderRequest, mTag + TAG);
                }
            });
        }

        mFooterView = LayoutInflater.from(this).inflate(R.layout.layout_load_more, null, false);
        mLoadMoreTv = (TextView) mFooterView.findViewById(R.id.tv_footer_text);
        mLoadMoreProgress = (CircularProgress) mFooterView.findViewById(R.id.progress);

        mOrderLv.setPullLoadEnable(false);
        mOrderLv.setPullRefreshEnable(true);
        mOrderLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && mLastVisibleIndex >= mAdapter.getCount() - 1) {
                    loadMore();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mLastVisibleIndex = firstVisibleItem + visibleItemCount - 1;
            }
        });

        mOrderLv.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {

            }
        });

        mAdapter = new ErrandOrderListAdapter(this, mOrders);
        mAdapter.setListener(new ErrandOrderListAdapter.ErrandOrderListAdapterListener() {
            @Override
            public void onFinish(int position) {
                try {
                    doFinish(position);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }

            @Override
            public void onFeedback(final int position) {
                FeedbackPriceBottomFragment feedbackPriceBottomFragment = new FeedbackPriceBottomFragment();
                feedbackPriceBottomFragment.setListener(new FeedbackPriceBottomFragment.OnFeedbackListener() {
                    @Override
                    public void onConfirm(String totalMoney) {
                        try {
                            doFeedback(position, totalMoney);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                feedbackPriceBottomFragment.show(getFragmentManager(), "FeedbackPriceBottomFragment");
            }

            @Override
            public void onGrab(int position) {
                try {
                    doGrab(position);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        mOrderLv.setAdapter(mAdapter);

        mOrderLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mOrders && mOrders.size() > position - 1) {
                    ErrandOrderDetailActivity.invoke(ErrandOrderListActivity.this, mOrders.get(position - 1).order_id, mTag, mIsOffWork);
                }
            }
        });
    }

    private void doFinish(final int position) throws Exception {
        ErrandOrder errandOrder = mOrders.get(position);

        showNetDialog("");
        OkHttpClientManager.getInstance().updateErrandOrderState(Utils.getUserId(this),
                Utils.getToken(this), errandOrder.order_id, FusionCode.OrderStatus.ORDER_STATUS_FINISHED,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(final RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != data && null != data.data) {
                                    ErrandOrder order = mOrders.get(position);
                                    order.state = FusionCode.OrderStatus.ORDER_STATUS_FINISHED;
                                    mOrders.remove(position);
                                    mOrders.add(position, order);
                                    mAdapter.notifyDataSetChanged();
                                    EventBus.getDefault().post(new ApplicationEvent(EventID.ERRAND_ORDER_LIST_FINISH, mTag));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
                    }
                });
    }

    private void doGrab(final int position) throws Exception {
        ErrandOrder errandOrder = mOrders.get(position);

        showNetDialog("");
        OkHttpClientManager.getInstance().takeErrandOrder(Utils.getUserId(this),
                Utils.getToken(this), errandOrder.order_id,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(final RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != data && null != data.data) {
                                    mOrders.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    EventBus.getDefault().post(new ApplicationEvent(EventID.GRAB_ERRAND_ORDER_SUCCESS, mTag));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
                    }
                });

    }

    private void doFeedback(final int position, String money) throws Exception {
        ErrandOrder errandOrder = mOrders.get(position);
        showNetDialog("");
        OkHttpClientManager.getInstance().responseMoney(Utils.getUserId(this), Utils.getToken(this),
                errandOrder.order_id, money, new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(final RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != data && null != data.data) {
                                    ErrandOrder order = mOrders.get(position);
                                    order.state = FusionCode.OrderStatus.ORDER_STATUS_FEEDBACK;
                                    mOrders.remove(position);
                                    mOrders.add(position, order);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
                    }
                });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        EventBus.getDefault().register(this);

        mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
        mOrderRequest.page = mCurrentPage;
        mOrderRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;
        mOrderRequest.zip_code = mZipCode;
        showNetDialog("");
        mIsInit = true;
        getOrderList();
    }

    private void loadMore() {
        if (mIsLoadMore) {
            return;
        }

        if (mIsRefresh || mIsRefresh) {
            mLoadMoreProgress.stopSpinning();
            mLoadMoreProgress.setVisibility(View.GONE);
            mLoadMoreTv.setText(R.string.xlistview_footer_hint_normal);
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            mLoadMoreProgress.setVisibility(View.VISIBLE);
            mLoadMoreProgress.startSpinning();
            mLoadMoreTv.setText(R.string.xlistview_header_hint_loading);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mLoadMoreProgress.stopSpinning();
                    mLoadMoreProgress.setVisibility(View.GONE);
                    mLoadMoreTv.setText(R.string.xlistview_footer_hint_normal);
                    mIsLoadMore = false;
                }
            }, 100);
            return;
        }

        if (!mHasAddFooterView) {
            return;
        }

        mIsLoadMore = true;
        mLoadMoreProgress.setVisibility(View.VISIBLE);
        mLoadMoreProgress.startSpinning();
        mLoadMoreTv.setText(R.string.xlistview_header_hint_loading);

        mOrderRequest.page = mCurrentPage;
        getOrderList();
    }

    private void refresh() {
        if (mIsRefresh) {
            return;
        }

        if (mIsLoadMore || mIsInit) {
            mOrderLv.stopRefresh();
            return;
        }

        mIsRefresh = true;
        mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
        mOrderRequest.page = mCurrentPage;
        getOrderList();
    }

    private void getOrderList() {
        OkHttpClientManager.getInstance().getErrandOrderList(mOrderRequest, new WtNetWorkListener<ErrandOrderList>() {
            @Override
            public void onSucess(final RemoteReturnData<ErrandOrderList> data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != data
                                && null != data.data
                                && null != data.data.rows
                                && data.data.rows.size() > 0) {
                            List<ErrandOrder> orders = data.data.rows;
                            int page = data.data.total_page;
                            final int size = orders.size();
                            if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE) {
                                mOrders.clear();
                            }

                            if (size < FusionCode.PageConstant.PAGE_SIZE
                                    && mCurrentPage > FusionCode.PageConstant.INIT_PAGE
                                    && mHasAddFooterView) {
                                mOrderLv.removeFooterView(mFooterView);
                                mHasAddFooterView = false;
                            }

                            if (size == FusionCode.PageConstant.PAGE_SIZE
                                    && mCurrentPage < page) {
                                if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE && !mHasAddFooterView) {
                                    mOrderLv.addFooterView(mFooterView);
                                    mHasAddFooterView = true;
                                }

                                mCurrentPage = mCurrentPage + 1;
                            }

                            mOrders.addAll(orders);
                            if (!StringUtils.getInstance().isNullOrEmpty(data.data.onwork)
                                    && data.data.onwork.equals(FusionCode.WorkStatus.WORK_STATUS_OFFWORK)) {
                                mIsOffWork = true;
                                mAdapter.setOffWork(true);
                            } else {
                                mIsOffWork = false;
                                mAdapter.setOffWork(false);
                            }


                        } else {
                            if (mCurrentPage != FusionCode.PageConstant.INIT_PAGE && mHasAddFooterView) {
                                mOrderLv.removeFooterView(mFooterView);
                                mHasAddFooterView = false;
                            }

                            if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE) {
                                mOrders.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(String status, final String msg_cn, final String msg_en) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseError(msg_cn, msg_en);
                    }
                });
            }

            @Override
            public void onFinished() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIsInit = false;
                        mIsLoadMore = false;
                        mIsRefresh = false;
                        closeNetDialog();
                        mOrderLv.stopLoadMore();
                        mOrderLv.stopRefresh();

                        mLoadMoreProgress.stopSpinning();
                        mLoadMoreProgress.setVisibility(View.GONE);
                        mLoadMoreTv.setText(R.string.xlistview_footer_hint_normal);
                    }
                });
            }
        });

    }


    @OnClick({R.id.layout_back})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                ErrandOrderListActivity.this.finish();
                break;
            }

            default: {
                break;
            }
        }
    }

    @Subscribe
    public void onEvent(ApplicationEvent event) {
        if (null == event) {
            return;
        }

        final int eventId = event.getEventId();
        switch (eventId) {
            case EventID.ERRAND_SEARCH_SETTING_SUCCESS: {
                if (!event.getEventMessage().equals(mTag + TAG) || null == event.getData()) {
                    return;
                }

                mOrderRequest = (OrderListRequest) event.getData();
                mOrderRequest.zip_code = mZipCode;
                mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                showNetDialog("");
                getOrderList();
                break;
            }

            case EventID.ERRAND_ORDER_DETAIL_ORDER_STATE_CHANGE_SUCCESS: {
                if (!event.getEventMessage().equals(mTag)) {
                    return;
                }
                refresh();
                break;
            }

            case EventID.ERRAND_ORDER_LIST_FINISH: {
                if (!event.getEventMessage().equals(mTag)) {
                    return;
                }

                refresh();
                break;
            }
        }
    }

    public static void invoke(Context context, OrderListRequest request, String tag, String zipCode) {
        Intent intent = new Intent(context, ErrandOrderListActivity.class);
        intent.putExtra(INTENT_REQUEST, request);
        intent.putExtra(INTENT_TAG, tag);
        intent.putExtra(INTENT_ZIPCODE, zipCode);
        context.startActivity(intent);
    }
}
