package com.mhysa.waimai.kuaidi.ui.adapters.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.ui.customerviews.OrderStoreLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单列表
 * Date: 2017/7/22
 *
 * @author xusheng
 */

public class HomeOrderListAdapter extends BaseAdapter {

    private Context mContext;

    private List<Order> mOrders = new ArrayList<Order>();

    private boolean mIsOffWork = false;

    private HomeOrderListAdapterListener mListener;

    public HomeOrderListAdapter(Context context, List<Order> orders) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_home_order, null);

            holder.mGrabOrderTv = (TextView) convertView.findViewById(R.id.tv_grab_order);
            holder.mOrderAddressTv = (TextView) convertView.findViewById(R.id.tv_address);
            holder.mStoreLayout = (LinearLayout) convertView.findViewById(R.id.layout_store);
            holder.mOrderNumberTv = (TextView) convertView.findViewById(R.id.tv_order_number);
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

            StringUtils.getInstance().setText(order.distribution_time, holder.mDistributionTimeTv);

            if (null != order.store_list && order.store_list.size() > 0) {
                holder.mStoreLayout.setVisibility(View.VISIBLE);
                holder.mStoreLayout.removeAllViews();

                final int size = order.store_list.size();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                for (int i = 0; i < size; i++) {
                    OrderStoreLayout orderStoreLayout = new OrderStoreLayout(mContext);
                    orderStoreLayout.setData(order.order_id, order.store_list.get(i), false, null);
                    holder.mStoreLayout.addView(orderStoreLayout, params);
                }
            } else {
                holder.mStoreLayout.setVisibility(View.GONE);
            }

            holder.mGrabOrderTv.setEnabled(!mIsOffWork);
            holder.mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.grabOrder(position);
                    }
                }
            });
        }

        return convertView;
    }

    public void setOffWork(boolean isOffWork) {
        this.mIsOffWork = isOffWork;
        this.notifyDataSetChanged();
    }

    public void setListener(HomeOrderListAdapterListener listener) {
        this.mListener = listener;
    }

    public interface HomeOrderListAdapterListener {
        void grabOrder(int position);
    }

    class ViewHolder {

        TextView mGrabOrderTv;
        TextView mOrderAddressTv;
        LinearLayout mStoreLayout;
        TextView mOrderNumberTv;
        TextView mDistributionTimeTv;
    }
}
