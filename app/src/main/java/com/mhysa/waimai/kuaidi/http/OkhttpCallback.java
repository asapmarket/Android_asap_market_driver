package com.mhysa.waimai.kuaidi.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.joey.devilfish.utils.StringUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 文件描述
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class OkhttpCallback extends Callback<RemoteReturnData> {

    private Type type;

    public OkhttpCallback(Type type) {
        this.type = type;
    }

    @Override
    public RemoteReturnData parseNetworkResponse(Response response, int id) throws Exception {
        try {
            String result = response.body().string();
            if (StringUtils.getInstance().isNullOrEmpty(result)) {
                return new RemoteReturnData("", "No Data", "没有收到数据", null);
            }
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            return gson.fromJson(result, type);
        } finally {
            response.body().close();
        }
    }

    @Override
    public void onError(Call call, Exception e, int id) {

    }

    @Override
    public void onResponse(RemoteReturnData response, int id) {

    }
}
