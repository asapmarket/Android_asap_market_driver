package com.mhysa.waimai.kuaidi;

import com.joey.devilfish.application.BaseApplication;
import com.joey.devilfish.fusion.SharedPreferenceConstant;

/**
 * 文件描述
 * Date: 2017/7/20
 *
 * @author xusheng
 */

public class KuaidiApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferenceConstant.PREFERENCE_NAME = "Mhysa_Kuaidi";
    }
}
