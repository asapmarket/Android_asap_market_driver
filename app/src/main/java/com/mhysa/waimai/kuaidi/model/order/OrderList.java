package com.mhysa.waimai.kuaidi.model.order;

import java.util.List;

/**
 * 订单数据，按照邮编划分
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class OrderList {

    // 订单列表
    public List<Order> rows;

    // 总条数
    public int total;

    // 总页数
    public int total_page;

    public String onwork;
}
