package co.kr.skycall;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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

public class AndroidLocationServices extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    public NotificationManager mNotificationManager;
    public static int NoticeCountNum = 0;
    public String NowURL = "";
    //public String URL_Header = "http://192.168.10.2/";
    public String URL_Header = "http://jbbuller.kr/";
    public GPSTracker gps;
    public double userGPS1;
    public double userGPS2;

    public String userGPS1_last;
    public String userGPS2_last;
    public MainActivity main_m;
    public String pushFnNew = "";
    private TextToSpeech tts=null;
    private DbOpenHelper mDbOpenHelper = null;
    String CallType;
    @SuppressLint("WrongConstant")
    private Context mContext;
    private TextToSpeech mTts = null;
    @Override
    public void onCreate(){
        super.onCreate();
    }
    public void alertt(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.mContext = getApplicationContext();
        if( mDbOpenHelper == null ){
            mDbOpenHelper = new DbOpenHelper(this);
            //new ToastMessageTask().execute("디비초기화 " + mDbOpenHelper );
        }
        if( intent == null){
            return START_STICKY;
        }
        if( intent.getExtras() == null){
            return START_STICKY;
        }
        CallType = intent.getStringExtra("CallType");


        mTts = MyTell.mTts;
        Log.e("AndroidLocationServices", "시작한다");

        String save_fnis = intent.getStringExtra("soundPushFn");
        if (save_fnis != null) {
            pushFnNew = save_fnis;
            Log.e("PushFnTosave_onstart", "Fn = " + save_fnis);
            SharedPreferences userDetails = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
            SharedPreferences.Editor editor = userDetails.edit();
            editor.putString("soundFn", save_fnis);
            editor.apply();
            return START_STICKY;
        }

        String message = intent.getStringExtra("message");
        String notiTitle = intent.getStringExtra("notiTitle");
        String linkurl = intent.getStringExtra("linkurl");
        String noticejob = intent.getStringExtra("noticejob") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";

        String getGPS1 = intent.getStringExtra("getGPS1") + "";
        String getGPS2 = intent.getStringExtra("getGPS2") + "";
        String getGPSadr = intent.getStringExtra("getGPSadr") + "";

        Log.e("noticejob", "처음받은 서비스 에서 = : " + noticejob);
        Intent service = new Intent(getApplicationContext(), MyTell.class);
        startService(service);
        //MyTell.mTts.speak("TEST", TextToSpeech.QUEUE_FLUSH,null);
        if(noticejob.equals("notiCntsetzero")) {
            //new ToastMessageTask().execute("토티 초기화하기");
            SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
            NoticeCountNum = 0;
            SharedPreferences.Editor editor = prefx.edit();
            editor.putString("NoticNum", "0");
            Log.d("토티 초기화하기", "토티 초기화하기 0");
            editor.commit();


        }else if(noticejob.equals("GPSSAVE")){
            Boolean A = isNetworkConnected();
            Log.e("인터넷 체크", "isNetworkConnected = " + A);
            if( A != true ) return START_STICKY;
            float isGps = 0;
            String userAddress = "";

            if( getGPS1.length() > 5 && getGPS2.length() > 5 && getGPSadr.length() > 1 ){
                double dgetGPS1 = Double.valueOf(intent.getStringExtra("getGPS1")).doubleValue();
                double dgetGPS2 = Double.valueOf(intent.getStringExtra("getGPS2")).doubleValue();

                userAddress = getGPSadr;
                userGPS1 = dgetGPS1;
                userGPS2 = dgetGPS2;
            }else{
                main_m = new MainActivity();
                gps = new GPSTracker(getApplicationContext());
                String getMUrl = main_m.MainURLGET();
                userGPS1 = 0;
                userGPS2 = 0;
                try {
                    userGPS1 = gps.getLatitude();
                    userGPS2 = gps.getLongitude();
                    isGps = gps.getGpsType();
                    userAddress = gps.getAddress(userGPS1, userGPS2);
                } catch (Exception e) {
                }
            }


            SharedPreferences userDetails = getSharedPreferences("userInfo_apset", MODE_PRIVATE);
            userGPS1_last = userDetails.getString("userGPS1_last", "0");
            userGPS2_last = userDetails.getString("userGPS2_last", "0");
            String chuserGPS1 = userGPS1 + "";
            String chuserGPS2 = userGPS2 + "";


            gpsinfo = userGPS1 + "," + userGPS2;
            String noSame = "Yes";
            Log.e("GCMIntentService", "userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + "/ Saved1 :" + chuserGPS1 + " Saved2:" + chuserGPS2 + " / address=" + userAddress );
            if( userGPS1_last.equals(chuserGPS1) && userGPS2_last.equals(chuserGPS2) && noSame.equals("NO")){
                Log.e("같은값 저장안함", "userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + " /// " +chuserGPS1 + "  / " + chuserGPS2);
            }else{
                if(userAddress==null)userAddress="";
                userAddress = userAddress.replace("대한민국 ","");
                userAddress = userAddress.replace("대한민국","");
                final String user_addressGo = userAddress;
                final String userNum_go = userNum;
                final String gpsinfoGo = gpsinfo;
                final String isGps_val = isGps + "";


                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            String go_UrlNew = URL_Header + "_app_location_refresh_save_re2.php";
                            HttpPost httppost = new HttpPost(go_UrlNew);
                            HttpClient httpclient = new DefaultHttpClient();
                            try {
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("user_num", userNum_go + ""));
                                nameValuePairs.add(new BasicNameValuePair("userGPS", gpsinfoGo + ""));
                                nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));
                                nameValuePairs.add(new BasicNameValuePair("gps_address", user_addressGo));
                                nameValuePairs.add(new BasicNameValuePair("isGps", isGps_val));
                                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                                httppost.setEntity(ent);
                                HttpResponse response = httpclient.execute(httppost);
                                Log.e("이전과 다른값 저장함", "userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + " /// " +gpsinfoGo);
                            } catch (ClientProtocolException e) {} catch (IOException e) {
                                Log.e("saveGPSNO", "goStrurl : " + userNum_go + " / userGPS : " + gpsinfoGo + " URL :" + go_UrlNew );
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();

                SharedPreferences.Editor editor = userDetails.edit();
                editor.putString("userGPS1_last",""+chuserGPS1);
                editor.putString("userGPS2_last",""+chuserGPS2);
                editor.commit();
            }
        }else{
            Log.e("notBoth", "매칭 호출하기");

            NotificationManagerJob(getApplicationContext(), intent);
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }

        return START_STICKY;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    protected void onHandleIntent(Intent intent) {
        String save_fnis = intent.getStringExtra("soundPushFn");
        if (save_fnis != null) {
            pushFnNew = save_fnis;
            Log.e("PushFnTosave", "Fn = " + save_fnis);
            SharedPreferences userDetails = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
            SharedPreferences.Editor editor = userDetails.edit();
            editor.putString("soundFn", save_fnis);
            editor.apply();
        }
        String message = intent.getStringExtra("message");
        String notiTitle = intent.getStringExtra("notiTitle");
        String linkurl = intent.getStringExtra("linkurl");
        String noticejob = intent.getStringExtra("noticejob") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";
        Log.e("noticejob", "noticejob= : " + noticejob);


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

    public class save_pushDoneInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... passing) {
            //HttpClient httpclient = HttpClients.createDefault();
            HttpClient httpclient = new DefaultHttpClient();
            String go_UrlNew = "http://jbbuller.kr/error_savepush.php";
            HttpPost httppost = new HttpPost(go_UrlNew);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("error_text", passing[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                httppost.setEntity(ent);
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
            } catch (IOException e) {

            }
            return null;
        }
        protected void onPostExecute(ArrayList<String> result) {}
        protected void onProgressUpdate(Integer... progress) {}
    }
    public void NotificationManagerJob_emp(Context context, Intent intentx) {
        Log.e("PUSHSOUNDPLAY", "로케이션서비스");
    }
    public void NotificationManagerJob(Context context, Intent intentx) {
        String message = intentx.getStringExtra("message")+ "";
        String notiTitle = intentx.getStringExtra("notiTitle") + "";
        String linkurl = intentx.getStringExtra("linkurl") + "";
        String noticejob = intentx.getStringExtra("noticejob") + "";
        String userNum = intentx.getStringExtra("userNum") + "";
        String gpsinfo = intentx.getStringExtra("gpsinfo") + "";

        SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String NOTIOFF = "";//pref.getString("NOTIOFF", "") + "";
        String MACHINGOFF = "";//pref.getString("MACHINGOFF", "") + "";


        String push_speechset = "";//pref.getString("pushread_text_ornot", "") + "";
        String speech_typeset = "";//pref.getString("pushread_text_type", "") + "";
        if( mDbOpenHelper == null )mDbOpenHelper = new DbOpenHelper(this);
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        Log.e("디비읽은값",  "값 : " + getDbInfo);

        float readspeed_Set = (float) 1.0;
        float readPitch_Set = (float) 1.0;

        String use_or_not_sound = "";
        String pushFn = "";

        if( getDbInfo.length > 10 ) {
            push_speechset = getDbInfo[1];
            speech_typeset = getDbInfo[2];
            NOTIOFF = getDbInfo[3];
            MACHINGOFF = getDbInfo[4];
            String readspeed = getDbInfo[6];
            String readPitch = getDbInfo[7];

            if( readspeed == null) readspeed = "100";
            if( readPitch == null) readPitch = "100";

            use_or_not_sound = getDbInfo[4];
            pushFn = getDbInfo[5];

            if( readspeed.length() > 0 ) readspeed_Set = (float) Double.parseDouble(readspeed);
            if( readPitch.length() > 0 ) readPitch_Set = (float) Double.parseDouble(readPitch);

        }
        Log.e("set_push_settingREAD", "디비닫은값 push_speechset :" + push_speechset +  " /use_or_not_sound :" + use_or_not_sound +
                " / speech_typeset: " + speech_typeset + " / NOTIOFF :" + NOTIOFF + "/ MACHINGOFF :" + MACHINGOFF +
                " / 속도 : " + readspeed_Set + " / 피치 : " + readPitch_Set);

        if( push_speechset==null) push_speechset = "0";
        if( speech_typeset==null) speech_typeset = "0";
        if( NOTIOFF==null) NOTIOFF = "0";
        if( MACHINGOFF==null) MACHINGOFF = "0";

        if( push_speechset.equals("")) push_speechset = "0";
        if( speech_typeset.equals("")) speech_typeset = "0";
        if( NOTIOFF.equals("")) NOTIOFF = "0";
        if( MACHINGOFF.equals("")) MACHINGOFF = "0";
        if( push_speechset.equals("1") && speech_typeset.equals("0") ) speech_typeset = "2";




        String TTS_message = message;
        String infoTxt = "읽어주기 : " + push_speechset + " 유형 : " + " /use_or_not_sound :" + use_or_not_sound +
                speech_typeset + " / 노티유형 :" + noticejob +
                "/ 접수알림 : " + NOTIOFF + "/ 매칭알림 :" + MACHINGOFF +
                " / 속도 : " + readspeed_Set + " / 피치 : " + readPitch_Set;
        //new ToastMessageTask().execute(infoTxt);

        boolean ison = false;
        if( mTts != null) ison = mTts.isSpeaking();
        String sttss = "메인첵 재생중 : " + ison + "/";
        //new ToastMessageTask().execute(sttss);
        String Pstr = sttss;
        Pstr+="잡타입:" + noticejob + " / notiTitle :" + notiTitle + " | ";
        Pstr+="재생여부:" + push_speechset + " /";
        Pstr+="읽어줄대상:" + speech_typeset + " /";
        String ststa = "반응안함";

        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int RingMode = 0;
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                RingMode = 1;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                RingMode = 2;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                RingMode = 3;
                break;
        }
        Log.e("링톤", " 링톤 상태 : " +  RingMode);
        String ringTonSt = "링톤 상태 : " +  RingMode ;
        //new ToastMessageTask().execute(ringTonSt);
        if( mTts == null) mTts = MyTell.mTts;
        if( push_speechset.equals("1") && RingMode == 3 ){
            mTts.setSpeechRate(readspeed_Set);// 0.995
            mTts.setPitch(readPitch_Set); // 1.0
            int recc = 0;
            if(notiTitle.contains("접수알림")) recc = 1;
            if(noticejob.equals("Matching") && speech_typeset.equals("2")){
                NOTIOFF ="NO";
                if(!ison) mTts.speak(TTS_message, TextToSpeech.QUEUE_FLUSH,null);
                message = "장비불러 매칭오더 자세한 내용은 문자방을 참조해 주세요";
                ststa = "매칭읽어줌";
            }else if(recc ==1 && speech_typeset.equals("1")) {
                if(!ison) mTts.speak(TTS_message, TextToSpeech.QUEUE_FLUSH,null);
                NOTIOFF ="NO";
                message = "장비불러 접수오더 자세한 내용은 문자방을 참조해 주세요";
                ststa = "접수읽어줌";
            }else{
                ststa = "매칭도 접수도 아니여서 패스";
            }
        }else{
            if( RingMode != 3 ){
                ststa = "진동 또는 무음이여서 반응안함";
                Log.e("받은 내용", " 진동 또는 무음이여서 반응안함 ");
            }else{
                Log.e("받은 내용", " 안읽어주기로 설정됨 실행안됨 ");
                ststa = "읽어주시 OFF여서 반응안함";
            }
        }
        //Pstr+=" / 결과 :" + ststa;
        //new save_pushDoneInfo().execute(Pstr);
       // Log.e("NowURLNowURLNowURL =  ", NowURL);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //sendBroadcast(intent);

        //Bitmap bigPictureBitmap  = BitmapFactory.decodeResource(context.getResources(), R.raw.bigicon); //드래그 후 공간에
        final Bitmap photo = BitmapFactory.decodeResource(getResources(), R.raw.bigicon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notiTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setWhen(System.currentTimeMillis())
                .setNumber(10)
                .setAutoCancel(true)
                .setLargeIcon(photo)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(message);
        mBuilder.setVibrate(new long[]{250, 250});
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("linkUrlClick", linkurl);

        if (noticejob.equals("Matching")) {
            resultIntent.putExtra("IntentJob", "Matching");
        }else{
            resultIntent.putExtra("IntentJob", "pushGet");
        }
        resultIntent.putExtra("NotiLInk_send", linkurl);
        resultIntent.putExtra("notiTitle", notiTitle);

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setAction("android.intent.action.MAIN");
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);


        if( NOTIOFF.equals("NO") || use_or_not_sound.equals("NO")){
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pushoff);
            if( use_or_not_sound.equals("NO")){
                sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pushoff);
                Log.e("푸시노티설정", "푸시 notisnd 알립니다./" + NOTIOFF + "/"); //pushoff
            }
            mBuilder.setSound(sound);
            long[] v = {-1};
            mBuilder.setVibrate(v);
            Log.e("푸시노티설정", "푸시 무음으로 알립니다./" + NOTIOFF + "/"); //pushoff
        }else{
            if (noticejob.equals("Matching")) {
                if( MACHINGOFF.equals("NO") || use_or_not_sound.equals("NO")){
                    Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pushoff);
                    mBuilder.setSound(sound);
                    Log.e("푸시메칭", "메칭을 무음으로 돌립니다./" + MACHINGOFF + "/");
                }else{

                    SharedPreferences userDetails = getSharedPreferences("pushNoticeSnd", MODE_PRIVATE);
                    Log.e("푸시파일", "읽은결과 : " + pushFn + " / " + pushFnNew );
                    if( pushFn.equals("")) pushFn = "notisnd";
                    if( pushFn.length() < 2 ) pushFn = "notisnd";
                    if( pushFnNew.length() > 1 ) pushFn = pushFnNew;
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
                    long[] v = {100,500,300,500,300,500};
                    if( use_or_not_sound.equals("NO"))sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pushoff);
                    mBuilder.setVibrate(v);

                    Log.e("푸시메칭", "정상사운드로 꽁꽝울립니다..." + " / 로케이션서비스 use_or_not_sound :" + use_or_not_sound + "/ MACHINGOFF : " + MACHINGOFF);
                    mBuilder.setSound(sound);
                }
                linkurl = URL_Header + "_my_message_list.php";
            } else {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                Log.e("알림메칭", "정상사운드로 기본값으로 울립니다...");
            }
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(5214, mBuilder.build());
        new ToastMessageTask().execute(notiTitle);
        if(linkurl==null || noticejob==null){
            linkurl = "";
        }else{
            if(linkurl.length() < 10 || noticejob.indexOf("enW")==0) linkurl = "";
        }
        SharedPreferences prefx = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        String ntcnum = prefx.getString("NoticNum", "");
        if (!containsOnlyNumbers(ntcnum)) ntcnum = "0";
        NoticeCountNum = Integer.parseInt(ntcnum);
        NoticeCountNum = NoticeCountNum + 1;
        SharedPreferences.Editor editor = prefx.edit();
        editor.putString("linkURL", linkurl);
        editor.putString("NoticNum", String.valueOf(NoticeCountNum));
        Log.e("ChagetoString=",String.valueOf(NoticeCountNum));
        Log.e("(NoticeCountNum)=",String.valueOf(NoticeCountNum));
        editor.apply();


        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {return;}
        Intent intentX = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intentX.putExtra("badge_count", NoticeCountNum);
        intentX.putExtra("badge_count_package_name", context.getPackageName());
        intentX.putExtra("badge_count_class_name", launcherClassName);

        Log.e("BadgeInfo ", "ctxpackgage=" + context.getPackageName() + ", launcherclassname=" + launcherClassName + " ,," + linkurl);
        context.sendBroadcast(intentX);


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
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

}