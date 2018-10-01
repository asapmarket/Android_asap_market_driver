package com.mhysa.waimai.kuaidi.http;

/**
 * 文件描述
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class RemoteReturnData<T> {

    public String msg_cn;

    public String msg_en;

    public String status;

    public T data;

    public RemoteReturnData(T data, String msg_cn, String msg_en, String status) {
        this.data = data;
        this.msg_cn = msg_cn;
        this.msg_en = msg_en;
        this.status = status;
    }
}
