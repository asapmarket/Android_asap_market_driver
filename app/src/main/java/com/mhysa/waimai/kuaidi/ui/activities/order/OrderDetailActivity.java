package com.mhysa.waimai.kuaidi.ui.activities.order;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.ExtendUtils;
import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.model.order.PickupResult;
import com.mhysa.waimai.kuaidi.model.store.Store;
import com.mhysa.waimai.kuaidi.ui.customerviews.OrderFoodLayout;
import com.mhysa.waimai.kuaidi.ui.customerviews.OrderStoreLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 订单详情
 * Date: 2017/7/23
 *
 * @author xusheng
 */

public class OrderDetailActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    private static final String INTENT_ORDER_ID = "intent_order_id";

    private static final String INTENT_IS_MINE = "intent_is_mine";

    private static final String INTENT_IS_OFFWORK = "intent_is_offwork";

    @Bind(R.id.prsv)
    PullToRefreshScrollView mScrollView;

    @Bind(R.id.tv_grab_order)
    TextView mGrabOrderTv;

    @Bind(R.id.layout_order_time)
    RelativeLayout mOrderTimeLayout;

    @Bind(R.id.layout_order_status)
    LinearLayout mOrderStatusLayout;

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.tv_distance)
    TextView mDistanceTv;

    @Bind(R.id.tv_address)
    TextView mAddressTv;

    @Bind(R.id.layout_store)
    LinearLayout mStoreLayout;

    @Bind(R.id.tv_email_code)
    TextView mEmailCodeTv;

    @Bind(R.id.tv_order_time)
    TextView mOrderTimeTv;

    @Bind(R.id.tv_pay_type)
    TextView mPayTypeTv;

    @Bind(R.id.tv_order_number)
    TextView mOrderNumberTv;

    @Bind(R.id.tv_order_phone)
    TextView mOrderPhoneTv;

    @Bind(R.id.tv_order_status)
    TextView mOrderStatusTv;

    @Bind(R.id.tv_remark)
    TextView mRemarkTv;

    @Bind(R.id.tv_price)
    TextView mPriceTv;

    @Bind(R.id.layout_confirm_receipt)
    LinearLayout mConfirmReceiptLayout;

    @Bind(R.id.btn_confirm_receipt)
    Button mConfirmReceiptBtn;

    @Bind(R.id.tv_distribution_time)
    TextView mDistributionTimeTv;

    private String mOrderId;

    private Order mOrder;

    private boolean mIsMine;

    private boolean mIsOffWork;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_order_detail;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mOrderId = getIntent().getStringExtra(INTENT_ORDER_ID);
            mIsMine = getIntent().getBooleanExtra(INTENT_IS_MINE, false);
            mIsOffWork = getIntent().getBooleanExtra(INTENT_IS_OFFWORK, false);
        }
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.order_detail);
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);

        mOrderTimeLayout.setVisibility(mIsMine ? View.VISIBLE : View.GONE);
        mOrderStatusLayout.setVisibility(mIsMine ? View.VISIBLE : View.GONE);
        mGrabOrderTv.setVisibility(mIsMine ? View.GONE : View.VISIBLE);
        int marginBottom = mIsMine ? 0 : ExtendUtils.getInstance().dip2px(this, 49);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.bottomMargin = marginBottom;
        mScrollView.setLayoutParams(params);

        if (!mIsOffWork) {
            mGrabOrderTv.setBackgroundColor(Color.parseColor("#2196f3"));
            mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    grabOrder();
                }
            });
        } else {
            mGrabOrderTv.setBackgroundColor(Color.parseColor("#b3b3b3"));
        }

        mScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                loadData(true);
            }
        });

        mAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOrder
                        && !StringUtils.getInstance().isNullOrEmpty(mOrder.lat)
                        && !StringUtils.getInstance().isNullOrEmpty(mOrder.lng)) {
//                    MapRouteActivity.invoke(OrderDetailActivity.this, mOrder.lat, mOrder.lng);
                    startNaviGoogle(mOrder.lat, mOrder.lng);
                }
            }
        });

        mConfirmReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmReceipt();
            }
        });
    }

    public void startNaviGoogle(String lat, String lng) {
        if (Utils.isAppAvailable(this, "com.google.android.apps.maps")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            PromptUtils.getInstance().showShortPromptToast(this, R.string.no_google_map_app);
        }
    }

    private void grabOrder() {
        showNetDialog("");
        OkHttpClientManager.getInstance().grabOrder(Utils.getUserId(this),
                Utils.getToken(this), mOrderId,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        if (null != data && null != data.data) {
                            EventBus.getDefault().post(new ApplicationEvent(EventID.ORDER_DETAIL_GRAB_ORDER_SUCCESS));
                            mIsMine = true;

                            mOrderTimeLayout.setVisibility(mIsMine ? View.VISIBLE : View.GONE);
                            mOrderStatusLayout.setVisibility(mIsMine ? View.VISIBLE : View.GONE);
                            mGrabOrderTv.setVisibility(mIsMine ? View.GONE : View.VISIBLE);
                            int marginBottom = mIsMine ? 0 : ExtendUtils.getInstance().dip2px(OrderDetailActivity.this, 49);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);
                            params.bottomMargin = marginBottom;
                            mScrollView.setLayoutParams(params);

                            loadData(true);
                        }
                    }

                    @Override
                    public void onError(String status, String msg_cn, String msg_en) {
                        responseError(msg_cn, msg_en);
                    }

                    @Override
                    public void onFinished() {
                        closeNetDialog();
                    }
                });
    }

    private void confirmReceipt() {
        AlertDialog.Builder builder = PromptUtils.getInstance().getAlertDialog(OrderDetailActivity.this, true);
        builder.setTitle(R.string.confirm_receipt)
                .setMessage(R.string.confirm_receipt_tip2)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showNetDialog("");
                        doConfirmReceipt();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void doConfirmReceipt() {
        OkHttpClientManager.getInstance().payOrder(Utils.getUserId(this),
                Utils.getToken(this), mOrder.order_id,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        if (null != data) {
                            PromptUtils.getInstance().showShortPromptToast(OrderDetailActivity.this, "SUCCESS");
                            mConfirmReceiptLayout.setVisibility(View.GONE);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);
                            params.bottomMargin = 0;
                            mScrollView.setLayoutParams(params);
                        }
                    }

                    @Override
                    public void onError(String status, String msg_cn, String msg_en) {
                        responseError(msg_cn, msg_en);
                    }

                    @Override
                    public void onFinished() {
                        closeNetDialog();
                    }
                });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        loadData(false);
    }

    @OnClick({R.id.layout_back, R.id.layout_phone})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                OrderDetailActivity.this.finish();
                break;
            }

            case R.id.layout_phone: {
                if (ContextCompat.checkSelfPermission(OrderDetailActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // 没有获得授权，申请授权
                    if (ActivityCompat.shouldShowRequestPermissionRationale(OrderDetailActivity.this,
                            Manifest.permission.CALL_PHONE)) {
                        PromptUtils.getInstance().showShortPromptToast(OrderDetailActivity.this, R.string.no_permission);

                        // 帮跳转到该应用的设置界面，让用户手动授权
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        // 不需要解释为何需要该权限，直接请求授权
                        ActivityCompat.requestPermissions(OrderDetailActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    }
                } else {
                    // 已经获得授权，可以打电话
                    callPhone();
                }
                break;
            }

            default: {
                break;
            }
        }
    }

    private void callPhone() {
        if (null != mOrder
                && !StringUtils.getInstance().isNullOrEmpty(mOrder.cust_phone)) {
            ExtendUtils.getInstance().doDial(OrderDetailActivity.this, mOrder.cust_phone);
        }
    }

    private void loadData(boolean isRefresh) {
        if (StringUtils.getInstance().isNullOrEmpty(mOrderId)) {
            mScrollView.onRefreshComplete();
            return;
        }

        if (!isRefresh) {
            showNetDialog("");
        }

        OkHttpClientManager.getInstance().getOrderDetail(Utils.getUserId(this),
                Utils.getToken(this), mOrderId, new WtNetWorkListener<Order>() {
                    @Override
                    public void onSucess(RemoteReturnData<Order> data) {
                        if (null != data && null != data.data) {
                            mOrder = data.data;
                            drawviews();
                        }
                    }

                    @Override
                    public void onError(String status, String msg_cn, String msg_en) {
                        responseError(msg_cn, msg_en);
                    }

                    @Override
                    public void onFinished() {
                        closeNetDialog();
                        mScrollView.onRefreshComplete();
                    }
                });
    }

    private void drawviews() {
        if (null != mOrder.store_list
                && mOrder.store_list.size() > 0) {
            mStoreLayout.removeAllViews();
            List<Store> stores = mOrder.store_list;
            final int size = stores.size();
            for (int i = 0; i < size; i++) {
                OrderStoreLayout layout = new OrderStoreLayout(OrderDetailActivity.this);
                layout.setData(mOrder.order_id, stores.get(i), mIsMine, mPickupListener);
                mStoreLayout.addView(layout);
            }
        }

        mAddressTv.setText(mOrder.cust_address);
        mEmailCodeTv.setText(mOrder.zip_code);
        mOrderTimeTv.setText(mOrder.order_time);
        if (!StringUtils.getInstance().isNullOrEmpty(mOrder.payment_method)) {
            if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_METHOD_VISA)) {
                mPayTypeTv.setText("VISA");
            } else if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_METHOD_PAYPAL)) {
                mPayTypeTv.setText("paypal");
            } else if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_METHOD_CREDIT_CARD)) {
                mPayTypeTv.setText(getResources().getString(R.string.credit_card));
            } else if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_METHOD_CASH)) {
                mPayTypeTv.setText(getResources().getString(R.string.pay_in_cash));
            } else if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_REWARD_POINT)) {
                mPayTypeTv.setText(getResources().getString(R.string.paymethod_rewardpoint));
            }

            if (mOrder.payment_method.equals(FusionCode.PayMethod.PAY_METHOD_CASH)
                    && !StringUtils.getInstance().isNullOrEmpty(mOrder.pay_state)
                    && !mOrder.pay_state.equals(FusionCode.PayStatus.PAY_STATUS_PAIED)
                    && mIsMine) {
                // 未支付的使用现金的订单，显示确认收款
                mConfirmReceiptLayout.setVisibility(View.VISIBLE);
                mGrabOrderTv.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.bottomMargin = ExtendUtils.getInstance().dip2px(this, 85);
                mScrollView.setLayoutParams(params);
            }
        }

        if (!StringUtils.getInstance().isNullOrEmpty(mOrder.state)) {
            String state = mOrder.state;
            if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
                mOrderStatusTv.setText(R.string.order_status_buy_success);
            } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_TAKEN)) {
                mOrderStatusTv.setText(R.string.order_status_taken);
            } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_IN_PIECE)) {
                mOrderStatusTv.setText(R.string.order_status_taking);
            } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD)) {
                mOrderStatusTv.setText(R.string.order_status_on_road);
            } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
                mOrderStatusTv.setText(R.string.order_status_finished);
            }
        }

        mOrderNumberTv.setText(mOrder.order_id);
        mOrderPhoneTv.setText(mOrder.cust_phone);
        mDistanceTv.setText(mOrder.distance);
        mPriceTv.setText("$" + mOrder.total_money);
        StringUtils.getInstance().setText(mOrder.remark, mRemarkTv);
        StringUtils.getInstance().setText(mOrder.distribution_time, mDistributionTimeTv);
    }

    private OrderFoodLayout.PickUpListener mPickupListener = new OrderFoodLayout.PickUpListener() {
        @Override
        public void pickUp(String foodId, String storeId, String orderId, final TextView textView) {
            if (StringUtils.getInstance().isNullOrEmpty(foodId)
                    || StringUtils.getInstance().isNullOrEmpty(storeId)
                    || StringUtils.getInstance().isNullOrEmpty(orderId)) {
                return;
            }

            showNetDialog("");
            OkHttpClientManager.getInstance().pickup(Utils.getUserId(OrderDetailActivity.this),
                    Utils.getToken(OrderDetailActivity.this), orderId, storeId, foodId,
                    new WtNetWorkListener<PickupResult>() {
                        @Override
                        public void onSucess(RemoteReturnData<PickupResult> data) {
                            if (null != data && null != data.data) {
                                try {
                                    PickupResult result = data.data;
                                    String state = result.order_state;
                                    textView.setText(R.string.taken);
                                    textView.setTextColor(Color.parseColor("#999999"));
                                    textView.setBackgroundColor(Color.parseColor("#00000000"));
                                    if (state.equals(FusionCode.FoodPickupState.STATE_ALREADY_PICKUP)) {
                                        // 已取
                                        mOrderStatusTv.setText(R.string.taken);
                                    } else {
                                        mOrderStatusTv.setText(R.string.order_status_taking);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String status, String msg_cn, String msg_en) {
                            responseError(msg_cn, msg_en);
                        }

                        @Override
                        public void onFinished() {
                            closeNetDialog();
                        }
                    });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 授权成功，继续打电话
                    callPhone();
                } else {
                    // 授权失败！
                    PromptUtils.getInstance().showShortPromptToast(OrderDetailActivity.this, R.string.no_permission);
                }
                break;
            }
        }
    }

    public static void invoke(Context context, String orderId, boolean isMine, boolean isOffWork) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(INTENT_ORDER_ID, orderId);
        intent.putExtra(INTENT_IS_MINE, isMine);
        intent.putExtra(INTENT_IS_OFFWORK, isOffWork);
        context.startActivity(intent);
    }
}
