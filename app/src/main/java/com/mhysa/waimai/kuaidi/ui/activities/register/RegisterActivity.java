package com.mhysa.waimai.kuaidi.ui.activities.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.verifycode.VerifyCode;
import com.mhysa.waimai.kuaidi.ui.activities.login.LoginActivity;
import com.mhysa.waimai.kuaidi.ui.customerviews.EditTextLayout;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 注册
 * Date: 2017/7/20
 *
 * @author xusheng
 */

public class RegisterActivity extends BaseActivity {

    private static final int COUNT_DOWN = 60;

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.layout_phone)
    EditTextLayout mPhoneLayout;

    @Bind(R.id.layout_code)
    EditTextLayout mCodeLayout;

    @Bind(R.id.layout_password)
    EditTextLayout mPwdLayout;

    @Bind(R.id.cb_protocol)
    CheckBox mProtocolCb;

    @Bind(R.id.btn_register)
    Button mRegisterBtn;

    private String mPhone;

    private String mPwd;

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
        return R.layout.activity_register;
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.register);
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
        mPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mRegisterBtn.setEnabled(true);
        mProtocolCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mRegisterBtn.setEnabled(isChecked);
            }
        });
    }

    @OnClick({R.id.layout_back, R.id.layout_has_account,
            R.id.btn_register})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                RegisterActivity.this.finish();
                break;
            }

            case R.id.layout_has_account: {
                LoginActivity.invoke(RegisterActivity.this);
                RegisterActivity.this.finish();
                break;
            }

            case R.id.btn_register: {
                register();
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

    private void register() {
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

        mPwd = mPwdLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mPwd)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.password_hint);
            return;
        }

        showNetDialog("");
        OkHttpClientManager.getInstance().register(mPhone, mPwd, mVerifyCode, new WtNetWorkListener<JsonElement>() {
            @Override
            public void onSucess(RemoteReturnData<JsonElement> data) {
                PromptUtils.getInstance().showShortPromptToast(RegisterActivity.this, R.string.register_success);
                RegisterActivity.this.finish();
            }

            @Override
            public void onError(String status, String msg_cn, String msg_en) {
                if (Utils.isChinese(RegisterActivity.this)) {
                    PromptUtils.getInstance().showShortPromptToast(RegisterActivity.this, msg_cn);
                } else {
                    PromptUtils.getInstance().showShortPromptToast(RegisterActivity.this, msg_en);
                }
            }

            @Override
            public void onFinished() {
                closeNetDialog();
            }
        });
    }

    public static void invoke(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

}
