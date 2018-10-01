package com.mhysa.waimai.kuaidi.ui.activities.order;

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
import com.mhysa.waimai.kuaidi.ui.adapters.home.HomeOrderListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 主页订单列表
 * Date: 2017/8/17
 *
 * @author xusheng
 */

public class HomeOrderListActivity extends BaseActivity {

    private static final String INTENT_ZIPCODE = "intent_zipcode";

    @Bind(R.id.tv_zip_code)
    TextView mZipCodeTv;

    @Bind(R.id.lv_order)
    XListView mOrderLv;

    private View mFooterView;

    private TextView mLoadMoreTv;

    private CircularProgress mLoadMoreProgress;

    private ArrayList<Order> mOrders = new ArrayList<Order>();

    private HomeOrderListAdapter mAdapter;

    private int mLastVisibleIndex;

    private boolean mIsLoadMore = false;

    private boolean mIsRefresh = false;

    private boolean mIsInit = false;

    private boolean mHasAddFooterView = false;

    private int mCurrentPage = FusionCode.PageConstant.INIT_PAGE;

    private String mZipCode;

    private OrderListRequest mOrderListRequest;

    private boolean mIsOffWork = false;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_home_order_list;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mZipCode = getIntent().getStringExtra(INTENT_ZIPCODE);
        }
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        StringUtils.getInstance().setText(mZipCode, mZipCodeTv);

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

        mAdapter = new HomeOrderListAdapter(this, mOrders);
        mAdapter.setListener(new HomeOrderListAdapter.HomeOrderListAdapterListener() {
            @Override
            public void grabOrder(int position) {
                doGrabOrder(position);
            }
        });
        mOrderLv.setAdapter(mAdapter);

        mOrderLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mOrders && mOrders.size() > position - 1) {
                    OrderDetailActivity.invoke(HomeOrderListActivity.this,
                            mOrders.get(position - 1).order_id, false, mIsOffWork);
                }
            }
        });
    }

    private void doGrabOrder(final int position) {
        if (null != mOrders && mOrders.size() > position) {
            showNetDialog("");

            OkHttpClientManager.getInstance().grabOrder(Utils.getUserId(this),
                    Utils.getToken(this), mOrders.get(position).order_id,
                    new WtNetWorkListener<JsonElement>() {
                        @Override
                        public void onSucess(RemoteReturnData<JsonElement> data) {
                            if (null != data && null != data.data) {
                                mOrders.remove(position);
                                mAdapter.notifyDataSetChanged();
                                PromptUtils.getInstance().showShortPromptToast(HomeOrderListActivity.this, R.string.success);
                                EventBus.getDefault().post(new ApplicationEvent(EventID.GRAB_ORDER_SUCCESS));
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

        mOrderListRequest = new OrderListRequest();
        mOrderListRequest.zip_code = mZipCode;
        mOrderListRequest.user_id = Utils.getUserId(this);
        mOrderListRequest.token = Utils.getToken(this);

        mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
        mOrderListRequest.page = mCurrentPage;
        mOrderListRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;

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
        OkHttpClientManager.getInstance().getOrderList(mOrderListRequest, new WtNetWorkListener<OrderList>() {
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

    @OnClick({R.id.layout_back})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                HomeOrderListActivity.this.finish();
                break;
            }

            default: {
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
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
            case EventID.ORDER_DETAIL_GRAB_ORDER_SUCCESS: {
                refresh();
                break;
            }
        }
    }

    public static void invoke(Context context, String zipCode) {
        Intent intent = new Intent(context, HomeOrderListActivity.class);
        intent.putExtra(INTENT_ZIPCODE, zipCode);
        context.startActivity(intent);
    }
}
