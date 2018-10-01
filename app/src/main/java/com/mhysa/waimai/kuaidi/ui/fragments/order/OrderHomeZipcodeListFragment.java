package com.mhysa.waimai.kuaidi.ui.fragments.order;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.fragment.base.BaseFragment;
import com.joey.devilfish.utils.NetworkUtils;
import com.joey.devilfish.utils.Utils;
import com.joey.devilfish.widget.CircularProgress;
import com.joey.devilfish.widget.listview.XListView;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCode;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCodeList;
import com.mhysa.waimai.kuaidi.ui.activities.order.HomeOrderListActivity;
import com.mhysa.waimai.kuaidi.ui.adapters.order.OrderZipCodeAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 文件描述
 * Date: 2018/3/24
 *
 * @author xusheng
 */

public class OrderHomeZipcodeListFragment extends BaseFragment {

    @Bind(R.id.lv_zipcode_order)
    XListView mZipcodeOrderLv;

    private View mFooterView;

    private TextView mLoadMoreTv;

    private CircularProgress mLoadMoreProgress;

    private ArrayList<OrderZipCode> mOrderZipCodes = new ArrayList<OrderZipCode>();

    private OrderZipCodeAdapter mAdapter;

    private int mLastVisibleIndex;

    private boolean mIsLoadMore = false;

    private boolean mIsRefresh = false;

    private boolean mIsInit = false;

    private boolean mHasAddFooterView = false;

    private int mCurrentPage = FusionCode.PageConstant.INIT_PAGE;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_home_order_zipcode_list;
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_load_more, null, false);
        mLoadMoreTv = (TextView) mFooterView.findViewById(R.id.tv_footer_text);
        mLoadMoreProgress = (CircularProgress) mFooterView.findViewById(R.id.progress);

        mZipcodeOrderLv.setPullLoadEnable(false);
        mZipcodeOrderLv.setPullRefreshEnable(true);
        mZipcodeOrderLv.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        mZipcodeOrderLv.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {

            }
        });

        mAdapter = new OrderZipCodeAdapter(getActivity(), mOrderZipCodes);
        mZipcodeOrderLv.setAdapter(mAdapter);

        mZipcodeOrderLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 订单列表
                if (null != mOrderZipCodes && mOrderZipCodes.size() > position - 1) {
                    HomeOrderListActivity.invoke(mContext, mOrderZipCodes.get(position - 1).zip_code);
                }
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();

        EventBus.getDefault().register(this);

        showNetDialog("");
        mIsInit = true;
        getHomeInfo();
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
            case EventID.GRAB_ORDER_SUCCESS:
            case EventID.ORDER_DETAIL_GRAB_ORDER_SUCCESS: {
                refresh();
                break;
            }
        }
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

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
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

        getHomeInfo();
    }

    private void refresh() {
        if (mIsRefresh) {
            return;
        }

        if (mIsLoadMore || mIsInit) {
            mZipcodeOrderLv.stopRefresh();
            return;
        }

        mIsRefresh = true;
        mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
        getHomeInfo();
    }

    private void getHomeInfo() {
        OkHttpClientManager.getInstance().getHomeInfo(
                Utils.getUserId(mContext),
                Utils.getToken(mContext),
                mCurrentPage, FusionCode.PageConstant.PAGE_SIZE * 2,
                new WtNetWorkListener<OrderZipCodeList>() {
                    @Override
                    public void onSucess(RemoteReturnData<OrderZipCodeList> data) {
                        if (null != data
                                && null != data.data
                                && null != data.data.rows
                                && data.data.rows.size() > 0) {
                            List<OrderZipCode> orders = data.data.rows;
                            int page = data.data.total_page;
                            final int size = orders.size();
                            if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE) {
                                mOrderZipCodes.clear();
                            }

                            if (size < FusionCode.PageConstant.PAGE_SIZE
                                    && mCurrentPage > FusionCode.PageConstant.INIT_PAGE
                                    && mHasAddFooterView) {
                                mZipcodeOrderLv.removeFooterView(mFooterView);
                                mHasAddFooterView = false;
                            }

                            if (size == FusionCode.PageConstant.PAGE_SIZE
                                    && mCurrentPage < page) {
                                if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE && !mHasAddFooterView) {
                                    mZipcodeOrderLv.addFooterView(mFooterView);
                                    mHasAddFooterView = true;
                                }

                                mCurrentPage = mCurrentPage + 1;
                            }

                            mOrderZipCodes.addAll(orders);
                            mAdapter.notifyDataSetChanged();

                        } else {
                            if (mCurrentPage != FusionCode.PageConstant.INIT_PAGE && mHasAddFooterView) {
                                mZipcodeOrderLv.removeFooterView(mFooterView);
                                mHasAddFooterView = false;
                            }

                            if (mCurrentPage == FusionCode.PageConstant.INIT_PAGE) {
                                mOrderZipCodes.clear();
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
                        mZipcodeOrderLv.stopLoadMore();
                        mZipcodeOrderLv.stopRefresh();

                        mLoadMoreProgress.stopSpinning();
                        mLoadMoreProgress.setVisibility(View.GONE);
                        mLoadMoreTv.setText(R.string.xlistview_footer_hint_normal);
                    }
                });
    }
}
