package com.mhysa.waimai.kuaidi.ui.activities.errandorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.joey.devilfish.config.AppConfig;
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
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrder;
import com.mhysa.waimai.kuaidi.ui.customerviews.addpicture.AddPictureLayout;
import com.mhysa.waimai.kuaidi.ui.fragments.bottommenu.FeedbackPriceBottomFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 文件描述
 * Date: 2018/3/24
 *
 * @author xusheng
 */

public class ErrandOrderDetailActivity extends BaseActivity {

    private static final int HANDLER_REFRESH_DATA = 0;

    private static final int REFRESH_DATA_DELAY = 10 * 1000;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    private static final String INTENT_ID = "intent_id";

    private static final String INTENT_TAG = "intent_tag";

    private static final String INTENT_IS_OFFWORK = "intent_is_offwork";

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    @Bind(R.id.prsv)
    PullToRefreshScrollView mScrollView;

    @Bind(R.id.tv_grab_order)
    TextView mGrabOrderTv;

    @Bind(R.id.layout_order_time)
    RelativeLayout mOrderTimeLayout;

    @Bind(R.id.layout_order_status)
    LinearLayout mOrderStatusLayout;

    @Bind(R.id.tv_distance)
    TextView mDistanceTv;

    @Bind(R.id.tv_address)
    TextView mAddressTv;

    @Bind(R.id.tv_description)
    TextView mDescriptinTv;

    @Bind(R.id.layout_pictures)
    AddPictureLayout mAddPictureLayout;

    @Bind(R.id.btn_finish_buy)
    Button mFinishBuyBtn;

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

    @Bind(R.id.layout_confirm_receipt)
    LinearLayout mConfirmReceiptLayout;

    @Bind(R.id.btn_confirm_receipt)
    Button mConfirmReceiptBtn;

    private String mOrderId;

    private ErrandOrder mOrder;

    private boolean mIsOffWork;

    private String mTag;

    private boolean mIsMine;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case HANDLER_REFRESH_DATA: {
                    loadData(false);
                    mHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_DATA, REFRESH_DATA_DELAY);
                    break;
                }
            }
        }
    };

    @Override
    protected int getContentLayout() {
        return R.layout.activity_errand_order_detail;
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        if (null != getIntent()) {
            mOrderId = getIntent().getStringExtra(INTENT_ID);
            mIsOffWork = getIntent().getBooleanExtra(INTENT_IS_OFFWORK, false);
            mTag = getIntent().getStringExtra(INTENT_TAG);
            mIsMine = (!StringUtils.getInstance().isNullOrEmpty(mTag) && !mTag.equals(ErrandOrderListActivity.TAG_HOME)) ? true : false;
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

        int marginBottom = ExtendUtils.getInstance().dip2px(this, 49);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.bottomMargin = marginBottom;
        mScrollView.setLayoutParams(params);
        mScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                loadData(true);
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        EventBus.getDefault().register(this);
        loadData(false);
        mHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_DATA, REFRESH_DATA_DELAY);
    }

    private void loadData(boolean isRefresh) {
        if (StringUtils.getInstance().isNullOrEmpty(mOrderId)) {
            mScrollView.onRefreshComplete();
            return;
        }

        if (!isRefresh) {
//            showNetDialog("");
        }

        OkHttpClientManager.getInstance().getErrandOrderDetail(Utils.getUserId(this), Utils.getToken(this),
                mOrderId, new WtNetWorkListener<ErrandOrder>() {
                    @Override
                    public void onSucess(final RemoteReturnData<ErrandOrder> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != data && null != data.data) {
                                    mOrder = data.data;
                                    drawViews();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                                mScrollView.onRefreshComplete();
                            }
                        });
                    }
                });

    }

    private void drawViews() {
        if (null == mOrder
                || StringUtils.getInstance().isNullOrEmpty(mOrder.order_id)
                || StringUtils.getInstance().isNullOrEmpty(mOrder.state)) {
            return;
        }

        StringUtils.getInstance().setText(mOrder.cust_address, mAddressTv);
        mAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOrder
                        && !StringUtils.getInstance().isNullOrEmpty(mOrder.addr_lat)
                        && !StringUtils.getInstance().isNullOrEmpty(mOrder.addr_lng)) {
                    startNaviGoogle(mOrder.addr_lat, mOrder.addr_lng);
                }
            }
        });
        StringUtils.getInstance().setText(mOrder.distance, mDistanceTv);
        StringUtils.getInstance().setText(mOrder.remark, mDescriptinTv);
        if (null != mOrder.imgs && mOrder.imgs.size() > 0) {
            mAddPictureLayout.setVisibility(View.VISIBLE);
            int imageWidth = (AppConfig.getScreenWidth() - ExtendUtils.getInstance().dip2px(this, 110)) / 5;
            int imageHeight = imageWidth;
            mAddPictureLayout.setVisibility(View.VISIBLE);
            mAddPictureLayout.setNumInLine(5);
            mAddPictureLayout.setIsView(true);
            mAddPictureLayout.setLimitSize(5);
            mAddPictureLayout.setDimensions(imageWidth, imageHeight);
            mAddPictureLayout.addPictures(mOrder.imgs);
        } else {
            mAddPictureLayout.setVisibility(View.GONE);
        }

        String state = mOrder.state;

        if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_FEEDBACK)) {
            mFinishBuyBtn.setVisibility(View.GONE);
            mFinishBuyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateOrderState(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD);
                }
            });
        } else {
            mFinishBuyBtn.setVisibility(View.GONE);
        }

        StringUtils.getInstance().setText(mOrder.zip_code, mEmailCodeTv);

        if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
            mOrderTimeLayout.setVisibility(View.GONE);
        } else {
            mOrderTimeLayout.setVisibility(View.VISIBLE);
            StringUtils.getInstance().setText(mOrder.order_time, mOrderTimeTv);
        }

        String pay_method = mOrder.payment_method;
        if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
            mOrderStatusTv.setText(R.string.order_status_wait_for_taken);
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_TAKEN)) {
            mOrderStatusTv.setText(R.string.order_status_wait_for_feedback);
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_FEEDBACK)) {
            if (!StringUtils.getInstance().isNullOrEmpty(pay_method)) {
                if (pay_method.equals(FusionCode.PayMethod.PAY_METHOD_CASH)) {
                    mOrderStatusTv.setText(R.string.order_status_pay_in_cash);
                } else {
                    mOrderStatusTv.setText(R.string.order_status_pay_in_cash);
                }
            } else {
                mOrderStatusTv.setText(R.string.order_status_wait_for_pay);
            }
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD)) {
            mOrderStatusTv.setText(R.string.order_status_on_road);
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
            mOrderStatusTv.setText(R.string.order_status_finished);
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CANCELLED)) {
            mOrderStatusTv.setText(R.string.order_status_cancel);
        }

        if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_CHECKOUT_SUCCESS)) {
            mGrabOrderTv.setVisibility(View.VISIBLE);
            mGrabOrderTv.setEnabled(!mIsOffWork);
            mGrabOrderTv.setText(R.string.grab_order);
            mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    grabOrder();
                }
            });
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_TAKEN)) {
            mGrabOrderTv.setVisibility(View.VISIBLE);
            mGrabOrderTv.setText(R.string.feedback_price);
            mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    feedbackPrice();
                }
            });
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_FEEDBACK)) {
            mGrabOrderTv.setVisibility(View.VISIBLE);
            mGrabOrderTv.setText(R.string.order_status_on_road);
            mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateOrderState(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD);
                }
            });
        } else if (state.equals(FusionCode.OrderStatus.ORDER_STATUS_ON_ROAD)) {
            mGrabOrderTv.setVisibility(View.VISIBLE);
            mGrabOrderTv.setText(R.string.order_status_finished);
            mGrabOrderTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateOrderState(FusionCode.OrderStatus.ORDER_STATUS_FINISHED);
                }
            });
        } else {
            mGrabOrderTv.setVisibility(View.GONE);
        }

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
                    && mIsMine && !state.equals(FusionCode.OrderStatus.ORDER_STATUS_FINISHED)) {
                // 未支付的使用现金的订单，显示确认收款
                mConfirmReceiptLayout.setVisibility(View.VISIBLE);
                mGrabOrderTv.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.bottomMargin = ExtendUtils.getInstance().dip2px(this, 85);
                mScrollView.setLayoutParams(params);

                mConfirmReceiptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmReceipt();
                    }
                });
            } else {
                mConfirmReceiptLayout.setVisibility(View.GONE);
            }
        }

        StringUtils.getInstance().setText(mOrder.order_id, mOrderNumberTv);
        StringUtils.getInstance().setText(mOrder.cust_phone, mOrderPhoneTv);
    }

    private void grabOrder() {
        showNetDialog("");
        OkHttpClientManager.getInstance().takeErrandOrder(Utils.getUserId(this), Utils.getToken(this), mOrderId,
                new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadData(false);
                                EventBus.getDefault().post(new ApplicationEvent(EventID.ERRAND_ORDER_DETAIL_ORDER_STATE_CHANGE_SUCCESS, mTag));
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
                    }
                });
    }

    private void feedbackPrice() {
        FeedbackPriceBottomFragment feedbackPriceBottomFragment = new FeedbackPriceBottomFragment();
        feedbackPriceBottomFragment.setListener(new FeedbackPriceBottomFragment.OnFeedbackListener() {
            @Override
            public void onConfirm(String totalMoney) {
                try {
                    doFeedback(totalMoney);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        feedbackPriceBottomFragment.show(getFragmentManager(), "FeedbackPriceBottomFragment");
    }

    private void confirmReceipt() {
        AlertDialog.Builder builder = PromptUtils.getInstance().getAlertDialog(ErrandOrderDetailActivity.this, true);
        builder.setTitle(R.string.confirm_receipt)
                .setMessage(R.string.confirm_receipt_tip2)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showNetDialog("");
                        updateOrderState(FusionCode.OrderStatus.ORDER_STATUS_FINISHED);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void doFeedback(String money) throws Exception {
        showNetDialog("");
        OkHttpClientManager.getInstance().responseMoney(Utils.getUserId(this), Utils.getToken(this),
                mOrderId, money, new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(final RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadData(false);
                                EventBus.getDefault().post(new ApplicationEvent(EventID.ERRAND_ORDER_DETAIL_ORDER_STATE_CHANGE_SUCCESS, mTag));
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
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

    private void updateOrderState(String state) {
        showNetDialog("");
        OkHttpClientManager.getInstance().updateErrandOrderState(Utils.getUserId(this), Utils.getToken(this),
                mOrderId, state, new WtNetWorkListener<JsonElement>() {
                    @Override
                    public void onSucess(RemoteReturnData<JsonElement> data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new ApplicationEvent(EventID.ERRAND_ORDER_DETAIL_ORDER_STATE_CHANGE_SUCCESS, mTag));
                                loadData(false);
                            }
                        });
                    }

                    @Override
                    public void onError(String status, final String msg_cn, final String msg_en) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseError(msg_cn, msg_en);
                            }
                        });
                    }

                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeNetDialog();
                            }
                        });
                    }
                });
    }

    @OnClick({R.id.layout_back, R.id.layout_phone})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                ErrandOrderDetailActivity.this.finish();
                break;
            }

            case R.id.layout_phone: {
                if (ContextCompat.checkSelfPermission(ErrandOrderDetailActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // 没有获得授权，申请授权
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ErrandOrderDetailActivity.this,
                            Manifest.permission.CALL_PHONE)) {
                        PromptUtils.getInstance().showShortPromptToast(ErrandOrderDetailActivity.this, R.string.no_permission);

                        // 帮跳转到该应用的设置界面，让用户手动授权
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        // 不需要解释为何需要该权限，直接请求授权
                        ActivityCompat.requestPermissions(ErrandOrderDetailActivity.this,
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
            ExtendUtils.getInstance().doDial(ErrandOrderDetailActivity.this, mOrder.cust_phone);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(HANDLER_REFRESH_DATA);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(HANDLER_REFRESH_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_DATA, REFRESH_DATA_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(ApplicationEvent event) {

    }

    public static void invoke(Context context, String orderId, String tag, boolean isOffWork) {
        Intent intent = new Intent(context, ErrandOrderDetailActivity.class);
        intent.putExtra(INTENT_ID, orderId);
        intent.putExtra(INTENT_TAG, tag);
        intent.putExtra(INTENT_IS_OFFWORK, isOffWork);
        context.startActivity(intent);
    }
}
