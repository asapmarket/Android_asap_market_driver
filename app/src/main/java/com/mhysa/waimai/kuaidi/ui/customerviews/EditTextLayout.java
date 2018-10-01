package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 带图片的输入框
 * Date: 2017/7/20
 *
 * @author xusheng
 */

public class EditTextLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.iv_icon)
    ImageView mIconIv;

    @Bind(R.id.tv_left)
    TextView mLeftTv;

    @Bind(R.id.et_content)
    EditText mContentEt;

    @Bind(R.id.tv_right)
    TextView mRightTv;

    @Bind(R.id.layout_error)
    RelativeLayout mErrorLayout;

    @Bind(R.id.tv_error)
    TextView mErrorTv;

    @Bind(R.id.iv_right)
    ImageView mRightIv;

    private boolean mShowLeft;

    private boolean mShowRight;

    private int mIconId;

    private int mHintId;

    public EditTextLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public EditTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Mhysa_EditText);
        mShowLeft = typedArray.getBoolean(R.styleable.Mhysa_EditText_showLeft, true);
        mShowRight = typedArray.getBoolean(R.styleable.Mhysa_EditText_showRight, true);
        mHintId = typedArray.getResourceId(R.styleable.Mhysa_EditText_hint, 0);
        mIconId = typedArray.getResourceId(R.styleable.Mhysa_EditText_icon, 0);

        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_edittext, null);

        ButterKnife.bind(this, mRootView);

        mLeftTv.setVisibility(mShowLeft ? View.VISIBLE : View.GONE);
        mRightTv.setVisibility(mShowRight ? View.VISIBLE : View.GONE);

        if (mIconId > 0) {
            mIconIv.setVisibility(View.VISIBLE);
            mIconIv.setImageResource(mIconId);
        } else {
            mIconIv.setVisibility(View.GONE);
        }

        if (mHintId > 0) {
            mContentEt.setHint(mHintId);
        }

        this.addView(mRootView);
    }

    public void setLeftTv(String text) {
        if (!StringUtils.getInstance().isNullOrEmpty(text)) {
            mLeftTv.setText(text);
        }
    }

    public void setRightTv(String text, OnClickListener listener) {
        if (!StringUtils.getInstance().isNullOrEmpty(text)) {
            mRightTv.setText(text);
        }

        if (null != listener) {
            mRightTv.setOnClickListener(listener);
        }
    }

    public void setRightTv(String text) {
        if (!StringUtils.getInstance().isNullOrEmpty(text)) {
            mRightTv.setText(text);
        }
    }

    public void setRightTvClickable(boolean clickable) {
        mRightTv.setClickable(clickable);
    }

    public void setInputType(int type) {
        mContentEt.setInputType(type);
    }

    public String getContent() {
        if (null != mContentEt) {
            return mContentEt.getText().toString().trim();
        }

        return "";
    }

    public void setContent(String content) {
        if (null != mContentEt
                && !StringUtils.getInstance().isNullOrEmpty(content)) {
            mContentEt.setText(content);
        }
    }

    public void setError(String errorMsg) {
        mErrorTv.setText(errorMsg);
    }

    public void setErrorVisibility(int visibility) {
        mErrorLayout.setVisibility(visibility);
    }

    public void setRightVisibility(int visibility) {
        mRightIv.setVisibility(visibility);
    }
}
