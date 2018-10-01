package com.mhysa.waimai.kuaidi.ui.activities.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.ExtendUtils;
import com.joey.devilfish.utils.PromptUtils;
import com.mhysa.waimai.kuaidi.KuaidiApplication;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.service.LocationService;
import com.mhysa.waimai.kuaidi.ui.adapters.main.MainPageAdapter;
import com.mhysa.waimai.kuaidi.ui.customerviews.MainBottomLayout;
import com.mhysa.waimai.kuaidi.ui.fragments.home.HomeFragment;
import com.mhysa.waimai.kuaidi.ui.fragments.order.OrderFragment;
import com.mhysa.waimai.kuaidi.ui.fragments.personal.PersonalFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0;

    private static final int MAX_BACK_DURATION = 2000;

    private long mLastBackPressTime;

    private MainBottomLayout mBottomLayout;

    private List<Fragment> mFragments = new ArrayList<Fragment>();

    private MainPageAdapter mAdapter;

    private HomeFragment mHomeFragment;

    private OrderFragment mOrderFragment;

    private PersonalFragment mPersonalFragment;

    private MainBottomLayout.OnOperateListener mBottomViewListener = new MainBottomLayout.OnOperateListener() {
        @Override
        public void onMenuSelected(int selectedId) {
            if (null != mAdapter) {
                mAdapter.switchFragment(selectedId);
            }
        }

    };

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);
        mBottomLayout = (MainBottomLayout) this.findViewById(R.id.layout_bottom);
        mBottomLayout.setListener(mBottomViewListener);

        initFragmentPage();
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        EventBus.getDefault().register(this);

        if (ExtendUtils.getInstance().hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ExtendUtils.getInstance().hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            startLocationService();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    /**
     * 初始化页面
     */
    private void initFragmentPage() {
        mFragments.clear();

        mHomeFragment = new HomeFragment();
        mOrderFragment = new OrderFragment();
        mPersonalFragment = new PersonalFragment();

        mFragments.add(mHomeFragment);
        mFragments.add(mOrderFragment);
        mFragments.add(mPersonalFragment);

        mAdapter = new MainPageAdapter(MainActivity.this, mFragments,
                R.id.main_detail_fragment, mBottomLayout);

        mBottomLayout.setHomeChecked();
    }

    @Override
    public void onBackPressed() {
        long currentMills = System.currentTimeMillis();
        if (currentMills - mLastBackPressTime <= MAX_BACK_DURATION) {
            this.finish();
            KuaidiApplication.getInstance().exit();
        } else {
            PromptUtils.getInstance().showLongPromptToast(this, R.string.exit_after_press_again);
            mLastBackPressTime = currentMills;
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
            case EventID.CHANGE_LANGUAGE_SUCCESS: {
                MainActivity.this.finish();
                break;
            }

            case EventID.LOGOUT_SUCCESS: {
                MainActivity.this.finish();
                break;
            }
        }
    }

    private void startLocationService() {
        // 启动服务
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    startLocationService();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static void invoke(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}
