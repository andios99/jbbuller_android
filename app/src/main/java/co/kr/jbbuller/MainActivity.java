package co.kr.jbbuller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Browser;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.BuildConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kakao.kakaonavi.Destination;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.Location;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;
import com.skp.Tmap.TMapTapi;
import com.skp.Tmap.TMapView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.graphics.Color.parseColor;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static com.kakao.util.helper.Utility.getPackageInfo;
import static java.lang.Integer.parseInt;

import com.andremion.louvre.Louvre;

@SuppressWarnings("ALL")
// ADB C:\Users\junyBuller\AppData\Local\Android\Sdk\platform-tools\
/*
    adb devices
    adb shell setprop service.adb.tcp.port 4444
    adb tcpip 4444
    adb connect 192.168.1.10:4444
 */
public class MainActivity extends Activity {
    private static final int SELECT_PICTURE = 1;
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
    public String URL_Header = "http://jbbuller.kr/";
    public String goUrl = "_sky_m_00_login.php";
    public String page_URLNow = "_sky_m_00_login.php";
    public String version_info = "";
    private PendingIntent pendingIntent;
    long GPS_interval = 1000 * 60 * 2;
    public Context MainContext;
    public GPSTracker gps;
    private int userNumberN;
    private String userID;
    private String userGPS;
    private String requestUploadJob;
    public int diRect_userNUM = 0;
    private String SERVER_Upload_URL = "";
    private String UPLOAD_Fname = "";
    private String LastDeleteFn;
    private String userAddress = "";
    public float isGPS;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<File> fileList_last = new ArrayList<File>();
    private String record_path_last = "";
    private int recod_getLast = 0;
    public boolean isAvtivity_On = false;
    private Uri mImageCaptureUri;
    private String camera_job3;
    private String camera_job4;
    private String camera_job5;
    private String camera_job6;
    private String camera_job7;
    public int COMPARETYPE_NAME = 0;
    public int COMPARETYPE_DATE = 1;
    private GoogleApiClient client;
    private int phone_isBusy = 0;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private DbOpenHelper mDbOpenHelper = null;
    public int badCompanycheckonce = 0;
    private int pannel_clearOnce = 0;
    private String AppVersionCode;
    private String mWebViewgetUrl;
    private int isRestarting = 0;
    private SpeechRecognizer sr;
    //private static final String TAG = "MyStt3Activity";
    private String EHead = "불러스피치";
    private Intent vIntent;
    private final int REQ_CODE_SPEECH_INPUT = 9514;
    private Intent recognizerIntent = null;
    private SpeechRecognizer speech = null;
    private TextToSpeech Main_Tts;
    private String speech_Job;
    private String speech_howinput;
    private int speech_on = 0;
    private int speech_setting = 1;
    public String badC_reload = "";
    private WebView mSuperSafeWebView;
    private boolean mSafeBrowsingIsInitialized;
    private int DbOnceInited = 0;
    public int messageMaxno = 0;
    public int orderListMaxno = 0;
    public int DB_MBNO = 0;
    ArrayList<HashMap<String, String>> contactList;
    public HttpHandler HTTPH = null;
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int DIRECT_SAVE = 5;
    private AlertDialog.Builder builder;
    String mCurrentPhotoPath;
    public Uri camera_path;
    public int MainScreenWakeLock = 0;
    private static final int LOUVRE_REQUEST_CODE = 65;
    public static final int REQUEST_TAKE_GALLERY_VIDEO = 68;
    private String isCamera_chat = "";
    private boolean isCloseTabOn = false;

    public static final String ACTION_SHOW_TEXT = "GCMMESSAGE_NOTICECHECK";
    public Share_utils utilApp = new Share_utils(this);
    public UploadTo_server UploadUtil = new UploadTo_server(this);
    public multi_upload_util MultiUp = new multi_upload_util(this);
    boolean isGPSSet = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    LocationManager locationManager;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    public LocationManager mLM;
    private long lastTime = 0;
    public String uploadMediaType = "";
    public String mb_no;
    public String room_num;
    public String upload_folder;
    public String jobAct;
    public String Sign_orderNum = "0";
    public String Sign_workDetail = "";
    public String Sign_doc_price = "0";
    public String last_Update_pushNo = "2019-01-01 00:00:00";
    private int callTimeCnt = 0;
    public int Upload_File_sizeSet = 1800000;
    public static final String PREFERENCE_NAME = "noti_Setting";
    public int mymessage_getOnce = 0;
    public int GpsNotice = 0;
    private int SMSTry = 0;
    private boolean isBadCoInstall = false;
    String authority = "kr.jbbuller.voicerecorder.READ_DATABASE";
    public String AppSideOn = "OFF";
    private String voice_record_file = "";
    private MediaPlayer JbPlayer = null;
    private Handler hdlr ;
    private int isJBSoundPlaying = 0;


    @Override@RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSuperSafeWebView = new WebView(this);
        mSafeBrowsingIsInitialized = false;
        setContentView(R.layout.activity_main);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        isAvtivity_On = true;
        //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        TTsInit();
        MainContext = this.getApplicationContext();
        MyPakageName = MainContext.getPackageName();
        myGCMIDsaved = "";
        HTTPH = new HttpHandler(MainContext, this);
        HTTPH.get_and_makeMessageList(-1);
        new MyTask(this).execute();
        boolean ok = this.checkPlayServices();
        Log.e("토큰값 변경확인", "Token :" + mGcmRegId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            try {
                Class.forName("android.os.AsyncTask");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        permition_listMake();
        call_webView();
        iconNumRest();
        //addShortcut(1);
        gps = new GPSTracker(this);
        gps.stopUsingGPSs();
        Integer k = Build.VERSION.SDK_INT;
        Log.d("Build.VERSION", "Build.VERSION = " + k);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        findViewById(R.id.textInfos).setVisibility(View.GONE);
        SharedPreferences pref = getSharedPreferences("noti_Setting", Context.MODE_PRIVATE);
        String NOTIOFF = pref.getString("NOTIOFF", "") + "";
        String MACHINGOFF = pref.getString("MACHINGOFF", "") + "";
        mWebView = (WebView) findViewById(R.id.webview);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPSSet = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        mWebView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mWebView.getWindowVisibleDisplayFrame(r);
                int screenHeight = mWebView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                String md = android.os.Build.MODEL;
                int BuildVer = Build.VERSION.SDK_INT;
                final String goStrurl = "javascript:AppKeybord_heightStatus('" + screenHeight + "','" + keypadHeight + "','" + md + "','" + BuildVer + "','" + 1 + "')";
                Log.e("키보드", "ST:" + goStrurl);

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
            }
        });
        Share_utils.get_backButton_ExceptList();
        //LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ACTION_SHOW_TEXT));
        //IntentFilter iff= new IntentFilter(MainActivity.INTENT_ACTION);
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, iff);
        SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefx.edit();
        editor.putString("linkURL", "");
        editor.putString("NoticNum", "0");
        editor.apply();
        isAvtivity_On = true;
        addShortcut(0);
        get_Version();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Intent intent = new Intent();
//            String packageName = getPackageName();
//
//            배터리 최적화 무시 권한 요청 여부 해지
//            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                startActivity(intent);
//            }
//
//        }

        //String hdds = getKeyHash(MainContext);
        getAppKeyHash();
        //BadCompny_RecentCall_listGetfromDB();
        check_badCo_AppInstall();
        utilApp.chFile();
    }


    public void check_badCo_AppInstall(){
        try {
            Cursor c = getContentResolver().query(Uri.parse("content://"+authority+"/callLogTable_list"), null, null, null, null);
            if(c == null) {
                isBadCoInstall = false;
                Log.e("최근통화목록받기", "설치 안됨");
            }else {
                isBadCoInstall = true;
                Log.e("최근통화목록받기", "설치되어 있음");
            }
        } catch(Exception e) {
            isBadCoInstall = false;
            Log.e("최근통화목록받기", "설치 안됨22222");
            //BadCompny_RecentCall_listGetfromDB();
        }
    }
    public String BadCompny_RecentCall_listGetfromDB(){
        Log.e("최근통화목록받기", "GETRECENT_PHONE_LIST");
        Cursor c = getContentResolver().query(Uri.parse("content://"+authority+"/callLogTable_list"), null, null, null, null);
        if(c == null) return "";
        Log.e("test","aaabbb "+c.getCount());
        isBadCoInstall = true;
        Log.e("최근통화목록받기", "설치됨 체크 3333333333333  ");
        //StringBuilder aa = new StringBuilder();
        String retStr = "";
        while(c.moveToNext()) {
            String str = c.getString(0) + "↕" + c.getString(1)+"↕"+c.getString(2)+
                    "↕"+c.getString(3)+"↕"+c.getString(4);
            System.out.println(str);
            retStr += str + "|";
            //aa.append(str+"|");
        }
        c.close();
        return retStr;
    }
    private Boolean checkNetOK(){
        boolean connected = false;
        ConnectivityManager connectivityManager;
        try {
            connectivityManager = (ConnectivityManager) MainContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            return connected;
        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;

    }
    public void call_webView() {
        SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        String app_getURL = prefx.getString("linkURL", "") + "";
        String testStr = "__" + app_getURL;
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setDomStorageEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= 21) flushCookies();
        else CookieSyncManager.getInstance().sync();

        final WebSettings webSetting = mWebView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSetting.setSafeBrowsingEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //////////////////////////////////////////////////////////////////////////////////아래는 OK
        clearApplicationCache(null);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSetting.setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
            webSetting.setAllowUniversalAccessFromFileURLs(true);
        }
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setDisplayZoomControls(false);
        //webSetting.setUseWideViewPort(true);

        //mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSetting.setEnableSmoothTransition(true);

        //mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            //mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setFocusable(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.clearHistory();
        mWebView.clearView();
        mWebView.setBackgroundColor(0x00000000);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new wmLoginMDVer(), "wmLoginMDVer");
        mWebView.addJavascriptInterface(new AppV13(), "AppV13");
        mWebView.addJavascriptInterface(new AppVersin_Checker(), "AppVersin_Checker");
        mWebView.addJavascriptInterface(new AppV65(), "AppV65");
        mWebView.addJavascriptInterface(new AppVwhite(), "AppVwhite");
        mWebView.addJavascriptInterface(new UnInsApp(), "UnInsApp");
        mWebView.addJavascriptInterface(new UploadUtil(), "UploadUtil");

        mWebView.addJavascriptInterface(new Location_get(), "location_bridge");
        mWebView.addJavascriptInterface(new App(), "App");
        mWebView.addJavascriptInterface(new Noticefrom_Web(), "Noticefrom_Web");
        mWebView.addJavascriptInterface(new App_VNew(), "App_VNew");
        mWebView.addJavascriptInterface(new App_VchFF(), "App_VchFF");
        mWebView.addJavascriptInterface(new get_gps_set(), "GPS_set");
        mWebView.addJavascriptInterface(new WebPos_save(), "WebPos_save");
        mWebView.addJavascriptInterface(new GPS_Reload_functon(), "GPS_Reload");
        mWebView.addJavascriptInterface(new App_record_voiceSet(), "App_record_voiceSet"); /// v 2.26부터

        mWebView.setWebChromeClient(new webViewChrome());
        mWebView.setWebViewClient(new MyWebViewClient());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        String goUrln = "";
        goUrln = URL_Header + "_sky_m_00_login.php";
        if (testStr.indexOf(URL_Header) > 0 && app_getURL.length() > URL_Header.length()) {
            //goUrln = app_getURL;
        }
        mWebView.loadUrl(goUrln);
    }
    public void Screen_KeepOn(String act){
        if( act.equals("OFF")){
            if( MainScreenWakeLock == 0) return;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            MainScreenWakeLock = 0;
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainScreenWakeLock = 1;
    }
    private static class MyTask extends AsyncTask<Void, Void, String> {
        private GoogleCloudMessaging mGcm;
        private WeakReference<MainActivity> activityReference;
        private String mGcmRegId_local = "";
        private Context mainContext = null;
        // only retain a weak reference to the activity
        MyTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
            mainContext = context;
        }
        @Override
        protected String doInBackground(Void... params) {
            MainActivity activity = activityReference.get();
            Context mmc = activity.getApplicationContext();
            if (mGcm == null) {
                mGcm = GoogleCloudMessaging.getInstance(mmc);
            }
            String msg = "";
            try {
                mGcmRegId_local = mGcm.register(SENDER_ID);

                //activityReference.mGcmRegId = "";
            } catch (IOException ex) {
                msg = "registerInBackground Error :" + ex.getMessage();
            }
            Log.e("토큰값받기", "Token:" + mGcmRegId_local );
            return mGcmRegId_local;
        }

        @Override
        protected void onPostExecute(String result) {
            MainActivity activity = activityReference.get();
            activity.mGcmRegId = mGcmRegId_local;
            Log.e("토큰값전달", "Token:" + activity.mGcmRegId );
        }
    }
    public void get_send_Gps() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        version_info = versionCode + " / " + versionName;
        double latitude = 0;
        double longitude = 0;
        String userAddress = "";

        try {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            userAddress = gps.getAddress(latitude, longitude);
            isGPS = gps.getGpsType();
        } catch (Exception e) {
        }
        Context context = getApplicationContext(); // or activity.getApplicationContext()
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        String myVersionName = "not available"; // initialize String

        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String manufacturer = Build.MANUFACTURER;
        String model = "";

        model = Build.MANUFACTURER
            + " | " + Build.MODEL + " | " + Build.VERSION.RELEASE
            + " | " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

        String adrs = "'" + latitude + "','" + longitude + "','" + mGcmRegId + "','" + myVersionName + "','" + userAddress + "',";
        adrs += "'" + diRect_userNUM + "','" + isGPS + "', '" + model + "'";
        final String goStrurl = "javascript:location_infofrom_app_withModel(" + adrs + ")";
        userGPS = latitude + "','" + longitude; //location_infofrom_app_withModel
        if( GpsNotice == 1 ) alertt("GPS값을 갱신했습니다.");

        GpsNotice = 0;
        Date currentTime = Calendar.getInstance().getTime();
        int okSend = 0;
        long currentTimeLong = System.currentTimeMillis();
        long difference = currentTimeLong - lastTime;
        Log.e("TimeGabShow", "Gab:" + difference );
        if( difference > (1000 * 60) ){
            Log.e("goStrurl", "URL:" + goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), "goStrurl : " + goStrurl, Toast.LENGTH_SHORT);
                    mWebView.loadUrl(goStrurl);
                }
            });
        }

        lastTime =currentTimeLong;
        SharedPreferences userDetails = getSharedPreferences("userInfo_apset", MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.putString("userGPS1_last", "-1");
        editor.putString("userGPS2_last", "-1");
        editor.commit();
    }
    public void getFCM_NewID(){
        FirebaseApp.initializeApp(MainContext);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();
                if( deviceToken == null || deviceToken.isEmpty() ) deviceToken = "";
                if(deviceToken.length() > 10 ) myGCMIDsaved = deviceToken;
            }
        });
    }
    public void SpeechStart_do(String howHandle, String jobtype, String ttsWord) {
        speech_howinput = howHandle;
        speech_Job = jobtype;
        if (Main_Tts != null) Main_Tts.stop();
        if (speech_on == 1 && speech != null) {
            Speech_Stop();
            speech_on = 0;
        }
        TTS_Speak(ttsWord);
    }
    public void TTS_Speak(String word) {
        //Main_Tts.speak(word, TextToSpeech.QUEUE_FLUSH,null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        Main_Tts.speak(word, TextToSpeech.QUEUE_ADD, map);
    }
    public void TTsInit() {
        if (speech_Job == null) speech_Job = "";
        Main_Tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //alertt("음성 인식완료");
                Main_Tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                    @Override
                    public void onUtteranceCompleted(final String utteranceId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (speech_Job.equals("onlySpeech") ) {//|| speech_setting == 3
                                    return;
                                }
                                alertt("듣기 중 입니다.");
                                Speech_listen_start();
                            }
                        });
                    }
                });
            }
        });
        Main_Tts.setLanguage(Locale.KOREAN);
    }
    public void Speech_Stop() {
        runOnUiThread(new Runnable() {
            public void run() {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        speech.stopListening();

                    }
                });

            }
        });
    }
    public void Speech_listen_start() {
        //Intent mServiceIntent = new Intent(this, AndroidLocationServices.class);
        //mServiceIntent.putExtra("CallType", "SPEECHSTART");
        //this.startService(mServiceIntent);
        if (speech_setting == 3){
            //return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 8513);
            return;
        }
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        int A1 = 2000; int A2 = 4500;
        if (speech_howinput.equals("directinput") && speech_Job.equals("easyBal_textmemo")) {
            A1= 10000; A2 = 8000;
        }
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, A1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, A2);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, A2);

        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요");

        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speech_on = 1;
        runOnUiThread(new Runnable() {
            public void run() {
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        speech = SpeechRecognizer.createSpeechRecognizer(MainContext);
                        speech.setRecognitionListener(listener);
                        speech.startListening(recognizerIntent);

                    }
                });

            }
        });
    }
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }
        @Override
        public void onRmsChanged(float rmsdB) {
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
        }
        @Override
        public void onPartialResults(Bundle arg0) {
            Log.d("Log", "onPartialResults");
            ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            text = matches.get(0); //  Remove this line while uncommenting above    codes
            if (speech_howinput.equals("directinput")) {
                //final String goStrurl = "javascript:get_speechWord_partial('" + speech_howinput + "','" + speech_Job + "','" + text + "')";
                final String goStrurl = "javascript:get_speechWord_partial_keep('" + speech_howinput + "','" + speech_Job + "','" + text + "')";
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
            }else{
                final String goStrurl = "javascript:get_speechWord_partial_keep('" + speech_howinput + "','" + speech_Job + "','" + text + "')";
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });

            }
        }
        @Override
        public void onEndOfSpeech() {
        }
        @Override
        public void onError(int error) {
            String mError = "";
            String mStatus = "Error detected";
            switch (error) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    mError = " network timeout";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    mError = " network" ;
                    //toast("Please check data bundle or network settings");
                    return;
                case SpeechRecognizer.ERROR_AUDIO:
                    mError = " audio";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    mError = " server";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    mError = " client";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    mError = " speech time out" ;
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    mError = " no match" ;
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    mError = " recogniser busy" ;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    mError = " insufficient permissions" ;
                    break;
            }


        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            String speechword = rs[0];
            //alertt(rs[0]);
            final String goStrurl = "javascript:get_speechWord('" + speech_howinput + "','" + speech_Job + "','" + speechword + "')";
            Log.e("get_speechWord" , goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            speech.stopListening();
            speech_on = 0;
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    private List<String> permitionList = new ArrayList<>();
    private int permition_num = 0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permition_listMake() {
        int ACCESS_COARSE_LOCATIONPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int READ_CONTACTSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int permissionSendMessage = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int CAMERAPermition = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int READ_CALL_LOGPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);
        int READ_PHONE_STATEPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int WRITE_EXTERNAL_STORAGEermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int WRITE_CONTACTSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        int Audio_CONTACTSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int Phone_Outgoing = ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS);


        if (ACCESS_COARSE_LOCATIONPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (locationPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (READ_CONTACTSPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.READ_CONTACTS);
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.SEND_SMS);
        if (CAMERAPermition != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.CAMERA);
        if (READ_CALL_LOGPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.READ_CALL_LOG);
        if (READ_PHONE_STATEPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.READ_PHONE_STATE);
        if (WRITE_EXTERNAL_STORAGEermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (WRITE_CONTACTSPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.WRITE_CONTACTS);
        if (Audio_CONTACTSPermission != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.RECORD_AUDIO);
        if (Phone_Outgoing != PackageManager.PERMISSION_GRANTED) permitionList.add(Manifest.permission.PROCESS_OUTGOING_CALLS);

        if (!permitionList.isEmpty()) { ActivityCompat.requestPermissions(this, permitionList.toArray(new String[permitionList.size()]), permition_num); }
        //permition_num

    }
    public void check_and_get_badcopany() {
        if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        try {
            mDbOpenHelper.init_DB(0);
            int maxNum = mDbOpenHelper.getMax_savedNum();
            get_badCompanyList(maxNum);
            badCompanycheckonce = 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void reset_andGetAgainBad(int i) {
        badCompanycheckonce = 0;
        mDbOpenHelper = new DbOpenHelper(MainContext);
        try {
            mDbOpenHelper.init_DB(1);
            get_badCompanyList(i);
            badCompanycheckonce = 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void get_Both_Count_matched() {
        String url = "http://jbbuller.kr/_api_get_badcompanymax.php";

        class GetDataJSON_Cnt extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject json_data = new JSONObject(result);
                    String num = json_data.getString("result");
                    int numInt = Integer.parseInt(num);

                    int TotalCnt = mDbOpenHelper.getMax_savedNum_max();
                    if (numInt != TotalCnt && badC_reload.equals("")) {
                        badC_reload = "RELOAD";
                        reset_andGetAgainBad(0);
                    } else if (!badC_reload.equals("")) {
                        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
                        try {
                            mDbOpenHelper.init_DB(1);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        GetDataJSON_Cnt g = new GetDataJSON_Cnt();
        g.execute(url);
    }
    public void get_badCompanyList(int maxnum) {
        Boolean netOK = checkNetOK();
        Log.e("이터넷체크 ", "netOK:" + netOK + "/ myGCMIDsaved:" + myGCMIDsaved);
        Log.e("토큰값 변경확인 ", "get_badCompanyList Token :" + mGcmRegId);
        if( netOK == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogTheme);
            builder.setTitle("인터넷 연결안됨");
            builder.setCancelable(false);
            builder.setMessage("인터넷 연결 후 이용해 주세요");
            builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Activity mActivity = MainActivity.this;
                    finish();
                    return;
                }
            });
            builder.show();
            return;
        }
        if (badCompanycheckonce > 0) {return;}
        String url = "http://jbbuller.kr/_api_get_badcompanyList.php?maxNum=" + maxnum;
        Log.e("Json url","url: " + url );
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Log.e("Json Check","Errore: " + result );
                if( result == null|| result.equals("null")) result = "";
                if(result.length() > 10 ){
                    try {
                        JSONObject json_data = new JSONObject(result);
                        JSONArray son_data_list = json_data.getJSONArray("result");
                        int size = 0;
                        if( !son_data_list.isNull(1)) son_data_list.length();
                        ArrayList<JSONObject> arrays = new ArrayList<JSONObject>();
                        String myLists = "";
                        String defMyname = "";
                        int addnew = 0;
                        for (int i = 0; i < size; i++) {
                            JSONObject another_json_object = son_data_list.getJSONObject(i);
                            arrays.add(another_json_object);
                            String num = another_json_object.getString("num");
                            String writer_name = another_json_object.getString("writer_name");
                            String writer_phone = another_json_object.getString("writer_phone");
                            String my_num = another_json_object.getString("my_num");
                            String bad_coname = another_json_object.getString("bad_coname");
                            String bad_co_number = another_json_object.getString("bad_co_number");
                            String bad_car_plate = another_json_object.getString("bad_car_plate");
                            String bnd_car_memo = another_json_object.getString("bnd_car_memo");
                            String driver_num = another_json_object.getString("driver_num");
                            String driver_name = another_json_object.getString("driver_name");
                            String driver_phone = another_json_object.getString("driver_phone");
                            String reg_date = another_json_object.getString("reg_date");
                            if (num.length() > 0 && my_num.length() > 0) {
                                addnew++;
                                int ok = mDbOpenHelper.save_badcompany_listone(
                                        num, my_num, bad_coname, bad_co_number, bad_car_plate, bnd_car_memo, driver_num, driver_name, driver_phone, reg_date, writer_name, writer_phone
                                );

                            }
                        }
                        if (addnew > 0 && badC_reload.equals("") && addnew < 20 ) {
                            alertt("악덕업체 [ " + addnew + " ] 개가 신규등록 업데이트 하였습니다..");
                        }
                        get_Both_Count_matched();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
    public void kakao_go(float gps1, float gps2, String addr) {
        Location kakao = Destination.newBuilder(addr, gps1, gps2).build();
        KakaoNaviParams params = KakaoNaviParams.newBuilder(kakao).setNaviOptions(NaviOptions.newBuilder().setCoordType(CoordType.WGS84).build()).build();
        KakaoNaviService.shareDestination(this, params);
    }
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MY KEY HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null){

            return "패키지못받음";
        }

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String AKey = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                return AKey;
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Unable to get MessageDigest. signature=" + signature, e);
                return "___해시못받음222";
            }
        }
        return "___해시못받음";
    }
    public static boolean checkSigneture(Context context){
        return true;
    }
    public void notice_checkDoJob(Intent intent) {
        String notiTitle = intent.getStringExtra("notiTitle");
        String linkurl = intent.getStringExtra("linkurl") + "";
        String noticejob = intent.getStringExtra("noticejob") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";
    }
    protected void onHandleIntent(Intent intent) {
        System.out.println("Working till here fine In RegisterAlarmonHandleIntentonHandleIntentonHandleIntent");
        Bundle bundle = intent.getExtras();
        //Intent localIntent = new Intent(custom-event-name);
        //localIntent.putExtras(bundle );
        //LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
    public void get_Version() {
        try {
            final String packageName = MainContext.getPackageName();
            PackageInfo packageInfo = MainContext.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            AppVersionCode = packageInfo.versionName; // for example
            saveAppVerToDb();
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
        }
    }
    public void saveAppVerToDb() {
        SharedPreferences userDetails = getApplicationContext().getSharedPreferences("userInfo_apset", MODE_PRIVATE);
        String member_Num = userDetails.getString("USERNUMBER", "");
        String GPSGap = userDetails.getString("USERNUGPSGABMBER", "");
        if (member_Num.equals("")) member_Num = "0";
        userNumberN = Integer.parseInt(member_Num);
        if (userNumberN < 2) userNumberN = diRect_userNUM;
        if (userNumberN < 2) return;
        final String goStrurl = "javascript:got_AppVer_from_App(" + AppVersionCode + "," + userNumberN + ")";
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void overLayCheck() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }
        boolean canDrawOverlays = Settings.canDrawOverlays(this);
        if (Settings.canDrawOverlays(this)) {
            return;
        }
        if (!canDrawOverlays) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }

        //Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + getPackageName()));
        //startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }
    public void test_open() {
        Intent pupInt;
        pupInt = new Intent(MainContext, Pupup_notice.class);
        //pupInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pupInt.putExtra("callmode", "calling");
        pupInt.putExtra("pnum", "010-4587-9658");
        pupInt.putExtra("bad_coname", "악덕이름");
        pupInt.putExtra("writer_ph", "010-5555-6666");
        pupInt.putExtra("memo", "악덕입니다.");
        pupInt.putExtra("reg_date", "2016-06-12 11:30:55");
        PendingIntent pi = PendingIntent.getActivity(MainContext, 0, pupInt, PendingIntent.FLAG_ONE_SHOT);
        try {
            pi.send();
        } catch (Exception e) {
            Toast.makeText(MainContext, e.toString(), Toast.LENGTH_LONG);
        }
    }
    public void sendExcepListToWeb() {
        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        String excepList = mDbOpenHelper.getExceopBadList();
        final String goStrurl = "javascript:set_badCompanyExcepList('" + excepList + "')";
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    TMapView mMapView = null; //티맵 함수 정의
    public void TMapRun(String adrgo, float gps1, float gps2) {
        TMapTapi tmaptapi = new TMapTapi(this);
        mMapView = new TMapView(this); //생성자 함수 정의
        mMapView.setSKPMapApiKey("53bf6e79-db3d-3676-b759-7d798ee20fe3");
        boolean isAppInstalled = appInstalledOrNot("com.skt.tmap.ku");
        if (isAppInstalled == true) {
            tmaptapi.invokeRoute(adrgo, gps1, gps2);
        } else {
            final String goStrurl = "javascript:open_windowFromApp('https://play.google.com/store/apps/details?id=com.skt.tmap.ku&hl=ko')";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }

    }
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean ok = false;
        try {
            PackageInfo oo = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        overLayCheck();
        switch (requestCode) {
            case 7789: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    Intent i = new Intent(MainActivity.this, SignatureActivity.class);
                    i.putExtra("ordernum", Sign_orderNum);
                    i.putExtra("workDetail", Sign_workDetail);
                    i.putExtra("doc_price", Sign_doc_price);
                    startActivity(i);
                } else {
                    alertt("휴대폰에 싸인파일을 저장할 수 있는 권한이 없어 싸인을 이용할 수 없습니다. 싸인파일을 저장할 수 있게 내 휴대폰에서 권한을 부여해 주세요");
                }
                break;
            }
            case 9810: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("READ_PHONE_STATE", "permison_READ_CONTACTS OK");
                } else {
                    alertt("통화정보 조회가 거부되어 기능을 이용할 수 없습니다. 9654");
                }
                break;
            }
            case 9654: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("READ_CONTACTS", "permison_READ_CONTACTS OK");
                } else {
                    alertt("전화번호 조회가 거부되어 기능을 이용할 수 없습니다. 9654");
                }
                break;
            }
            case 1001: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

                } else {
                    alertt("위치정보 받기가 거부되어 기능을 이용할 수 없습니다. 1001");
                }
                break;
            }
            case 1231: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    imgSelAndreay();
                } else {
                    alertt("외부 파일 읽기 거부되어 기능을 이용할 수 없습니다. 1231");
                }
            }
            case 1232: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    alertt("쓰기허용됨 다운로드를 다시 실행해 주세요");
                } else {
                    alertt("외부 파일 읽기 거부되어 기능을 이용할 수 없습니다.1232");
                }
            }
            case 1006: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)) {
                    readSMSMessage_sms(100);
                } else {
                    alertt("문자메시지 읽기 거부되어 기능을 이용할 수 없습니다.1006");
                }
            }
            case 1009: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    record_list_get(100);
                } else {
                    alertt("외부 파일 읽기 거부되어 기능을 이용할 수 없습니다.1009");
                }
            }
            case 1005: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
                    myPhoneCallLog(100);
                } else {
                    alertt("통화목록 읽기 거부되어 기능을 이용할 수 없습니다.1005");
                }
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void photo_upload(String uptype) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = new Intent(ACTION_IMAGE_CAPTURE); // 카메라 촬영 // camera_job6.equals("nocrop")  camera_job6.equals("crop")
        //intent.setRequestCode()
        if (uptype.equals("camera")) {
            String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"; // 임시로 사용할 파일의 경로를 생성
            mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            if (Build.VERSION.SDK_INT >= 24) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    }catch (IOException ex) {
                        //Error occurred while creating the File
                        return;
                    }
                    if (photoFile != null) {
                        //mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                        mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, "co.kr.jbbuller.provider", photoFile);
                        List<ResolveInfo> resInfoList = MainContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            MainContext.grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                        if( isCamera_chat.equals("personUpload")){
                            startActivityForResult(intent, 2);
                        }else{
                            startActivityForResult(intent, PICK_FROM_CAMERA);
                        }

                        return;
                    }
                }///////////////////////////////////////////
                return;
            }

            startActivityForResult(intent, PICK_FROM_CAMERA);
        } else {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }
    }
    private void imgSelAndreay(String upfolder, String jobact, String mbno, String opt) {
        mb_no = mbno;
        upload_folder = upfolder;
        jobAct = jobact;
        LastDeleteFn = "";
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 1231);
            }
            alertt("Please try again after allowing permission !");
            return;
        }
        if( opt.equals("camera")){
            photo_upload("camera");
            return;
        }else if( opt.equals("gallery")){
            photo_upload("gall"); //run_ImageGallery_Select();
            return;
        }else{
            final CharSequence[] options = {"From Camera", "From gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select one");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("From Camera")) {
                        photo_upload("camera");
                    } else if (options[item].equals("From gallery")) {
                        photo_upload("gall"); //run_ImageGallery_Select();
                    } else if (options[item].equals("Cancel")) {
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }


    } ///갤러리 또는 카메라 중 선택
    public void tell_Webview_hold() {//////////////카메라 또는 기타 비활성화 된것을 새로고침 하지 않기
        final String goStrurl = "javascript:chat_deActivateOnce()";
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void run_ImageGallery_Select(){
        tell_Webview_hold();
        Activity myAct = MainActivity.this;
        int OKOK = 1;
        if(OKOK == 1){
            Louvre.init(MainActivity.this)
                    .setMaxSelection(100)
                    .setRequestCode(LOUVRE_REQUEST_CODE)
                    .open();//.setSelection(selection)
        }else{
            photo_upload("gall");
        }
    }
    final class UploadUtil{
        UploadUtil(){}
        @JavascriptInterface
        public void Upload_start(final String jobact, final String upfolder, final String mbno, final String where ) {
            uploadMediaType = "image";
            runOnUiThread(new Runnable() {
                public void run() {
                    imgSelAndreay(upfolder,jobact,mbno,where);
                }
            });
        }
        @JavascriptInterface
        public void Upload_start_gallery(final String roomnum, final String mbno) { //UploadUtil.Upload_start_gallery(jobAct,upload_folder,mb_no);
            uploadMediaType = "image";
            mb_no = mbno;
            room_num=roomnum;
            if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == -1) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE)) {
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 1298);
                }
                return;
            }
            Louvre.init(MainActivity.this).setMaxSelection(100).setRequestCode(LOUVRE_REQUEST_CODE).open();
            //run_ImageGallery_Select();
        }

        @JavascriptInterface
        public void Upload_start_camera(final String roomnum, final String mbno) {
            uploadMediaType = "image";
            mb_no = mbno;
            room_num=roomnum;
            isCamera_chat = "chatupload";
            photo_upload("camera");
        }


        @JavascriptInterface
        public void Upload_start_videos(final String jobact, final String upfolder, final String mbno) {
            uploadMediaType = "video";
            tell_Webview_hold();
            mb_no = mbno;
            upload_folder=upfolder;
            jobAct=jobact;
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);

        }

        @JavascriptInterface
        public void Chat_ImagePopShow(final String show_fn_list, String stnum) {
            Start_Chat_imageShow(show_fn_list, stnum);
        }
    }
    public void Start_Chat_imageShow(String url, String stnum){ // UploadUtil.Start_Chat_imageShow( url stnum);
        Intent intent = new Intent(this, Image_Zoomer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = new Bundle();
        b.putString("fnlist", url);
        b.putString("startnum", stnum);
        intent.putExtras(b);
        startActivity(intent);
    }
    public void callSignPad(){}
    public void call_Message_refresh(){
        if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        String recentDate = mDbOpenHelper.getMin_DB_Record();
        last_Update_pushNo = recentDate;
        HTTPH.My_messageList_get("refresh", recentDate, MainContext);
    }
    public void sendSavedLoginInfoToApp(int act){
        if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        String oldData = mDbOpenHelper.getApps_loginInfo();
        String urlgo = "javascript:getSaved_loginInfo_Return('" + oldData + "')";
        if( act == 2 ) urlgo = "javascript:getSaved_loginInfo_ReturnFrame('" + oldData + "')";
        Log.e("이전로그인정보" , urlgo);
        final String goStrurl =urlgo;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final class wmLoginMDVer{
        wmLoginMDVer() {}
        @JavascriptInterface
        public void Login_setTo(String bytes){
            if( bytes==null) bytes = "";
            String[] fex = bytes.split("\\|");
            int exten = fex.length;
            System.out.println(fex);

            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            mDbOpenHelper.clearLoginInfos();
            int resaved_cnt = 0;
            for (int i = 0; i < fex.length; i++) {
                String eaLine = fex[i];
                if(eaLine.length() > 4  ){
                    String[] eaArray = eaLine.split(":");
                    if( eaArray.length > 1 ){
                        if( eaArray[0].length() > 1 ) {
                            String GoSql = "Insert into LoginInfoSaved (fd_title,fd_value,td_type) values ('" + eaArray[0] + "','" + eaArray[1] + "','" + eaArray[2] + "')";
                            String OK = mDbOpenHelper.LoginDbInputUpdate(GoSql);
                            resaved_cnt++;
                            //System.out.println(OK + "," + GoSql);
                        }
                    }
                }
            }
            System.out.println("총 " + resaved_cnt + " 개로그인 항목 저장됨 ");
        }
        @JavascriptInterface
        public void getSaved_loginInfo(){ // window.wmLoginMDVer.getSaved_loginInfo(); function getSaved_loginInfo_Return(str){}
            sendSavedLoginInfoToApp(1);
        }
        @JavascriptInterface
        public void getSaved_loginInfoFrame(){ // window.wmLoginMDVer.getSaved_loginInfo(); function getSaved_loginInfo_Return(str){}
            sendSavedLoginInfoToApp(2);
        }

    }
    final class UnInsApp { //
        UnInsApp(){}
        @JavascriptInterface
        public void AppUninstallFirst() {  //window.AppVwhite.start_SignPad(fn);
            UninstalApp();
        }
    }
    final class AppVwhite { //
        AppVwhite() {}//LoadingBar_ShowHide
        @JavascriptInterface
        public void get_JB_RecoedDatas(){
            Log.e("JsGet" , "get_JB_RecoedDatas");
            //doSomeApp();
        }
        @JavascriptInterface
        public void start_SignPadDetail(String ordernum, String workDetail,String doc_price,String ww_addr,String ww_day , String d_title, String d_cont ) {  //window.AppVwhite.start_SignPad(fn);
            Sign_orderNum = ordernum;
            Sign_workDetail = workDetail;
            Sign_doc_price = doc_price;
            int WRITE_EXTERNAL_STORAGEermission = ContextCompat.checkSelfPermission(MainContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (WRITE_EXTERNAL_STORAGEermission != PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //requestPermissions(MainActivity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 778);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7789);
                }
            }else{
                Intent i = new Intent(MainActivity.this, SignatureActivity.class);
                i.putExtra("ordernum", Sign_orderNum);
                i.putExtra("workDetail", Sign_workDetail);
                i.putExtra("doc_price", Sign_doc_price);
                /*
                i.putExtra("ww_addr", ww_addr);
                i.putExtra("ww_day", ww_day);
                i.putExtra("d_title", d_title);
                i.putExtra("d_cont", d_cont);
                */
                startActivity(i);
            }
        }
        @JavascriptInterface
        public void clearWebviewfromSite(){
            Share_utils.clearApplicationCache(MainContext,null);
        }
        @JavascriptInterface
        public void getCacheFolderSize(){
            long fSize = Share_utils.get_CacheSize(MainContext);

            final String goStrurl = "javascript:cacheFolder_SizeCheck(" + fSize + ")";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }
        @JavascriptInterface
        public void LoadingPanHide() {
            LoadingBar_ShowHide(0);
        }
        @JavascriptInterface
        public void start_SignPad(String ordernum) {  //window.AppVwhite.clearWebviewfromSite();
            Sign_orderNum = ordernum;
            int WRITE_EXTERNAL_STORAGEermission = ContextCompat.checkSelfPermission(MainContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (WRITE_EXTERNAL_STORAGEermission != PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //requestPermissions(MainActivity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 778);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7789);
                }
            }else{
                Intent i = new Intent(MainActivity.this, SignatureActivity.class);
                i.putExtra("ordernum", Sign_orderNum);
                startActivity(i);
            }

        }
        @JavascriptInterface
        public void js_set_Session_value_Set(String keyTitle, String keyValue) {
            Session_key_valueSetTo(keyTitle, keyValue);
        }
        @JavascriptInterface
        public void js_get_pushnotice_filename_both(String sndFn, String noticdFn) {
            play_snd_save_both(sndFn, noticdFn);
        }
        @JavascriptInterface
        public void check_mgList_orderList() {
            HTTPH.get_and_makeMessageList(99);
        }
        @JavascriptInterface
        public void reset_Push_Order_Datas(int d) { // window.AppVwhite.reset_Push_Order_Datas(3);
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            HTTPH.reset_DBOnce();
            mDbOpenHelper.reset_push_order_DB(d);
            if( d == 1 || d == 2 ) HTTPH.get_and_makeMessageList(1);
        }
        @JavascriptInterface
        public void test_show_MessageLIst() {
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            String datas = mDbOpenHelper.Test_show_MessageLIst();

        }
        @JavascriptInterface
        public void show_messageList(int offset , int limit) {
            String Datas = mDbOpenHelper.get_messageList(offset, limit);
            final String goStrurl = "javascript:AppDB_messageList_print(1,'" + Datas + "')";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            //String datas = mDbOpenHelper.Test_show_MessageLIst();

        }
        @JavascriptInterface
        public void get_more_list(int d) {/////////////////////////////////////////////////////////////////////////////////////////////
            int ok = 0;
            if( callTimeCnt == 4 ) ok = 1;
            if( callTimeCnt > 4 ) callTimeCnt = 0;
            callTimeCnt++;
            if( ok == 1 ) call_Message_refresh();
        }
        @JavascriptInterface
        public void init_NoticePage() {
            callTimeCnt = 0;

        }
        @JavascriptInterface
        public void get_recent_list(int d) {
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            String recentDate = mDbOpenHelper.getMax_DB_Record();
            last_Update_pushNo = recentDate;
            HTTPH.My_messageList_get("recentListGet", recentDate, MainContext);
        }
        @JavascriptInterface
        public void test_show_orderLIst() {
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            String datas = mDbOpenHelper.Test_OrderListshow();
        }
        @JavascriptInterface
        public void test_OrderUpdate(int no) {
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            String datas = mDbOpenHelper.Test_OrderUpdate(no);
        }
        @JavascriptInterface
        public void askTo_QuitApp(){
            backButtonHandler();
        }
        //Test_OrderUpdate // AppVwhite.askTo_QuitApp();
        //backButtonHandler()
    }
    final class App_VchFF { // window.App_VchFF.webView_getApp_Ver();
        App_VchFF() {
        }
        @JavascriptInterface
        public void webView_getApp_Ver() {
            get_Version();
        }

    }
    final class App_VNew { // window.App_VNew.run_Tmap('우리집', 37.506206, 126.721459);
        App_VNew() {
        }
        @JavascriptInterface
        public void run_Tmap(String adrgo, float gps1, float gps2) {
            TMapRun(adrgo, gps1, gps2);
        }
    }
    final class App {
        App() {
        }

        @JavascriptInterface
        public void Upload_start_camera_crop(final String roomnum, final String mbno) {
            uploadMediaType = "image";
            isCamera_chat = "personUpload";
            requestUploadJob = roomnum;
            camera_job5 = mbno;
            photo_upload("camera");


        }

        @JavascriptInterface
        public void video_play_call(String vurl) {
            Video_play(vurl);
        }
        @JavascriptInterface
        public void reset_badCompany() {
            //Log.e("악덕초기화", "reset_badCompany");
            reset_andGetAgainBad(0);
        }
        @JavascriptInterface
        public void get_badCompany_Except() {
            Log.e("get_badCompany_Except", "get_badCompany_Except");
            sendExcepListToWeb();
        }
        @JavascriptInterface
        public void make_except_ref_cancel(int no) {
            if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            int exceptRst = mDbOpenHelper.make_except_Cancel_Do(no);
            Log.e("make_except_ref", "Result =" + exceptRst);
            sendExcepListToWeb();
        }
        @JavascriptInterface
        public void make_except_ref(int no) {
            if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            int exceptRst = mDbOpenHelper.make_except_refDo(no);
            Log.e("make_except_ref", "Result =" + exceptRst);
            sendExcepListToWeb();

        }
        @JavascriptInterface
        public void badTest() {
            Log.e("시작", "자바스크립호출되고");
            test_open();
        }
        @JavascriptInterface
        public void make_ShortCut() {
            addShortcut(9);
        }
        @JavascriptInterface
        public void requestUpload(final String jobtype, final String type2, final String type3, final String type4, final String type5, final String type6) {
            //window.App.requestUpload("workUploadimg","login_id",photoUPOrderNum,dtype,memnum,"-");
            Log.e("사진업로드", "유형:" + jobtype);
            requestUploadJob = jobtype;
            userID = type2;   /// 로그인 아이디
            camera_job3 = type3; ////오더번호
            camera_job4 = type4;  /// 문서 또는 현장사진 //m6_docUPload
            camera_job5 = type5;  // mb no
            camera_job6 = type6; ///nocrop 이면 크롭하지 않기   crop 이면 크롭하기
            imgSelAndreay();
        }
        @JavascriptInterface
        public void App_Upload_Start(final String jobtype, final String type2, final String type3, final String type4, final String type5, String fileSizeSet) {
            //window.App.requestUpload("workUploadimg","login_id",photoUPOrderNum,dtype,memnum,"-");
            requestUploadJob = jobtype;
            userID = type2;   /// 로그인 아이디
            camera_job3 = type3; ////오더번호
            camera_job4 = type4;  /// 문서 또는 현장사진 //m6_docUPload
            camera_job5 = type5;  // mb no
            if (fileSizeSet == null) fileSizeSet = "1800000";
            if (fileSizeSet.equals("")) fileSizeSet = "1800000";
            int upSize = 1800000;
            try{
                upSize = Integer.parseInt(fileSizeSet);
            }catch(NumberFormatException e){
                Log.e("값이정상아님","fileSizeSet :" + fileSizeSet + "|");
            }
            Log.e("App_Upload_StartAT", "유형:" + jobtype + " / 파일크기 :" + fileSizeSet + "/ 형변환 크기 : " +  upSize);
            Upload_File_sizeSet = upSize;
            imgSelAndreay();
        }
        @JavascriptInterface
        public void readSMSMessage_start(String getTypeval, int howmany) {
            SMSTry = 0;

            if (getTypeval.equals("1")) {
                Log.e("단문문자받기", "시작");
                readSMSMessage_sms(howmany); ////////////////단문보기
            } else {
                Log.e("장문문자받기", "시작");
                readSMSMessage(howmany);
            }
        }////문자 메시지 받기 App.novi_Start(gps1,gps2,addr);
        @JavascriptInterface
        public void novi_Start(float gps1, float gps2, String addr) {
            Log.e("네비받음" , "GPS1 : " + gps1 + " / gps2 : " + gps2 );
            kakao_go(gps1, gps2, addr);
        } ////녹음파일받기
        @JavascriptInterface
        public void record_list_get_start(int howmany) {
            record_list_get(howmany);
        } ////녹음파일받기
        @JavascriptInterface
        public void myPhoneCallLog_start(int howmany) {
            Log.e("통화목록받기", "시작");
            myPhoneCallLog(howmany);

        } ////통화목록 받기
        @JavascriptInterface
        public void readContacts_start(int howmany) {
            readContacts(howmany);
        }  ////연락처 동기화하기
        @JavascriptInterface
        public void readContacts_detailStart(int howmany) {
            readContacts_detail(howmany);
        }  ////연락처 동기화하기 상세버전
        @JavascriptInterface
        public void record_fn_play(String fn) {
            sound_record_play(fn);
        } ////사운드 파일 실행하기
    }
    final class GPS_Reload_functon { //
        GPS_Reload_functon() {
        }
        @JavascriptInterface
        public void GPS_Reload_App() {
            GPS_Reload_do();
        }
    }
    final class AppV65 { // window.AppV65.get_member_phonenum()
        AppV65() {
        }
        @JavascriptInterface
        public void read_text_to_voice(String text){
            read_textLong(text);
        }
        @JavascriptInterface
        public void set_LocallApp_SettFrom_Server(String push_onoff, String machingUse) {
        }
        @JavascriptInterface
        public void get_member_phonenum() {
            TelephonyManager tMgr = (TelephonyManager) MainContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(MainContext, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainContext, Manifest.permission.READ_PHONE_NUMBERS)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainContext, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String mPhoneNumber = tMgr.getLine1Number();
            sendPhone_number_To_web(mPhoneNumber);
        }
    }
    final class get_gps_set { //  GPS_set.logo_off()
        get_gps_set() {
        }
        @JavascriptInterface
        public void voice_Speech_listen_stop() {
            if (Main_Tts != null) {
                Main_Tts.stop();
            }
        }
        @JavascriptInterface
        public void speechSet(String readspeed,String readPitch ) {

        }
        @JavascriptInterface
        public void voice_Speech_listen_App(String howHandle ,String actval,String TtsWord,int speechSet){
            speech_setting = speechSet;
            Log.e("스피치호출됨","스피치 시작하기222 / " + howHandle + " / " + actval + " / " +  TtsWord + " / " +  speechSet);
            SpeechStart_do(howHandle, actval , TtsWord);
            if( actval.equals("contract_message")){
                final String goStrurl = "javascript:contract_messagRecStart(1)";
                Log.e("버전,contract_messagRecStart" , goStrurl);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
            }
        }
        @JavascriptInterface
        public void voice_stopListening(){
            if( speech != null){
                Log.e("스피치중지","듣기 중지됨");
                speech.stopListening();
            }
        }
        @JavascriptInterface
        public void voice_Speech_listen(String howHandle ,String actval,String TtsWord){
            Log.e("스피치호출됨","스피치 시작하기");
            SpeechStart_do(howHandle, actval , TtsWord);
        }
        @JavascriptInterface
        public void getSpeechSet(){
            read_voiceSetting();
        }
        @JavascriptInterface
        public void get_gps_set(int gpsGab_Time, int memeBerNum) {
            userNumberN = memeBerNum;
            SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("SkyMbno", Integer.toString(userNumberN));
            editor.commit();

            String my_mbno = pref.getString("SkyMbno", "0");
            mDbOpenHelper.SetSettingvalue("seting_24",String.valueOf(memeBerNum));
            Log.e("회원번호","회원번호 받음" + my_mbno);
            HTTPH.get_and_makeMessageList(-1);
        }
        @JavascriptInterface
        public void logo_off() { //window.get_gps_set.logo_off()
            initPageDo();
        }
        @JavascriptInterface
        public void set_push_settingmore(String notioff, String matching, int onsettings, String push_speechset,String speech_typeset, String readspeed, String readPitch  )  {
            SharedPreferences pref = getSharedPreferences("noti_Setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("NOTIOFF", notioff + "");
            editor.putString("MACHINGOFF", matching + "");
            editor.commit();
            mDbOpenHelper.SetSettingvalue("seting_1",push_speechset);
            mDbOpenHelper.SetSettingvalue("seting_2",speech_typeset);
            mDbOpenHelper.SetSettingvalue("seting_6",readspeed);
            mDbOpenHelper.SetSettingvalue("seting_7",readPitch);
            mDbOpenHelper.SetSettingvalue("seting_3",notioff);
            mDbOpenHelper.SetSettingvalue("seting_4",matching);
            String[] getDbInfo = mDbOpenHelper.getSettingvalue();
            String NOTIOFF = getDbInfo[3];
            String MACHINGOFF = getDbInfo[4];

            push_speechset = getDbInfo[1];
            speech_typeset = getDbInfo[2];
            readspeed = getDbInfo[6];
            readPitch = getDbInfo[7];
            /// Log.e("DBSAVERESILT", "Resut nitioff :" + NOTIOFF + " / Resut matching : " + MACHINGOFF +
            //" / push_speechset : " + push_speechset + " / speech_typeset : " + speech_typeset + " / readspeed : " + readspeed + " / readPitch : " + readPitch );


            if (onsettings == 1) {
                alertt("푸시알림:" + NOTIOFF + "  꽝꽝알림:" + MACHINGOFF);
            }
        }
        @JavascriptInterface
        public void set_push_setting(String notioff, String matching, int onsettings) {
            SharedPreferences pref = getSharedPreferences("noti_Setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("NOTIOFF", notioff + "");
            editor.putString("MACHINGOFF", matching + "");
            editor.commit();

            mDbOpenHelper.SetSettingvalue("seting_3",notioff);
            mDbOpenHelper.SetSettingvalue("seting_4",matching);
            String[] getDbInfo = mDbOpenHelper.getSettingvalue();
            String NOTIOFF = getDbInfo[3];
            String MACHINGOFF = getDbInfo[4];
            Log.e("DBSAVERESILT", "Resut nitioff :" + NOTIOFF + " / Resut matching : " + MACHINGOFF);
            if (onsettings == 1) {
                if( notioff.equals("NO")) MACHINGOFF = "NO";
                alertt("푸시알림:" + NOTIOFF + "  꽝꽝알림:" + MACHINGOFF);
            }
        }
        @JavascriptInterface
        public void push_read_setting(String readornot,String readtype) {
            SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("pushread_text_ornot", readornot + "");
            editor.putString("pushread_text_type", readtype + "");
            editor.commit();

            Log.e("push_read_setting", "자바스크립에서 받은값 저장전 : pushread_text_ornot= " +  readornot + " pushread_text_ornot : " + readtype);
            mDbOpenHelper.SetSettingvalue("seting_1",readornot);
            mDbOpenHelper.SetSettingvalue("seting_2",readtype);

            String OK = "OFF";
            String Oktype = ", 접수";
            String[] getDbInfo = mDbOpenHelper.getSettingvalue();
            readornot = getDbInfo[1];
            readtype = getDbInfo[2];
            Log.e("push_read_setting", "자바스크립에서 받은값 저장 후 null체크전 : pushread_text_ornot= " +  readornot + " pushread_text_ornot : " + readtype);
            Log.e("push_read_setting", "디비체크 1 :"+getDbInfo[1] + "/2:" + getDbInfo[2] + "/3:" + getDbInfo[3] + "/4:" + getDbInfo[4] + "/5:" + getDbInfo[5]);

            if( readornot== null) readornot = "0";
            if( readtype== null) readtype = "0";

            Log.e("push_read_setting", "자바스크립에서 받은값 저장 후 : pushread_text_ornot= " +  readornot + " pushread_text_ornot : " + readtype);
            if( readtype.equals("2")) Oktype = ", 매칭";
            if( readornot.equals("1")){
                OK = "ON";
            }else{
                Oktype = "";
            }
            alertt("매칭 읽어주기 : " + OK + " " + Oktype);

        }
        @JavascriptInterface
        public void message_read_all() {
            iconNumRest();
        }
    }
    final class AppV13 {
        AppV13() {
        }
        @JavascriptInterface
        public void sound_play_one(String sndFn) {
            //Log.e("sound_play_one", "사운드 재생 호출함.");
            play_snd_test(sndFn);
        }
        @JavascriptInterface
        public void js_get_pushnotice_filename(String sndFn) {
            //Log.e("소리정정호출", "js_get_pushnotice_filename value= " + sndFn);
            play_snd_save(sndFn);
        }

        @JavascriptInterface
        public void get_app_infos(int UserNum) {
            //Log.e("get_app_infos", "get_app_infosjob 호출함= " + UserNum);
            get_app_infos_jobDo(UserNum);
        }
    }
    final class AppVersin_Checker {
        AppVersin_Checker() {
        }
        @JavascriptInterface
        public void App_ver_check_return(String jobCode){
            AppVersionCode = "0";
            try {
                final String packageName = MainContext.getPackageName();
                PackageInfo packageInfo = MainContext.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
                AppVersionCode = packageInfo.versionName; // for example
                saveAppVerToDb();
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            }
            int isBadCoInstallInt = 0;
            if( isBadCoInstall) isBadCoInstallInt = 1;
            final String goStrurl = "javascript:App_version_push('" + jobCode + "'," + AppVersionCode + "," + userNumberN + "," + isBadCoInstallInt +")";
            Log.e("버전,악덕앱설치체크" , goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }
        @JavascriptInterface
        public void BadCall_RecentListGet(int tabno){ // window.AppVersin_Checker.BadCall_RecentListGet();
            Intent intent = new Intent(MainContext, popup_page.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String uUrl = "http://jbbuller.kr/call_record/call_log_list.php?tabno=" +  tabno;
            mDbOpenHelper.SetSettingvalue("seting_10", uUrl);
            Log.e("새창", uUrl);
            String[] getDbInfo = mDbOpenHelper.getSettingvalue();
            String goUrl = getDbInfo[10];
            Bundle b = new Bundle();
            intent.putExtras(b);
            startActivity(intent);
            /*


            String retStr = BadCompny_RecentCall_listGetfromDB();
            final String goStrurl = "javascript:phone_RecentCallListPush('" + retStr + "')";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });

             */
        }
    }
    final class Location_get { //
        Location_get() {
        }
        @JavascriptInterface
        public void open_newPage(String nurl) { // window.Location_get.popupPageShow(url);
            Log.e("스크립에서받음" , "nurl : " + nurl);
            popupPageShow(nurl);
        }

        @JavascriptInterface
        public void GpsNoticeSetMgshow(){
            GpsNotice=1;
        }

        @JavascriptInterface
        public void sendToAndroid() { //Location_get.sendToAndroid()
            ///gps_StopOnce();
            get_send_Gps();
        }

        @JavascriptInterface
        public void Timer_start() {

        }
    }
    final class Noticefrom_Web {
        Noticefrom_Web() {
        }
        @JavascriptInterface
        public void webViewZoomSet(final int zoomSet){
            webView_zoomSet(zoomSet);
        }

        @JavascriptInterface
        public void webview_NotiOnly(final String messageString) { //window.Noticefrom_Web.webview_NotiOnly('');
            //window.Location_get.webview_NotiOnly();
            alertt(messageString);
        }
        @JavascriptInterface
        public void webNotice(final String messageString) {
            Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 200, 200, 200};
            vibs.vibrate(pattern, -1); //-1 is important

            Boolean sk = isMyServiceRunning(getApplicationContext());
            alertt(messageString);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (ActivityNotFoundException e) {
                return;
            }

            if( messageString.equals("발주가 완료 되었습니다.")) {
                play_snd_check_do("order_noticement","bok");
            }
            if(messageString.equals("수주가 완료 되었습니다.")){
                play_snd_check_do("order_noticement", "sok");
            }
            Log.e("알림메시지", messageString);
        }

        @JavascriptInterface
        public void save_setting(final String setname,String setvalue) {
            Log.e("설정값 변경 JS start ", "설정키 :" + setname + " / 설정값:" + setvalue);
            set_user_setting(setname, setvalue);
        }
    }
    final class WebPos_save { // window.WebPos_save.work_done_SignStart();
        WebPos_save() {
        }
        //diRect_userNUM
        @JavascriptInterface
        public void SetMBNO_FromWeb(int mbno) { // window.WebPos_save.SetMBNO_FromWeb(mbno);
            diRect_userNUM = mbno;
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
            mDbOpenHelper.SetSettingvalue("seting_24",String.valueOf(diRect_userNUM));
            HTTPH.get_and_makeMessageList(1);
            get_send_Gps();
        }
        @JavascriptInterface
        public void App_sidemenu_onOffset(String isOnOff) { // window.WebPos_save.App_sidemenu_onOffset(act);
            AppSideOn = isOnOff;
            Log.e("사이드메뉴", "현상태:" + AppSideOn );
        }
        @JavascriptInterface
        public void web_url_pos_get(String pageUrlvalue) {
            page_URLNow = pageUrlvalue.replace("#", "");
            Log.e("page_URLNowSetTo", "Set URL:" + page_URLNow);
            AppSideOn = "OFF";
            String onoff ="OFF";
            if( page_URLNow.equals("_sky_m_02.php") || page_URLNow.equals("_show_order_detail_v1.php")) onoff = "ON";
            final String go = onoff;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final String act = go;
                            Screen_KeepOn(act);
                        }
                    });
                }
            }).start();
        }
        @JavascriptInterface
        public void SaveTokenInfos() { // window.WebPos_save.RunTokenTestStart
            Log.e("톤큰값 재 저장", "TokenNew : " + myGCMIDsaved);
            get_send_Gps();
            alertt("토큰값을 저장했습니다.");

        }
        @JavascriptInterface
        public void RunTokenTestStart(int mbno) { // window.WebPos_save.RunTokenTestStart
            Log.e("토큰겁사", "회원번호 : " + mbno);
            final String goStrurl = "javascript:TokenTestAnswerTo(" + mbno + ",'" + mGcmRegId + "')";
            Log.e("RunTokenTestStart", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }
        /*
        var ordernum = orderNum
        var w_time = tar.getAttribute("w_time");
        var car_plateset = tar.getAttribute("car_plateset");
        var car_plate_list = tar.getAttribute("car_plate_list");
        var w_type = tar.getAttribute("w_type");
        var added_time = tar.getAttribute("added_time");
        var t_price = tar.getAttribute("t_price");
        var paydonemsg = tar.getAttribute("paydonemsg");
         */
        @JavascriptInterface
        public void work_done_SignStart(String ordernum, String w_time, String car_plateset,
                                        String car_plate_list, String w_type, String t_price, String paydonemsg, String carNo,String work_addr){
            Intent i = new Intent(MainActivity.this, Full_signatureActivity.class);
            i.putExtra("ordernum", ordernum);
            i.putExtra("w_time", w_time);
            i.putExtra("doc_price", t_price);

            i.putExtra("car_plateset", car_plateset);
            i.putExtra("car_plate_list", car_plate_list);
            i.putExtra("car_num", carNo);
            i.putExtra("w_type", w_type);
            i.putExtra("paydonemsg", paydonemsg);
            i.putExtra("work_addr", work_addr);
            Log.e("SendData", "D" + i);
            startActivity(i);

        }
    }
    final class App_record_voiceSet { //
        App_record_voiceSet() {}
        @JavascriptInterface
        public void record_file_Play(){
            Log.e("Play", "파일 start : " + voice_record_file);
            File oFile = new File(voice_record_file);
            if (!oFile.exists()) {
                Log.e("Play", "파일 없음 :");
                return;
            }
            if( isJBSoundPlaying == 0 ){

                Uri Filepath = Uri.parse(oFile.toString());
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(oFile.toString());
                JbPlayer = MediaPlayer.create(getBaseContext(), Filepath);
                JbPlayer.setLooping(false); // Set looping
                JbPlayer.setVolume(100,100);
                JbPlayer.start();
                hdlr = new Handler();
                hdlr.postDelayed(UpdateSongTime, 100);
                isJBSoundPlaying = 1;
                JbPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.e("재생완료", "record_file_Stop 호출함");
                        record_file_Stop();
                        JbPlayer.release();
                    }
                });
            }else{
                record_file_Stop();
            }
        }
        @JavascriptInterface
        public void upload_recoed_fileTo(){
            Upload_LIst = new ArrayList<String>();
            fileNames.clear();
            Upload_LIst.clear();
            fileNames.add(voice_record_file);
            Upload_LIst.addAll(fileNames);
            uploadMultiDo();
        }
        @JavascriptInterface
        public void clear_recorded_Obj() { // window.WebPos_save.record_file_Play(mbno);
            Log.e("clear_recorded_Obj", "대상 초기화 실행중");
            resetMediaSource();
        }
        @JavascriptInterface
        public void record_start(String mbno) { // window.WebPos_save.record_file_Play(mbno);
            mb_no = mbno;
            room_num=mbno;
            resetMediaSource();
            isJBSoundPlaying = 0;

            voice_record_file = utilApp.voice_record_Start(mbno);
            Log.e("Recording", "Starting MBNO :" + mbno + " file :" + voice_record_file);
            //App_startingRecording
            final String goStrurl = "javascript:App_startingRecording()";
            Log.e("녹음상태 알리기", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }
        @JavascriptInterface
        public void record_stop(){
            String fn = utilApp.voice_record_stop();
            String ftype = "audio/mpeg";
            Log.e("Recording", "녹음 종료 fn :" + voice_record_file + " Type:" + ftype);
            File oFile = new File(voice_record_file);
            isJBSoundPlaying = 0;
            resetMediaSource();
            if (oFile.exists()) {
                long L = oFile.length();
                Log.e("Recording", "파일크기 :" + L);
                if( L < 1000 ) return;
                Uri Filepath = Uri.parse(oFile.toString());
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(oFile.toString());
                JbPlayer = MediaPlayer.create(getBaseContext(), Filepath);
                JbPlayer.setLooping(false); // Set looping
                JbPlayer.setVolume(100,100);
                /*
                try {
                    JbPlayer.prepare();
                } catch (IOException e) {
                    Log.e("파일준비에러", "Prepare Error");
                    e.printStackTrace();
                }
                 */
                int millisecond = JbPlayer.getDuration();
                Log.e("파일 길이", "파일 길이:" + millisecond);
                mDbOpenHelper.SetSettingvalue("seting_10", voice_record_file);
                final String goStrurl = "javascript:App_recorded_sound_display(" + millisecond + ")";
                Log.e("녹음상태 알리기", goStrurl);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
            }else{
                Log.e("Recording", "파일없음");
            }
        }
        @JavascriptInterface
        public void recordFileRemove(){
            resetMediaSource();
            isJBSoundPlaying = 0;
            File f = new File(voice_record_file);
            Boolean deleted = f.delete();
            Log.e("녹음 파일삭제", "Filename :" + deleted + " / " + voice_record_file);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void javascript_RecordErrorShow(){
        final String goStrurl = "javascript:RecordingErrorShow()";
        Log.e("에러 알리기", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void record_file_Stop(){
        if( JbPlayer != null) JbPlayer.stop();
        isJBSoundPlaying = 0;
        final String goStrurl = "javascript:App_SoundPlay_callBack_stop()";
        Log.e("재생중지 알리기", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void resetMediaSource(){
        utilApp.voice_record_reset();
        Log.e("녹음된플레이어초기화", "AAJbPlayer:" + JbPlayer);
        if( JbPlayer != null){
            Log.e("RESET에서", "값이 있음:" + JbPlayer);
            try{
                Log.e("RESET에서", "AAJbPlayer 재생중 정지하기");
                if(JbPlayer.isPlaying()) JbPlayer.stop();
            }catch (Exception e){
                Log.e("RESET에서", "AAJbPlayer 재생중아님:");
            }
            JbPlayer.release();
            JbPlayer=null;
        }
        JbPlayer = null;
    }
    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            if( isJBSoundPlaying == 0 ) return;
            int sTime = JbPlayer.getCurrentPosition();
            int millisecond = JbPlayer.getDuration();
            if( JbPlayer == null) return;
            if( JbPlayer.isPlaying()){

                if( sTime < millisecond){
                    Log.e("Player", "currentTime:" + sTime + "/ " + millisecond );
                    final String goStrurl = "javascript:App_SoundPlay_callBack(" + sTime + "," + millisecond + ")";
                    Log.e("재생상태 알리기", goStrurl);
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(goStrurl);
                        }
                    });
                    hdlr.postDelayed(this, 100);
                }
            }
        }
    };
    public void UPload_ResultGet_sendToWebview(String filename, String jobtype,String gpsValue, String dateString, String gpsAddress){
        final String goStrurl = "javascript:Upload_AllKind_result('" + filename + "', '" + jobtype + "', '" + gpsValue + "', '" + dateString + "', '" + gpsAddress +"')";
        Log.e("웹뷰에전달", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void LoadingBar_ShowHide(final int showHide){
        final int showSet =  showHide;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if( showSet == 1 ){
                            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }

                    }
                });
            }
        }).start();
    }
    public void do_upDateP(String lastno){
        Log.e("받음 호출번호", "lastno : " + lastno);
        if(lastno==null)lastno = "";
        if( lastno.length() < 10 ) return;
        if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        String Datas = mDbOpenHelper.get_current_ListDatas(lastno);
        if( Datas.length() < 20 ){
            Log.e("do_upDateP", "서버자료 없어 호출안함");
        }else{
            Log.e("do_upDateP", "호출됨");
            final String goStrurl = "javascript:AppDB_messageList_print(-1,'" + Datas + "')";
            Log.e("서버자료", goStrurl);
            if(mWebView == null) mWebView = (WebView) findViewById(R.id.webview);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }

    }
    public class Push_UpdateDo extends AsyncTask<String, Void, String> {
        private Activity myActivity;
        private Context myContext;
        public Push_UpdateDo(Activity activity, Context sContext){
            this.myActivity = activity;
            this.myContext = sContext;
        }
        @Override
        protected String doInBackground(String... position) {
            String lastno = position[0].toString();
            Log.e("Push_UpdateDo", "Push_UpdateDoDone : " + lastno);
            return lastno;
        }
        @Override
        protected void onPostExecute(String nostr) {
            Log.e("BBBBB", "last_Update_pushNo : " + nostr);
            Log.e("MainContext", "MainContext:" + myContext);
            Log.e("받음 호출번호", "lastno : " + nostr);
            if( nostr==null) nostr = "";
            if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(myContext);
            if( nostr.length() < 10 ) {
                nostr = "2018-12-30 12:00:00";

                String Datas = mDbOpenHelper.get_messageList(0, 20);
                Log.e("show_messageList", "호출됨 20개 ");
                final String goStrurl = "javascript:AppDB_messageList_print(1,'" + Datas + "')";
                Log.e("자료", goStrurl);
                final WebView AWebView ;//= (WebView) activity.findViewById(R.id.webview);
                AWebView=(WebView)((Activity) myActivity).findViewById(R.id.webview);
                AWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        AWebView.loadUrl(goStrurl);
                    }
                });
                return;

            };

            String Datas = mDbOpenHelper.get_current_ListDatas(nostr);
            final String goStrurl = "javascript:AppDB_messageList_print(-1,'" + Datas + "')";
            Log.e("자료", goStrurl);
            //MainActivity.this.setContentView(R.layout.AWebView);
            final WebView AWebView ;//= (WebView) activity.findViewById(R.id.webview);
            AWebView=(WebView)((Activity) myActivity).findViewById(R.id.webview);
            AWebView.post(new Runnable() {
                @Override
                public void run() {
                    AWebView.loadUrl(goStrurl);
                }
            });
            return;
        }

        protected void onProgressUpdate(Integer... progress) {
        }
        //new Push_UpdateDo().execute();
    }
    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
    public void re_openSMS(){
        readSMSMessage_sms(100);
    }
    public int readSMSMessage_sms(int howmany) {
        Log.e("문자받기", "readSMSMessage_sms222222222222222222222222222");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == -1) {
            Log.e("문자받기", "AA1");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                Log.e("문자받기", "AA2");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_SMS}, 1006);
                Log.e("문자받기", "AA3");
            }
            Log.e("문자받기", "AA4");
            Toast.makeText(this.getApplicationContext(), "메시지를 읽을 권한을 받지 못했습니다 10 초 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            //if( SMSTry < 1 ) re_openSMS();
            SMSTry++;
            return 0;
        }
        Log.e("문자받기", "AA5");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CONTACTS}, 1007);
            }
            Toast.makeText(this.getApplicationContext(), "주소록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return 0;
        }
        Log.e("문자받기", "AA5555555555555555555555555 howmany : " + howmany);

        Uri allMessage = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(allMessage,
                new String[]{"_id", "thread_id", "address", "person", "date", "body", "protocol", "read", "status", "type", "reply_path_present",
                        "subject", "service_center", "locked", "error_code", "seen"}, null, null, " date DESC");
        String string = "";
        int count = 0;
        while (c.moveToNext()) {
            count++;

            if (count > howmany) break;
            String address = c.getString(2);

            long contactId = c.getLong(10);
            long timestamp = c.getLong(4);
            //Log.e("SMS", c.getString(3) + "/" + c.getString(2));
            String person = getContactName(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));

            String MgDate = make_time_str(timestamp);
            String body = c.getString(5);
            body = body.replaceAll("|", "");
            body = body.replaceAll("`", "");
            body = body.replaceAll("`", "");
            body = body.replaceAll("~", "-");
            body = body.replaceAll("\"", "");
            body = body.replaceAll("'", "");

            body = body.replaceAll("[\\t\\n\\r]", " ");
            Log.e("문자", "person : " + person + "/ body : " + body + " / address : " + address );
            string += address + "|" + contactId + "|" + MgDate + "|" + body + "|" + person + "`";
        }
        c.close();
        Log.e("받은문자", string);

        if (string.equals("")) string = "받은 문자가 없습니다.|&nbsp;|&nbsp;|&nbsp;";
        final String goStrurl = "javascript:SMS_MESSAGELISTS(1,'" + string + "')";
        Log.e("readSMSMessage_sms", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
        return 0;
    }
    public int readSMSMessage(int howmany) { //readSMSMessage_sms
        Log.e("문자받기", "readSMSMessage_sms1111111111111111111");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_SMS}, 1006);
            }
            Toast.makeText(this.getApplicationContext(), "메시지를 읽을 권한을 받지 못했습니다1.", Toast.LENGTH_SHORT).show();
            return 0;
        }
        int BuildVer = Build.VERSION.SDK_INT;
        int cntt = 0;
        String string = "";
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            cursor = MainContext.getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI, null, null, null, null);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cntt < howmany) {

                        int id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID));
                        String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
                        int threadId = cursor.getInt(cursor.getColumnIndex(Telephony.Mms.THREAD_ID));
                        long date = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE));
                        int addColIndx = cursor.getColumnIndex("address");
                        int typeColIndx = cursor.getColumnIndex("type");

                        //Log.e("MSGG", )
                        long dX = date;
                        date = date * 1000;
                        String MgDate = make_time_str(date);
                        String part = getPartOfMMS(id);
                        String Pnum = getAddress(id);
                        String body = getMmsText(id);
                        String What1 = "";
                        String type = getMmsType(id); //1464039255 10자리
                        if (body == null) body = "";

                        if (body.indexOf("(광고)") < 0) {
                            if (type.equals("text/plain")) {
                                body = body.replaceAll("|", "");
                                body = body.replaceAll("`", "");
                                body = body.replaceAll("`", "");
                                body = body.replaceAll("~", "-");
                                body = body.replaceAll("\"", "");
                                body = body.replaceAll("'", "");
                                body = body.replaceAll("[\\t\\n\\r]", " ");
                                String eaA = Pnum + "||" + MgDate + "|" + body + "`";
                                Log.e("MMSEALIne :", eaA);
                                string += eaA;
                            }
                            cntt++;
                        }
                    } else {
                        break;
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        if (string.equals("")) string = "받은 문자가 없습니다.|&nbsp;|&nbsp;|&nbsp;";
        final String goStrurl = "javascript:SMS_MESSAGELISTS(2,'" + string + "')";
        Log.e("SMS", goStrurl);

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });
            }
        }).start();

        return 0;
    }
    private String getAddress(int id) {
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = MainContext.getContentResolver().query(uriAddress, null, selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return name;
    }
    private String getMmsText(int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = MainContext.getContentResolver().query(uri, null, selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if ("text/plain".equals(type)) {
                        String path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
                        if (path != null) {
                            return path;
                        }
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    private String getPartOfMMS(int mmsID) {
        String selectionPart = "mid=" + mmsID;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = MainContext.getContentResolver().query(uri, null, selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
                    if (path != null) {
                        return path;
                    }
                } while (cursor.moveToNext());
            }
            return null;
        } finally {
            cursor.close();
        }

    }
    private String getMmsType(int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = MainContext.getContentResolver().query(uri, null, selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if (!type.equals("application/smil")) {
                        return type;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    public void sound_record_play(String fileName) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.parse("file://" + fileName);
        String type = "audio/mp3";
        intent.setDataAndType(data, type);
        startActivity(intent);
    }
    public void record_list_getAll() {
    }
    public File[] sortFileList(File[] files, final int compareType) {
        Arrays.sort(files, new Comparator<Object>() {
            @Override
            public int compare(Object object1, Object object2) {
                String s1 = "";
                String s2 = "";
                if (compareType == COMPARETYPE_NAME) {
                    s1 = ((File) object1).getName();
                    s2 = ((File) object2).getName();
                } else if (compareType == COMPARETYPE_DATE) {
                    s1 = ((File) object1).lastModified() + "";
                    s2 = ((File) object2).lastModified() + "";
                }
                return s1.compareTo(s2);
            }
        });
        return files;
    }
    public void getfile(File dir, int howmany) {
        File listFile[] = sortFileList(dir.listFiles(), COMPARETYPE_DATE);
        int eaCnt = 0;
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    getfile(listFile[i], howmany);
                } else {
                    if (listFile[i].getName().endsWith(".3gp") || listFile[i].getName().endsWith(".3GP") || listFile[i].getName().endsWith(".AMR") || listFile[i].getName().endsWith(".amr") || listFile[i].getName().endsWith(".mp3") || listFile[i].getName().endsWith(".MP3") ||
                            listFile[i].getName().endsWith(".ogg") || listFile[i].getName().endsWith(".OGG")) {
                        fileList.add(listFile[i]);
                        eaCnt++;
                    }
                }
                if (eaCnt > howmany) break;
            }
        }
        if (eaCnt > recod_getLast) {
            recod_getLast = eaCnt;
            SharedPreferences Axxx = getSharedPreferences("SkyCallGcm", MODE_PRIVATE);
            SharedPreferences.Editor editor = Axxx.edit();
            editor.putString("record_path_last", dir + "");
            editor.commit();
        }
    }
    public void record_list_get(int howmany) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == -1) {
            //if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.READ_EXTERNAL_STORAGE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1009);
            }
            Toast.makeText(this.getApplicationContext(), "녹음파일 목록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        //record_path_last = "";
        //recod_getLast = 0;
        SharedPreferences mGcmREad = getSharedPreferences("SkyCallGcm", MODE_PRIVATE);
        record_path_last = mGcmREad.getString("record_path_last", "");
        fileList = new ArrayList<File>();
        fileList_last = new ArrayList<File>(); //ArrayList<File>
        recod_getLast = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
        File root;
        File Base_root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File testF = new File(Base_root + "/Android/data/com.globaleffect.callrecord/files");
        File testF2 = new File(Base_root + "/com.globaleffect.callrecord/files");
        if (testF2.isDirectory()) getfile(testF2, howmany);
        if (fileList.size() < 2) {
            if (testF.isDirectory()) getfile(testF, howmany);
        }
        if (fileList.size() < 2) {
            record_path_last = "";
            if (record_path_last.length() > 3) {
                root = new File(record_path_last);
            } else {
                root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } else {

        }
        String string = "";
        int cnt = 0;

        if (fileList.size() < howmany) {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String projection[] = {android.provider.MediaStore.Audio.Media.DATE_ADDED, android.provider.MediaStore.Audio.Media.DATA, android.provider.MediaStore.Audio.Media.DATE_MODIFIED};
            ContentResolver cr = getContentResolver();
            //String selection = MediaStore.Audio.Media.DATE_ADDED +" > ?";
            String selection = MediaStore.Audio.Media.DATE_ADDED + "";
            String before24hour = ((new Date().getTime() - (60 * 60 * 24 * 1000)) / 1000) + "";
            String[] selectionArgs = {before24hour};
            String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
            Cursor cursor = cr.query(uri, projection, selection, null, sortOrder); //DATE_ADDED
            InputStream is = null;
            while (cursor.moveToNext()) {
                if (cnt < howmany) {
                    cnt++;
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    Uri pathea = Uri.fromFile(new File(path));
                    String filename = pathea.getLastPathSegment().toString();
                    String FullPath = pathea.toString();

                    File file = new File(path);
                    long n = file.lastModified();
                    String modff2 = make_time(n);
                    string += filename + "|" + FullPath + "|" + modff2 + "`";
                }
                //Log.e("RecordFn " , "filePath = " + filePath + " / fileName=" + fileName + " /extension= "+ extension);
            }
            cursor.close();
        } else {
            Collections.reverse(fileList);
            Log.e("fileList Size=", "" + fileList.size() + " 개");
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList_last.size() < howmany) {
                    //Log.e("ADD", "folder = " + fileList.get(i).getPath() + " /file =  " + fileList.get(i).getName() + " /date = " + make_time(fileList.get(i).lastModified()));
                    fileList_last.add(fileList.get(i));
                    cnt++;
                    string += "" + fileList.get(i).getName() + "|file://" + fileList.get(i).getPath() + "|" + make_time(fileList.get(i).lastModified()) + "`";
                }
                //System.out.println(fileList.get(i).getName());
            }
        }

        final String goStrurl = "javascript:Record_listshow('" + string + "')";
        Log.e("Record_listshow:", string);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }
    public String name, id, phNo, phDisplayName, phType;
    public boolean isChecked = false;
    public void readContacts(int howmany) {
        Log.e("거래처목록", "T:" + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CONTACTS}, 1007);
            }
            Toast.makeText(this.getApplicationContext(), "주소록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

        int num = 0;
        int kxx = 0;
        String string = "";
        HashMap<Long, String> groupList = new HashMap<Long, String>();
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI, new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE}, null, null, null);
        int c_id = c.getColumnIndex(ContactsContract.Groups._ID);
        int c_name = c.getColumnIndex(ContactsContract.Groups.TITLE);
        ArrayList<ArrayList<String>> mGroupList = new ArrayList<ArrayList<String>>();
        ArrayList<String> mChildList = new ArrayList<String>();
        ArrayList<String> done_reglist = new ArrayList<String>();


        String wherenul = " ";
        while (c.moveToNext()) {
            groupList.put(c.getLong(c_id), c.getString(c_name));
            String gid = String.valueOf(c.getLong(c_id));
            String GroupName = c.getString(c_name);
            mChildList = new ArrayList<String>();
            mChildList.add(gid);
            mChildList.add(GroupName);
            mGroupList.add(mChildList);
            wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '" + gid + "' AND ";

            Log.e("Ea", gid + " / " + GroupName);
        }
        mChildList = new ArrayList<String>();
        mChildList.add("0");
        mChildList.add("미지정");
        mGroupList.add(mChildList);

        wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '96325874' ";
        mChildList.add("0");
        mChildList.add("Endifitxjunyok");
        mGroupList.add(mChildList);
        c.close();
        for (int i = 0; i < mGroupList.size(); i++) {
            String groupId = mGroupList.get(i).get(0);
            String GroupName = mGroupList.get(i).get(1);
            if (!GroupName.equals("Endifitxjunyok")) {
                String where = "";
                String[] projection = null;
                if (GroupName.equals("미지정")) {
                    where = wherenul; //ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE +" = '" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE+"' " +
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                } else {
                    where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId + " AND " + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                }
                Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, where, null, ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                //Log.e("Sql", where );
                Log.e("내부 연락처:", GroupName + " : " + cursor.getCount());
                // Log.e("Group 타입", GroupName);
                while (cursor.moveToNext()) {
                    String user_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID));
                    Cursor phoneFetchCursor = null;

                    if (GroupName.equals("미지정")) {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    } else {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    }
                    while (phoneFetchCursor.moveToNext()) {
                        String PhoneNumber = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String user_disName = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneType = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (done_reglist.indexOf(PhoneNumber) < 0) {
                            Log.e("내부 연락처:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                            string += user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`";
                            done_reglist.add(PhoneNumber);
                        } else {
                            Log.e("내부 연락처 등록된것 제외:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                        }
                    }
                    phoneFetchCursor.close();
                }
                cursor.close();
            }
        }
        final String goStrurl = "javascript:ContractList('" + string + "')";
        Log.e("ContractList = :", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE); //GONE
                    }
                });
            }
        }).start();
    }
    public void getContract(){

    }
    public void readContacts_detail(int howmany) {
        Log.e("거래처목록", "T:" + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CONTACTS}, 1007);
            }
            Toast.makeText(this.getApplicationContext(), "주소록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        int istest = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.textInfos).setVisibility(View.VISIBLE);
                        final TextView mTextViewT = (TextView) findViewById(R.id.callingInfoshow);
                        mTextViewT.setText("불러오는중");

                    }
                });
            }
        }).start();


        //if( istest == 1 )return ;

        int num = 0;
        int kxx = 0;
        String string = "";
        HashMap<Long, String> groupList = new HashMap<Long, String>();
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI,
                new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE}, null, null, null);
        int c_id = c.getColumnIndex(ContactsContract.Groups._ID);
        int c_name = c.getColumnIndex(ContactsContract.Groups.TITLE);
        ArrayList<ArrayList<String>> mGroupList = new ArrayList<ArrayList<String>>();
        ArrayList<String> mChildList = new ArrayList<String>();
        ArrayList<String> done_reglist = new ArrayList<String>();


        String wherenul = " ";
        String regGrop = "";
        int onceNogroup = 0;
        String LastInfo = "";
        String ReadNumbers = "";
        while (c.moveToNext()) {
            groupList.put(c.getLong(c_id), c.getString(c_name));
            String gid = String.valueOf(c.getLong(c_id));
            String GroupName = c.getString(c_name);
            if (regGrop.indexOf(GroupName) < 0) {
                regGrop += GroupName + "|";
                mChildList = new ArrayList<String>();
                mChildList.add(gid);
                if( gid.equals("0")) onceNogroup = 1;
                mChildList.add(GroupName);
                mGroupList.add(mChildList);
                wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '" + gid + "' AND ";
                //Log.e("Ea", gid + " / " + GroupName);
            }
        }
        if (onceNogroup == 0) {
            mChildList = new ArrayList<String>();
            mChildList.add("0");
            mChildList.add("미지정");
            mGroupList.add(mChildList);
        }

        wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '96325874' ";

        c.close();
        int gSize = mGroupList.size();
        //Log.e("mGroupList", mGroupList.toString());
        int goTot = 0;
        int dupTot = 0;
        for (int i = 0; i < mGroupList.size(); i++) {
            String groupId = mGroupList.get(i).get(0);
            String GroupName = mGroupList.get(i).get(1);
            if (!GroupName.equals("Endifitxjunyok")) {
                String where = "";
                String[] projection = null;
                if (groupId.equals("0")) {
                    where = wherenul; //ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE +" = '" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE+"' " +
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                } else {
                    where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId + " AND " + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                }

                Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                        projection, where, null, ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
               //Log.e("where", where);
                //Log.e("그룹명:", GroupName + " : 그룹내연락처 " + cursor.getCount());
                //Log.e("내부 연락처:", GroupName + " : " + cursor.getCount());
                int eaCnt = cursor.getCount();
                int eaRP = 0;
                while (cursor.moveToNext()) {
                    String user_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID));
                    Cursor phoneFetchCursor = null;
                    goTot++;
                    if (GroupName.equals("미지정")) {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    } else {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    }
                    while (phoneFetchCursor.moveToNext()) {
                        String PhoneNumber = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String user_disName = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneType = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        eaRP++;
                        ReadNumbers += goTot + "," + GroupName + "," + user_disName + "," + PhoneNumber + "\n";
                        String eaInfo = "내 연락처 불러오는중" +
                                "\n\n\n그룹명:" + GroupName + "(" + i + "/" + gSize + ")" +
                                "\n\n이름:" + user_disName + "" +
                                "\n연락처:" + PhoneNumber + "\n\n";

                        LastInfo = eaInfo + "중복갯수" + dupTot;
                        if (done_reglist.indexOf(PhoneNumber) < 0) {
                            Log.e("내부 연락처:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                            string += user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`";
                            done_reglist.add(PhoneNumber);
                            //eaInfo+= "|A";

                            eaInfo += "\n\n";
                        } else {
                            //eaInfo+= "|X";
                            dupTot++;
                            Log.e("내부 연락처 등록된것 제외:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                            eaInfo += "다른그룹에있는 중복된번호(중복갯수" + dupTot + ")\n\n";
                        }
                        eaInfo += "불러오기는 10분이상 걸릴 수 있습니다.\n";
                        //callingInfoshow
                        final String textShow = eaInfo;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final TextView mTextViewB = (TextView) findViewById(R.id.callingInfoshow);
                                        mTextViewB.setText(textShow);
                                    }
                                });
                            }
                        }).start();
                    }
                    phoneFetchCursor.close();
                }
                cursor.close();
            }
        }

        final String goStrurl = "javascript:ContractList('" + string + "')";
        Log.e("ContractList = :", goStrurl);
        Log.e("LastInfo", LastInfo);
        Log.e("ReadNumbers", ReadNumbers);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.textInfos).setVisibility(View.GONE); //GONE
                    }
                });
            }
        }).start();

    }
    public void readContacts_detail_old(int howmany) {
        Log.e("거래처목록", "T:" + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CONTACTS}, 1007);
            }
            Toast.makeText(this.getApplicationContext(), "주소록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        int istest =1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.textInfos).setVisibility(View.VISIBLE);
                        final TextView mTextViewT = (TextView) findViewById(R.id.callingInfoshow);
                        mTextViewT.setText("불러오는중");

                    }
                });
            }
        }).start();


        //if( istest == 1 )return ;

        int num = 0;
        int kxx = 0;
        String string = "";
        HashMap<Long, String> groupList = new HashMap<Long, String>();
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI, new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE}, null, null, null);
        int c_id = c.getColumnIndex(ContactsContract.Groups._ID);
        int c_name = c.getColumnIndex(ContactsContract.Groups.TITLE);
        ArrayList<ArrayList<String>> mGroupList = new ArrayList<ArrayList<String>>();
        ArrayList<String> mChildList = new ArrayList<String>();
        ArrayList<String> done_reglist = new ArrayList<String>();


        String wherenul = " ";
        String regGrop = "";
        while (c.moveToNext()) {
            groupList.put(c.getLong(c_id), c.getString(c_name));
            String gid = String.valueOf(c.getLong(c_id));
            String GroupName = c.getString(c_name);
            if( regGrop.indexOf(GroupName) < 0 ){
                regGrop+=GroupName + "|";
                mChildList = new ArrayList<String>();
                mChildList.add(gid);
                mChildList.add(GroupName);
                mGroupList.add(mChildList);
                wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '" + gid + "' AND ";
                Log.e("Ea", gid + " / " + GroupName);
            }
        }
        if( regGrop.indexOf("미지정") < 0 ) {
            mChildList = new ArrayList<String>();
            mChildList.add("0");
            mChildList.add("미지정");
            mGroupList.add(mChildList);
        }

        wherenul += ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " NOT LIKE '96325874' ";
        mChildList.add("0");
        mChildList.add("Endifitxjunyok");
        mGroupList.add(mChildList);
        c.close();
        int gSize = mGroupList.size();
        for (int i = 0; i < mGroupList.size(); i++) {
            String groupId = mGroupList.get(i).get(0);
            String GroupName = mGroupList.get(i).get(1);
            if (!GroupName.equals("Endifitxjunyok")) {
                String where = "";
                String[] projection = null;
                if (GroupName.equals("미지정")) {
                    where = wherenul; //ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE +" = '" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE+"' " +
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                } else {
                    where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId + " AND " + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                    projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
                }
                Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, where, null, ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                //Log.e("Sql", where );
                Log.e("내부 연락처:", GroupName + " : " + cursor.getCount());
                // Log.e("Group 타입", GroupName);
                while (cursor.moveToNext()) {
                    String user_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID));
                    Cursor phoneFetchCursor = null;

                    if (GroupName.equals("미지정")) {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    } else {
                        phoneFetchCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    }
                    while (phoneFetchCursor.moveToNext()) {
                        String PhoneNumber = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String user_disName = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneType = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        String eaInfo = "내 연락처 불러오는중\n\n\n그룹명:" + GroupName + "(" + i + "/" + gSize + ")\n\n\n이름:" + user_disName + "\n연락처:" + PhoneNumber + "\n\n\n\n\n\n";
                        eaInfo += "불러오기는 10분이상 걸릴 수 있습니다.\n\n";
                        if (done_reglist.indexOf(PhoneNumber) < 0) {
                            Log.e("내부 연락처:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                            string += user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`";
                            done_reglist.add(PhoneNumber);
                            //eaInfo+= "|A";

                        } else {
                            //eaInfo+= "|X";
                            Log.e("내부 연락처 등록된것 제외:", user_disName + "|" + PhoneNumber + "|0|" + GroupName + "|" + groupId + "`");
                        }
                        //callingInfoshow
                        final String textShow = eaInfo;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final TextView mTextViewB = (TextView) findViewById(R.id.callingInfoshow);
                                        mTextViewB.setText(textShow);
                                    }
                                });
                            }
                        }).start();
                    }
                    phoneFetchCursor.close();
                }
                cursor.close();
            }
        }
        final String goStrurl = "javascript:ContractList('" + string + "')";
        Log.e("ContractList = :", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.textInfos).setVisibility(View.GONE); //GONE
                    }
                });
            }
        }).start();
    }
    private void myPhoneCallLog(int howmany) {/** * Calls.TYPE {1=>수신,2=>송신,3=>부재중전화,5=>수신거부*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CALL_LOG}, 1005);
            }

            Toast.makeText(this.getApplicationContext(), "통화목록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
        Cursor callLogCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
        int type = callLogCursor.getColumnIndex(CallLog.Calls.TYPE);
        String string = "";
        int count = 0;
        while (callLogCursor.moveToNext()) {
            count++;
            if (count < howmany) {
                String name = callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                if (name == null) name = "";
                name = name.replace("|", "");
                name = name.replace("`", "");

                String cacheNumber = callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                String number = callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.NUMBER));
                long dateTimeMillis = callLogCursor.getLong(callLogCursor.getColumnIndex(CallLog.Calls.DATE));
                String MgDate = make_time_str(dateTimeMillis);
                String callType = callLogCursor.getString(type);
                String dir = "";
                int callcode = Integer.parseInt(callType);
                switch (callcode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "발신";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "수신";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        dir = "부재";
                        break;
                }

                Log.e("CALL:", name + " / " + cacheNumber + " / " + number + " / " + MgDate + " / " + callType + " / " + dir);

                string += number + "|" + name + "|" + MgDate + "|" + dir + "`";
            }

        }
        callLogCursor.close();
        final String goStrurlOK = "javascript:CALL_LISTS_job('" + string + "')";
        Log.e("CALL_LISTS:", string);


        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurlOK);
            }
        });

    }
    public String make_time_str(long eatime) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(eatime);
        //String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
        SimpleDateFormat an = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = DateFormat.format("yyyy-MM-dd hh:mm:ss a", cal).toString();

        return date;

    }
    public String make_time(long eatime) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(eatime);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
        return date;
    }
    public void record_list_getxx(int howmany) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == -1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1009);
            }

            Toast.makeText(this.getApplicationContext(), "녹음파일 목록을 읽을 권한을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String string = "";
        int cnt = 0;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        final String[] projection = new String[]{
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getBaseContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
        try {
            if (cursor != null) {
                //ArrayList<File> fileList = new ArrayList<File>();
                ArrayList<genericSongClass> songs = new ArrayList<genericSongClass>(cursor.getCount());
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    genericSongClass GSC = new genericSongClass();
                    GSC.songTitle = cursor.getString(0);
                    GSC.songArtist = cursor.getString(1);
                    GSC.songData = cursor.getString(2);

                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    Uri pathea = Uri.fromFile(new File(path));
                    String filename = pathea.getLastPathSegment().toString();
                    String FullPath = pathea.toString();

                    File file = new File(path);
                    long n = file.lastModified();
                    String modff2 = make_time(n);

                    Log.e("date assed", "fn :" + path + " / " + filename + " / " + modff2);
                    string += filename + "|" + FullPath + "|" + modff2 + "`";

                    songs.add(GSC);
                    cursor.moveToNext();
                }
            }
        } catch (Exception ex) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }
    public class genericSongClass {
        String songTitle = "";
        String songArtist = "";
        String songData = "";
        String isChecked = "false";
    }
    public String MainURLGET() {
        return goUrl;
    }
    private void clearApplicationCache(java.io.File dir) {
        if (dir == null)
            dir = getCacheDir();
        else ;
        if (dir == null)
            return;
        else ;
        java.io.File[] children = dir.listFiles();
        try {
            for (int i = 0; i < children.length; i++)
                if (children[i].isDirectory())
                    clearApplicationCache(children[i]);
                else children[i].delete();
        } catch (Exception e) {
        }
    }
    @TargetApi(21)
    private void flushCookies() {
        CookieManager.getInstance().flush();
    }
    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GCMIntentService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void GPS_Reload_do() {
        if (Build.VERSION.SDK_INT >= 23) {
            askUserToOpenGPS();
        }
        gps.getLocation();
        get_send_Gps();
    }
    public void askUserToOpenGPS() {
    }
    private void saveGPS() {
        Log.i("saveGPS", "saveGPS start");
        SharedPreferences userDetails = getApplicationContext().getSharedPreferences("userInfo_apset", MODE_PRIVATE);
        String member_Num = userDetails.getString("USERNUMBER", "");
        String GPSGap = userDetails.getString("USERNUGPSGABMBER", "");
        userNumberN = Integer.parseInt(member_Num);
        double latitude = 0;
        double longitude = 0;

        try {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            userAddress = gps.getAddress(latitude, longitude);
            isGPS = gps.getGpsType();

        } catch (Exception e) {
        }
        //Log.i("apsetINfos", "apsetINfos before : ");

        userGPS = latitude + "|" + longitude;

        Boolean A = isNetworkConnected();
        if (A != true) return;
        new MyAsyncTask_GPS().execute("Go");
    }
    public class MyAsyncTask_GPS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... passing) {
            //HttpClient httpclient = HttpClients.createDefault();
            HttpClient httpclient = new DefaultHttpClient();
            String go_UrlNew = "http://jbbuller.kr/_app_location_refresh_save_re2.php";
            HttpPost httppost = new HttpPost(go_UrlNew);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_num", userNumberN + ""));
                nameValuePairs.add(new BasicNameValuePair("userGPS", userGPS + ""));
                nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));
                nameValuePairs.add(new BasicNameValuePair("gps_address", userAddress));
                nameValuePairs.add(new BasicNameValuePair("isGps", isGPS + ""));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                httppost.setEntity(ent);
                HttpResponse response = httpclient.execute(httppost);
                //Log.i("saveGPS", "goStrurl : " + userNumberN + " / userGPS : " + userGPS + " URL :" + go_UrlNew + "/userAddress=" + userAddress);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
                Log.i("saveGPS", "errrrrrrrrrrrrrrrrror : " + userNumberN + " / userGPS : " + userGPS);
            }
            return null;
        }

        protected void onPostExecute(ArrayList<String> result) {
        }

        protected void onProgressUpdate(Integer... progress) {
        }
    }
    public void initPageDo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.imageLoading1).setVisibility(View.GONE);
                        //show webview
                        findViewById(R.id.webview).setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }
    private void Video_play(String vfn) {
        Intent intent = new Intent(this, VideoPlayer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = new Bundle();
        b.putString("vfn", vfn);
        intent.putExtras(b);
        startActivity(intent);
    }
    public void play_snd_test(String fn) {
        Log.e("재생시작 : ", "파일:" + fn);
        int resID = getResources().getIdentifier(fn, "raw", getPackageName());
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), resID);
        mediaPlayer.start();
    }
    public void play_snd_save(String fn) {
        //Log.e("푸시소리 저장중 : ", "파일:" + fn);
        SharedPreferences userDetails = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.putString("soundFn", fn);
        editor.apply();
        mDbOpenHelper.SetSettingvalue("seting_5", fn);
        SharedPreferences userDetails_after = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
        String pushFn = userDetails_after.getString("soundFn", "0");
        //Log.e("저장결과 : ", "파일:" + pushFn);

        Intent serviceIntent = new Intent(this, AndroidLocationServices.class);
        serviceIntent.putExtra("soundPushFn", fn);
        this.startService(serviceIntent);
    }
    public void play_snd_save_both(String fn, String notifn) {
        Log.e("푸시소리 저장중 : ", "매칭소리:" + fn + " / 알림소리 : " + notifn);
        SharedPreferences userDetails = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.putString("soundFn", fn);
        editor.putString("notiFn", notifn);
        editor.apply();
        mDbOpenHelper.SetSettingvalue("seting_5", fn);
        mDbOpenHelper.SetSettingvalue("seting_9", notifn);

        //String matchingString= "AAA";
        //String pushString = "BBB";
        String UpGo1 = "Update LoginInfoSaved set fd_value='" + fn + "' where fd_title='mobile_push_matching'";
        String OK = mDbOpenHelper.LoginDbInputUpdate(UpGo1);
        String UpGo2 = "Update LoginInfoSaved set fd_value='" + notifn + "' where fd_title='mobile_push_notification'";
        OK = mDbOpenHelper.LoginDbInputUpdate(UpGo2);
        Log.e("LOGINFOUPDATE", UpGo1);
        Log.e("LOGINFOUPDATE", UpGo2);
        SharedPreferences userDetails_after = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
        String pushFn = userDetails_after.getString("soundFn", "0");
        //Log.e("저장결과 : ", "파일:" + pushFn);

        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String readspeed_Set = getDbInfo[5];
        String readPitch_Set = getDbInfo[9];
        String chval1 = mDbOpenHelper.get_App_loginInfo_Each("mobile_push_matching");
        String chval2 = mDbOpenHelper.get_App_loginInfo_Each("mobile_push_notification");

        Log.e("소리 파일설정 저장 후 ", "매칭소리 : " + readspeed_Set + " / 알림소리 :" + readPitch_Set + " 매칭소리DB : " + chval1 + " / 알림소리DB :" + chval2);
    }
    public void Session_key_valueSetTo(String keyTitle, String keyValue) {
        Log.e("세션키값실시간변경 : ", "keyTitle:" + keyTitle + " / keyValue : " + keyValue);
        String UpGo1 = "Update LoginInfoSaved set fd_value='" + keyValue + "' where fd_title='"+keyTitle+"'";
        String OK = mDbOpenHelper.LoginDbInputUpdate(UpGo1);
        Log.e("LOGINFOUPDATE", UpGo1);
        String chval1 = mDbOpenHelper.get_App_loginInfo_Each(keyTitle);
        Log.e("세션변경후 ", "keyTitle : " + chval1);
    }
    public void read_voiceSetting() {
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String readspeed_Set = getDbInfo[6];
        String readPitch_Set = getDbInfo[7];
        //Log.e("읽기속도 저장 후 ", "readspeed_Set : " + readspeed_Set + " / readPitch_Set :" + readPitch_Set);
        final String goStrurl = "javascript:push_readSetAfter('" + readspeed_Set + "','" + readPitch_Set + "')";
        //Log.e("읽기소고저장후", "결과 :" + goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void sendPhone_number_To_web(String pno){
        final String goStrurl = "javascript:phone_number_exchange('" + pno + "')";
        //Log.e("전화번호전달", goStrurl);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });
    }
    public void read_textLong(String text){
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        float readspeed_Set = (float) 1.0;
        float readPitch_Set = (float) 1.0;
        String readspeed = getDbInfo[6];
        String readPitch = getDbInfo[7];
        if( readspeed.length() > 0 ) readspeed_Set = (float) Double.parseDouble(readspeed);
        if( readPitch.length() > 0 ) readPitch_Set = (float) Double.parseDouble(readPitch);
        Intent speechIntent = new Intent(MainContext, MyTell.class);
        speechIntent.putExtra("readmessage", text);
        speechIntent.putExtra("readspeed_Set", Float.toString(readspeed_Set)); // mTts.setSpeechRate(readspeed_Set);// 0.995
        speechIntent.putExtra("readPitch_Set", Float.toString(readPitch_Set)); //mTts.setPitch(readPitch_Set); // 1.0
        Log.e("문자읽어주기",  "push_speechset : " + readspeed_Set + " | readPitch_Set : " + readPitch_Set);
        Log.e("문자읽어주기",  "TEXT : " + text);
        MainContext.startService(speechIntent); // if(!ison) mTts.speak(TTS_message, TextToSpeech.QUEUE_FLUSH,null);
    }
    public void gps_StopOnce(){
        gps.stopUsingGPSs();
    }
    public  void get_app_infos_jobDo(int useNum){
        String md = android.os.Build.MODEL;
        int BuildVer = Build.VERSION.SDK_INT;
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        final String goStrurl = "javascript:user_version_infoSave('" + useNum + "','" + versionName + "')";
        //Log.e("get_app_infos_jobDo",goStrurl + "/" + versionName);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(goStrurl);
            }
        });

    }
    private void popupPageShow(String url) {
        Intent intent = new Intent(this, popup_page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mDbOpenHelper.SetSettingvalue("seting_10", url);
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String goUrl = getDbInfo[10];
        Bundle b = new Bundle();
        intent.putExtras(b);
        startActivity(intent);
    }
    public String set_user_setting(String setname, String setvalue){
        SharedPreferences pref = getSharedPreferences("user_appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(setname, setvalue);
        Log.e("설정값 저장", "설정키 :" + setname + " / 설정값:" + setvalue);
        editor.apply();
        return "OK";
    }
    public String get_user_setting(String setname){
        SharedPreferences pref = getSharedPreferences("user_appSettings", Context.MODE_PRIVATE);
        String eaValue = pref.getString(setname, "0");
        return eaValue;
    }
    public void play_snd_check_do(String setname, String fn){
        String readSetting = get_user_setting(setname);
        Log.e("설정값읽기", "설정값:" + readSetting);
        if( readSetting.equals("NO")) return;
        int resID = getResources().getIdentifier(fn , "raw", getPackageName());
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), resID);
        mediaPlayer.start();
    }
    public String getFileName(String url) {
        String filenameWithoutExtension = "";
        String Extens = ".jpg";
        if(url.toLowerCase().endsWith(".png")) Extens = ".png";
        filenameWithoutExtension = String.valueOf(System.currentTimeMillis() + Extens);
        return filenameWithoutExtension;
    }
    private class MyWebViewClient extends WebViewClient {
        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";
        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event){
            Log.e("MyWebViewClient", "onUnhandledKeyEvent :" + event.toString());
            return;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("ssss","URL:" + url);
            //Log.e("Current","URL : " + page_URLNow );
            isRestarting = 0;
            if (url.startsWith("http://")) {
                if(url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".png")) {
                }
                if(url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".png") ) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == -1) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1232);
                        }

                        alertt("임시 파일 쓰기 권한이 거부되어 진행할 수 없습니다.");
                        return false;
                    }
                    DownloadManager mdDownloadManager = (DownloadManager) MainContext.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    File destinationFile = new File(Environment.getExternalStorageDirectory(), getFileName(url));
                    request.setDescription("받기중 ...");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationUri(Uri.fromFile(destinationFile));
                    mdDownloadManager.enqueue(request);
                    alertt("다운로드 시작");
                    return true;


                }else if(url.startsWith("http://jbbuller.kr/") || url.startsWith("http://sub.jbbuller.kr/")) {
                    Log.e("불러자체" , "return Trum do");
                    mWebView.loadUrl(url);
                    return true;

                }else{
                    Log.e("httpNotdown" , "return False do");
                    return false;
                }
                //mWebView.loadUrl(url);

            } else {
                boolean override = false;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                if (url.startsWith("sms:")) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if (url.startsWith("tel:")) {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if (url.startsWith("mailto:")) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if (url.startsWith(INTENT_PROTOCOL_START)) {
                    final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                    final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                    if (customUrlEndIndex < 0) {
                        return false;
                    } else {
                        final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                        try {
                            //getBaseContext().startActivity(sharingIntent, Uri.parse(customUrl)));

                            Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
                            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            sharingIntent.setData(Uri.parse(customUrl));
                            Intent chooserIntent = Intent.createChooser(sharingIntent, "Open With");
                            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(chooserIntent);

                        } catch (ActivityNotFoundException e) {
                            final int packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length();
                            final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);
                            final String packageName = url.substring(packageStartIndex, packageEndIndex < 0 ? url.length() : packageEndIndex);
                            getBaseContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                        }
                        return true;
                    }
                }

                try {
                    startActivity(intent);
                    override = true;
                } catch (ActivityNotFoundException e) {
                    return override;
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            //Log.e("webviewfinished", "URL :" + url + " 로딩완료");
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
    private void iconNumRest() {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", 0);

        String launcherClassName = getLauncherClassName(this);
        intent.putExtra("badge_count_package_name", this.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        this.sendBroadcast(intent);
        if(mDbOpenHelper ==null) mDbOpenHelper = new DbOpenHelper(MainContext);
        if( DbOnceInited != 1 ){
            try {
                mDbOpenHelper.init_DB(0);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            DbOnceInited =1;
        }
        mDbOpenHelper.SetSettingvalue("seting_8", "0");
        //Log.e("뱃지리셋", "BADGE_COUNT_UPDATE BADGE_COUNT_UPDATE");
        Intent Inew = new Intent(MainContext, AndroidLocationServices.class);
        Inew.putExtra("noticejob", "notiCntsetzero");
        MainContext.startService(Inew);

    }
    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }
    public void check_Margin(){
        WebView track =(WebView) findViewById(R.id.webview);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)track.getLayoutParams();
        int marginTopPixelSize = params.topMargin;
        Log.e("웹뷰마진", "topMargin:" + marginTopPixelSize );
    }
    public void clear_loading(){
        new Thread(new Runnable() {
            @Override
            public void run() {runOnUiThread(new Runnable(){
                @Override
                public void run() {findViewById(R.id.imageLoading1).setVisibility(View.GONE);}
            });}
        }).start();
        pannel_clearOnce++;
        //Log.e("타이머","로딩바 클리어 완료");
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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            });
            return true;
        }
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100) {
                mProgressBar.setProgress(newProgress * 100);
            } else {
                mProgressBar.setVisibility(View.INVISIBLE);
                if( pannel_clearOnce < 1 ){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Log.e("타이머","로딩바 클리어실행");
                            clear_loading();
                        }
                    }, 5000);
                }
                //check_Margin();
                if( badCompanycheckonce == 0 )check_and_get_badcopany();
                //mProgressBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
        }
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialoPopup);
            builder.setTitle("알림");
            builder.setCancelable(false);
            builder.setMessage(message);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
            builder.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result){
            new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogStyle)
                    .setTitle("선택하기")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .create()
                    .show();
            return true;
            //alert.show();
        }
    }
    private void call_to(String Pnum) {
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
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((netInfo != null) && netInfo.isConnected());
    }
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }
    private void sendRegistrationIdToBackend() {}
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }
    private void addShortcut(int shotType) {
        SharedPreferences pref = getSharedPreferences("shortCutInfo", MODE_PRIVATE);
        String install_CH = pref.getString("shortCut", "");//
        if (!install_CH.equals("OKSky") || shotType == 9 ) {
            Intent shortcutIntent = new Intent();
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent.setClassName(this.getApplicationContext(), getClass().getName());
            //shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Parcelable iconResource;
            iconResource = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "장비불러");
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
            intent.putExtra("duplicate", false);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            sendBroadcast(intent);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("shortCut", "OKSky");
            editor.apply();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isPopUp = is_PopQuieOn();
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backTime;
        Log.e("onKeyDown", "PopCheck " + isPopUp + "intervalTime = " + intervalTime + " / " + backTime + "/ tempTime:" + tempTime );
        if (intervalTime < 500 ) {
            Log.e("intervalTimeDo", "intervalTime = " + intervalTime);
            backButtonHandler();
            return false;
        }
        backTime = tempTime;

        List<String> HisVal = new ArrayList<>();
        String LastUrlIn = "";
        int OnceOk = 0;
        int HasMyself = 0;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) return false;

        int webViewAsk = 0;
        if(page_URLNow.equals("sky_2000.php") || page_URLNow.equals("_sky_m_03.php") || page_URLNow.indexOf("sky_m_05_phtoviewer") > 0) webViewAsk = 1;
        if(page_URLNow.equals("_sky_m_06.php")) webViewAsk = 1;
        if(page_URLNow.equals("_sky_m_01_chat_do.php") ) webViewAsk = 1;
        if(page_URLNow.equals("_sky__m_menu_setting_003.php") ) webViewAsk = 1;
        if(page_URLNow.equals("_sky_m_05_0A.php") || page_URLNow.equals("_sky_m_05.php")) webViewAsk = 1;
        List<String> expUrls = Share_utils.return_Data;
        //Log.e("ret_datas", "Length:" + expUrls.size() + ", " + expUrls.get(0) + "/" + expUrls.toString());
        if( expUrls.size() > 1 ){
            webViewAsk = 0;
            for(int x =0; x < expUrls.size(); x++){
                String ea = expUrls.get(x);
                if(page_URLNow.equals(ea)) webViewAsk = 1;
                //Log.e("EaChecker", "N:" + x + " / " + ea + ":" + page_URLNow + " |" +  webViewAsk);
            }
        }
        Log.e("뒤로가기버튼", "page_URLNow:" + page_URLNow + " webViewAsk:" + webViewAsk + " AppSideOn:" + AppSideOn );

        if( AppSideOn.equals("ON")){
            final String goStrurl = "javascript:hideSideCh(null)";
            Log.e("뒤로가기", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return true;
        }

        if ( webViewAsk == 1 ) {
            final String goStrurl = "javascript:device_backButtonCall()";
            Log.e("뒤로가기", goStrurl);

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return false;
        }

        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            WebBackForwardList webBackForwardList = mWebView.copyBackForwardList();
            String backUrl = "";
            String OtherUrl = "";
            Log.e("webBackForwardList", webBackForwardList.toString());
            if(webBackForwardList.getSize() > 0 ){
                for( int xx = 1; xx < webBackForwardList.getSize() ; xx++) {
                    try {
                        String EabackUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - xx).getUrl();
                        if (EabackUrl.indexOf(page_URLNow) > 1) {
                            HasMyself = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //if( HasMyself == 0) OnceOk = 1;
                for( int x = 1; x < webBackForwardList.getSize() ; x++){
                    try{
                        String EabackUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - x).getUrl();
                        if( x > 1 && ( ! EabackUrl.equals("http://sub.jbbuller.kr/_sky_m_05_0A.php") && ! EabackUrl.equals("http://sub.jbbuller.kr/_sky_m_05.php")) ){
                            if(OtherUrl.equals("") ) OtherUrl = EabackUrl;
                        }
                        Log.e("History", x + ":" + EabackUrl );
                        if( ! EabackUrl.equals("http://jbbuller.kr/_sky_m_00_login.php")){
                            if(EabackUrl.indexOf("_register_order_a_frame.") > 1 || EabackUrl.indexOf("_order_proceed_send_frame.") > 1 || EabackUrl.indexOf("_sky_m_03_ready.") > 1 ){
                            }else{
                                if(HisVal.indexOf(EabackUrl) < 0 && OnceOk > 0 ) {
                                    //Log.e("OnceOk", "OnceOk:" + OnceOk + "/ EabackUrl:" + EabackUrl + " / page_URLNow:" + page_URLNow);
                                    String chUrl = URL_Header + page_URLNow;
                                    if( ! LastUrlIn.equals(EabackUrl) ) {
                                        HisVal.add(EabackUrl);
                                    }
                                }else{
                                    if( EabackUrl.indexOf(page_URLNow) > 1 ) {
                                        //Log.e("현재부터 넣기", "EabackUrl:" + EabackUrl + " / page_URLNow:" + page_URLNow);
                                        OnceOk = 1;
                                    }
                                }


                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            Log.e("HisVal", HisVal.toString() + "/Size:" + HisVal.size());
            if( HisVal.size() > 0 ) {
                Log.e("HisVal List", HisVal.toString());
                Log.e("HisVal ch", "page_URLNow:" + page_URLNow);
                String FUrl = HisVal.get(0);
                if( FUrl.indexOf("_push_p_to_p_new.php") > 1 ){
                    Log.e("HisErr", "FUrl _push_p_to_p_new 한단계전:" + FUrl + "");
                    FUrl = HisVal.get(1);
                }
                if( FUrl.length() > 5 ){
                    final String ackUrl_GO = FUrl;

                    if( FUrl.indexOf("_sky_m_01.php") > 1 ) {
                        Log.e("HisVal", "FUrl:" + FUrl + " clearHistory");
                        mWebView.clearHistory();
                    }else{
                        Log.e("HisVal", "FUrl:" + FUrl);
                    }

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(ackUrl_GO);
                        }
                    });
                    return false;

                }

            }
            if(webBackForwardList.getSize() > 0 ){
                String Url = URL_Header + "_sky_m_01.php";
                backUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1).getUrl();
                if( page_URLNow.equals("_sky_m_05_0A.php")) {
                    if( backUrl.equals("http://jbbuller.kr/_sky_m_01.php")|| backUrl.equals("http://sub.jbbuller.kr/_sky_m_05_0A.php")) {
                        if(webBackForwardList.getSize() > 2 ){
                            Url = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 2).getUrl();
                            Log.e("History2단계", Url );
                        }
                        if( Url.equals("http://sub.jbbuller.kr/_sky_m_05_0A.php") && OtherUrl.length() > 10 )Url = OtherUrl;

                    }
                }
                if(backUrl.indexOf("_register_order_a_frame.") > 1 || backUrl.indexOf("_order_proceed_send_frame.") > 1 || backUrl.indexOf("_sky_m_03_ready.") > 1 ){
                    Url = URL_Header + "_sky_m_01.php";
                    Log.e("History Base", backUrl );
                }
                Log.e("StopGet : ", "현재페이즌 :  " + page_URLNow + " / " + Url + " /뒤로순서:" + mWebView.canGoBack());
                if( ! Url.equals("http://jbbuller.kr/_sky_m_01.php") && ! page_URLNow.equals("_sky_m_01.php") ){
                    final String ackUrl_GO = Url;
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(ackUrl_GO);
                        }
                    });
                    return false;
                }


            }else{
                Log.e("History", "사이즈가0");
                backUrl = "";
            }
            Log.e("onKeyDown : ", "현재페이즌 :  " + page_URLNow + " / " + intervalTime + " /뒤로순서:" + mWebView.canGoBack());
            if( TextUtils.isEmpty(backUrl)) backUrl = "";
            int show_ClosePop = 0;
            if(backUrl.indexOf("_sky_m_00_login.php") > 1 || backUrl.length() < 4 )show_ClosePop = 1;




            Log.e("뒤로가기주소", "주소값은 = " + backUrl + " / page_URLNow : "+ page_URLNow);

            if (intervalTime < 500 || page_URLNow.equals("_sky_m_01.php") || page_URLNow.equals("_sky_m_00_login.php") || show_ClosePop > 0 ) {
                Toast.makeText(getApplicationContext(), "뒤로 버튼을 빨리 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                Log.e("BackJob1", "주소값은 = " + backUrl + " / page_URLNow : "+ page_URLNow + "/ show_ClosePop:" + show_ClosePop + "/intervalTime:" + intervalTime);
                backButtonHandler();
                return is_PopQuieOn();
            }

            if( backUrl.length() > 10) {
                if( backUrl.indexOf("_sky_m_03.php") > 0 && page_URLNow.equals("_sky_m_01.php")  ){
                    if(dialog!=null) {
                        if( dialog.isShowing()) return true;
                    }
                    Log.e("BackJob2", "주소값은 = " + backUrl + " / page_URLNow : "+ page_URLNow);
                    backButtonHandler();
                    return false;
                }
                if( backUrl.indexOf("_register_order_") > 0 || backUrl.indexOf("_order_proceed_send") > 0 || backUrl.indexOf("/sky_2000.php") > 0){
                    Log.e("뒤로가기주소", "뒤로가기 멈춤");
                    final String ackUrl_GO = URL_Header + "_sky_m_01.php";
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(ackUrl_GO);
                        }
                    });
                    return false;
                }

            }
            if (0 <= intervalTime || 500 >= intervalTime) {
                Log.e("backUrlLast", "backUrl : " + backUrl ) ;
                if (page_URLNow.equals("_my_message_list.php") || page_URLNow.equals("_sky_m_00_login.php") || page_URLNow.equals("_sky_m_03.php") ) {
                    backButtonHandler();
                    return is_PopQuieOn();
                }
                mWebView.goBack();
                return false;
                // super.onBackPressed();
            } else {
                mWebView.goBack();
                if (intervalTime < 500) {
                    //Toast.makeText(getApplicationContext(), "뒤로 버튼을 빨리 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    backButtonHandler();
                    return false;
                }
            }
            mWebView.goBack();
            return false;
        } else {
            Log.e("backButtonHandler", "뒤로갈게 없어 닫을지 묻기");
            backButtonHandler();
            return is_PopQuieOn();
        }
        // return super.onKeyDown(keyCode, event);
    }
    public boolean is_PopQuieOn(){
        if(Closedialog!=null) {
            if (Closedialog.isShowing()) return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        boolean ok = is_PopQuieOn();
        Log.e("onBackPressed", "onBackPressed : " + ok);
    }
    public AlertDialog Closedialog = null;
    public void backButtonHandler() {
        final String goStrurl = "javascript:app_exit_logsave()";
        boolean ok = is_PopQuieOn();
        Log.e("앱종료알리기", "backButtonHandler : app_exit_logsave" + "/ isPopOn : " + ok);
        if( ok ) return;
        final int mb_no = userNumberN;
        WebView WebV = (WebView) findViewById(R.id.webview);
        if (WebV.getVisibility() == View.VISIBLE) {
            Log.e("웹뷰상태", "보여지고 있음");
        } else {
            Log.e("웹뷰상태", "안 보여지고 있음");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogTheme);
        builder.setTitle("앱 종료하기");
        builder.setCancelable(false);
        builder.setMessage("장비불러를 종료할까요 ?");
        builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity mActivity = MainActivity.this;
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        new finishReport().execute();
                    }
                });
                finish();
                return;
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Closedialog = builder.create();
        Closedialog.show();
        Button bq1 = Closedialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button bq2 = Closedialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        bq1.setTextColor(parseColor("#0080FF"));
        bq2.setTextColor(parseColor("#0080FF"));
        bq1.setTextSize(18);
        bq2.setTextSize(18);
        bq1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bq2.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        Closedialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.e("앱종료알리기", "창이 보이는 상태 그대로 유지하기");

            }
        });
    }
    public void alertt(String text) {
        Log.e("alertt", " text MAIN : " + text);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    private Uri imageFileUri;
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void imgSelAndreay() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == -1) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1231);
            }
            return;
        }

        //final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        final CharSequence[] options = {"카메라촬영", "갤러리에서선택", "취소"};
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("사진 선택 및 촬영");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("카메라촬영")) {
                    dialog.dismiss();
                    doTakePhotoAction();
                } else if (options[item].equals("갤러리에서선택")) {
                    dialog.dismiss();
                    doTakeAlbumAction();
                } else if (options[item].equals("취소")) {
                    dialog.dismiss();
                }
                dialog.dismiss();
            }
        });

        builder.show();

    } ///갤러리 또는 카메라 중 선택
    private void doTakePhotoAction() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Uri contentUri = null;
        Context context = MainActivity.this;
        isCamera_chat = "";
        Intent intent = new Intent(ACTION_IMAGE_CAPTURE); // 카메라 촬영 // camera_job6.equals("nocrop")  camera_job6.equals("crop")
        if( requestUploadJob.equals("stampupload") || camera_job6.equals("crop")){
            /*
            String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"; // 임시로 사용할 파일의 경로를 생성
            mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
            */
            if (Build.VERSION.SDK_INT >= 24) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        return;
                    }
                    if (photoFile != null) {
                        //mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                        mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, "co.kr.jbbuller.provider", photoFile);
                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            MainContext.grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        Log.e("버전24 111", "photoURI : " + mImageCaptureUri);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                        startActivityForResult(intent, DIRECT_SAVE); //PICK_FROM_CAMERA
                    }
                }///////////////////////////////////////////
            }else{
                startActivityForResult(intent, DIRECT_SAVE); //PICK_FROM_CAMERA
            }
            return;
        }else{

        }

        if (Build.VERSION.SDK_INT >= 24) {
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    return;
                }
                if (photoFile != null) {
                    Log.e("AppID Ds" , BuildConfig.APPLICATION_ID);
                    mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, "co.kr.jbbuller.provider", photoFile);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        MainContext.grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    Log.e("버전24 222", "photoURI : " + mImageCaptureUri);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(intent, DIRECT_SAVE);
                }
            }///////////////////////////////////////////
        }else{
            startActivityForResult(intent, DIRECT_SAVE);
        }

    }
    private void doTakeAlbumAction() {// 갤러리 선택// 앨범 호출
        if( requestUploadJob.equals("stampupload")|| camera_job6.equals("crop")){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }else{
            Intent intent = new Intent(this, MultiImageChooserActivity.class);
            Bundle b = new Bundle();
            b.putString("requestUploadJob", requestUploadJob );
            b.putString("userID", userID ); //회원아이디 전번
            b.putString("ordernum", camera_job3 ); //오더번호
            b.putString("phototype", camera_job4 ); //문서 또는 현장사진
            b.putString("mb_no", camera_job5 ); //전송하는 사람 고유번호
            intent.putExtras(b);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                startActivity(intent);
            }
            //finish();

        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    public void deleteUpLoadedFn(String sourceFileUri, int callFrom){
        if (sourceFileUri.length() > 5) {
            File f = new File(sourceFileUri);
            Boolean deleted = f.delete();
            Log.e("임시 파일삭제", "Filename :" + deleted + " / " + sourceFileUri + " / callFrom : " + callFrom);
        }
    }
    public String resizeImg(String path,int iOrder){
        Bitmap resultBitmap = null;
        String orgPath = path;
        ContentResolver resolver = MainActivity.this.getContentResolver();
        Uri uri = Uri.fromFile(new File(path)); ;//Uri.parse(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1800000; // 1.2MP
            in = resolver.openInputStream(uri);
            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }

            in = resolver.openInputStream(uri);
            ExifInterface exif = new ExifInterface(path);
            //int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            String Image_rotate = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = Image_rotate != null ? Integer.parseInt(Image_rotate) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                resultBitmap = BitmapFactory.decodeStream(in, null, options);

                // resize to desired dimensions
                int height = resultBitmap.getHeight();
                int width = resultBitmap.getWidth();
                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x, (int) y, true);
                resultBitmap.recycle();
                resultBitmap = scaledBitmap;
                if( rotationAngle > 0 ) {
                    resultBitmap = rotateImage(resultBitmap,rotationAngle); ////////////이미지 회전
                    height = resultBitmap.getHeight();
                    width = resultBitmap.getWidth();
                }else{
                }

                int ImgQulity = 92;

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, ImgQulity, bytes);
                String[] fex = path.split("\\.");
                String exten = fex[fex.length -1];
                String mid_n = String.valueOf(System.currentTimeMillis());
                File file = new File(Environment.getExternalStorageDirectory(), "/JbBuller/");
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "Uploading canceld(Cound not Make temp folder)", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                String new_Fn = Environment.getExternalStorageDirectory() + File.separator + "JbBuller/mcTempFile" + iOrder + "_" + mid_n + "." + exten;
                File f = new File(new_Fn);
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                String f_ex = "모름";
                if(f.exists()){
                    f_ex = "파일 있음 ";
                }else{
                    f_ex = "파일 없음ㅌㅌ ";
                }
                path = new_Fn;

            } else {
                resultBitmap = BitmapFactory.decodeStream(in);
            }
            in.close();
            return path;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return path;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }
    public static String saveImageInExternalCacheDir(Context context, Bitmap bitmap, String myfileName) {

        String filePath = (context.getExternalCacheDir()).toString() + "/" + myfileName + ".jpg";
        try {
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override public void onResume() {
        Log.e("onResume" , "onResume  ");
        super.onResume();  // Always call the superclass method first
        isAvtivity_On = false;

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public void download_WebLink(){
        try{
            Log.e("DOWNLOAD", "start");
            URL url = new URL("http://j2enty.tistory.com/attachment/cfile24.uf@154AFA254CC9242B3CF889.apk");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.e("DOWNLOAD", "Connect");
            File SDCardRoot = Environment.getExternalStorageDirectory();
            File file = new File(SDCardRoot,"Geocoder_Test.apk");
            FileOutputStream fileOutput = new FileOutputStream(file);
            Log.e("DOWNLOAD", "fileoutput");
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) > 0 ){
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                //mProgressBar.setProgress(downloadedSize);
                Log.e("DOWNLOAD", "saving...");
            }
            fileOutput.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        Log.e("DOWNLOAD", "end");
        Log.e("DOWNLOAD", "InstallAPK Method Called");
        installAPK();
    }
    public void webView_zoomSet(int n) {
        final int ns = n;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                if( ns==1){
                    mWebView.getSettings().setSupportZoom(true);
                    mWebView.getSettings().setBuiltInZoomControls(true);
                    mWebView.getSettings().setDisplayZoomControls(false);
                }else{
                    mWebView.getSettings().setSupportZoom(false);
                    mWebView.getSettings().setBuiltInZoomControls(false);
                    mWebView.getSettings().setDisplayZoomControls(false);
                }
                Log.e("ZZZZZZZZZZZom set to " , ":" + ns + "/Zoom done");
            }
        });
    }
    public void installAPK(){
        Log.e("InstallApk", "Start");
        File apkFile = new File("/sdcard/Geocoder_Test.apk");
        Uri apkUri = Uri.fromFile(apkFile);
        Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
        webLinkIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        startActivity(webLinkIntent);
    }
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
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
    private class finishReport extends AsyncTask<String, String, Void> {
        protected Void doInBackground(String... urls) {
            final int mb_no = diRect_userNUM;
            try {
                //--------------------------
                //   URL 설정하고 접속하기
                //--------------------------
                URL url = new URL("http://jbbuller.kr/app_quit_report.php");       // URL 설정
                HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속
                //--------------------------
                //   전송 모드 설정 - 기본적인 설정이다
                //--------------------------
                http.setDefaultUseCaches(false);
                http.setDoInput(true);                         // 서버에서 읽기 모드 지정
                http.setDoOutput(true);                       // 서버로 쓰기 모드 지정
                http.setRequestMethod("POST");         // 전송 방식은 POST

                // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                //--------------------------
                //   서버로 값 전송
                //--------------------------
                StringBuffer buffer = new StringBuffer();
                String uid = "test";
                buffer.append("mb_no").append("=").append(mb_no).append("&");                 // php 변수에 값 대입
                buffer.append("uid").append("=").append(uid);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                //--------------------------
                //   서버에서 전송받기
                //--------------------------
                InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "EUC-KR");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                    builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
                }
                String myResult = builder.toString();                       // 전송결과를 전역 변수에 저장
                Log.e("받고 결과 ", " / " + mb_no + " / " +  myResult);
            } catch (MalformedURLException e) {
                //
            } catch (IOException e) {
                //
            } // try
            return null;
        }
    } // HttpPostData
    public void remove_NotyAll(){
        NotificationManager notifManager= (NotificationManager) MainContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }
    @Override public void onStart() {
        super.onStart();
        remove_NotyAll();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }
    @Override public void onDestroy() {
        super.onDestroy();
        if (Main_Tts != null) {
            Main_Tts.stop();
            Main_Tts.shutdown();
        }
        if( MainScreenWakeLock > 0 ) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainScreenWakeLock = 0;
        isAvtivity_On = false;
    }
    @Override public void onPostResume() {
        //Log.e("onPostResume","onPostResume");
        super.onPostResume();
        isAvtivity_On = false;
    }
    @Override public void onStop() {
        super.onStop();
        if( MainScreenWakeLock > 0 ) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainScreenWakeLock = 0;
        isAvtivity_On=false;
        Log.e("CurrentSite onStop",page_URLNow );
        if( page_URLNow.equals("_sky_m_01_chat_do.php") || page_URLNow.equals("_sky_m_03_new.php")){
            final String goStrurl = "javascript:sendNode_AppOff()";
            Log.d(TAG,goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }
    }
    @Override protected void onPause() {
        super.onPause();
        isAvtivity_On=false;
    }
    @Override protected void onRestart() {
        super.onRestart();
        isAvtivity_On=true;
        isRestarting = 1;
        remove_NotyAll();
        //Log.e("onRestart","onRestart onRestart");
        //final String goStrurl = "javascript:document.location.reload()";
        Log.e("CurrentSite onRestart",page_URLNow );
        if( page_URLNow.equals("_sky_m_01_chat_do.php") || page_URLNow.equals("_sky_m_03_new.php")){
            final String goStrurl = "javascript:Char_RoomReload()";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }else{
            final String goStrurl = "javascript:activity_recall()";
            //Log.e("onPostResume", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            //Log.e("onPostResume","regocall window app");
        }
    }
    @Override public void onDetachedFromWindow() {
        //Log.e("onDetachedFromWindow","onDetachedFromWindowonDetachedFromWindowonDetachedFromWindowonDetachedFromWindowonDetachedFromWindowonDetachedFromWindow");
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            Log.e("onDetachedFromWindow", "SafeViewFlipper ignoring IllegalArgumentException");

            // Call stopFlipping() in order to kick off updateRunning()

        }
    }
    public void UninstalApp(){
        /*
        Uri packageURI = Uri.parse("package:com.android.gesture.builder");
        Uri packageUri = Uri.parse("co.kr.skycall");
        //Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        //Intent uninstallIntent = new Intent( (Build.VERSION.SDK_INT >= 14)?Intent.ACTION_UNINSTALL_PACKAGE:Intent.ACTION_DELETE, packageUri);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivity(uninstallIntent);
        */

        Intent ita = new Intent(Intent.ACTION_VIEW, Uri.parse("http://jbbuller.kr/apk_notice_download.php"));
        startActivity(ita);

        String app_pkg_name = "co.kr.jbbuller";
        int UNINSTALL_REQUEST_CODE = 99;

        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("actionjob", "UninstallJBbuller");

        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);

        //Intent intent = new Intent(Intent.ACTION_DELETE);//ACTION_UNINSTALL_PACKAGE
        //intent.setData(Uri.parse("co.kr.skycall"));
        //startActivity(intent);
    }
    public static String getURLDecode(String content){
        try {
            return URLDecoder.decode(content, "utf-8");  // EUC-KR
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public ArrayList<String> Upload_LIst;
    private Set<String> fileNames = new HashSet<String>();

    public void javascript_UploadJobDo(String script_toGo){
        final String ret_jvascript = script_toGo;
        Log.e("javascript_UploadJobDo" , "JS : " + ret_jvascript);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(ret_jvascript);
            }
        });
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public ProgressDialog dialog;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(camera_job4 == null) camera_job4 = "";
        if( requestUploadJob == null)requestUploadJob = "";

        Log.e("onActivityResult", "xxxxxxxxxxxxxxxx 받은값은 :" + requestCode + "/ resultCode : " + resultCode + "/requestUploadJob : " + requestUploadJob);
        Log.e("data", "GetData " + data + "/camera_job4 :" + camera_job4 );
        //camera_job4 = type4;  /// 문서 또는 현장사진 //m6_docUPload


        if( data !=null && camera_job4.equals("m6_docUPload") && requestUploadJob.equals("stampupload")){//
            Uri selectedImage = data.getData();
            String fileName = "";
            Log.e("Imag", String.valueOf(selectedImage));

            Uri selectedImageUri = data.getData();
            String  selectedImagePath = getPath(selectedImageUri);
            Log.e("selectedImagePath", "selectedImagePath:" + selectedImagePath);
            if( selectedImagePath==null){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialogTheme);
                builder.setTitle("잘못된 파일");
                builder.setCancelable(false);
                builder.setMessage("선택한 사진의 원본이 갤러리에 있습니다. 갤러리에서 원본을 선택해 주세요");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity mActivity = MainActivity.this;
                        return;
                    }
                });
                builder.show();
                return;
            }

            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = MainContext.getContentResolver( ).query( selectedImage, proj, null, null, null );
            if(cursor != null){
                Log.e("커사가 있음","GO122222");
                if ( cursor.moveToFirst( ) ) {
                    int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                    fileName = cursor.getString( column_index );
                    Log.e("커사가 있음","fileName:" + fileName);
                }else{
                    Log.e("커사가 있음","No movetoFirst:" + fileName);

                }
                cursor.close( );
            }
            Log.e("fileName", fileName);
            String Server_savedFile = UploadUtil.Upload_One_Start(fileName,camera_job4, Upload_File_sizeSet );
            return;
        }
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String msgg = result.get(0);
            //Log.e(EHead, "스피치값: " + msgg);
            alertt(msgg);
            return;
        }
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                //Log.e("canDrawOverlays", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx퍼미션 에러 :" + requestCode);
            }
            return;
        }
        if( requestCode == 745 ) {
            Log.e("사진JOB" , "크롭 후 저장 isCamera_chat:"+ isCamera_chat);
            Photo_Job_Do();
            return;
        }
        if( requestCode == 65 ) {
            if (data == null) return;
            Bundle bundle = data.getExtras();
            Object selList = bundle.get("com.andremion.louvre.home.extra.SELECTION");
            List<Uri> dd = (List<Uri>) bundle.get("com.andremion.louvre.home.extra.SELECTION");
            Log.e("다중업", dd.toString());

            String OneUri = "";
            Upload_LIst = new ArrayList<String>();
            fileNames.clear();
            Upload_LIst.clear();
            Log.e("다중선택", "DD:" + dd);
            for (Uri uri: dd) {
                OneUri = uri.toString();
                OneUri = OneUri.replace("file://","");
                OneUri = getURLDecode(OneUri);
                fileNames.add(OneUri);
            }
            Log.e("다중선택후", "DD:" + fileNames.size());
            if( fileNames.size() < 1 ) return;
            Upload_LIst.addAll(fileNames);
            uploadMultiDo();
            return;
        }
        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };

        int k = 1;
        if (k == 1) {//resultCode == RESULT_OK
            switch (requestCode) {
                case 33: {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mWebView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWebView.loadUrl("javascript:uploadDone()");
                                }
                            });
                            Toast.makeText(getApplicationContext(), "파일전송완료.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                case DIRECT_SAVE: {
                    Log.e("사진JOB" , "바로 저장 / isCamera_chat:" + isCamera_chat);

                    Photo_Job_Do();
                    break;
                }
                case CROP_FROM_CAMERA: {
                    Log.e("사진JOB" , "크롭 시작");
                    cropImage();

                    break;
                }
                /*
                case PICK_FROM_ALBUM: {
                    mImageCaptureUri = data.getData();
                    Log.e("PICK_FROM_ALBUM","카메라에서 선택됨 : " + mImageCaptureUri) ;
                }
                */
                case PICK_FROM_CAMERA: {
                    Log.e("PICK_FROM_CAMERA", "크롭 실행 됨 : " + mImageCaptureUri);
                    if( isCamera_chat.equals("chatupload")){
                        String FullPath = "";
                        if (Build.VERSION.SDK_INT >= 24) {
                            Uri imageUri = Uri.parse(mCurrentPhotoPath);
                            File file = new File(imageUri.getPath());
                            FullPath = imageUri.getPath().toString();
                        }else{
                            final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
                            if (cursor.moveToFirst()) {
                                String filePath = cursor.getString(1);
                                File f = new File(filePath);
                                FullPath = filePath.toString();
                                //Log.e("KKK", "최근것에서 찾기 / " + filePath);
                            } else {
                                //filePath = camera_path.toString();
                                Toast.makeText(getApplicationContext(), "파일 전송실패(사진경로를 못찾았습니다.)", Toast.LENGTH_SHORT).show();
                                break;
                            }

                            cursor.close();
                        }
                        Upload_LIst = new ArrayList<String>();
                        fileNames.clear();
                        Upload_LIst.clear();
                        File sourceFile = new File(FullPath);
                        int UpFile_sizeBefore = (int) sourceFile.length();
                        Log.e("카메라촬영", "FullPath:" + FullPath + " / FileSize:" + UpFile_sizeBefore);
                        if( UpFile_sizeBefore < 10 ) return;
                        fileNames.add(FullPath);
                        Upload_LIst.addAll(fileNames);

                        dialog = new ProgressDialog(this);
                        dialog.setMessage("Uploading...");
                        dialog.setIndeterminate(false);
                        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        dialog.setProgress(0);
                        dialog.show();
                        dialog.setProgressNumberFormat(null);
                        Log.e("채팅으로보내기", Upload_LIst.toString() + "/ mbno :" + mb_no + " /room:" + room_num);
                        MultiUp.UploadMultiStart(Upload_LIst,mb_no,room_num);
                        return;
                    }

                    break;
                }
            }
        } else {
            Log.e("onActivityResult", "result code <> ok = " + resultCode + "/ requestCode :" + requestCode);
        }
    }
    public void show_App_front_message(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyAlertDialoPopup);
        builder.setTitle("알림");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }
    public void uploadMultiDo(){
        Log.e("uploadMultiDo", "프로세스창 띄우기 Upload_LIst :" + Upload_LIst.toString());
        if( Upload_LIst.size() < 1 ) {
            show_App_front_message("전송할 파일이 없습니다.");
            resetMediaSource();
            return;
        }
        String togofn = Upload_LIst.get(0);
        File sourceFile = new File(togofn);
        int UpFile_size = (int) sourceFile.length();
        Log.e("uploadMultiDo", "파일크기 :" + UpFile_size);
        if( UpFile_size < 500 ){
            show_App_front_message("전송할 파일의 크기가 0 이거나 너무 작습니다.");
            resetMediaSource();
            return;
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.show();
        dialog.setProgressNumberFormat(null);

        MultiUp.UploadMultiStart(Upload_LIst,mb_no,room_num);
    }
    private void Photo_Job_Do(){
        String filePath;
        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        String[] what = new String[]{MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.DATA};

        if (Build.VERSION.SDK_INT >= 24) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            String FullPath = imageUri.getPath().toString();
            if (file.exists() && file.isFile()) {
                if( camera_job4.equals("m6_docUPload") && requestUploadJob.equals("stampupload") ){
                    Log.e("촬영사진 바로 올리기", "FullPath : " + FullPath);
                    String Server_savedFile = UploadUtil.Upload_One_Start(FullPath,camera_job4, Upload_File_sizeSet );
                    return;
                }
                SERVER_Upload_URL = URL_Header + "uploade_images.php";
                UPLOAD_Fname = FullPath;
                LastDeleteFn = "";
                mWebViewgetUrl = mWebView.getUrl();
                Activity mActivity = MainActivity.this;
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        new UpdateTask().execute();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "파일 전송실패(사진경로를 못찾았습니다.)", Toast.LENGTH_SHORT).show();
            }
        } else {
            String where = MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'" + " OR " + MediaStore.Images.Media.MIME_TYPE + "='image/png'";
            final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(1);
            } else {
                Toast.makeText(getApplicationContext(), "파일 전송실패(사진경로를 못찾았습니다.)", Toast.LENGTH_SHORT).show();
            }
            filePath = cursor.getString(1);
            File f = new File(filePath);
            String FullPath = filePath.toString();
            cursor.close();
            //Log.e("DDD", "File check :" + f.exists() + "/filename:" + FullPath);
            if (f.exists() && f.isFile()) {
                if( camera_job4.equals("m6_docUPload") && requestUploadJob.equals("stampupload") ){
                    Log.e("촬영사진 바로 올리기", "FullPath : " + FullPath);
                    String Server_savedFile = UploadUtil.Upload_One_Start(FullPath,camera_job4, Upload_File_sizeSet );
                    return;
                }
                SERVER_Upload_URL = URL_Header + "uploade_images.php";
                UPLOAD_Fname = FullPath;
                LastDeleteFn = "";
                mWebViewgetUrl = mWebView.getUrl();
                Activity mActivity = MainActivity.this;
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        new UpdateTask().execute();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "파일 전송실패(사진경로를 못찾았습니다.)", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class UpdateTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {
            String sourceFileUri = UPLOAD_Fname;
            Log.e("업로드 준비" , "파일명 : " + UPLOAD_Fname);
            HttpURLConnection mHttpURLConnection = null;
            DataOutputStream mOutputStream = null;
            String strLineEnd = "\r\n";
            String strTwoHyphens = "--";
            String strUpLoadServerUri = SERVER_Upload_URL;
            String strBoundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            int serverResponseCode = 0;
            File sourceFile = new File(UPLOAD_Fname);
            Log.e("AAAAAAAAAA","BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBb");
            String LatDeleteFn= "";

            String dateString = "2000-01-01 12:30:00";
            double Latitude=0;
            double Longitude = 0;
            String gpsValue = "0,0";
            String gpsAddress = "";

            if (!sourceFile.isFile()) {
                Log.e("업로드", "업로드 파일 없음" + sourceFileUri );
                Toast.makeText(getApplicationContext(), "업로드 실패 : 파일 없음.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("업로드", "업로드 파일 OK" + sourceFileUri );

                try {
                    final ExifInterface exifInterface = new ExifInterface(UPLOAD_Fname);
                    float[] latLong = new float[2];
                    File file = new File(UPLOAD_Fname);
                    Date lastModDate = new Date(file.lastModified());
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateString = dateFormat.format(lastModDate); //lastModDate.toString();
                    Log.e("Date Info", "Date :" + dateString);
                    if (exifInterface.getLatLong(latLong)) {
                        String attrLATITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String attrLATITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String attrLONGITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String attrLONGITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                        if((attrLATITUDE !=null)&& (attrLATITUDE_REF !=null)&& (attrLONGITUDE != null)&& (attrLONGITUDE_REF !=null)) {
                            if (attrLATITUDE_REF.equals("N")) {Latitude = utilApp.convertToDegree(attrLATITUDE);} else {Latitude = 0 - utilApp.convertToDegree(attrLATITUDE);}
                            if (attrLONGITUDE_REF.equals("E")) {Longitude = utilApp.convertToDegree(attrLONGITUDE);} else {Longitude = 0 - utilApp.convertToDegree(attrLONGITUDE);}
                        }
                    }

                } catch (IOException e) {
                    Log.e("GPSERROR","Couldn't read exif info: " + e.getLocalizedMessage());
                }

                File oFile = new File(sourceFileUri);
                if (oFile.exists()) {
                    long L = oFile.length();
                    System.out.println( "축소이전 크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
                }
                sourceFileUri = resizeImg(sourceFileUri,0);
                oFile = new File(sourceFileUri);
                if (oFile.exists()) {
                    long L = oFile.length();
                    System.out.println("축소 후 크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
                }
                String SERVER_Upload_URL = "http://jbbuller.kr/uploade_images.php";

                Log.e("파일생일", dateString);
                gpsValue = Latitude + "," + Longitude;
                gpsAddress = utilApp.getAddress(Latitude,Longitude);
                String str_encode = "";
                try{
                    str_encode = URLEncoder.encode(gpsAddress,"UTF-8");
                } catch(Exception e) {
                    e.printStackTrace();
                }
                Log.e("촬영후 GPS", "GPS : " + gpsValue);
                Log.e("gpsValue", "gpsValue : " + gpsValue + " / gpsAddress : " + gpsAddress);
                Log.e("str_encode", "str_encode : " + str_encode);
                try {
                    /////////////////////////////////////////////////
                    Bitmap b = BitmapFactory.decodeFile(UPLOAD_Fname);
                    ///////////////////////////////////////////////////////////////////////////////////////
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(SERVER_Upload_URL);
                    mHttpURLConnection = (HttpURLConnection) url.openConnection();
                    mHttpURLConnection.setReadTimeout(1000 * 300);
                    mHttpURLConnection.setReadTimeout(1000 * 300);
                    mHttpURLConnection.setDoInput(true); // Allow Inputs
                    mHttpURLConnection.setDoOutput(true); // Allow Outputs
                    mHttpURLConnection.setUseCaches(false); // Don't use a Cached Copy
                    mHttpURLConnection.setRequestMethod("POST");
                    mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    mHttpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    mHttpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + strBoundary);
                    mOutputStream = new DataOutputStream(mHttpURLConnection.getOutputStream());
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFileUri + "\"" + strLineEnd);

                    mOutputStream.writeBytes(strLineEnd);
                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        mOutputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    Log.e("포스트 하기전","jobtype :" + requestUploadJob + "/file:" + sourceFileUri +
                            "/ type2:" + userID  +
                            "/ type3:" + camera_job3 +
                            "/ type4:" + camera_job4 +
                            "/ type5:" + camera_job5 );
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"jobtype\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(requestUploadJob + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type2\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(userID + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type3\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(camera_job3 + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type4\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(camera_job4 + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type5\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(camera_job5 + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type6\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(camera_job6 + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strTwoHyphens + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    serverResponseCode = mHttpURLConnection.getResponseCode();

                    if (serverResponseCode == 200) {
                        Log.d("File Uploaded For ", sourceFileUri + "   Successful");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mWebView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWebView.loadUrl("javascript:uploadDone(" + camera_job3 + ")");
                                        Log.e("JobDoneCall", "UploadDone " + camera_job3 );
                                    }
                                });
                                Toast.makeText(getApplicationContext(), "파일전송완료.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        fileInputStream.close();
                        mOutputStream.flush();
                        mOutputStream.close();
                        deleteUpLoadedFn(sourceFileUri,1);
                    } else {
                        Log.e("File Uploaded For ", sourceFileUri + "   Failed " + serverResponseCode + "| " + mHttpURLConnection.getErrorStream());
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl("javascript:uploadDone(" + camera_job3 + ")");
                            }
                        });
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "파일 전송실패.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        deleteUpLoadedFn(sourceFileUri,2);
                        fileInputStream.close();
                        mOutputStream.flush();
                        mOutputStream.close();


                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    deleteUpLoadedFn(sourceFileUri,3);
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteUpLoadedFn(sourceFileUri,4);
                    Log.e("error:" + e.getMessage(), "Fie upload error");
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            return "OK";
        }
    }
    public void cropImage() {
        this.grantUriPermission("com.android.camera", mImageCaptureUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, mImageCaptureUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 4);
            intent.putExtra("aspectY", 4);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/JbBuller/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());
            UPLOAD_Fname = tempFile.toString();
            Log.e("크롭파일", "UPLOAD_Fname:" + UPLOAD_Fname);
            mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, "co.kr.jbbuller.provider", tempFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, mImageCaptureUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, 745);
        }

    }
    public void progress_int_change(int num){dialog.setProgress(num);}
    public void progress_string_change(String msg){
        Handler handler = new Handler(){
            public void handleMessage(String msg){
                dialog.setMessage(msg);
            }
        };
    }
    public void progress_setMax(int num){dialog.setMax(num);}
    public void progress_dismiss(){dialog.dismiss();}

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        Log.d("onNewIntent", "SSSSSSSSSSs");
        Log.d("extras", "E" + extras);

        if(! intent.hasExtra("actionjob")){
            Log.e("onNewIntent", "actionjob 값이 없어 멈춤");
            return;
        }
        String b = extras.getString("moveUrl") + "";
        String c = extras.getString("IntentJob") + "";
        String ActJob = extras.getString("actionjob") + "";
        String image_path = extras.getString("imagePath");


        String notyCodeN = "0";
        Log.e("뉴인텐드" , "IntentJob :" + c + " / ActJob : " + ActJob + " /image_path : " + image_path) ;
        if( c.equals("recent_phone_seldone")){
            String username = extras.getString("username") + "";
            String phoneNumber = extras.getString("phoneNumber") + "";
            final String goStrurl = "javascript:use_thisnum('" + username + "','" + phoneNumber + "')";
            Log.e(TAG, goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return;
        }
        if( ActJob.equals("UninstallJBbuller")){
            Intent ita = new Intent(Intent.ACTION_VIEW, Uri.parse("http://jbbuller.kr/apk_notice_download.php"));
            startActivity(ita);
            return;
        }
        if(ActJob.equals("SignAfterJobPriceChange")){
            String orderNum = extras.getString("orderNum") + "";
            final String goStrurl = "http://jbbuller.kr/__app_price_change_pop.php?callmode=signPad&ordernum=" + orderNum ;
            Log.e("SignAfterJobDonenew", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return;

        }
        if(ActJob.equals("SignAfterJobDonenew")){
            String orderNum = extras.getString("orderNum") + "";
            final String goStrurl = "http://sub.jbbuller.kr/_sky_m_05_0A.php?showipicknum=" + orderNum ;
            Log.e("SignAfterJobDonenew", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return;
        }
        if( ActJob.equals("SignAfterJob")){
            //paymentstatus
            String paymentstatus = extras.getString("paymentstatus") + "";
            String orderNum = extras.getString("orderNum") + "";
            String Co_name = extras.getString("Co_name") + "";
            String Co_tel = extras.getString("Co_tel") + "";

            //final String goStrurl = "javascript:reload_signFileImage()";
            final String goStrurl = "javascript:android_job_after_sign('" + orderNum + "','" + paymentstatus + "','" + Co_name + "','" + Co_tel + "')";
            Log.e("SignAfterJob", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
            return;
        }
        if( ActJob.equals("SignAfterJobCancel")){
            String returnorderNo = extras.getString("returnordernum") + " ";
            Log.e("SignAfterJobCancel", "창만 닫기 returnordernum:" + returnorderNo);
            final String goStrurl = "javascript:work_Diary_Dayshow('" + returnorderNo + "')";
            Log.e("SignAfterJob", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });

            return;
        }
        if (extras != null) {
            iconNumRest();
            String url = intent.getStringExtra("NotiLInk_send");
            String notiTitle = intent.getStringExtra("notiTitle");
            //Log.e("mMessageReceiver", "job :" + c);
            if( c.equals("PushDBUpdated")){
                String OrderNumber = intent.getStringExtra("updateno");
                String okstatus = intent.getStringExtra("okstatus");
                String noti_type = intent.getStringExtra("noti_type");
                if( noti_type == null) noti_type = "";
                Log.e("mMessageReceiver", "OrderNumber :" + OrderNumber);
                if( OrderNumber==null) return;
                if( OrderNumber.length()< 1 ) return;
                int OrderNumberInt = parseInt(OrderNumber);
                if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
                String Datas = mDbOpenHelper.get_Updated_on_Row(OrderNumberInt, noti_type);
                final String goStrurl = "javascript:push_message_Update_line(" + OrderNumberInt + ", '" + Datas + "'," + okstatus+")";
                Log.e("PushDBUpdated", goStrurl);
                Log.e("okstatus", okstatus);

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
                return;
            }


            Log.e("위드레그 액션 처리:" , b + " / " +  c + " / " + url + " / " + notiTitle + "/" + page_URLNow.indexOf("sky_2000") + "/" );
            String goUrln = "";
            //IntentJob", "Matching
            if (c.equals("GPS_RESAVE")) {
                saveGPS();
            }else if(c.equals("multiupload")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl("javascript:uploadDone()");
                            }
                        });
                        Toast.makeText(getApplicationContext(), "파일전송완료.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else if(c.equals("Matching")){
                final String goStrurl = "javascript:push_message_get_react('" + c + "','" + notiTitle + "','" + url + "',9)";
                //Log.d(TAG, goStrurl);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
            } else  {//if (b.equals("Show_NotiActivity") || c.equals("pushGet") )
                if( c != ""){
                    iconNumRest();
                    if(url==null) url ="";
                    if (url.length() < 5) url = URL_Header + "_my_message_list.php";
                    if( ! page_URLNow.contains("sky_2000") && ! page_URLNow.contains("sky_3000") ){
                        Log.e("위드레그 액션 처리 받고", "주소이동:" + url + " / B:" + b + " /C:" + c);
                        final String goStrurl = "javascript:push_message_get_react('" + c + "','" + notiTitle + "','" + url + "',2)";
                        //Log.d(TAG, goStrurl);
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl(goStrurl);
                            }
                        });
                    }
                }
            }

            SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefx.edit();
            editor.putString("linkURL", "");
            editor.apply();

        }
    }
    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String IntentJob = intent.getStringExtra("IntentJob");
            String noti_type = intent.getStringExtra("noti_type");
            String url = intent.getStringExtra("NotiLInk_send");
            String notiTitle = intent.getStringExtra("notiTitle");//
            String Intentmessage = intent.getStringExtra("notimessage");
            String soojo_soundOK = intent.getStringExtra("soojo_soundOK");

            SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            String NOTIOFF = pref.getString("NOTIOFF", "") + "";
            String MACHINGOFF = pref.getString("MACHINGOFF", "") + "";
            SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefx.edit();
            editor.putString("linkURL", url);
            editor.apply();

            if( IntentJob.equals("PushDBUpdated")){
                String OrderNumber = intent.getStringExtra("updateno");
                String okstatus = intent.getStringExtra("okstatus");
                if( noti_type == null) noti_type = "";
                Log.e("mMessageReceiver_Up", "OrderNumber :" + OrderNumber + " / noti_type : "+ noti_type);
                if( noti_type.equals("TalkNotice")){
                    final String goStrurl = "javascript:jbTalk_notice_fromApp()";
                    Log.e("PushDBUpdated", goStrurl);
                    Log.e("okstatus", okstatus);

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(goStrurl);
                        }
                    });
                    return;
                }
                if( OrderNumber==null) return;
                if( OrderNumber.length()< 1 ) return;
                int OrderNumberInt = parseInt(OrderNumber);
                if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
                String Datas = mDbOpenHelper.get_Updated_on_Row(OrderNumberInt, noti_type);
                final String goStrurl = "javascript:push_message_Update_line(" + OrderNumberInt + ", '" + Datas + "'," + okstatus+")";
                Log.e("PushDBUpdated", goStrurl);
                Log.e("okstatus", okstatus);

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(goStrurl);
                    }
                });
                return;
            }

            if( phone_isBusy == 1){
                Log.e("통화중이다", "소리 다시 알리자");
                if( NOTIOFF.equals("NO")){
                    Log.e("푸시노티설정", "푸시 무음임으로 통화중 처리 안함."); //pushoff
                }else{
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] vv = {100,200,100,200};
                    v.vibrate(vv,-1);
                    String[] getDbInfo = mDbOpenHelper.getSettingvalue();
                    String use_or_not_sound = getDbInfo[4];
                    String pushFn = getDbInfo[5];
                    if( pushFn == null) pushFn = "notisnd";
                    if( use_or_not_sound == null ) use_or_not_sound= "YES";
                    Uri sound = null;
                    if( pushFn.equals("chk")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chk);
                    if( pushFn.equals("ding1")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ding1);
                    if( pushFn.equals("ding2")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ding2);
                    if( pushFn.equals("ding3")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ding3);
                    if( pushFn.equals("gllag")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.gllag);
                    if( pushFn.equals("mbclog")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mbclog);
                    if( pushFn.equals("notisnd")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notisnd);
                    if( pushFn.equals("ring1")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring1);
                    if( pushFn.equals("ring2")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring2);
                    if( sound==null) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notisnd);
                    if( ! use_or_not_sound.equals("YES")) sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pushoff);

                    if (IntentJob.equals("Matching")) {
                        if( MACHINGOFF.equals("NO")){
                            Log.e("푸시노티설정", "푸시 무음임으로 통화중 처리 안함."); //pushoff
                        }else{
                            Ringtone r = RingtoneManager.getRingtone(MainContext, sound);
                            r.play();
                            Log.e("푸시메칭222", "정상사운드로 꽁꽝 통화 중 울립니다. mainactivity..");
                        }
                    } else {
                        sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mnoti);
                        Ringtone r = RingtoneManager.getRingtone(MainContext, sound);
                        r.play();
                        Log.e("알림메칭222", "정상사운드로 통화 중 기본값으로 울립니다... mainactivity");

                    }
                }
            }
            String notycode = "0";
            if( url == null) url = "";
            if(IntentJob.equals("Matching")) notycode="9";
            if (url.length() < 5) url = URL_Header + "_my_message_list.php";
            Log.e("onNewIntent", "푸시 왔음 알려주기:" + url);
            final String goStrurl = "javascript:push_message_get_react('" + IntentJob + "','" + notiTitle + "','" + url + "'," + notycode + ")";
            //Log.e("일반푸시", goStrurl);
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(goStrurl);
                }
            });
        }

    };
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state > 0) {
                phone_isBusy = 1;
            } else {
                phone_isBusy = 0;
            }
            //Log.e("폰상태", "phone_isBusy:" + phone_isBusy + "/|state:" + state + "(ringing:" + TelephonyManager.CALL_STATE_RINGING + ", offhook:" +
            //TelephonyManager.CALL_STATE_OFFHOOK + ", idle:" + TelephonyManager.CALL_STATE_IDLE + ")" + "|number:" + incomingNumber + "| 결과 " + phone_isBusy);
        }
    };
}
