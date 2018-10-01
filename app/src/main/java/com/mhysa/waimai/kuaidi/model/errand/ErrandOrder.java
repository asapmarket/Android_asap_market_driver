package com.mhysa.waimai.kuaidi.model.errand;

import java.io.Serializable;
import java.util.List;

/**
 * 跑腿订单
 * Date: 2018/3/15
 *
 * @author xusheng
 */

public class ErrandOrder implements Serializable {

    public String token;

    public String user_id;

    public String page;

    public String page_size;

    public String order_id;

    public String state;

    public String exp_name;

    public String remark;

    public List<ErrandImage> imgs;

    public String exp_id;

    public String exp_lat;

    public String exp_lng;

    public String total_money;

    public String create_time;

    public String cust_address;

    public String cust_name;

    public String cust_phone;

    public String extm_id;

    public String pay_state;

    public String payment_method;

    public String order_time;

    public String distance;

    public String addr_lat;

    public String addr_lng;

    public String zip_code;
}
