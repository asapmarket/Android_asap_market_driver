package com.mhysa.waimai.kuaidi.http;

/**
 * 文件描述
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public interface WtNetWorkListener<T> {

    /**
     * 成功
     *
     * @param data
     */
    void onSucess(RemoteReturnData<T> data);

    /**
     * 失败，会有详细失败说明
     */
    void onError(String status, String msg_cn, String msg_en);

    /**
     * 请求完成，页面做一些操作
     */
    void onFinished();
}
