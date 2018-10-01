package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.mhysa.waimai.kuaidi.R;

/**
 * 主页底部菜单
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class MainBottomLayout extends RelativeLayout implements RadioGroup.OnCheckedChangeListener {

    private Context mContext;

    private View mRootView;

    private RadioGroup mRadioGroup;

    private OnOperateListener mListener;

    public MainBottomLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public MainBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_main_bottom, null);

        mRadioGroup = (RadioGroup) mRootView.findViewById(R.id.rg_buttons);
        mRadioGroup.setOnCheckedChangeListener(this);

        this.addView(mRootView);
    }

    public void setOrderChecked() {
        RadioButton orderRb = (RadioButton) mRootView.findViewById(R.id.rb_order);
        orderRb.setChecked(true);
    }

    public void setHomeChecked() {
        RadioButton homeRb = (RadioButton) mRootView.findViewById(R.id.rb_home);
        homeRb.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (null == group || null == mRadioGroup || mRadioGroup != group) {
            return;
        }

        if (null != mListener) {
            mListener.onMenuSelected(checkedId);
        }
    }

    public void setListener(OnOperateListener listener) {
        this.mListener = listener;
    }

    public RadioGroup getRadioGroup() {
        return mRadioGroup;
    }

    public interface OnOperateListener {
        void onMenuSelected(int selecteId);
    }
}
