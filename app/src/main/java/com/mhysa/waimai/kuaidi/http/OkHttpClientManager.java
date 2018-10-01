package com.mhysa.waimai.kuaidi.http;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.joey.devilfish.utils.MD5;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.BuildConfig;
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrder;
import com.mhysa.waimai.kuaidi.model.errand.ErrandOrderList;
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.model.order.OrderList;
import com.mhysa.waimai.kuaidi.model.order.OrderListLocation;
import com.mhysa.waimai.kuaidi.model.order.OrderListRequest;
import com.mhysa.waimai.kuaidi.model.order.OrderZipCodeList;
import com.mhysa.waimai.kuaidi.model.order.PickupResult;
import com.mhysa.waimai.kuaidi.model.upload.UploadInfo;
import com.mhysa.waimai.kuaidi.model.user.User;
import com.mhysa.waimai.kuaidi.model.verifycode.VerifyCode;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 文件描述
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class OkHttpClientManager {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClientManager mInstance;

    private OkHttpClientManager() {
    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 上传文件
     *
     * @param name
     * @param file
     * @param netWorkListener
     */
    public void uploadInfo(String name, File file, final WtNetWorkListener<UploadInfo> netWorkListener) {
        String url = BuildConfig.BASE_URL + "app/common/upload.do";
        HashMap<String, String> params = new HashMap<>();

        OkHttpUtils.post().params(params)
                .addFile("file", name, file)
                .url(url).build()
                .readTimeOut(5000)
                .writeTimeOut(5000)
                .connTimeOut(3000)
                .execute(new OkhttpCallback(new TypeToken<RemoteReturnData<UploadInfo>>() {
                }.getType()) {
                    @Override
                    public void onResponse(RemoteReturnData response, int id) {
                        super.onResponse(response, id);
                        if (null == netWorkListener) {
                            return;
                        }

                        if (null == response
                                || StringUtils.getInstance().isNullOrEmpty(response.status)) {
                            netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                            return;
                        }

                        String code = response.status;
                        if (code.equals(HttpConstant.MsgCode.MSG_CODE_SUCCESS)) {
                            // 成功
                            netWorkListener.onSucess(response);
                        } else {
                            netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        super.onError(call, e, id);
                        if (null == netWorkListener) {
                            return;
                        }
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, e.getMessage(), e.getMessage());
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        if (null == netWorkListener) {
                            return;
                        }
                        netWorkListener.onFinished();
                    }
                });
    }

    private void get(String url, Map<String, String> params, Type type, final WtNetWorkListener netWorkListener) {
        try {
            if (null != params && params.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(url);
                Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
                while (it.hasNext()) {
                    stringBuilder.append("&");
                    Map.Entry<String, String> entry = it.next();
                    stringBuilder.append(entry.getKey());
                    stringBuilder.append("=");
                    stringBuilder.append(entry.getValue());
                }

                url = stringBuilder.toString();

                if (url.contains("&")) {
                    url = url.replaceFirst("\\^|&", "?");
                }
            }

            OkHttpUtils.get().url(url).build().execute(new OkhttpCallback(type) {
                @Override
                public void onResponse(RemoteReturnData response, int id) {
                    super.onResponse(response, id);
                    if (null == netWorkListener) {
                        return;
                    }

                    if (null == response
                            || StringUtils.getInstance().isNullOrEmpty(response.status)) {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                        return;
                    }

                    String code = response.status;
                    if (code.equals(HttpConstant.MsgCode.MSG_CODE_SUCCESS)) {
                        // 成功
                        netWorkListener.onSucess(response);
                    } else {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                    }
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    super.onError(call, e, id);
                    if (null == netWorkListener) {
                        return;
                    }
                    netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, e.getMessage(), e.getMessage());
                }

                @Override
                public void onAfter(int id) {
                    super.onAfter(id);
                    if (null == netWorkListener) {
                        return;
                    }
                    netWorkListener.onFinished();
                }
            });
        } catch (Exception ex) {
            if (null != netWorkListener) {
                netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, ex.getMessage(), ex.getMessage());
            }
        }
    }

    private void post(String url, Map<String, String> params, Type type, final WtNetWorkListener netWorkListener) {
        try {
            OkHttpUtils.post().url(url).params(params).build().execute(new OkhttpCallback(type) {
                @Override
                public void onResponse(RemoteReturnData response, int id) {
                    super.onResponse(response, id);
                    if (null == netWorkListener) {
                        return;
                    }

                    if (null == response || StringUtils.getInstance().isNullOrEmpty(response.status)) {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                        return;
                    }

                    String code = response.status;
                    if (code.equals(HttpConstant.MsgCode.MSG_CODE_SUCCESS)) {
                        // 成功
                        netWorkListener.onSucess(response);
                    } else {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, response.msg_cn, response.msg_en);
                    }
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    super.onError(call, e, id);
                    if (null == netWorkListener) {
                        return;
                    }
                    netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, e.getMessage(), e.getMessage());
                }

                @Override
                public void onAfter(int id) {
                    super.onAfter(id);
                    if (null == netWorkListener) {
                        return;
                    }
                    netWorkListener.onFinished();
                }
            });
        } catch (Exception e) {
            if (null != netWorkListener) {
                netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, e.getMessage(), e.getMessage());
            }
        }
    }

    private void post(String url, RequestBody requestBody, final Type type, final WtNetWorkListener netWorkListener) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            if (BuildConfig.DEBUG) {
                Log.e("xusheng", "url=" + url);
            }
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, e.getMessage(), e.getMessage());
                    if (null != netWorkListener) {
                        netWorkListener.onFinished();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != netWorkListener) {
                        netWorkListener.onFinished();
                    }

                    String result = response.body().string();
                    if (BuildConfig.DEBUG) {
                        Log.e("xusheng", "result=" + result);
                    }
                    if (StringUtils.getInstance().isNullOrEmpty(result)) {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, "没有收到数据", "No Data");
                    }
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                            .create();
                    RemoteReturnData returnData = gson.fromJson(result, type);
                    if (null == returnData) {
                        netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, "没有收到数据", "No Data");
                    } else {
                        String code = returnData.status;
                        if (code.equals(HttpConstant.MsgCode.MSG_CODE_SUCCESS)) {
                            // 成功
                            netWorkListener.onSucess(returnData);
                        } else {
                            netWorkListener.onError(code, returnData.msg_cn, returnData.msg_en);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            if (null != netWorkListener) {
                netWorkListener.onFinished();
            }

            if (null != netWorkListener) {
                netWorkListener.onError(HttpConstant.MsgCode.MSG_CODE_FAIL, ex.getMessage(), ex.getMessage());
            }
        }
    }

    /**
     * 登录
     *
     * @param phone
     * @param pwd
     * @param wtNetWorkListener
     */
    public void login(String phone, String pwd, WtNetWorkListener<User> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/login.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("pwd", MD5.md5(pwd));
        this.get(url, params, new TypeToken<RemoteReturnData<User>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 注册
     *
     * @param phone
     * @param pwd
     * @param code
     * @param wtNetWorkListener
     */
    public void register(String phone, String pwd, String code, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/expReg.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("pwd", MD5.md5(pwd));
        params.put("code", code);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 验证码
     *
     * @param phone
     * @param wtNetWorkListener
     */
    public void verifyCode(String phone, WtNetWorkListener<VerifyCode> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/getCerCode.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phone);
        this.get(url, params, new TypeToken<RemoteReturnData<VerifyCode>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 修改密码
     *
     * @param old_pwd
     * @param new_pwd
     * @param token
     * @param wtNetWorkListener
     */
    public void changePwd(String old_pwd, String new_pwd,
                          String token, String user_id,
                          WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/updatePwd.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("old_pwd", MD5.md5(old_pwd));
        params.put("new_pwd", MD5.md5(new_pwd));
        params.put("token", token);
        params.put("user_id", user_id);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 修改昵称
     *
     * @param nick_name
     * @param token
     * @param user_id
     * @param wtNetWorkListener
     */
    public void changeNickname(String nick_name,
                               String token, String user_id,
                               WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/updateNickName.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("nick_name", nick_name);
        params.put("token", token);
        params.put("user_id", user_id);
        this.post(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 修改头像
     *
     * @param head_image
     * @param token
     * @param user_id
     * @param wtNetWorkListener
     */
    public void changeAvatar(String head_image,
                             String token, String user_id,
                             WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/updateHead.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("head_image", head_image);
        params.put("token", token);
        params.put("user_id", user_id);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 找回密码
     *
     * @param phone
     * @param code
     * @param new_pwd
     * @param wtNetWorkListener
     */
    public void findPassword(String phone,
                             String code, String new_pwd,
                             WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/expLogin/forgetPwd.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("code", code);
        params.put("new_pwd", MD5.md5(new_pwd));
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 首页数据
     *
     * @param user_id
     * @param token
     * @param page
     * @param page_size
     */
    public void getHomeInfo(String user_id, String token,
                            int page, int page_size,
                            WtNetWorkListener<OrderZipCodeList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getOrderCodeList.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("page", page + "");
        params.put("page_size", page_size + "");
        this.get(url, params, new TypeToken<RemoteReturnData<OrderZipCodeList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 根据条件获取邮编列表
     *
     * @param request
     * @param wtNetWorkListener
     */
    public void getOrderZipCodeList(OrderListRequest request,
                                    WtNetWorkListener<OrderZipCodeList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getExpOrderCodeList.do";

        Gson gson = new Gson();
        String json = gson.toJson(request);
        HashMap<String, String> params = gson.fromJson(json,
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        this.get(url, params, new TypeToken<RemoteReturnData<OrderZipCodeList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 获取订单列表
     *
     * @param request
     * @param wtNetWorkListener
     */
    public void getOrderList(OrderListRequest request,
                             WtNetWorkListener<OrderList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getOrderListByZipCode.do";

        Gson gson = new Gson();
        String json = gson.toJson(request);
        HashMap<String, String> params = gson.fromJson(json,
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        this.get(url, params, new TypeToken<RemoteReturnData<OrderList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 获取快递员订单列表
     *
     * @param request
     * @param wtNetWorkListener
     */
    public void getExpOrderList(OrderListRequest request,
                                WtNetWorkListener<OrderList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getExpOrderListByZipCode.do";

        Gson gson = new Gson();
        String json = gson.toJson(request);
        HashMap<String, String> params = gson.fromJson(json,
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        this.get(url, params, new TypeToken<RemoteReturnData<OrderList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 获取订单详情
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param wtNetWorkListener
     */
    public void getOrderDetail(String user_id, String token, String order_id,
                               WtNetWorkListener<Order> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getOrderDetail.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("order_id", order_id);
        this.get(url, params, new TypeToken<RemoteReturnData<Order>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 取件
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param store_id
     * @param food_id
     * @param wtNetWorkListener
     */
    public void pickup(String user_id, String token, String order_id,
                       String store_id, String food_id,
                       WtNetWorkListener<PickupResult> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/pickupOrderFoods.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("order_id", order_id);
        params.put("store_id", store_id);
        params.put("foods_id", food_id);
        this.get(url, params, new TypeToken<RemoteReturnData<PickupResult>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 上传经纬度
     *
     * @param user_id
     * @param token
     * @param lng
     * @param lat
     * @param wtNetWorkListener
     */
    public void uploadLocation(String user_id, String token,
                               String lng, String lat,
                               WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/uploadLocation.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("lng", lng);
        params.put("lat", lat);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 标记订单在路上
     *
     * @param user_id
     * @param token
     * @param wtNetWorkListener
     */
    public void onRoad(String user_id, String token,
                       WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/onTheWay.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 确认订单-即完成订单
     *
     * @param user_id
     * @param token
     * @param orderId
     * @param wtNetWorkListener
     */
    public void confirmOrder(String user_id, String token,
                             String orderId, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/sendTo.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("order_id", orderId);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 接单
     *
     * @param user_id
     * @param token
     * @param orderId
     * @param wtNetWorkListener
     */
    public void grabOrder(String user_id, String token,
                          String orderId, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/takeOrders.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("order_id", orderId);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 地图接单
     *
     * @param wtNetWorkListener
     */
    public void orderListMap(String user_id, String token,
                             WtNetWorkListener<OrderListLocation> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/getOrderList4Map.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        this.get(url, params, new TypeToken<RemoteReturnData<OrderListLocation>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 现金支付的订单，确认收款
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param wtNetWorkListener
     */
    public void payOrder(String user_id, String token,
                         String order_id, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/order/payOrder.do";
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("token", token);
        params.put("order_id", order_id);
        this.get(url, params, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 根据条件获取跑腿订单邮编列表
     *
     * @param request
     * @param wtNetWorkListener
     */
    public void getErrandOrderZipCodeList(OrderListRequest request,
                                          WtNetWorkListener<OrderZipCodeList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/getExpOrderCodeList.do";

        Gson gson = new Gson();
        String json = gson.toJson(request);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<OrderZipCodeList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 根据条件查询跑腿订单列表
     *
     * @param request
     * @param wtNetWorkListener
     */
    public void getErrandOrderList(OrderListRequest request,
                                   WtNetWorkListener<ErrandOrderList> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/getOrderListByZipCode.do";

        Gson gson = new Gson();
        String json = gson.toJson(request);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<ErrandOrderList>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 查看跑腿订单详情
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param wtNetWorkListener
     */
    public void getErrandOrderDetail(String user_id, String token, String order_id,
                                     WtNetWorkListener<ErrandOrder> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/getOrderDetail.do";

        ErrandOrder errandOrder = new ErrandOrder();
        errandOrder.user_id = user_id;
        errandOrder.token = token;
        errandOrder.order_id = order_id;
        Gson gson = new Gson();
        String json = gson.toJson(errandOrder);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<ErrandOrder>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 接单
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param wtNetWorkListener
     */
    public void takeErrandOrder(String user_id, String token, String order_id,
                                WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/takeOrders.do";

        ErrandOrder errandOrder = new ErrandOrder();
        errandOrder.user_id = user_id;
        errandOrder.token = token;
        errandOrder.order_id = order_id;
        Gson gson = new Gson();
        String json = gson.toJson(errandOrder);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 反馈价格
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param total_money
     * @param wtNetWorkListener
     */
    public void responseMoney(String user_id, String token, String order_id,
                              String total_money, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/updateOrderErrandsTotalMoney.do";

        ErrandOrder errandOrder = new ErrandOrder();
        errandOrder.user_id = user_id;
        errandOrder.token = token;
        errandOrder.order_id = order_id;
        errandOrder.total_money = total_money;
        Gson gson = new Gson();
        String json = gson.toJson(errandOrder);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }

    /**
     * 修改跑腿订单状态
     *
     * @param user_id
     * @param token
     * @param order_id
     * @param state
     * @param wtNetWorkListener
     */
    public void updateErrandOrderState(String user_id, String token, String order_id,
                                       String state, WtNetWorkListener<JsonElement> wtNetWorkListener) {
        String url = BuildConfig.BASE_URL + "app/orderErrands/updateOrderErrandsState.do";

        ErrandOrder errandOrder = new ErrandOrder();
        errandOrder.user_id = user_id;
        errandOrder.token = token;
        errandOrder.order_id = order_id;
        errandOrder.state = state;
        Gson gson = new Gson();
        String json = gson.toJson(errandOrder);
        RequestBody requestBody = RequestBody.create(JSON, json);
        this.post(url, requestBody, new TypeToken<RemoteReturnData<JsonElement>>() {
        }.getType(), wtNetWorkListener);
    }
}
