package com.mhysa.waimai.kuaidi.ui.activities.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
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
import com.mhysa.waimai.kuaidi.ui.customerviews.EditTextLayout;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 修改密码
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class ChangePwdActivity extends BaseActivity {

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.layout_old_password)
    EditTextLayout mOldPwdLayout;

    @Bind(R.id.layout_new_password)
    EditTextLayout mNewPwdLayout;

    @Bind(R.id.layout_confirm_new_password)
    EditTextLayout mConfirmNewPwdLayout;

    private String mOldPwd;

    private String mNewPwd;

    private String mConfirmPwd;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_change_pwd;
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.change_password);
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);
        mOldPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmNewPwdLayout.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @OnClick({R.id.layout_back, R.id.btn_commit})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                ChangePwdActivity.this.finish();
                break;
            }

            case R.id.btn_commit: {
                changePwd();
                break;
            }

            default: {
                break;
            }
        }
    }

    private void changePwd() {
        mOldPwd = mOldPwdLayout.getContent();
        if (StringUtils.getInstance().isNullOrEmpty(mOldPwd)) {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.old_password_hint);
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
        OkHttpClientManager.getInstance().changePwd(mOldPwd, mNewPwd,
                Utils.getToken(this), Utils.getUserId(this), new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        PromptUtils.getInstance().showShortPromptToast(ChangePwdActivity.this, R.string.change_password_success);
                        ChangePwdActivity.this.finish();
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
        Intent intent = new Intent(context, ChangePwdActivity.class);
        context.startActivity(intent);
    }
}
