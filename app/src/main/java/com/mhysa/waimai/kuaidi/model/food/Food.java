package com.mhysa.waimai.kuaidi.model.food;

import java.util.List;

/**
 * 食物
 * Date: 2017/7/27
 *
 * @author xusheng
 */

public class Food {

    public String foods_id;

    public String foods_name_cn;

    public String foods_name_en;

    public int foods_quantity;

    // 0：未取件 1：已取件
    public String pickup_state;

    public List<Spec> spec_list;
}
