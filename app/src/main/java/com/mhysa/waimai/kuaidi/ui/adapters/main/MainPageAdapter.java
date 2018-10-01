package com.mhysa.waimai.kuaidi.ui.adapters.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.mhysa.waimai.kuaidi.ui.customerviews.MainBottomLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class MainPageAdapter {

    private List<Fragment> mFragments = new ArrayList<Fragment>();

    private MainBottomLayout mBottomLayout;

    private RadioGroup mRadioGroup;

    private int mFragmentContentId;

    private int mCurrentTab;

    private FragmentManager mFragmentManager;

    public MainPageAdapter(FragmentActivity fragmentActivity, List<Fragment> fragments, int fragmentContentId,
                           MainBottomLayout mainBottomLayout) {

        if (null != mFragments) {
            this.mFragments.clear();
        }

        this.mFragments = fragments;
        this.mBottomLayout = mainBottomLayout;
        this.mFragmentContentId = fragmentContentId;
        this.mRadioGroup = mBottomLayout.getRadioGroup();
        this.mFragmentManager = fragmentActivity.getSupportFragmentManager();
    }

    /**
     * 切换fragment
     */
    public void switchFragment(int id) {
        int size = mRadioGroup.getChildCount();
        int selectedPosition = 0;

        for (int i = 0; i < size; i++) {
            if (mRadioGroup.getChildAt(i).getId() == id) {
                selectedPosition = i;
                break;
            }
        }
        Fragment fragment = mFragments.get(selectedPosition);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        getCurrentFragment().onPause();
        if (fragment.isAdded()) {
            fragment.onResume();
        } else {
            ft.add(mFragmentContentId, fragment);
        }
        showTab(selectedPosition);
        ft.commitAllowingStateLoss();
    }

    /**
     * 描述：切换tab
     */
    private void showTab(int idx) {
        final int size = mFragments.size();
        for (int i = 0; i < size; i++) {
            Fragment fragment = mFragments.get(i);
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            if (idx == i) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
            ft.commitAllowingStateLoss();
        }
        mCurrentTab = idx;
    }

    public Fragment getCurrentFragment() {
        return mFragments.get(mCurrentTab);
    }
}
