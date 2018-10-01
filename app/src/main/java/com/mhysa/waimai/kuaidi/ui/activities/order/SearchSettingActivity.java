package com.mhysa.waimai.kuaidi.ui.activities.order;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.TimeUtils;
import com.joey.devilfish.utils.Utils;
import com.joey.devilfish.widget.datetimepicker.DateTimePickerWidget;
import com.joey.devilfish.widget.datetimepicker.OnDateChangedListener;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.model.order.OrderListRequest;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 搜索条件设置界面
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class SearchSettingActivity extends BaseActivity {

    private static final String INTENT_ORDER_REQUEST = "INTENT_ORDER_REQUEST";

    private static final String INTENT_TAG = "INTENT_TAG";

    @Bind(R.id.layout_order_status)
    RelativeLayout mStatusLayout;

    @Bind(R.id.tv_order_status)
    TextView mOrderStatusTv;

    @Bind(R.id.iv_order_status)
    ImageView mOrderStatusIv;

    @Bind(R.id.layout_order_status_list)
    LinearLayout mStatusListLayout;

    @Bind(R.id.layout_start_time)
    RelativeLayout mStartTimeLayout;

    @Bind(R.id.tv_start_time)
    TextView mStartTimeTv;

    @Bind(R.id.layout_end_time)
    RelativeLayout mEndTimeLayout;

    @Bind(R.id.tv_end_time)
    TextView mEndTimeTv;

    @Bind(R.id.et_order)
    EditText mOrderEt;

    @Bind(R.id.et_phone)
    EditText mPhoneEt;

    @Bind(R.id.layout_pay_type)
    RelativeLayout mPayTypeLayout;

    @Bind(R.id.tv_pay_type)
    TextView mPayTypeTv;

    @Bind(R.id.iv_pay_type)
    ImageView mPayTypeIv;

    @Bind(R.id.layout_pay_type_list)
    LinearLayout mPayTypeListLayout;

    private Date mCurrentStartDate;

    private Date mCurrentEndDate;

    private DateTimePickerWidget mChooseWidget;

    private OrderListRequest mRequest;

    private String mPayMethod;

    private String mOrderStatus;

    private String mTag;

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mRequest = (OrderListRequest) getIntent().getSerializableExtra(INTENT_ORDER_REQUEST);
            mTag = getIntent().getStringExtra(INTENT_TAG);
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_search_setting;
    }

    @OnClick({R.id.layout_back, R.id.tv_reset,
            R.id.tv_search, R.id.layout_order_status,
            R.id.layout_pay_type, R.id.layout_start_time,
            R.id.layout_end_time, R.id.tv_paytype_visa,
            R.id.tv_paytype_paypal, R.id.tv_paytype_balance,
            R.id.tv_paytype_credit_card,
            R.id.tv_paytype_cash,
            R.id.tv_success, R.id.tv_taken,
            R.id.tv_taking, R.id.tv_onroad,
            R.id.tv_finished})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                SearchSettingActivity.this.finish();
                break;
            }

            case R.id.tv_reset: {
                reset();
                break;
            }

            case R.id.tv_search: {
                search();
                break;
            }

            case R.id.layout_order_status: {
                if (mStatusListLayout.getVisibility() == View.VISIBLE) {
                    mStatusListLayout.setVisibility(View.GONE);
                    mOrderStatusIv.setImageResource(R.mipmap.ic_arrow_down);
                } else {
                    mStatusListLayout.setVisibility(View.VISIBLE);
                    mOrderStatusIv.setImageResource(R.mipmap.ic_arrow_up);
                }
                break;
            }

            case R.id.layout_pay_type: {
                if (mPayTypeListLayout.getVisibility() == View.VISIBLE) {
                    mPayTypeListLayout.setVisibility(View.GONE);
                    mPayTypeIv.setImageResource(R.mipmap.ic_arrow_down);
                } else {
                    mPayTypeListLayout.setVisibility(View.VISIBLE);
                    mPayTypeIv.setImageResource(R.mipmap.ic_arrow_up);
                }
                break;
            }

            case R.id.layout_start_time: {
                selectDate(true);
                break;
            }

            case R.id.layout_end_time: {
                selectDate(false);
                break;
            }

            case R.id.tv_paytype_visa: {
                mPayMethod = FusionCode.PayMethod.PAY_METHOD_VISA;
                mPayTypeTv.setText("visa");
                break;
            }

            case R.id.tv_paytype_paypal: {
                mPayMethod = FusionCode.PayMethod.PAY_METHOD_PAYPAL;
                mPayTypeTv.setText("paypal");
                break;
            }

            case R.id.tv_paytype_balance: {
                mPayMethod = FusionCode.PayMethod.PAY_METHOD_BALANCE;
                mPayTypeTv.setText(getResources().getString(R.string.balance));
                break;
            }

            case R.id.tv_paytype_credit_card: {
                mPayMethod = FusionCode.PayMethod.PAY_METHOD_CREDIT_CARD;
                mPayTypeTv.setText(getResources().getString(R.string.credit_card));
                break;
            }

            case R.id.tv_paytype_cash: {
                mPayMethod = FusionCode.PayMethod.PAY_METHOD_CASH;
                mPayTypeTv.setText(getResources().getString(R.string.pay_in_cash));
                break;
            }

            case R.id.tv_success: {
                // 下单成功
                mOrderStatus = FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS;
                mOrderStatusTv.setText(getResources().getString(R.string.order_status_buy_success));
                break;
            }

            case R.id.tv_taken: {
                // 快递员接单
                mOrderStatus = FusionCode.OrderStatus.ORDER_STATUS_TAKEN;
                mOrderStatusTv.setText(getResources().getString(R.string.order_status_taken));
                break;
            }

            case R.id.tv_taking: {
                // 取件中
                mOrderStatus = FusionCode.OrderStatus.ORDER_STATUS_IN_PIECE;
                mOrderStatusTv.setText(getResources().getString(R.string.order_status_taking));
                break;
            }

            case R.id.tv_onroad: {
                // 在路上
                mOrderStatus = FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD;
                mOrderStatusTv.setText(getResources().getString(R.string.order_status_on_road));
                break;
            }

            case R.id.tv_finished: {
                // 下单成功
                mOrderStatus = FusionCode.OrderStatus.ORDER_STATUS_FINISHED;
                mOrderStatusTv.setText(getResources().getString(R.string.order_status_finished));
                break;
            }

            default: {
                break;
            }
        }
    }

    private void reset() {
        mOrderStatusTv.setText(getResources().getString(R.string.order_status));
        mCurrentStartDate = null;
        mCurrentEndDate = null;

        mStartTimeTv.setText(getResources().getString(R.string.start_time));
        mEndTimeTv.setText(getResources().getString(R.string.end_time));
        mPayTypeTv.setText(getResources().getString(R.string.pay_type));
        mOrderEt.setText("");
        mPhoneEt.setText("");

        mPayMethod = "";
        mOrderStatus = "";
    }

    private void search() {
        mRequest = new OrderListRequest();
        mRequest.user_id = Utils.getUserId(SearchSettingActivity.this);
        mRequest.token = Utils.getToken(SearchSettingActivity.this);
        mRequest.page = FusionCode.PageConstant.INIT_PAGE;
        mRequest.page_size = FusionCode.PageConstant.PAGE_SIZE;
        mRequest.state = mOrderStatus;
        if (!StringUtils.getInstance().isNullOrEmpty(mPayMethod)) {
            mRequest.pay_method = mPayMethod;
        }

        if (null != mCurrentStartDate
                && !StringUtils.getInstance().isNullOrEmpty(mStartTimeTv.getText().toString().trim())) {
            mRequest.start_time = mStartTimeTv.getText().toString().trim();
        }

        if (null != mCurrentEndDate
                && !StringUtils.getInstance().isNullOrEmpty(mEndTimeTv.getText().toString().trim())) {
            mRequest.end_time = mEndTimeTv.getText().toString().trim();
        }

        if (!StringUtils.getInstance().isNullOrEmpty(mPhoneEt.getText().toString().trim())) {
            mRequest.cust_phone = mPhoneEt.getText().toString().trim();
        }

        if (!StringUtils.getInstance().isNullOrEmpty(mOrderEt.getText().toString().trim())) {
            mRequest.order_id = mOrderEt.getText().toString().trim();
        }

        EventBus.getDefault().post(new ApplicationEvent(EventID.SEARCH_SETTING_SUCCESS, mTag, mRequest));
        SearchSettingActivity.this.finish();
    }

    private void selectDate(boolean isStartDate) {
        if (null == mChooseWidget) {
            mChooseWidget = new DateTimePickerWidget(this, getSupportFragmentManager());
            mChooseWidget.setNeedSetTime(true);
        }

        if (isStartDate) {
            mChooseWidget.setOnDateChangedListener(new OnDateChangedListener() {
                @Override
                public void onDateChanged(Date date) {
                    if (null == date) {
                        return;
                    }

                    if (null != mCurrentEndDate && TimeUtils.getInstance().compareDate(date, mCurrentEndDate) > 0) {
                        PromptUtils.getInstance().showShortPromptToast(SearchSettingActivity.this, R.string.appointment_time_valid);
                        return;
                    }

                    mCurrentStartDate = date;
                    mStartTimeTv.setText(TimeUtils.getInstance().getAllFormatByDate(mCurrentStartDate, new SimpleDateFormat("yyyy-MM-dd HH:mm")));
                }
            });

            if (null == mCurrentStartDate) {
                Calendar calendar = Calendar.getInstance();
                mCurrentStartDate = calendar.getTime();
            }
            mChooseWidget.pickDate(mCurrentStartDate);
            return;
        }

        mChooseWidget.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(Date date) {
                if (null == date) {
                    return;
                }


                if (null != mCurrentStartDate && TimeUtils.getInstance().compareDate(date, mCurrentStartDate) <= 0) {
                    PromptUtils.getInstance().showShortPromptToast(SearchSettingActivity.this, R.string.appointment_time_valid);
                    return;
                }

                mCurrentEndDate = date;
                mEndTimeTv.setText(TimeUtils.getInstance().getAllFormatByDate(mCurrentEndDate, new SimpleDateFormat("yyyy-MM-dd HH:mm")));
            }
        });

        if (null == mCurrentEndDate) {
            Calendar calendar = Calendar.getInstance();
            mCurrentEndDate = calendar.getTime();
        }

        mChooseWidget.pickDate(mCurrentEndDate);
    }

    public static void invoke(Context context, OrderListRequest request, String tag) {
        Intent intent = new Intent(context, SearchSettingActivity.class);
        intent.putExtra(INTENT_ORDER_REQUEST, request);
        intent.putExtra(INTENT_TAG, tag);
        context.startActivity(intent);
    }
}
