package com.mhysa.waimai.kuaidi.ui.activities.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.http.HttpConstant;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.user.User;
import com.mhysa.waimai.kuaidi.ui.activities.main.MainActivity;
import com.mhysa.waimai.kuaidi.ui.activities.password.FindPwdActivity;
import com.mhysa.waimai.kuaidi.ui.activities.register.RegisterActivity;
import com.mhysa.waimai.kuaidi.ui.customerviews.EditTextLayout;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 登录
 * Date: 2017/7/20
 *
 * @author xusheng
 */

public class LoginActivity extends BaseActivity {

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.layout_phone)
    EditTextLayout mPhoneLayout;

    @Bind(R.id.layout_password)
    EditTextLayout mPwdLayout;

    private String mPhone;

    private String mPassword;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.login);
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        findViewById(R.id.layout_back).setVisibility(View.GONE);

        mPhoneLayout.setLeftTv("+1");
        mPhoneLayout.setInputType(InputType.TYPE_CLASS_PHONE);
        mPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        if (!StringUtils.getInstance().isNullOrEmpty(Utils.getUserId(LoginActivity.this))
                && !StringUtils.getInstance().isNullOrEmpty(Utils.getToken(LoginActivity.this))) {
            LoginActivity.this.finish();
            MainActivity.invoke(LoginActivity.this);
        }
    }

    @OnClick({R.id.layout_back, R.id.layout_register,
            R.id.layout_forget_password, R.id.btn_login})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                LoginActivity.this.finish();
                break;
            }

            case R.id.layout_register: {
                RegisterActivity.invoke(LoginActivity.this);
                break;
            }

            case R.id.layout_forget_password: {
                FindPwdActivity.invoke(LoginActivity.this);
                break;
            }

            case R.id.btn_login: {
                login();
                break;
            }

            default: {
                break;
            }
        }
    }

    private void login() {
        if (verifyInput()) {
            showNetDialog("");
            OkHttpClientManager.getInstance().login(mPhone, mPassword, new WtNetWorkListener<User>() {
                @Override
                public void onSucess(RemoteReturnData<User> data) {
                    if (null != data
                            && null != data.data
                            && !StringUtils.getInstance().isNullOrEmpty(data.data.token)) {
                        String token = data.data.token;
                        Utils.setToken(LoginActivity.this, token);
                        String userId = data.data.user_id;
                        Utils.setUserId(LoginActivity.this, userId);

                        if (!StringUtils.getInstance().isNullOrEmpty(data.data.nick_name)) {
                            Utils.setNickName(LoginActivity.this, data.data.nick_name);
                        }

                        if (!StringUtils.getInstance().isNullOrEmpty(data.data.head_image)) {
                            Utils.setHeadImage(LoginActivity.this, data.data.head_image);
                        }

                        MainActivity.invoke(LoginActivity.this);
                        LoginActivity.this.finish();
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

    private boolean verifyInput() {
        mPhone = mPhoneLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mPhone)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.phone_hint);
            return false;
        }

        mPassword = mPwdLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mPassword)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.password_hint);
            return false;
        }

        return true;
    }

    public static void invoke(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void loginError(Context context, String status) {
        if (StringUtils.getInstance().isNullOrEmpty(status)) {
            return;
        }

        if (status.equals(HttpConstant.MsgCode.MSG_CODE_LOGIN_ERROR_ONE)
                || status.equals(HttpConstant.MsgCode.MSG_CODE_LOGIN_ERROR_THREE)
                || status.equals(HttpConstant.MsgCode.MSG_CODE_LOGIN_ERROR_TWO)) {
            LoginActivity.invoke(context);
        }
    }

}
