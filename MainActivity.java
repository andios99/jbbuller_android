package co.kr.skycall;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Browser;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    public WebView mWebView;
    private long backTime = 0;
    private ProgressBar mProgressBar;
    public String MyPakageName;
    static final String TAG = "GCM Demo";
    public String mGcmRegId;
    public String myGCMIDsaved;
    private static final String SENDER_ID = "356456304428"; //    AIzaSyBu1o5HHLB7VzE-Wz3z0RLygtwpQAZFjuA
    private static final String PROPERTY_REG_ID = "regId";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging mGcm;
    //public String URLHder = "http://jbbuller.kr/";
    public String URL_Header = "http://192.168.1.7/";
    public String goUrl = "sky_login.php";

    public String page_URLNow = "sky_login.php";
    public String version_info = "";
    private PendingIntent pendingIntent;
    long GPS_interval = 1000 * 60 * 2;
    public AlarmManager alrm = null;
    public PendingIntent pi = null;
    public Context MainContext;
    public GPSTracker gps;
    private int userNumberN;
    private String userID;
    private String userGPS;
    private String requestUploadJob;
    public String OnActivity;
    private String SERVER_Upload_URL = "";
    private String UPLOAD_Fname = "";
    private String mWebViewgetUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainContext = this.getApplicationContext();
        MyPakageName = MainContext.getPackageName();
        SharedPreferences mGcmREad = getSharedPreferences("SkyCallGcm", MODE_PRIVATE);
        myGCMIDsaved = mGcmREad.getString("mGcmRegId", "");

        this.mGcmRegId = getGCMPreferences(this).getString(PROPERTY_REG_ID, null);
        boolean ok = this.checkPlayServices();
        if (myGCMIDsaved.length() > 10) {
            mGcmRegId = myGCMIDsaved;
        }
        if (ok && this.mGcmRegId == null) {
            this.registerInBackground();
        }
        this.registerInBackground();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == 0
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
            Log.d("GPS checkSe", "askaskaskaskaskaskaskask");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        call_webView();
        iconNumRest();
        //addShortcut(1);
        //AlarmReceiver dc = new AlarmReceiver();
        //dc.init(MainActivity.this); ////////////////////////////////////////////////////////////////////////////////////////////////////

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("GCMMESSAGE"));

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        gps = new GPSTracker(this);

    }

    public void call_webView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setBackgroundColor(0x00000000);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.addJavascriptInterface(new Location_get(), "location_bridge");
        mWebView.addJavascriptInterface(new App(), "App");
        mWebView.addJavascriptInterface(new get_gps_set(), "GPS_set");
        mWebView.addJavascriptInterface(new WebPos_save(), "WebPos_save");

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setFocusable(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebChromeClient(new webViewChrome());
        //mWebView.setWebContentsDebuggingEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.clearCache(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(URL_Header + goUrl);
        //mWebView.loadDataWithBaseURL(URL_Header + goUrl);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String url = intent.getStringExtra("NotiLInk_send");
            //goUrl = URL_Header + url;
            Log.d("mMessageReceiver", "BroadcastReceiver************************" + goUrl + " / 현재페이지는 : " + page_URLNow);

            if (page_URLNow.equals("_show_order_detail.php")) {
                goUrl = URL_Header + "_show_order_detail.php";
                Log.d("mMessageReceiver", "현재 수주페이지 접속중");
                Log.d("mMessageReceiver", "이동할 주소 :" + url + "/ 현재주소:" + page_URLNow);
                mWebView.loadUrl(goUrl);
                mWebView.clearCache(true);
                mWebView.setWebViewClient(new MyWebViewClient());

            } else {

            }
        }
    };


    public void get_send_Gps() {
        Log.d("sendToAndroid", "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + mGcmRegId);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        version_info = versionCode + " / " + versionName;
        double latitude = 0;
        double longitude = 0;

        try {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } catch (Exception e) {
        }
        final String goStrurl = "javascript:location_infofrom_app('" + latitude + "','" + longitude + "','" + mGcmRegId + "','" + version_info + "')";
        Log.i("sendToAndroid", "goStrurl : " + goStrurl);
        userGPS = latitude + "','" + longitude ;

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), "goStrurl : " + goStrurl, Toast.LENGTH_SHORT);
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    private void saveGPS() {
        Log.i("saveGPS", "saveGPS start");
        SharedPreferences userDetails = getApplicationContext().getSharedPreferences("userInfo_apset", MODE_PRIVATE);
        String member_Num=userDetails.getString("USERNUMBER", "");
        String GPSGap=userDetails.getString("USERNUGPSGABMBER", "");
        double latitude = 0;
        double longitude = 0;
        try {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } catch (Exception e) {
        }
        Log.i("apsetINfos", "apsetINfos before : ");
        // String member_Num=get_Gps_Gab_memid(1);
        userNumberN =Integer.parseInt(member_Num);
        userGPS = latitude + "|" + longitude ;
        final String goStrurl = "javascript:get_addressStr('" + member_Num + "','" + latitude + "','" + longitude + "')";

        new MyAsyncTask_GPS().execute("Go");
    }
    public class MyAsyncTask_GPS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... passing) {
            Log.i("MyAsyncTask_GPS", "MyAsyncTask_GPS MyAsyncTask_GPSMyAsyncTask_GPSMyAsyncTask_GPSMyAsyncTask_GPS : ");
            //HttpClient httpclient = HttpClients.createDefault();
            HttpClient httpclient = new DefaultHttpClient();
            String go_UrlNew = URL_Header + "_app_location_refresh_save_re2.php";
            HttpPost httppost = new HttpPost(go_UrlNew);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_num", userNumberN + ""));
                nameValuePairs.add(new BasicNameValuePair("userGPS", userGPS + ""));
                nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                Log.i("saveGPS", "goStrurl : " + userNumberN + " / userGPS : " + userGPS + " URL :" + go_UrlNew );
            } catch (ClientProtocolException e) {} catch (IOException e) {
                Log.i("saveGPS", "errrrrrrrrrrrrrrrrror : " + userNumberN + " / userGPS : " + userGPS );
            }
            return null;
        }

        protected void onPostExecute(ArrayList<String> result) {


        }
        protected void onProgressUpdate(Integer... progress){
        }
    }

    private static final String INTENT_ACTION = "co.kr.skycall.AlarmReceiver";
    public void onBtnAlarm2(int gpsGab_Time, int memeBerNum) {
        if (gpsGab_Time > 0 && memeBerNum > 0){
            SharedPreferences userDetails = getSharedPreferences("userInfo_apset", MODE_PRIVATE);
            SharedPreferences.Editor editor = userDetails.edit();
            editor.putString("GPSGAB",""+gpsGab_Time);
            editor.putString("USERNUMBER",""+memeBerNum);
            //editor.apply();
            editor.commit();

            GPS_interval=gpsGab_Time*60000;
        }
        Log.e("onBtnAlarm2 : ", "GPS_interval = " + GPS_interval);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(),AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243, intent, 0);
        if(gpsGab_Time==0){
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "내위치 새로고침이 꺼졌습니다.",Toast.LENGTH_LONG).show();
        }else{
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + GPS_interval, GPS_interval, pendingIntent);
            Toast.makeText(this, "내위치를 " + GPS_interval / 60000  + " 분마다 갱신합니다.",Toast.LENGTH_LONG).show();
        }



    }

    final class get_gps_set { //
        get_gps_set() {}
        @JavascriptInterface
        public void get_gps_set(int gpsGab_Time, int memeBerNum) {
            //onBtnAlarm2(1, memeBerNum);
            onBtnAlarm2(gpsGab_Time, memeBerNum);
        }
    }

    final class Location_get { //
        Location_get() {}
        @JavascriptInterface
        public void sendToAndroid() {
            get_send_Gps();
        }
        @JavascriptInterface
        public void Timer_start() {
            onBtnAlarm2(0, 0);
        }
    }

    final class App { //location_bridge
        App() {}
        @JavascriptInterface
        public void requestUpload(final String jobtype, final String type2) {
            Log.d("requestUpload", "requestUpload(" + jobtype + ", " + type2 + ")");
            requestUploadJob = jobtype;
            userID = type2;
            imgSelAndreay();

        }
    }

    final class Appe {
        Appe() {}
        @JavascriptInterface
        public void xrequdestUpload2(String wjob, int Unum) {
            // requestUploadJob = wjob;
            // userNumberN = Unum;
            imgSelAndreay();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if(extras !=null) {
            iconNumRest();

            String b = extras.getString("moveUrl") + "";
            String c = extras.getString("IntentJob") + "";
            String goUrln = "";

            if(c.equals("GPS_RESAVE")){
                saveGPS();
            }else if (b.equals("Show_NotiActivity")) {
                SharedPreferences pref = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                goUrln = pref.getString("linkURL", "") + "";
                editor.putString("linkURL", "");
                editor.putString("NoticNum", "0");
                //editor.apply();
                editor.commit();

                if( goUrln.length() < 5 ) goUrln=URL_Header + "_my_message_list.php";
                Log.d("onNewIntent", "푸시 드레그주소이동:" + goUrln + " / B:" + b + " /C:" + c);
                mWebView.setWebViewClient(new MyWebViewClient());
                mWebView.loadUrl(goUrln);


            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if( url.startsWith("http://")) {
                //mWebView.loadUrl(url);
                return false;
            }else {
                boolean override = false;
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                if( url.startsWith("sms:")){
                    Intent i = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if( url.startsWith("tel:")){
                    Intent i = new Intent( Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if( url.startsWith("mailto:")){
                    Intent i = new Intent( Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                try {
                    startActivity(intent);
                    override = true;
                }
                catch (ActivityNotFoundException e) {
                    return override;
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
        @Override
        public void onPageFinished(WebView view, final String url) {
        }
    }
    private void iconNumRest(){
        Context  context = this.getApplicationContext();
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", 0);
        intent.putExtra("badge_count_package_name",  context.getPackageName());
        intent.putExtra("badge_count_class_name", "co.kr.skycall.SplashScreen");
        context.sendBroadcast(intent);
        //co.kr.dodream.SplashScreen  ctxpackgage=co.kr.dodream, launcherclassname=co.kr.dodream.SplashScreen //co.kr.dodream.MainActivity  co.kr.dodream.MainActivity
        SharedPreferences pref = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("NoticNum", String.valueOf(0));
        editor.apply();
    }

    class webViewChrome extends WebChromeClient {
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            WebView newWebView = new WebView(MainActivity.this);
            view.addView(newWebView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            });
            return true;
        }
        public void onProgressChanged(WebView view, int newProgress) {
            if(newProgress < 100) {
                mProgressBar.setProgress(newProgress*100);
            } else {
                mProgressBar.setVisibility(View.INVISIBLE);
                //mProgressBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
        }
    }

    private void call_to(String Pnum){
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + Uri.encode(Pnum.trim())));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {Log.i(TAG, "This device is not supported.");finish();}return false;
        }return true;
    }
    private void registerInBackground() {/////////////////////////88888888888888888888888888888888888888888888888/////GCM 아이디 등록
        final Context context = this.getApplicationContext();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                if (mGcm == null) {mGcm = GoogleCloudMessaging.getInstance(context);}
                String msg = "";
                try {

                    mGcmRegId = mGcm.register(SENDER_ID);
                    Log.e("mGcmRegId : " ,  "mGcmRegId======= " + mGcmRegId );
                } catch (IOException ex) {
                    msg = "registerInBackground Error :" + ex.getMessage();
                    Log.e("GoogleCloudMessaging : " ,  "GEEEEEEEEEEErrrrrrr " + msg );
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {if(!mGcmRegId.equals("")&&mGcmRegId!=null)sendRegistrationIdToBackend();}
        }.execute(null, null, null);
    }
    private void sendRegistrationIdToBackend() {}
    private SharedPreferences getGCMPreferences(Context context) {return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);}
    private void addShortcut(int shotType) {
        SharedPreferences pref = getSharedPreferences("shortCutInfo", MODE_PRIVATE);
        String install_CH = pref.getString("shortCut", "");//
        if(!install_CH.equals("OK")){
            Intent shortcutIntent = new Intent();
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent.setClassName(this.getApplicationContext(), getClass().getName());
            //shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Parcelable iconResource;
            iconResource = Intent.ShortcutIconResource.fromContext( this,  R.mipmap.ic_launcher);
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "장비불러");
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,iconResource);
            intent.putExtra("duplicate", false);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            sendBroadcast(intent);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("shortCut","OK");
            editor.apply();
        }
    }
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if((keyCode==KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()){

            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backTime;
            Log.e("onKeyDown : " ,  "현재페이즌 :  " + page_URLNow + " / " + intervalTime + " /뒤로순서:" + mWebView.canGoBack());

            if(intervalTime < 500 ) {

                //Toast.makeText(getApplicationContext(), "뒤로 버튼을 빨리 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                backButtonHandler();
                return false;
            }

            if(0 <= intervalTime && 500 >= intervalTime){
                backTime = tempTime;
                if (page_URLNow.equals("_my_message_list.php") || page_URLNow.equals("sky_login.php")) {
                    backButtonHandler();
                    return false;
                }
                mWebView.goBack();
                return false;
                // super.onBackPressed();
            }else{
                backTime = tempTime;
                mWebView.goBack();
                if(intervalTime < 500 ) {

                    //Toast.makeText(getApplicationContext(), "뒤로 버튼을 빨리 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    backButtonHandler();
                    return false;
                }
            }
            return false;
        }else{
            backButtonHandler();
            return false;
        }
        // return super.onKeyDown(keyCode, event);
    }

    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("앱 종료하기");
        alertDialog.setMessage("장비불러를 종료할까요?");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void alertt(String text){Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();}

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    final class WebPos_save { //
        WebPos_save() {}
        @JavascriptInterface
        public void web_url_pos_get(String pageUrlvalue) {
            Log.e("web_url_pos_get : " ,  "page= " + pageUrlvalue );
            page_URLNow = pageUrlvalue;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void imgSelAndreay() {
        //final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        final CharSequence[] options = { "카메라촬영", "겔러리에서선택","취소" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("카메라촬영")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1381);
                }
                else if (options[item].equals("겔러리에서선택")){
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1382);
                }else if (options[item].equals("취소")) {dialog.dismiss();}
            }
        });
        builder.show();
    } ///겔러리 또는 카메라 중 선택
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ) {
            if(requestCode == 1381 || requestCode == 1382){
                activty_result_job(requestCode, resultCode, data);return;
            }
        }
    }


    private void activty_result_job(int requestCode, int resultCode, Intent data) {
        String togoFn = "";
        if (requestCode == 1381) {
            Uri uri =null;
            String[] IMAGE_PROJECTION = {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns._ID,
            };
            try {
                Cursor cursorImages = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null,null);
                if (cursorImages != null && cursorImages.moveToLast()) {
                    uri = Uri.parse(cursorImages.getString(0)); //경로
                    cursorImages.close(); // 커서 사용이 끝나면 꼭 닫아준다.
                }
            } catch(Exception e) {e.printStackTrace();}
            togoFn = "" + uri;

        } else if (requestCode == 1382) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            togoFn = picturePath;
        }
        if (!togoFn.equals("")) {
            SERVER_Upload_URL = URL_Header + "uploade.php";
            UPLOAD_Fname = togoFn;
            //uploadFile(togoFn);
            mWebViewgetUrl = mWebView.getUrl();
            //new UpdateTask().execute();
            Activity mActivity = MainActivity.this;
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new UpdateTask().execute();
                }
            });

        }
    }

    private class UpdateTask extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            String selectedFilePath= UPLOAD_Fname;

            Log.e("uploadFile : " ,  "fn = " + selectedFilePath );
            Log.e("webUrl : " ,  "webUrl = " + mWebViewgetUrl );

            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            int bytesRead,bytesAvailable,bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);


            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length-1];

            Log.e("selectedFile : " ,  "Exists = " + selectedFile.isFile() );


            if (!selectedFile.isFile()){
                //Toast.makeText(getApplicationContext(), selectedFilePath + "이 없습니다.", Toast.LENGTH_SHORT).show();
                return null;
            }else{
                try{
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(SERVER_Upload_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", selectedFilePath);




                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    Log.e("UPstep1 : ", "dataOutputStream" + SERVER_Upload_URL);

                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);


                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"jobtype\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(requestUploadJob); // mobile_no is String variable
                    dataOutputStream.writeBytes(lineEnd);

                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"type2\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(userID); // mobile_no is String variable
                    dataOutputStream.writeBytes(lineEnd);

                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(selectedFilePath); // mobile_no is String variable
                    dataOutputStream.writeBytes(lineEnd);



                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + selectedFilePath + "\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);


                    Log.e("UPstep2 : ", "dataOutputStream");
                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);

                    Log.e("UPstep3 : ", "bytesRead");

                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                    while (bytesRead > 0){
                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer,0,bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable,maxBufferSize);
                        bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    }

                    Log.e("UPstep4 : ", "bytesRead");

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    Log.e("UPstep5 : ", "dataOutputStream");
                    serverResponseCode = connection.getResponseCode();
                    Log.e("UPstep6 : ", "serverResponseCode");

                    String serverResponseMessage = connection.getResponseMessage();
                    Log.e("UPstep7 : ", "serverResponseMessage :" + serverResponseMessage + " / serverResponseCode :" + serverResponseCode );
                    //Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if(serverResponseCode == 200){
                        Activity mActivity = MainActivity.this;
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //alertt("전송완료");
                        // Toast.makeText(getApplicationContext(), "전송완료.", Toast.LENGTH_SHORT).show();
                    }

                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();



                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("error1", "FileNotFoundException");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e("error2", "MalformedURLException");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("error3", "IOException");
                }
                return null;
            }

        }
    }
    public int uploadFile(String selectedFilePath) {

        return 1;

        //return 1;
    } // End else block


    private String resize(String path){
        Bitmap b= BitmapFactory.decodeFile(path);
        File file_test = new File (path);

        try{
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            int rotation = 0;
            if      (orientation == 6)      rotation = 90;
            else if (orientation == 3)      rotation = 180;
            else if (orientation == 8)      rotation = 270;
            if (rotation != 0){
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotated = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                // Pretend none of this ever happened!
                b.recycle();
                b = rotated;
                rotated = null;
            }
        }catch (Exception e){}
        int orgw = b.getWidth();int orgh = b.getHeight();
        float ww = (float)1500.00 ; float hh = (float) 1500.00;
        int goww ; int gohh;
        float  r = ww /  (float) orgw;
        if( orgw > ww || orgh > hh) {
            if (orgw > orgh) {goww = (int) ww; gohh = (int) ((float) orgh * r);}
            else {r = hh /  (float) orgh; gohh = (int) hh; goww = (int) ((float) orgw * r);}}
        else{goww = orgw; gohh = orgh; }
        b = Bitmap.createScaledBitmap(b, goww, gohh, true);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/temp/");
        if (!myDir.exists())  myDir.mkdirs();
        String fnamealltogo = "ImageuploadTempxto_one.jpg";
        String ret_fn = root + "/temp/" + fnamealltogo;
        File file = new File (myDir, fnamealltogo);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return ret_fn;
        } catch (Exception e) {e.printStackTrace();}
        return null;
    }

}
