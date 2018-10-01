package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.food.Food;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 文件描述
 * Date: 2017/7/23
 *
 * @author xusheng
 */

public class OrderFoodLayout extends RelativeLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.tv_name)
    TextView mNameTv;

    @Bind(R.id.tv_spec)
    TextView mSpecTv;

    @Bind(R.id.tv_number)
    TextView mNumberTv;

    @Bind(R.id.tv_take)
    TextView mTakeTv;

    public OrderFoodLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public OrderFoodLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_order_food, null);

        ButterKnife.bind(this, mRootView);

        this.addView(mRootView);
    }

    public void setData(final String orderId, final String storeId,
                        final Food food, boolean isMine, final PickUpListener listener) {
        if (null == food) {
            return;
        }

        if (Utils.isChinese(mContext)) {
            mNameTv.setText(StringUtils.getInstance().isNullOrEmpty(food.foods_name_cn) ?
                    "" : food.foods_name_cn);
        } else {
            mNameTv.setText(StringUtils.getInstance().isNullOrEmpty(food.foods_name_en) ?
                    "" : food.foods_name_en);
        }

        if (null != food.spec_list
                && food.spec_list.size() > 0) {

            StringBuilder specSb = new StringBuilder();
            final int specSize = food.spec_list.size();
            if (Utils.isChinese(mContext)) {
                for (int i = 0; i < specSize; i++) {
                    if (null != food.spec_list.get(i)) {
                        if (i == 0) {
                            specSb.append(food.spec_list.get(i).spec_name_cn);
                        } else {
                            specSb.append(" " + food.spec_list.get(i).spec_name_cn);
                        }
                    }
                }
            } else {
                for (int i = 0; i < specSize; i++) {
                    if (null != food.spec_list.get(i)) {
                        if (i == 0) {
                            specSb.append(food.spec_list.get(i).spec_name_en);
                        } else {
                            specSb.append(" " + food.spec_list.get(i).spec_name_en);
                        }
                    }
                }
            }

            mSpecTv.setText("_" + specSb.toString());
        }

        mNumberTv.setText("*" + food.foods_quantity);
        if (!isMine) {
            mTakeTv.setVisibility(View.GONE);
        } else {
            mTakeTv.setVisibility(View.VISIBLE);

            if (food.pickup_state.equals(FusionCode.FoodPickupState.STATE_NOT_PICKUP)) {
                // 取件
                mTakeTv.setText(R.string.take);
                mTakeTv.setBackgroundColor(Color.parseColor("#2196f3"));
                mTakeTv.setTextColor(Color.parseColor("#ffffff"));
            } else if (food.pickup_state.equals(FusionCode.FoodPickupState.STATE_ALREADY_PICKUP)) {
                // 已取
                mTakeTv.setText(R.string.taken);
                mTakeTv.setTextColor(Color.parseColor("#999999"));
                mTakeTv.setBackgroundColor(Color.parseColor("#00000000"));
            }

            mTakeTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (food.pickup_state.equals(FusionCode.FoodPickupState.STATE_NOT_PICKUP)
                            && null != listener) {
                        listener.pickUp(food.foods_id, storeId, orderId, mTakeTv);
                    }
                }
            });
        }
    }

    public interface PickUpListener {
        void pickUp(String foodId, String storeId, String orderId, TextView textView);
    }

}
