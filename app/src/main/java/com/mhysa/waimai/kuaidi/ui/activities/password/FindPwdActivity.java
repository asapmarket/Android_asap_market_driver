package com.mhysa.waimai.kuaidi.ui.activities.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.verifycode.VerifyCode;
import com.mhysa.waimai.kuaidi.ui.customerviews.EditTextLayout;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 找回密码
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class FindPwdActivity extends BaseActivity {

    private static final int COUNT_DOWN = 60;

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.layout_phone)
    EditTextLayout mPhoneLayout;

    @Bind(R.id.layout_code)
    EditTextLayout mCodeLayout;

    @Bind(R.id.layout_new_password)
    EditTextLayout mNewPwdLayout;

    @Bind(R.id.layout_confirm_new_password)
    EditTextLayout mConfirmNewPwdLayout;

    private String mPhone;

    private String mNewPwd;

    private String mConfirmPwd;

    private String mVerifyCode;

    private int mCurrentCount = COUNT_DOWN;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int what = msg.what;
            switch (what) {
                case 0: {
                    if (mCurrentCount == 0) {
                        mCodeLayout.setRightTvClickable(true);
                        mCodeLayout.setRightTv(getString(R.string.get_verify_code));
                        mCurrentCount = COUNT_DOWN;
                        mHandler.removeMessages(0);
                    } else {
                        mCurrentCount = mCurrentCount - 1;
                        mCodeLayout.setRightTv(getString(R.string.verify_code_sending, mCurrentCount + ""));
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }

                    break;
                }
            }
        }
    };

    @Override
    protected int getContentLayout() {
        return R.layout.activity_find_pwd;
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.find_password);
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);
        mPhoneLayout.setLeftTv("+1");
        mPhoneLayout.setInputType(InputType.TYPE_CLASS_PHONE);
        mCodeLayout.setRightTv(getResources().getString(R.string.get_verify_code), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVerifyCode();
            }
        });
        mNewPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmNewPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @OnClick({R.id.layout_back, R.id.btn_commit})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                FindPwdActivity.this.finish();
                break;
            }

            case R.id.btn_commit: {
                commit();
                break;
            }

            default: {
                break;
            }
        }
    }

    private void getVerifyCode() {
        mPhone = mPhoneLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mPhone)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.phone_hint);
            return;
        }

        mCodeLayout.setRightTvClickable(false);
        OkHttpClientManager.getInstance().verifyCode(mPhone, new WtNetWorkListener<VerifyCode>() {
            @Override
            public void onSucess(RemoteReturnData<VerifyCode> data) {
                mHandler.sendEmptyMessage(0);

//                if (null != data
//                        && null != data.data
//                        && !StringUtils.getInstance().isNullOrEmpty(data.data.code)) {
//                    mCodeLayout.setContent(data.data.code);
//                }
            }

            @Override
            public void onError(String status, String msg_cn, String msg_en) {
                responseError(msg_cn, msg_en);
                mCodeLayout.setRightTvClickable(true);
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void commit() {
        mPhone = mPhoneLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mPhone)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.phone_hint);
            return;
        }

        mVerifyCode = mCodeLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mVerifyCode)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.verify_code_hint);
            return;
        }

        mNewPwd = mNewPwdLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mNewPwd)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.new_password_hint);
            return;
        }

        mConfirmPwd = mConfirmNewPwdLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mConfirmPwd)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.confirm_new_password_hint);
            return;
        }

        if (!mNewPwd.equals(mConfirmPwd)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.password_not_same);
            return;
        }

        showNetDialog("");
        OkHttpClientManager.getInstance().findPassword(mPhone, mVerifyCode, mNewPwd,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        PromptUtils.getInstance().showShortPromptToast(FindPwdActivity.this, R.string.change_password_success);
                        FindPwdActivity.this.finish();
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

    public static void invoke(Context context) {
        Intent intent = new Intent(context, FindPwdActivity.class);
        context.startActivity(intent);
    }
}
