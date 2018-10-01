package com.mhysa.waimai.kuaidi.ui.activities.launch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.ui.activities.login.LoginActivity;

/**
 * 启动页
 * Date: 2017/8/25
 *
 * @author xusheng
 */

public class LaunchActivity extends BaseActivity {

    private static final int ENTER_LOGIN = 0;

    private static final int DELAY = 2000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case ENTER_LOGIN: {
                    gotoLogin();
                    break;
                }

                default: {
                    break;
                }
            }
        }
    };

    @Override
    protected int getContentLayout() {
        return R.layout.activity_launch;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        mHandler.sendEmptyMessageDelayed(ENTER_LOGIN, DELAY);
    }

    private void gotoLogin() {
        LoginActivity.invoke(this);
        LaunchActivity.this.finish();
        overridePendingTransition(R.anim.activity_translate_right_in,
                R.anim.activity_translate_right_out);
    }
}
