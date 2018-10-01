package com.mhysa.waimai.kuaidi.ui.adapters.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 按邮编分组的订单列表
 * Date: 2017/8/16
 *
 * @author xusheng
 */

public class OrderZipCodeAdapter extends BaseAdapter {

    private Context mContext;

    private List<OrderZipCode> mOrderZipCodes = new ArrayList<OrderZipCode>();

    public OrderZipCodeAdapter(Context context, List<OrderZipCode> orderZipCodes) {
        this.mContext = context;
        this.mOrderZipCodes = orderZipCodes;
    }

    @Override
    public int getCount() {

        if (null != mOrderZipCodes) {
            return mOrderZipCodes.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != mOrderZipCodes && mOrderZipCodes.size() > position) {
            return mOrderZipCodes.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_order_zipcode, null);

            holder.mTopView = (View) convertView.findViewById(R.id.view_top);
            holder.mZipCodeTv = (TextView) convertView.findViewById(R.id.tv_zip_code);
            holder.mCountTv = (TextView) convertView.findViewById(R.id.tv_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OrderZipCode orderZipCode = mOrderZipCodes.get(position);
        if (null != orderZipCode) {
            holder.mCountTv.setText(orderZipCode.count + "");
            StringUtils.getInstance().setText(orderZipCode.zip_code, holder.mZipCodeTv);
        }

        holder.mTopView.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

        return convertView;
    }

    class ViewHolder {
        View mTopView;
        TextView mZipCodeTv;
        TextView mCountTv;
    }
}
