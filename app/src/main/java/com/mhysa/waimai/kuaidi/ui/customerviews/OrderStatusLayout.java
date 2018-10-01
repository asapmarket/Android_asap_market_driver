package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 订单状态
 * Date: 2017/7/23
 *
 * @author xusheng
 */

public class OrderStatusLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.iv_status)
    ImageView mStatusIv;

    @Bind(R.id.tv_status)
    TextView mStatusTv;

    public OrderStatusLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public OrderStatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_order_status, null);

        ButterKnife.bind(this, mRootView);

        this.addView(mRootView);
    }

    public void setOrderStatus(String status) {
        if (StringUtils.getInstance().isNullOrEmpty(status)) {
            mStatusTv.setText(R.string.order_in_delivery);
            mStatusTv.setTextColor(Color.parseColor("#40afff"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_40afff);
            return;
        }
        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
            // 订单完成
            mStatusTv.setText(R.string.order_finished);
            mStatusTv.setTextColor(Color.parseColor("#169a13"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_169a13);
        } else if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD)) {
            // 配送中
            mStatusTv.setText(R.string.order_status_on_road);
            mStatusTv.setTextColor(Color.parseColor("#40afff"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_40afff);
        } else {
            // 配送中
            mStatusTv.setText(R.string.order_in_delivery);
            mStatusTv.setTextColor(Color.parseColor("#40afff"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_40afff);
        }
    }

}
