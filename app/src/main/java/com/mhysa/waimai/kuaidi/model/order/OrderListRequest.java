package com.mhysa.waimai.kuaidi.model.order;

import java.io.Serializable;

/**
 * 订单请求
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class OrderListRequest implements Serializable {

    public String user_id;

    public String token;

    public String keyword;

    public int page;

    public int page_size;

    public String zip_code;

    public String state;

    public String start_time;

    public String end_time;

    public String pay_method;

    public String order_id;

    public String cust_phone;

    // 查询已接订单和已经完成的订单需要传
    public String exp_id;

}
