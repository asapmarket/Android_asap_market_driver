package com.mhysa.waimai.kuaidi.ui.activities.order;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.joey.devilfish.utils.PromptUtils;
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
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.model.order.OrderList;
import com.mhysa.waimai.kuaidi.model.order.OrderListRequest;
import com.mhysa.waimai.kuaidi.ui.adapters.order.OrderListAdapter;
import com.mhysa.waimai.kuaidi.ui.customerviews.SearchLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 文件描述
 * Date: 2017/7/22
 *
 * @author xusheng
 */

public class OrderListActivity extends BaseActivity {

    private static final String TAG = OrderListActivity.class.getSimpleName();

    private static final String INTENT_ZIPCODE = "intent_zipcode";

    private static final String INTENT_REQUEST = "intent_request";

    private static final String INTENT_NEED_ONROAD = "intent_need_onroad";

    private static final String INTENT_NEED_CHOOSE = "intent_need_choose";

    @Bind(R.id.tv_onroad)
    TextView mOnRoadTv;

    @Bind(R.id.tv_zip_code)
    TextView mZipCodeTv;

    @Bind(R.id.layout_search)
    SearchLayout mSearchLayout;

    @Bind(R.id.lv_order)
    XListView mOrderLv;

    private View mFooterView;

    private TextView mLoadMoreTv;

    private CircularProgress mLoadMoreProgress;

    private ArrayList<Order> mOrders = new ArrayList<Order>();

    private OrderListAdapter mAdapter;

    private int mLastVisibleIndex;

    private boolean mIsLoadMore = false;

    private boolean mIsRefresh = false;

    private boolean mIsInit = false;

    private boolean mHasAddFooterView = false;

    private int mCurrentPage = FusionCode.PageConstant.INIT_PAGE;

    private String mZipCode;

    private OrderListRequest mOrderListRequest;

    private boolean mNeedOnRoad;

    private boolean mNeedChoose;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_order_list;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mZipCode = getIntent().getStringExtra(INTENT_ZIPCODE);
            mOrderListRequest = (OrderListRequest) getIntent().getSerializableExtra(INTENT_REQUEST);
            mOrderListRequest.zip_code = mZipCode;
            mNeedOnRoad = getIntent().getBooleanExtra(INTENT_NEED_ONROAD, false);
            mNeedChoose = getIntent().getBooleanExtra(INTENT_NEED_CHOOSE, false);
        }
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        StringUtils.getInstance().setText(mZipCode, mZipCodeTv);

        mSearchLayout.setChooseVisibility(mNeedChoose ? View.VISIBLE : View.GONE);
        mSearchLayout.setListener(new SearchLayout.OnSearchListener() {
            @Override
            public void onSearch(String search) {

                mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                mOrderListRequest = new OrderListRequest();
                mOrderListRequest.user_id = Utils.getUserId(OrderListActivity.this);
                mOrderListRequest.token = Utils.getToken(OrderListActivity.this);
                mOrderListRequest.page = mCurrentPage;
                mOrderListRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;
                mOrderListRequest.keyword = search;
                mOrderListRequest.zip_code = mZipCode;
                showNetDialog("");
                getOrderList();
            }

            @Override
            public void onChoose() {
                SearchSettingActivity.invoke(OrderListActivity.this, mOrderListRequest, TAG);
            }
        });

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

        mAdapter = new OrderListAdapter(this, mOrders);
        mAdapter.setListener(new OrderListAdapter.OrderListAdapterListener() {
            @Override
            public void onFinished(int position) {
                doFinish(position);
            }
        });
        mOrderLv.setAdapter(mAdapter);

        mOrderLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mOrders && mOrders.size() > position - 1) {
                    OrderDetailActivity.invoke(OrderListActivity.this, mOrders.get(position - 1).order_id, true, false);
                }
            }
        });

        mOnRoadTv.setVisibility(mNeedOnRoad ? View.VISIBLE : View.GONE);
        if (mNeedOnRoad) {
            mOnRoadTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRoad();
                }
            });
        }
    }

    private void onRoad() {
        AlertDialog.Builder builder = PromptUtils.getInstance().getAlertDialog(OrderListActivity.this, true);
        builder.setMessage(R.string.on_road_tip)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doOnRoad();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void doOnRoad() {
        showNetDialog("");
        OkHttpClientManager.getInstance().onRoad(Utils.getUserId(this),
                Utils.getToken(this), new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        if (null != data
                                && null != data.data
                                && null != mOrders
                                && mOrders.size() > 0) {

                            PromptUtils.getInstance().showShortPromptToast(OrderListActivity.this, R.string.success);

                            final int size = mOrders.size();
                            for (int i = 0; i < size; i++) {
                                Order order = mOrders.get(i);
                                if (!order.state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
                                    order.state = FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD;
                                }

                                mOrders.remove(i);
                                mOrders.add(i, order);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String status, String msg_cn, String msg_en) {
                        responseError(msg_cn, msg_en);
                    }

                    @Override
                    public void onFinished() {
                        closeNetDialog();
                    }
                });
    }

    private void doFinish(final int position) {
        if (null != mOrders && mOrders.size() > position - 1) {
            showNetDialog("");
            OkHttpClientManager.getInstance().confirmOrder(Utils.getUserId(this),
                    Utils.getToken(this), mOrders.get(position).order_id,
                    new WtNetWorkListener<JsonElement>() {
                        @Override
                        public void onSucess(RemoteReturnData<JsonElement> data) {
                            if (null != data && null != data.data) {
                                Order order = mOrders.get(position);
                                order.state = FusionCode.OrderStatus.ORDER_STATUS_FINISHED;
                                mOrders.remove(position);
                                mOrders.add(position, order);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onError(String status, String msg_cn, String msg_en) {
                            responseError(msg_cn, msg_en);
                        }

                        @Override
                        public void onFinished() {
                            closeNetDialog();
                        }
                    });
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        EventBus.getDefault().register(this);

        mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
        mOrderListRequest.page = mCurrentPage;
        mOrderListRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;
        mOrderListRequest.zip_code = mZipCode;
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

        mOrderListRequest.page = mCurrentPage;
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
        mOrderListRequest.page = mCurrentPage;
        getOrderList();
    }

    private void getOrderList() {
        if (!mNeedChoose) {
            mOrderListRequest.state = FusionCode.OrderStatus.ORDER_STATUS_UNFINISHED;
        }

        OkHttpClientManager.getInstance().getExpOrderList(mOrderListRequest, new WtNetWorkListener<OrderList>() {
            @Override
            public void onSucess(RemoteReturnData<OrderList> data) {
                if (null != data
                        && null != data.data
                        && null != data.data.rows
                        && data.data.rows.size() > 0) {
                    List<Order> orders = data.data.rows;
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
                    mAdapter.notifyDataSetChanged();

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

            @Override
            public void onError(String status, String msg_cn, String msg_en) {
                responseError(msg_cn, msg_en);
            }

            @Override
            public void onFinished() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(ApplicationEvent event) {
        if (null == event) {
            return;
        }

        final int eventId = event.getEventId();
        switch (eventId) {
            case EventID.SEARCH_SETTING_SUCCESS: {
                if (!event.getEventMessage().equals(TAG) || null == event.getData()) {
                    return;
                }

                mOrderListRequest = (OrderListRequest) event.getData();
                mOrderListRequest.zip_code = mZipCode;
                mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                showNetDialog("");
                getOrderList();
                break;
            }
        }
    }

    @OnClick({R.id.layout_back})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                OrderListActivity.this.finish();
                break;
            }

            default: {
                break;
            }
        }
    }

    public static void invoke(Context context, String zipCode, OrderListRequest request,
                              boolean needOnRoad, boolean needChoose) {
        Intent intent = new Intent(context, OrderListActivity.class);
        intent.putExtra(INTENT_ZIPCODE, zipCode.trim());
        intent.putExtra(INTENT_REQUEST, request);
        intent.putExtra(INTENT_NEED_ONROAD, needOnRoad);
        intent.putExtra(INTENT_NEED_CHOOSE, needChoose);
        context.startActivity(intent);
    }
}
