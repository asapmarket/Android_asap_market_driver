package com.mhysa.waimai.kuaidi.model.order;

import com.mhysa.waimai.kuaidi.model.store.Store;

import java.util.List;

/**
 * 文件描述
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class Order {

    // 邮编
    public String zip_code;

    // 订单编号
    public String order_id;

    // 客户电话
    public String cust_phone;

    // 支付方式
    public String payment_method;

    // 客户地址
    public String cust_address;

    // 接单时间
    public String order_time;

    // 创建时间
    public String create_time;

    public String state;

    public String distance;

    // 取件商家列表
    public List<Store> store_list;

    // 经度
    public String lng;

    // 纬度
    public String lat;

    public double total_money;

    public String remark;

    // 支付状态，"1"是已经支付，"0"是未支付
    public String pay_state;

    // 配送时间
    public String distribution_time;
}
