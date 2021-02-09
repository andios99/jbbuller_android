package co.kr.skycall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class GPSTracker implements LocationListener {
    private final Context mContext;
    private  boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude
    private  float isGPS;
    LocationResult locationResult;
    private  int GpsCallonce = 0;
    public int netWorkCallonce = 0;
    private  String nowAddress = "";
    public long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    public long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private  LocationManager locationManager = null;
    private  GPSListener gpsListener;
    private  int GPSCALLTIME = 0;

    public  GPSTracker(Context context) {
        this.mContext = context;
        locationManager = null;
        if (mContext instanceof Activity) {
            //Log.e("context", "instanceof ActivityActivityActivityActivityActivity");
        } else if (mContext instanceof Service){
            //Log.e("context", "instanceof ServiceServiceServiceServiceService");
        }
        Log.e("GPSTracker", "위치 클래스 시작 : ");
        gpsListener = new GPSListener();
        getLocation();
    }
    public class GPSListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            location = loc;
            isGPS = location.getAccuracy();
            Log.e("GPS 변경됨", "location isNull " + location + " / " + GpsCallonce + "XXXXXXXXXXXXXXXXX");
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((netInfo != null) && netInfo.isConnected());
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }

    public Criteria getCriteria(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소리량
        criteria.setAltitudeRequired(false); // 고도
        criteria.setBearingRequired(false); // ..
        criteria.setSpeedRequired(false); // 속도
        criteria.setCostAllowed(true); // 금전적 비용
        return criteria;
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
    public Location getLocation() {
        //Log.e("GPS getLocation", "getLocation startinggggggggggggggggggggggggggggggg");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        if(locationManager==null) locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);//locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try{isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}
        try{isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}

        try {
            SharedPreferences userDetails = mContext.getSharedPreferences("userInfo_apset", mContext.MODE_PRIVATE);
            String GPSGap=userDetails.getString("GPSGAB", "1");
            String GPSTurnOn=userDetails.getString("GPSTurnOn", "0");
            if( GPSGap.equals("")) GPSGap="5";
            if( GPSTurnOn.equals("")) GPSTurnOn="5";
            int GPSGAPVAL= NumberFormat.getInstance().parse(GPSGap).intValue();
            int Gap_ValOrg = GPSGAPVAL;
            GPSGAPVAL = (GPSGAPVAL * 60000) ;
            if(GPSGAPVAL < 60000) GPSGAPVAL = 60000;
            if(GPSGAPVAL > 180000) GPSGAPVAL = 180000;
            if( Gap_ValOrg == 1  ) GPSGAPVAL = 30000;
            if (isGPSEnabled == false ) {
                // no network provider is enabled
                showSettingsAlert("");
                return null;
            }else{
                if( GPSCALLTIME == 0 ) {
                    GPSGAPVAL = 1000 * 60;
                    int a1 = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    int a2 = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    //Log.e("퍼미션", "ACCESS_COARSE_LOCATION" + a1 + "ACCESS_COARSE_LOCATION= " + a2);
                    final int INTERVAL_TIME_SECONDS = 60 * 1000; // 60 seconds
                    locationManager.removeUpdates(this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSGAPVAL, 100, gpsListener); //1000, 1
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60*10, 1000, gpsListener);
                    //Log.e("로케이션메니저", "로케이션메니저 실행함 초: " + GPSGAPVAL + " / 분:" + Gap_ValOrg);
                    if(location != null){
                        Handler h = new Handler(mContext.getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                if(location != null){
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                                //Toast.makeText(mContext, "locationManager 시작함 ", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    GPSCALLTIME++;
                    final int GPSGAPVAL_s = GPSGAPVAL;
                    final int GPSCALLTIMEF = GPSCALLTIME;
                    SharedPreferences.Editor editor = userDetails.edit();
                    editor.putString("GPSTurnOn", GPSCALLTIME + "");
                    editor.commit();

                /*h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "GPS 켜기 실행했음:/" + GPSCALLTIMEF + " 번째", Toast.LENGTH_LONG).show();
                    }
                });
                 */
                }
            }
            this.canGetLocation = true;
            if (isGPSEnabled) location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }else{
                if (isNetworkEnabled ) {
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                    String OKGPS = "OK";
                    if (isGPSEnabled == false ) OKGPS = "NO GPS";
                    //Log.e("GPS isMobileConnected", "GPS:" + OKGPS + " Network :" + isNetworkEnabled + "location= " + location);
                }else {
                    //Log.e("GPS NO", "NNNNNNNNNNNNNNNNNNNNNNN= " + isNetworkEnabled + " / " + isMobileConnected(mContext)+ " / " + location);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            //Log.e("GPS Exception", "Exception:" + exceptionAsString );
        }
        return location;
    }
    public void stopUsingGPSs() {
        //Log.e("Gps 정지","stopUsingGPSstopUsingGPS");
        GPSCALLTIME = 0;
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }
        locationManager.removeUpdates(this);
    }

    public float getGpsType() {
        return isGPS;
    }
    public double getLatitude() {
        //Log.e("getLatitude", "getLatitude:" + location );
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }
    public double getLongitude() {
        //Log.e("getLongitude", "getLongitude:" + location );
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    public void showSettingsAlert(String mg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS 가꺼져있습니다.");
        alertDialog.setMessage("GPS 가 꺼져있습니다. 설정페이지로 이동하시겠습니까? " + mg);
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {dialog.cancel();
                    }
                });
        alertDialog.show();
    }
    @Override
    public void onLocationChanged(Location location) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() == null) return "";
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String currentAddress = "";
            if( addresses.isEmpty() ) {
            }else{
                Address obj = addresses.get(0);
                currentAddress = obj.getAddressLine(0);
                nowAddress = currentAddress;
            }
            currentAddress = currentAddress.replace("대한민국 ","");
            currentAddress = currentAddress.replace("대한민국","");
            //Log.e("IGA", "lat:" + lat + " / lng : " + lng + "/Crt = " + currentAddress);
            return currentAddress;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            return "";
        }
    }
}