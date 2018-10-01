package com.mhysa.waimai.kuaidi.ui.adapters.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.ui.customerviews.OrderStatusLayout;
import com.mhysa.waimai.kuaidi.ui.customerviews.OrderStoreLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单adapter
 * Date: 2017/7/23
 *
 * @author xusheng
 */

public class OrderListAdapter extends BaseAdapter {

    private Context mContext;

    private List<Order> mOrders = new ArrayList<Order>();

    private OrderListAdapterListener mListener;

    public void setListener(OrderListAdapterListener listener) {
        this.mListener = listener;
    }

    public OrderListAdapter(Context context, List<Order> orders) {
        this.mContext = context;
        this.mOrders = orders;
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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_order, null);

            holder.mTakenIv = (ImageView) convertView.findViewById(R.id.iv_taken);
            holder.mOrderAddressTv = (TextView) convertView.findViewById(R.id.tv_address);
            holder.mOrderDistanceTv = (TextView) convertView.findViewById(R.id.tv_distance);
            holder.mStoreLayout = (LinearLayout) convertView.findViewById(R.id.layout_store);
            holder.mOrderNumberTv = (TextView) convertView.findViewById(R.id.tv_order_number);
            holder.mOrderDeliveryTimeTv = (TextView) convertView.findViewById(R.id.tv_order_delivery_time);
            holder.mOrderStatusLayout = (OrderStatusLayout) convertView.findViewById(R.id.layout_order_status);
            holder.mFinishTv = (TextView) convertView.findViewById(R.id.tv_finish);
            holder.mDistributionTimeTv = (TextView) convertView.findViewById(R.id.tv_distribution_time);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Order order = mOrders.get(position);
        if (null != order) {
            holder.mOrderAddressTv.setText(StringUtils.getInstance().isNullOrEmpty(order.cust_address)
                    ? "" : order.cust_address);
            holder.mOrderNumberTv.setText(StringUtils.getInstance().isNullOrEmpty(order.order_id) ? "" : order.order_id);

            if (null != order.store_list && order.store_list.size() > 0) {
                holder.mStoreLayout.setVisibility(View.VISIBLE);
                holder.mStoreLayout.removeAllViews();

                final int size = order.store_list.size();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                for (int i = 0; i < size; i++) {
                    OrderStoreLayout orderStoreLayout = new OrderStoreLayout(mContext);
                    orderStoreLayout.setData(order.order_id, order.store_list.get(i), true, null);
                    holder.mStoreLayout.addView(orderStoreLayout, params);
                }
            } else {
                holder.mStoreLayout.setVisibility(View.GONE);
            }

            holder.mOrderDeliveryTimeTv.setText(StringUtils.getInstance().isNullOrEmpty(order.create_time)
                    ? "" : order.create_time);

            holder.mOrderStatusLayout.setOrderStatus(order.state);

            holder.mTakenIv.setImageResource(R.drawable.ic_taken);

            StringUtils.getInstance().setText(order.distribution_time, holder.mDistributionTimeTv);

            if (!StringUtils.getInstance().isNullOrEmpty(order.state)
                    && order.state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
                holder.mFinishTv.setVisibility(View.GONE);
            } else {
                holder.mFinishTv.setVisibility(View.VISIBLE);
            }

            holder.mFinishTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 完成订单
                    if (null != mListener) {
                        mListener.onFinished(position);
                    }
                }
            });

        }

        return convertView;
    }

    class ViewHolder {

        ImageView mTakenIv;
        TextView mOrderAddressTv;
        TextView mOrderDistanceTv;
        LinearLayout mStoreLayout;
        TextView mOrderNumberTv;
        OrderStatusLayout mOrderStatusLayout;
        TextView mOrderDeliveryTimeTv;
        TextView mFinishTv;
        TextView mDistributionTimeTv;
    }

    public interface OrderListAdapterListener {

        void onFinished(int position);
    }
}
