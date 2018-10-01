package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joey.devilfish.utils.ExtendUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.food.Food;
import com.mhysa.waimai.kuaidi.model.store.Store;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商家
 * Date: 2017/7/23
 *
 * @author xusheng
 */

public class OrderStoreLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.tv_store_name)
    TextView mNameTv;

    @Bind(R.id.tv_store_address)
    TextView mAddressTv;

    @Bind(R.id.layout_food)
    LinearLayout mFoodLayout;

    public OrderStoreLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public OrderStoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_order_store, null);

        ButterKnife.bind(this, mRootView);

        this.addView(mRootView);
    }

    public void setData(String orderId, Store store, boolean isMine, OrderFoodLayout.PickUpListener listener) {
        if (null == store) {
            return;
        }

        if (Utils.isChinese(mContext)) {
            mNameTv.setText(StringUtils.getInstance().isNullOrEmpty(store.store_name_cn) ?
                    "" : store.store_name_cn);
            mAddressTv.setText(StringUtils.getInstance().isNullOrEmpty(store.store_address_cn) ?
                    "" : store.store_address_cn);
        } else {
            mNameTv.setText(StringUtils.getInstance().isNullOrEmpty(store.store_name_en) ?
                    "" : store.store_name_en);
            mAddressTv.setText(StringUtils.getInstance().isNullOrEmpty(store.store_address_en) ?
                    "" : store.store_address_en);
        }


        if (null != store.foods_list
                && store.foods_list.size() > 0) {
            mFoodLayout.setVisibility(View.VISIBLE);
            List<Food> foods = store.foods_list;
            final int size = foods.size();
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = ExtendUtils.getInstance().dip2px(mContext, 5);

            for (int i = 0; i < size; i++) {
                OrderFoodLayout orderFoodLayout = new OrderFoodLayout(mContext);
                orderFoodLayout.setData(orderId, store.store_id, foods.get(i), isMine, listener);
                mFoodLayout.addView(orderFoodLayout, params);
            }
        } else {
            mFoodLayout.setVisibility(View.GONE);
        }
    }

}
