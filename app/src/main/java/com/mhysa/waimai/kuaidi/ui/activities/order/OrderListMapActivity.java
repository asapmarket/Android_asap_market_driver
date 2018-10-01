package com.mhysa.waimai.kuaidi.ui.activities.order;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joey.devilfish.fusion.FusionCode;
import com.joey.devilfish.ui.activity.base.BaseActivity;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.http.OkHttpClientManager;
import com.mhysa.waimai.kuaidi.http.RemoteReturnData;
import com.mhysa.waimai.kuaidi.http.WtNetWorkListener;
import com.mhysa.waimai.kuaidi.model.order.Order;
import com.mhysa.waimai.kuaidi.model.order.OrderListLocation;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 地图接单
 * Date: 2017/8/17
 *
 * @author xusheng
 */

public class OrderListMapActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    @Bind(R.id.tv_title)
    TextView mTitleTv;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;

    private LocationRequest mLocationRequest;

    private boolean mLocationUpdateState;

    private boolean mIsFirst = true;

    private boolean mIsOffWork = false;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_order_list_map;
    }

    @Override
    protected void initHeaderView(Bundle savedInstanceState) {
        super.initHeaderView(savedInstanceState);
        mTitleTv.setText(R.string.map_order);
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        super.initContentView(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (null != mGoogleApiClient
                && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);

        getOrderList();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (null != mLastLocation) {
            placeMarkerOnMap(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (null != marker
                && !StringUtils.getInstance().isNullOrEmpty(marker.getTitle())) {
            OrderDetailActivity.invoke(OrderListMapActivity.this, marker.getTitle(), false, mIsOffWork);
        }
        return true;
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude());
                //add pin at user's location
                placeMarkerOnMap(currentLocation);
                if (mIsFirst) {
                    mIsFirst = false;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                }
            }
        }
    }

    private void placeOrderOnMap(Order order) {
        if (null == order
                || StringUtils.getInstance().isNullOrEmpty(order.order_id)
                || StringUtils.getInstance().isNullOrEmpty(order.lat)
                || StringUtils.getInstance().isNullOrEmpty(order.lng)) {
            return;
        }

        LatLng location = new LatLng(Double.valueOf(order.lat), Double.valueOf(order.lng));
        MarkerOptions markerOptions = new MarkerOptions().position(location);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource
                (getResources(), R.mipmap.ic_order_marker))).title(order.order_id);
        mMap.addMarker(markerOptions);
    }

    protected void placeMarkerOnMap(LatLng location) {
//        MarkerOptions markerOptions = new MarkerOptions().position(location);
//
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource
//                (getResources(), R.mipmap.ic_map_marker)));
//        mMap.addMarker(markerOptions);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mLocationUpdateState = true;
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(OrderListMapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationUpdateState = true;
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    @OnClick({R.id.layout_back})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.layout_back: {
                OrderListMapActivity.this.finish();
                break;
            }

            default: {
                break;
            }
        }
    }

    private void getOrderList() {
        showNetDialog("");
        OkHttpClientManager.getInstance().orderListMap(Utils.getUserId(this),
                Utils.getToken(this),
                new WtNetWorkListener<OrderListLocation>() {
                    @Override
                    public void onSucess(RemoteReturnData<OrderListLocation> data) {
                        if (null != data
                                && null != data.data
                                && null != data.data.rows
                                && data.data.rows.size() > 0) {

                            if (!StringUtils.getInstance().isNullOrEmpty(data.data.onwork)
                                    && data.data.onwork.equals(FusionCode.WorkStatus.WORK_STATUS_OFFWORK)) {
                                mIsOffWork = true;
                            }

                            List<Order> orders = data.data.rows;
                            final int size = orders.size();
                            for (int i = 0; i < size; i++) {
                                placeOrderOnMap(orders.get(i));
                            }
                        }
                    }

                    @Override
                    public void onError(String status, String msg_cn, String msg_en) {
                        responseError(msg_cn, msg_en);
                    }

                    @Override
                    public void onFinished() {
                        closeNetDialog();
                    }
                });

    }

    public static void invoke(Context context) {
        context.startActivity(new Intent(context, OrderListMapActivity.class));
    }
}
