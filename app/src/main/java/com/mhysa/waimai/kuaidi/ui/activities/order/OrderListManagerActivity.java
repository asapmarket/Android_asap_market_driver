package com.mhysa.waimai.kuaidi.ui.activities.order;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.model.tab.TabInfo;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.ui.fragment.base.BaseFragment;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.Utils;
import com.joey.devilfish.widget.indicator.TitleIndicator;
import com.joey.devilfish.widget.indicator.ViewPagerCompat;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.ui.activities.errandorder.ErrandOrderListActivity;
import com.mhysa.waimai.kuaidi.ui.fragments.errand.ErrandOrderZipCodeListFragment;
import com.mhysa.waimai.kuaidi.ui.fragments.order.OrderManagerZipcodeListFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 订单管理界面
 * Date: 2017/8/16
 *
 * @author xusheng
 */

public class OrderListManagerActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = OrderListManagerActivity.class.getSimpleName();

    @Bind(R.id.tv_onroad)
    TextView mOnRoadTv;

    private static final int TAB_TAKEOUT = 0;

    private static final int TAB_ERRAND = 1;

    private int mCurrentTab = TAB_TAKEOUT;

    private TitleIndicator mTitleIndicator;

    private ViewPagerCompat mViewPager;

    private OrderListManagerActivity.MyAdapter mAdapter;

    private List<TabInfo> mTabInfoList = new ArrayList<TabInfo>();

    @Override
    protected int getContentLayout() {
        return R.layout.activity_order_list_manager;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        mViewPager = (ViewPagerCompat) mRootView.findViewById(R.id.vpc_order_content);
        mViewPager.setNoScroll(false);
        mTitleIndicator = (TitleIndicator) mRootView.findViewById(R.id.ti_order_title);
        bindTabList();
        bindViewPager();

        mOnRoadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRoad();
            }
        });
    }

    private void bindTabList() {
        mTabInfoList.add(new TabInfo(TAB_TAKEOUT, getResources().getString(R.string.takeout_order)));
        mTabInfoList.add(new TabInfo(TAB_ERRAND, getResources().getString(R.string.errand_order)));
        mTitleIndicator.init(mCurrentTab, mTabInfoList, mViewPager, false);
    }

    private void bindViewPager() {
        mAdapter = new OrderListManagerActivity.MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mTabInfoList.size());
        mViewPager.setCurrentItem(mCurrentTab);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTitleIndicator.onScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mTitleIndicator.onSwitched(position);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void onRoad() {
        AlertDialog.Builder builder = PromptUtils.getInstance().getAlertDialog(OrderListManagerActivity.this, true);
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
                        PromptUtils.getInstance().showShortPromptToast(OrderListManagerActivity.this, R.string.success);
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

    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            BaseFragment fragment = null;
            switch (pos) {
                case TAB_TAKEOUT:
                    OrderManagerZipcodeListFragment firstFragment = new OrderManagerZipcodeListFragment();
                    fragment = firstFragment;
                    break;
                case TAB_ERRAND:
                    ErrandOrderZipCodeListFragment secondFragment = new ErrandOrderZipCodeListFragment();
                    secondFragment.setState(FusionCode.OrderStatus.ORDER_STATUS_ALL);
                    secondFragment.setExpId(Utils.getUserId(OrderListManagerActivity.this));
                    secondFragment.setTag(ErrandOrderListActivity.TAG_ORDER_MANAGER);
                    fragment = secondFragment;
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mTabInfoList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseFragment fragment = null;
            switch (position) {
                case TAB_TAKEOUT:
                    fragment = (OrderManagerZipcodeListFragment) super.instantiateItem(container, position);
                    break;
                case TAB_ERRAND:
                    fragment = (ErrandOrderZipCodeListFragment) super.instantiateItem(container, position);
                    break;
                default:
                    break;
            }

            return fragment;
        }
    }

    @OnClick({R.id.layout_back})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                OrderListManagerActivity.this.finish();
                break;
            }

            default: {
                break;
            }
        }
    }

    public static void invoke(Context context) {
        Intent intent = new Intent(context, OrderListManagerActivity.class);
        context.startActivity(intent);
    }
}
