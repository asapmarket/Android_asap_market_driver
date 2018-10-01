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
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 文件描述
 * Date: 2018/3/27
 *
 * @author xusheng
 */

public class ErrandOrderStatusLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.iv_status)
    ImageView mStatusIv;

    @Bind(R.id.tv_status)
    TextView mStatusTv;

    public ErrandOrderStatusLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public ErrandOrderStatusLayout(Context context, AttributeSet attrs) {
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

    public void setOrderStatus(ErrandOrder order) {
        if (null == order || StringUtils.getInstance().isNullOrEmpty(order.state)) {
            return;
        }
        final String status = order.state;
        final String pay_method = order.payment_method;
        mStatusTv.setVisibility(View.VISIBLE);
        mStatusIv.setVisibility(View.VISIBLE);
        if (StringUtils.getInstance().isNullOrEmpty(status)) {
            mStatusTv.setText(R.string.order_in_delivery);
            mStatusTv.setTextColor(Color.parseColor("#40afff"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_40afff);
            return;
        }
        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
            // 订单完成
            mStatusTv.setText(R.string.order_finished);
            mStatusTv.setTextColor(Color.parseColor("#40afff"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_40afff);

            return;
        }

        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_TAKEN)) {
            // 已接单
            mStatusTv.setText(R.string.order_status_wait_for_feedback);
            mStatusTv.setTextColor(Color.parseColor("#e01d1d"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_e01d1d);

            return;
        }

        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_FEEDBACK)) {
            // 已反馈
            if (!StringUtils.getInstance().isNullOrEmpty(pay_method)) {
                if (pay_method.equals(FusionCode.PayMethod.PAY_METHOD_CASH)) {
                    mStatusTv.setText(R.string.order_status_pay_in_cash);
                } else {
                    mStatusTv.setText(R.string.order_status_pay_in_cash);
                }
            } else {
                mStatusTv.setText(R.string.order_status_wait_for_pay);
            }
            mStatusTv.setTextColor(Color.parseColor("#009944"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_009944);

            return;
        }

        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD)) {
            // 在路上
            mStatusTv.setText(R.string.order_status_on_road);
            mStatusTv.setTextColor(Color.parseColor("#fa6b37"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_fa6b37);
            return;
        }

        if (status.equals(FusionCode.OrderStatus.ORDER_STATUS_CANCELLED)) {
            // 已取消
            mStatusTv.setText(R.string.order_status_cancel);
            mStatusTv.setTextColor(Color.parseColor("#e01d1d"));
            mStatusIv.setImageResource(R.drawable.bg_corner_2dp_e01d1d);

            return;
        }

        mStatusTv.setVisibility(View.INVISIBLE);
        mStatusIv.setVisibility(View.INVISIBLE);
    }
}
