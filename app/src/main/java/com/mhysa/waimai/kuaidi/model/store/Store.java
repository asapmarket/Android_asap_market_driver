package com.mhysa.waimai.kuaidi.model.store;

import com.mhysa.waimai.kuaidi.model.food.Food;

import java.util.List;

/**
 * 商家
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class Store {

    public String store_id;

    public String store_name_cn;

    public String store_name_en;

    public String store_address_cn;

    public String store_address_en;

    public List<Food> foods_list;
}
