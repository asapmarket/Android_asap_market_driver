package com.mhysa.waimai.kuaidi.ui.fragments.errand;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.ui.fragment.base.BaseFragment;
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
import com.mhysa.waimai.kuaidi.model.order.OrderListRequest;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCode;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCodeList;
import com.mhysa.waimai.kuaidi.ui.activities.errandorder.ErrandOrderListActivity;
import com.mhysa.waimai.kuaidi.ui.activities.errandorder.ErrandSearchSettingActivity;
import com.mhysa.waimai.kuaidi.ui.adapters.order.OrderZipCodeAdapter;
import com.mhysa.waimai.kuaidi.ui.customerviews.SearchLayout;

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

public class ErrandOrderZipCodeListFragment extends BaseFragment {

    @Bind(R.id.layout_search)
    SearchLayout mSearchLayout;

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

    private String mState;

    private String mExpId;

    private String mTag;

    private OrderListRequest mOrderListRequest;

    public void setState(String state) {
        this.mState = state;
    }

    public void setExpId(String expId) {
        this.mExpId = expId;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_errand_zipcode_list;
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
                    mOrderListRequest.zip_code = mOrderZipCodes.get(position - 1).zip_code;
                    ErrandOrderListActivity.invoke(mContext, mOrderListRequest, mTag, mOrderZipCodes.get(position - 1).zip_code);
                }
            }
        });

        if (mTag.equals(ErrandOrderListActivity.TAG_HOME)) {
            mSearchLayout.setVisibility(View.GONE);
        } else {
            mSearchLayout.setVisibility(View.VISIBLE);

            if (mTag.equals(ErrandOrderListActivity.TAG_ORDER)) {
                mSearchLayout.setChooseVisibility(View.GONE);
            }

            mSearchLayout.setListener(new SearchLayout.OnSearchListener() {
                @Override
                public void onSearch(String search) {
                    mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                    mOrderListRequest.user_id = Utils.getUserId(getActivity());
                    mOrderListRequest.token = Utils.getToken(getActivity());
                    mOrderListRequest.page = mCurrentPage;
                    mOrderListRequest.keyword = search;
                    showNetDialog("");
                    getData();
                }

                @Override
                public void onChoose() {
                    ErrandSearchSettingActivity.invoke(mContext, mOrderListRequest, mTag);
                }
            });
        }
    }

    @Override
    protected void initData() {
        super.initData();

        EventBus.getDefault().register(this);

        showNetDialog("");
        mIsInit = true;

        mOrderListRequest = new OrderListRequest();
        mOrderListRequest.page_size = FusionCode.PageConstant.PAGE_SIZE * 2;
        mOrderListRequest.state = mState;
        if (!StringUtils.getInstance().isNullOrEmpty(mExpId)) {
            mOrderListRequest.exp_id = mExpId;
        }
        mOrderListRequest.user_id = Utils.getUserId(mContext);
        mOrderListRequest.token = Utils.getToken(mContext);
        getData();
    }

    @Subscribe
    public void onEvent(ApplicationEvent event) {
        if (null == event) {
            return;
        }

        final int eventId = event.getEventId();
        switch (eventId) {
            case EventID.GRAB_ERRAND_ORDER_SUCCESS:
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

            case EventID.ERRAND_SEARCH_SETTING_SUCCESS: {
                if (!event.getEventMessage().equals(mTag) || null == event.getData()) {
                    return;
                }

                mOrderListRequest = (OrderListRequest) event.getData();
                mCurrentPage = FusionCode.PageConstant.INIT_PAGE;
                showNetDialog("");
                getData();
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

        getData();
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
        getData();
    }

    private void getData() {
        if (null == mOrderListRequest) {
            mOrderListRequest = new OrderListRequest();
        }
        mOrderListRequest.page = mCurrentPage;
        OkHttpClientManager.getInstance().getErrandOrderZipCodeList(mOrderListRequest, new WtNetWorkListener<OrderZipCodeList>() {
            @Override
            public void onSucess(final RemoteReturnData<OrderZipCodeList> data) {
                ((BaseActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });
            }

            @Override
            public void onError(String status, final String msg_cn, final String msg_en) {
                ((BaseActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseError(msg_cn, msg_en);
                    }
                });
            }

            @Override
            public void onFinished() {
                ((BaseActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
        });
    }
}
