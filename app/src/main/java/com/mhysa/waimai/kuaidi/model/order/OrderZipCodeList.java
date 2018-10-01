package com.mhysa.waimai.kuaidi.model.order;

import java.io.Serializable;
import java.util.List;

/**
 * 文件描述
 * Date: 2017/8/16
 *
 * @author xusheng
 */

public class OrderZipCodeList implements Serializable {

    public int total;

    public int total_page;

    public List<OrderZipCode> rows;
}
