package com.mhysa.waimai.kuaidi.ui.adapters.errandorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joey.devilfish.config.AppConfig;
import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.utils.ExtendUtils;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrder;
import com.mhysa.waimai.kuaidi.ui.customerviews.ErrandOrderStatusLayout;
import com.mhysa.waimai.kuaidi.ui.customerviews.addpicture.AddPictureLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述
 * Date: 2018/3/27
 *
 * @author xusheng
 */

public class ErrandOrderListAdapter extends BaseAdapter {

    private Context mContext;

    private List<ErrandOrder> mOrders = new ArrayList<ErrandOrder>();

    private ErrandOrderListAdapterListener mListener;

    private int mImageWidth;

    private int mImageHeight;

    private boolean mIsOffWork = false;

    public void setListener(ErrandOrderListAdapterListener listener) {
        this.mListener = listener;
    }

    public ErrandOrderListAdapter(Context context, List<ErrandOrder> orders) {
        this.mContext = context;
        this.mOrders = orders;

        mImageWidth = (AppConfig.getScreenWidth() - ExtendUtils.getInstance().dip2px(context, 122)) / 5;
        mImageHeight = mImageWidth;
    }

    @Override
    public int getCount() {

        if (null != mOrders) {
            return mOrders.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != mOrders && mOrders.size() > position) {
            return mOrders.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ErrandOrderListAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new ErrandOrderListAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_errand_order, null);

            holder.mTakenIv = (ImageView) convertView.findViewById(R.id.iv_taken);
            holder.mOrderAddressTv = (TextView) convertView.findViewById(R.id.tv_address);
            holder.mOrderDescriptionTv = (TextView) convertView.findViewById(R.id.tv_description);
            holder.mAddPictureLayout = (AddPictureLayout) convertView.findViewById(R.id.layout_pictures);
            holder.mOrderNumberTv = (TextView) convertView.findViewById(R.id.tv_order_number);
            holder.mOrderDeliveryTimeLayout = (LinearLayout) convertView.findViewById(R.id.layout_delivery_time);
            holder.mOrderDeliveryTimeTv = (TextView) convertView.findViewById(R.id.tv_order_delivery_time);
            holder.mOrderStatusLayout = (ErrandOrderStatusLayout) convertView.findViewById(R.id.layout_order_status);
            holder.mButtonTv = (TextView) convertView.findViewById(R.id.tv_button);

            convertView.setTag(holder);
        } else {
            holder = (ErrandOrderListAdapter.ViewHolder) convertView.getTag();
        }

        final ErrandOrder order = mOrders.get(position);
        if (null != order) {
            holder.mOrderAddressTv.setText(StringUtils.getInstance().isNullOrEmpty(order.cust_address)
                    ? "" : order.cust_address);
            holder.mOrderNumberTv.setText(StringUtils.getInstance().isNullOrEmpty(order.order_id) ? "" : order.order_id);
            StringUtils.getInstance().setText(order.remark, holder.mOrderDescriptionTv);

            if (!StringUtils.getInstance().isNullOrEmpty(order.state) &&
                    !order.state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
                holder.mOrderDeliveryTimeLayout.setVisibility(View.VISIBLE);
                holder.mOrderDeliveryTimeTv.setText(StringUtils.getInstance().isNullOrEmpty(order.order_time)
                        ? "" : order.order_time);
            } else {
                holder.mOrderDeliveryTimeLayout.setVisibility(View.GONE);
            }
            holder.mOrderStatusLayout.setOrderStatus(order);
            holder.mTakenIv.setImageResource(R.drawable.ic_taken);

            if (null != order.imgs && order.imgs.size() > 0) {
                holder.mAddPictureLayout.setVisibility(View.VISIBLE);
                holder.mAddPictureLayout.setNumInLine(5);
                holder.mAddPictureLayout.setLimitSize(5);
                holder.mAddPictureLayout.setIsView(true);
                holder.mAddPictureLayout.setDimensions(mImageWidth, mImageHeight);
                holder.mAddPictureLayout.addPictures(order.imgs);
            } else {
                holder.mAddPictureLayout.setVisibility(View.GONE);
            }

            if (StringUtils.getInstance().isNullOrEmpty(order.state)
                    || order.state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
                holder.mButtonTv.setVisibility(View.GONE);
            } else {
                holder.mButtonTv.setVisibility(View.VISIBLE);

                String state = order.state;
                if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
                    holder.mButtonTv.setEnabled(!mIsOffWork);
                    holder.mButtonTv.setText(R.string.grab_order);
                    holder.mButtonTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mListener) {
                                mListener.onGrab(position);
                            }
                        }
                    });
                } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_TAKEN)) {
                    holder.mButtonTv.setText(R.string.feedback_price);

                    holder.mButtonTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mListener) {
                                mListener.onFeedback(position);
                            }
                        }
                    });
                } else {
                    holder.mButtonTv.setText(R.string.order_status_finished);
                    holder.mButtonTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mListener) {
                                mListener.onFinish(position);
                            }
                        }
                    });
                }
            }

        }

        return convertView;
    }

    public void setOffWork(boolean isOffWork) {
        this.mIsOffWork = isOffWork;
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        ImageView mTakenIv;
        TextView mOrderAddressTv;
        TextView mOrderDescriptionTv;
        AddPictureLayout mAddPictureLayout;
        TextView mOrderNumberTv;
        ErrandOrderStatusLayout mOrderStatusLayout;
        LinearLayout mOrderDeliveryTimeLayout;
        TextView mOrderDeliveryTimeTv;
        TextView mButtonTv;
    }

    public interface ErrandOrderListAdapterListener {

        void onFinish(int position);

        void onFeedback(int position);

        void onGrab(int position);
    }
}
