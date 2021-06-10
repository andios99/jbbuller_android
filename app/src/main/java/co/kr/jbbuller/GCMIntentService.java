package co.kr.jbbuller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public NotificationManager mNotificationManager;
    public static int NoticeCountNum = 0;
    public String NowURL = "";
    //public String URL_Header = "http://192.168.1.7/";
    public String URL_Header = "http://jbbuller.kr/";
    public GPSTracker gps;
    public double userGPS1;
    public double userGPS2;
    public String userGPS1_last;
    public String userGPS2_last;
    public MainActivity main_m;
    private TextToSpeech tts=null;
    //NotificationCompat.Builder builder;
    private final String USER_AGENT = "Mozilla/5.0";
    public GCMIntentService() {

        super("GCMIntentService");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("GCMIntentService", "onStartCommandonStartCommandonStartCommandonStartCommandonStartCommand2");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra("message");
        String notiTitle = intent.getStringExtra("notiTitle");
        String linkurl = intent.getStringExtra("linkurl");
        String noticejob = intent.getStringExtra("noticejob") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";
        String getTime = intent.getStringExtra("sendTime") + "";
        Log.e("두번째 서비스", "onHandleIntent noticejob= : " + noticejob);

        if(noticejob.equals("GPSSAVE")){
            main_m = new MainActivity();
            gps = new GPSTracker(getApplicationContext());
            String getMUrl = main_m.MainURLGET();
            userGPS1 = 0;
            userGPS2 = 0;
            String userAddress = "";
            try {
                userGPS1 = gps.getLatitude();
                userGPS2 = gps.getLongitude();
                userAddress = gps.getAddress(userGPS1, userGPS2);
            } catch (Exception e) {
            }

            SharedPreferences userDetails = getSharedPreferences("userInfo_apset", MODE_PRIVATE);
            userGPS1_last = userDetails.getString("userGPS1_last", "0");
            userGPS2_last = userDetails.getString("userGPS2_last", "0");
            String chuserGPS1 = userGPS1 + "";
            String chuserGPS2 = userGPS2 + "";


            gpsinfo = userGPS1 + "," + userGPS2;
            userAddress = userAddress.replace("대한민국 ","");
            userAddress = userAddress.replace("대한민국","");

            Log.e("푸시 GPS만 이용하기", "userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + "/ Saved1 :" + chuserGPS1 + " Saved2:" + chuserGPS2 + " / address=" + userAddress );
            if(userAddress==null)userAddress="";

            String go_UrlNew = URL_Header + "_app_location_refresh_save_re2.php";
            HttpPost httppost = new HttpPost(go_UrlNew);
            HttpClient httpclient = new DefaultHttpClient();
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_num", userNum + ""));
                nameValuePairs.add(new BasicNameValuePair("userGPS", gpsinfo + ""));
                nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));
                nameValuePairs.add(new BasicNameValuePair("gps_address", userAddress));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.e("go_UrlNew", "URL = " + go_UrlNew + "/ " + nameValuePairs);

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                httppost.setEntity(ent);
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {} catch (IOException e) {
                Log.e("위치저장 실패", "goStrurl : " + userNum + " / userGPS : " + gpsinfo + " URL :" + go_UrlNew );
            }
            Log.e("이전과 다른값 저장함", "userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + " /// " +chuserGPS1 + "  / " + chuserGPS2);
            SharedPreferences.Editor editor = userDetails.edit();
            editor.putString("userGPS1_last",""+chuserGPS1);
            editor.putString("userGPS2_last",""+chuserGPS2);
            editor.commit();

        }else{

            Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            dialogIntent.putExtra("NotiLInk_send", linkurl);
            dialogIntent.putExtra("notiTitle", notiTitle);
            startActivity(dialogIntent);
            Log.e("GCMIntentService", "서비스 브로드케스팅 noticejob= : " + noticejob);
            //NotificationManagerJob(getApplicationContext(), intent);
            //GcmBroadcastReceiver.completeWakefulIntent(intent);

        }

        //new ToastMessageTask().execute(message);

    }

    private class ToastMessageTask extends AsyncTask<String, String, String> {
        String toastMessage;
        @Override
        protected String doInBackground(String... params) {toastMessage = params[0];return toastMessage;}
        protected void OnProgressUpdate(String... values) {super.onProgressUpdate(values);}
        protected void onPostExecute(String result){Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT);toast.show();}
    }
    private static final String PREFERENCE_NAME = "noti_Setting";
    public void textReader(String text){
        tts.setSpeechRate(0.999f);
        tts.setPitch(0.1f);
        boolean ison = tts.isSpeaking();
        if( ison ==true ) {
            Log.e("스피킹중", "스피킹");
        }else{
            tts.speak(text,TextToSpeech.QUEUE_FLUSH, null);
            Log.e("스피킹중", "노는중");
        }
    }
    public void NotificationManagerJob(Context context, Intent intentx) {

        if( tts == null){
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != ERROR) {
                        // 언어를 선택한다.
                        tts.setLanguage(Locale.KOREAN);
                    }
                }
            });
        }

        SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String NOTIOFF = pref.getString("NOTIOFF", "") + "";
        String MACHINGOFF = pref.getString("MACHINGOFF", "") + "";

        String push_speechset = pref.getString("pushread_text_ornot", "") + "";
        String speech_typeset = pref.getString("pushread_text_type", "") + "";


        String message = intentx.getStringExtra("message");
        String notiTitle = intentx.getStringExtra("notiTitle");
        String linkurl = intentx.getStringExtra("linkurl");
        String noticejob = intentx.getStringExtra("noticejob") + "";
        String userNum = intentx.getStringExtra("userNum") + "";
        String gpsinfo = intentx.getStringExtra("gpsinfo") + "";
        Log.e("GCMIntentService", "서비스 브로드케스팅2222222 noticejob= : " + noticejob);


        if( ! speech_typeset.equals("1") && ! speech_typeset.equals("2")) speech_typeset = "1";
        Log.e("받은 내용", " 읽음 끄기켜기 : " + push_speechset + " /읽음 종류 :" + speech_typeset);
        textReader(message);

        Log.e("푸시알림상태", "Resut nitioff :" + NOTIOFF + " / Resut matching : " + MACHINGOFF);


        if(noticejob==null) noticejob="";
        //Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.skysnd2);



        //new ToastMessageTask().execute(notiTitle);
        if(linkurl==null || noticejob==null){
            linkurl = "";
        }else{
            if(linkurl.length() < 10 || noticejob.indexOf("enW")==0) linkurl = "";
        }
        //SharedPreferences pref = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        String ntcnum = pref.getString("NoticNum", "");
        if (!containsOnlyNumbers(ntcnum)) ntcnum = "0";
        NoticeCountNum = Integer.parseInt(ntcnum);
        NoticeCountNum = NoticeCountNum + 1;
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("linkURL", linkurl);
        editor.putString("NoticNum", String.valueOf(NoticeCountNum));
        Log.e("ChagetoString=",String.valueOf(NoticeCountNum));
        Log.e("(NoticeCountNum)=",String.valueOf(NoticeCountNum));
        editor.apply();


        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {return;}
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", NoticeCountNum);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);

        Log.e("BadgeInfo ","ctxpackgage=" + context.getPackageName() + ", launcherclassname=" + launcherClassName + " ,," + linkurl);
        context.sendBroadcast(intent);

        Log.e("NowURLNowURLNowURL =  ",NowURL);
        Intent xintent = new Intent("GCMMESSAGE");
        xintent.putExtra("NotiLInk_send", linkurl);
        xintent.putExtra("notiTitle", notiTitle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(xintent);
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

    public boolean containsOnlyNumbers(String str) {
        if (str == null || str.length() == 0)return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))return false;
        }return true;
    }

}