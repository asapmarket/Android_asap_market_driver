package com.mhysa.waimai.kuaidi.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.google.gson.JsonElement;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 定位服务
 * Date: 2017/8/1
 *
 * @author xusheng
 */

public class LocationService extends IntentService
        implements LocationListener {

    private static final String SERVICE_NAME = "LocationService";

    private static final long MIN_TIME = 10001;

    private static final float MIN_DISTANCE = 5f;

    private LocationManager mLocationManager;

    public LocationService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /*
             * 进行定位
			 * provider:用于定位的locationProvider字符串
			 * minTime:时间更新间隔，单位：ms
			 * minDistance:位置刷新距离，单位：m
			 * listener:用于定位更新的监听者locationListener
			 */
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME, MIN_DISTANCE, LocationService.this);
                    } catch (SecurityException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            //无法定位：1、提示用户打开定位服务；2、跳转到设置界面
            Intent i = new Intent();
            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //得到纬度
        double latitude = location.getLatitude();
        //得到经度
        double longitude = location.getLongitude();

        if (!StringUtils.getInstance().isNullOrEmpty(Utils.getUserId(this))
                && !StringUtils.getInstance().isNullOrEmpty(Utils.getToken(this))) {
            OkHttpClientManager.getInstance().uploadLocation(Utils.getUserId(this),
                    Utils.getToken(this), "" + longitude, "" + latitude,
                    new WtNetWorkListener<JsonElement>() {
                        @Override
                        public void onSucess(RemoteReturnData<JsonElement> data) {

                        }

                        @Override
                        public void onError(String status, String msg_cn, String msg_en) {

                        }

                        @Override
                        public void onFinished() {

                        }
                    });
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }
}
