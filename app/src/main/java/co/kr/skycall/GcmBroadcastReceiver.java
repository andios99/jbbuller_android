package co.kr.skycall;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Integer.parseInt;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver { //WakefulBroadcastReceiver   BroadcastReceiver
    public GPSTracker gps;
    public double userGPS1;
    public double userGPS2;
    public String userGPS1_last;
    public String userGPS2_last;
    public String URL_Header = "http://jbbuller.kr/";
    private static final String PREFERENCE_NAME = "noti_Setting";
    private Context Mcontext;
    private int notiOnce = 0;
    private String speech_Job;
    private static TextToSpeech mTts = null;
    private PowerManager.WakeLock screenWakeLock=null;
    private LocalBroadcastManager broadcaster;
    private DbOpenHelper mDbOpenHelper = null;
    public static int NoticeCountNum = 0;
    private String lastSaveAddress = "";
    public Share_utils Util = null;
    public static String jbCode = "JbBULLERWAKEUP";
    String [] fds1 = {"noti_type","noti_from_unum","noti_from_type","order_level","sub_level","my_num","chr_soo",
            "bonsa","read_cnt","gisa","ref_num","mg_title","mg_body","job_dtype","reg_date","keepit",
            "o_stepcnt","ea_infos","action_doer","order_from","order_fromnum"};
    private String [] fds2 = {"num","noti_type","noti_from_unum","noti_from_type","order_level","sub_level","my_num","chr_soo",
            "bonsa","read_cnt","gisa","ref_num","mg_title","mg_body","job_dtype","reg_date","keepit",
            "o_stepcnt","ea_infos","action_doer","order_from","order_fromnum"};
    String [] odfds = {"order_member_num","order_mem_ownernum","serch_keyword", "contract_address","worker_num","order_phone","order_member_name","contract_message",
            "worker_directphone","worker_name","car_size_from","car_size_to","floor_sel","car_option","car_price","order_handle_by",
            "car_type","work_day","work_start_h","work_start_m","job_type","car_workhour","order_type","order_status","worker_ownernum",
            "work_time","car_typesub","car_workdaytype","car_workhour","car_worktype","car_amount","sign_howlong_int","sign_howlong_str",
            "howtopay","work_payment","orderwork_type","worker_directtype","worker_dirvernum","howtopay_done"};


    private void UpdateDbInfos(Context context, Intent intent) {
        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(Mcontext);
        String dbdatavalue = intent.getStringExtra("dbdatavalue");
        Log.e("dbdatavalue", "Data :" + dbdatavalue);
        JSONObject jsonObject = null;
        int DbUpdated = 0;
        int pushNum = 0;
        int OrderNum = 0;
        int OK = 0;
        try {
            jsonObject = new JSONObject(dbdatavalue);
            Iterator<String> iter = jsonObject.keys();
            ContentValues PushDb = new ContentValues();
            ContentValues OrderDb = new ContentValues();
            while(iter.hasNext()){
                String key = iter.next();
                String val = jsonObject.getString(key);
                if( key.equals("ref_num") && val.length()>1 ) OrderNum = parseInt(val);
                if( key.equals("ordereanum") && val.length()>1 ) OrderNum = parseInt(val);
                if( key.length() > 1 ){
                    if (Arrays.asList(fds1).contains(key)) {
                        if( ! key.equals("pushnum")) PushDb.put(key,val);
                    }else{
                        if( !key.equals("ordereanum") && !key.equals("pushnum") ) OrderDb.put(key,val);
                    }
                }
            }
            Log.e("업데이트 후 " , "dbdatavalue : " + dbdatavalue) ;
            if( OrderNum > 0 ) {
                OK = mDbOpenHelper.UpdateDB_push_order("_car_order_lists", OrderDb , OrderNum);
                Log.e("user_push_messages", "Update:" + OrderDb + "/ OrderNum :" + OrderNum + " / result : " + OK);
                DbUpdated = 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if( DbUpdated > 0 && OrderNum > 0 ){
            Log.e("DbUpdateNoticed", "메인으로 DBUPDATE 알리기 aaa");
            Intent resultIntent = new Intent(MainActivity.ACTION_SHOW_TEXT);
            resultIntent.putExtra("IntentJob", "PushDBUpdated");
            resultIntent.putExtra("updateno", String.valueOf(OrderNum));
            resultIntent.putExtra("okstatus", String.valueOf(OK));
            LocalBroadcastManager.getInstance(Mcontext).sendBroadcast(resultIntent);
        }else {
            Log.e("DbUpdate_Not", "메인으로 DBUPDATE 안알림 : DbUpdated : " + DbUpdated + " / OrderNum : " + OrderNum );
        }
    }
    private void Push_message_regist(JSONObject obj,String push_mg_title,String push_reg_date,String push_noti_type,int push_ref_num, String message, String push_order_level){
        JSONObject Data = new JSONObject();
        try {
            Data.put("noti_type", push_noti_type);
            Data.put("mg_title", push_mg_title);
            Data.put("reg_date", push_reg_date);
            Data.put("ref_num", push_ref_num);
            Data.put("mg_body", message);
            Data.put("noti_from_unum", "0");
            Data.put("noti_from_type", "");
            Data.put("order_level", push_order_level);
            Data.put("sub_level", "0");
            Data.put("my_num", "0");
            if( obj != null){
                for (String val : odfds) {
                    try {
                        String eaval = obj.getString(val);
                        if( eaval == null) eaval = "";
                        if( eaval.length()>0 && !val.equals("num"))  Data.put(val, eaval);
                    } catch (JSONException e) {
                        //Log.e("없는필드", "val:" + val + "/ " + e.getCause() );
                        //e.printStackTrace();
                    }
                }
            }else{
                Data.put("dontsaveOrder", "no");
            }
            Data.put("caroderlistnum", push_ref_num);
            if( mDbOpenHelper == null ) mDbOpenHelper = new DbOpenHelper(Mcontext);
            int OK = mDbOpenHelper.save_message_orderList(Data);
            Log.e("푸시저장하기", "저장결과 : " + OK + "/ Data : " + Data );
            Intent resultIntent = new Intent(MainActivity.ACTION_SHOW_TEXT);
            resultIntent.putExtra("IntentJob", "PushDBUpdated");
            resultIntent.putExtra("noti_type", push_noti_type);
            resultIntent.putExtra("updateno", String.valueOf(push_ref_num));
            resultIntent.putExtra("okstatus", String.valueOf(OK));
            LocalBroadcastManager.getInstance(Mcontext).sendBroadcast(resultIntent);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private String pushRefNum_Pick = "0";
    //adb shell am set-inactive co.kr.skycall true
    @Override
    public void onReceive(Context context, Intent intent) {
        if( Util == null) Util = new Share_utils(context);
        String noticejob = intent.getStringExtra("noticejob") + "";
        String message = intent.getStringExtra("message");
        String notiTitle = intent.getStringExtra("notiTitle");
        pushRefNum_Pick = intent.getStringExtra("ordereanum");

        if (screenWakeLock == null && ! noticejob.equals("GPSSAVEONLY_NEW")){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //screenWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, jbCode);
            screenWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, jbCode);
            screenWakeLock.acquire(10000);
        }
        Mcontext = context;
        broadcaster = LocalBroadcastManager.getInstance(Mcontext);
        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(Mcontext);
        if (intent.getAction().equals( "android.intent.action.PACKAGE_REMOVED")) {
            Log.e("앱삭제완료" , "앱삭제 후 작업 창 뛰우기");
            // Here you send one local broadcast to your activity or fragment and you can update in your view.
        }
        if(pushRefNum_Pick == null) pushRefNum_Pick = "0";
        Log.e("GcmBroadcastReceiver", "처음 받고 시작함 noticejob : + " + noticejob  + " / message :" + message + "/ Title : " + notiTitle + " / ordereanum : " + pushRefNum_Pick);
        if (TextUtils.isEmpty(intent.getStringExtra("noticejob"))) return;
        if( noticejob.equals("null")) return;
        if( noticejob.equals("TalkNotice")){
            mDbOpenHelper.SetSettingvalue("seting_11","1");
            String cnt = mDbOpenHelper.get_message_not_read();
        }
        if( noticejob.equals("mypushdbupdate")){
            UpdateDbInfos(context,intent);
            return;
        }

        String linkurl = intent.getStringExtra("linkurl") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";
        String getTime = intent.getStringExtra("sendTime") + "";
        String noti_realshowmg = intent.getStringExtra("noti_realshowmg") + "";
        String samePlaceNo = intent.getStringExtra("samePlaceNo") + "";
        String push_speech = intent.getStringExtra("push_speech") + "";
        String soojo_soundOK = intent.getStringExtra("mem_soojo_pushget") + "";

        if( noticejob.equals("GPSSAVEONLY_NEW") || noticejob.equals("GPSSAVEONLY_NEW_sound")){
            Log.e("GPS SAVe" , "samePlaceNo:" + samePlaceNo) ;
            gpsUpdate(context, getTime,noticejob,samePlaceNo);
            return;
        }

        if( noticejob.equals("교환완료")) message = "오더 교환이 완료되었습니다.";
        Bundle extras = intent.getExtras();
        Log.e("Extra", "D:" + extras);

        String push_mg_title = extras.getString("push_mg_title");
        String push_reg_date = extras.getString("push_reg_date");
        String push_noti_type = extras.getString("push_noti_type");
        String push_ref_num = extras.getString("push_ref_num");
        String push_order_level = extras.getString("push_order_level");
        String push_message = extras.getString("message");
        if( push_order_level == null) push_order_level = "0";
        Object value = extras.get("dbdatavalue");
        if( value== null ) value= "{\"hasnodata\":\"yes\"}";
        Log.e("value", "D:" + value);
        int OKNUM = 0; int OrderNo = 0; int pushRefNum = 0;
        JSONObject obj = null;


        Gson gson = new Gson();
        String json = gson.toJson(value); //convert
        Log.e("json", "D:" + json);

        JsonParser parser = new JsonParser();
        json = parser.parse(json).getAsString();
        String order_member_num = "";
        String contract_address = "";
        String ordereanum = "";
        try {
            obj = new JSONObject(json);
            ordereanum = obj.getString("ordereanum");
            order_member_num = obj.getString("order_member_num");
            contract_address = obj.getString("contract_address");
            if( order_member_num == null) order_member_num = "0";
            if( contract_address == null) contract_address = ""; //ordereanum
            try {
                OrderNo = Integer.parseInt(ordereanum);
                pushRefNum=Integer.parseInt(push_ref_num);
            }catch(NumberFormatException e){
                OKNUM=0;
            }
            if( OrderNo > 0 && contract_address.length() > 1 && pushRefNum > 0  ){}
        } catch (JSONException e) {
            //e.printStackTrace();
        }


        if( push_ref_num == null ) push_ref_num= "0";
        if( pushRefNum == 0 && push_ref_num.length() > 0 ) pushRefNum = parseInt(push_ref_num);
        /////////////////////////////////////////////////////////////////////////////////////////디비저장하기
        Push_message_regist(obj,push_mg_title,push_reg_date,push_noti_type,pushRefNum, push_message, push_order_level );
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Log.e("GetData", "push_reg_date_Ex:" + push_reg_date);


        //Intent service = new Intent(Mcontext.getApplicationContext(), MyTell.class);
        //context.startService(service);
        if (speech_Job == null) speech_Job = "";
        notiOnce = 0;

        getTime = getTime.trim();
        samePlaceNo = samePlaceNo.trim();
        if( message==null) message = "";


        sendNotification(notiTitle,message,linkurl,noticejob,"",notiTitle, noti_realshowmg);
    }
    private void sendNotification(String Title,String messageBody,String linkurl,String notyTypen, String imageURL,String notiTitle, String noti_realshowmg) {
        Log.e("sendNotification Start", "제목:" + Title + " / 내용 :" + messageBody + " / notyTypen :" + notyTypen);
        linkurl = URL_Header + "_my_message_list.php";
        Bitmap remote_picture = null;
        if (noti_realshowmg == null) noti_realshowmg = "";
        if (imageURL.length() > 10) {
            try {
                remote_picture = BitmapFactory.decodeStream((InputStream) new URL(imageURL).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mDbOpenHelper == null) {
            mDbOpenHelper = new DbOpenHelper(Mcontext);
            //new ToastMessageTask().execute("디비초기화 " + mDbOpenHelper );
        }
        boolean textReaded = false;
        boolean ison = false;
        if (mTts != null) ison = mTts.isSpeaking();
        if (notiTitle == null) notiTitle = "";
        String message = messageBody;
        String noticejob = notyTypen;
        String NOTIOFF = "";//pref.getString("NOTIOFF", "") + "";
        String MACHINGOFF = "";//pref.getString("MACHINGOFF", "") + "";
        String push_speechset = "";//pref.getString("pushread_text_ornot", "") + "";
        String speech_typeset = "";//pref.getString("pushread_text_type", "") + "";
        float readspeed_Set = (float) 1.0;
        float readPitch_Set = (float) 1.0;
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String use_or_not_sound = "";
        String pushFn = getDbInfo[5];
        push_speechset = getDbInfo[1] + "";
        speech_typeset = getDbInfo[2] + "";
        //Log.e("읽어주기처음","push_speechset:"+ push_speechset + " / speech_typeset :" + speech_typeset );
        NOTIOFF = getDbInfo[3];
        MACHINGOFF = getDbInfo[4];
        String readspeed = getDbInfo[6];
        String readPitch = getDbInfo[7];
        //Log.e("스피치속도",  "readspeed_Info Begin readspeed : " + readspeed  + " / readPitch : " + readPitch );
        if (readspeed == null) readspeed = "1.0";
        if (readPitch == null) readPitch = "1.0";
        use_or_not_sound = getDbInfo[4];
        pushFn = getDbInfo[5];
        String infoSs = "";
        for (int i = 0; i < getDbInfo.length; i++){
            infoSs += "| GET" + i + " : " + getDbInfo[i];
        }


        //Log.e("getDbInfoGet",infoSs );
        //if (TextUtils.isEmpty(intent.getStringExtra("noticejob")))
        if( TextUtils.isEmpty(push_speechset)) {speech_typeset = "1"; push_speechset = "1";}
        if( TextUtils.isEmpty(speech_typeset)) { speech_typeset = "1"; push_speechset = "1";}
        if( ! push_speechset.equals("0") && ! push_speechset.equals("1")) push_speechset = "1";
        if( ! speech_typeset.equals("1") && ! speech_typeset.equals("2")) speech_typeset = "1";

        if( readspeed.length() > 0 ) readspeed_Set = (float) Double.parseDouble(readspeed);
        if( readPitch.length() > 0 ) readPitch_Set = (float) Double.parseDouble(readPitch);
        String TTS_message = messageBody;
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////읽어주기 시작
        AudioManager am = (AudioManager)Mcontext.getSystemService(Context.AUDIO_SERVICE);
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

        Intent speechIntent = new Intent(Mcontext, MyTell.class);
        Log.e("푸시 내용", message);
        speechIntent.putExtra("readmessage", message);
        speechIntent.putExtra("readspeed_Set", Float.toString(readspeed_Set)); // mTts.setSpeechRate(readspeed_Set);// 0.995
        speechIntent.putExtra("readPitch_Set", Float.toString(readPitch_Set)); //mTts.setPitch(readPitch_Set); // 1.0
        //Log.e("push_speechseTing",  "push_speechset : " + push_speechset + " | readspeed_Set : " + readspeed_Set  + " / readPitch_Set : " + readPitch_Set );
        String ststa = "";
        String NosoundNotice = "";
        //if( push_speechset.equals("0") ) push_speechset = "1";
        Log.e("mTts", " RingMode : " + RingMode + " / push_speechset : " + push_speechset +
                " / mTts : " +  mTts + " / readspeed_Set : " + readspeed_Set + "/ readPitch_Set : " + readPitch_Set);
        if( push_speechset.equals("1")) {
            Log.e("SPEECHCHECK", "푸시 읽어주기 설정 중");
        }else{
            Log.e("SPEECHCHECK", "푸시 안 NONONO 설정 중");
        }
        if( push_speechset.equals("1") && RingMode == 3 || RingMode == 0 ){ ////////// 무음, 진동 아닐경우
            int recc = 0;
            if(notiTitle.contains("접수알림")) recc = 1;
            if(noticejob.equals("Matching") && speech_typeset.equals("2")){  //// 음성 소리 읽어주기 고 접수를 읽어주기 설정시
                NOTIOFF ="NO";
                textReaded = true;
                Mcontext.startService(speechIntent); //mTts.speak(TTS_message, TextToSpeech.QUEUE_FLUSH,null);
                message = "장비불러 매칭오더 자세한 내용은 문자방을 참조해 주세요";
                ststa = "매칭읽어줌";

            }else if(recc ==1 && speech_typeset.equals("1")) { //// 음성 소리 읽어주기 고 매칭을 읽어주기 설정시
                textReaded = true;
                Mcontext.startService(speechIntent); // if(!ison) mTts.speak(TTS_message, TextToSpeech.QUEUE_FLUSH,null);
                NOTIOFF ="NO";
                message = "장비불러 접수오더 자세한 내용은 문자방을 참조해 주세요";
                ststa = "접수읽어줌";

            }else{
                ststa = "매칭도 접수도 아니여서 패스";
            }
        }else{
            //NosoundNotice = " 진동/무음 소리꺼짐";
            if( RingMode != 3 ){
                ststa = "진동 또는 무음이여서 반응안함";
                //Log.e("받은 내용", " 진동 또는 무음이여서 반응안함 ");
            }else{
                //Log.e("받은 내용", " 안읽어주기로 설정됨 실행안됨 ");
                ststa = "읽어주시 OFF여서 반응안함";
            }
        }
        if(noticejob.equals("Matching")){
            alertt("매칭 알림" + NosoundNotice);
        }else if(noticejob.contains("접수알림") || noticejob.equals("linkpage") ){
            alertt("접수 알림" + NosoundNotice);
        }
        //Log.e("읽어준결과", "textReaded :" + textReaded + " / ststa : " + ststa);


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////읽어주기 완료

        String patternRegex = "(?i)<br\\p{javaSpaceChar}*(?:/>|>)";
        String newline = "\n";
        String titleNameRegex = message.replaceAll(patternRegex, newline);



        Log.e("푸시소리설정값 ","use_or_not_sound : " + use_or_not_sound + " / sound_fn :" + pushFn  );
        if( noti_realshowmg.length() > 5 ) titleNameRegex = noti_realshowmg;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Mcontext, "notify_001");
        Intent ii = new Intent(Mcontext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(Mcontext, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

        bigText.bigText(message);
        bigText.setBigContentTitle(Title);
        bigText.setSummaryText(message);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.raw.bigicon);
        mBuilder.setLargeIcon(remote_picture);
        //mBuilder.setContentTitle(Title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setAutoCancel(true);



        Log.e("푸시 원본 ",message );
        Log.e("푸시 내용 ",titleNameRegex );

        if( pushFn == null) pushFn = "notisnd";
        if( use_or_not_sound == null ) use_or_not_sound= "YES";
        if( use_or_not_sound.equals("0") ) use_or_not_sound= "YES";
        Uri sound = getSondFn(pushFn,use_or_not_sound);
        String notiSndSetfn = getDbInfo[9];
        if( notiSndSetfn == null || notiSndSetfn.isEmpty()) {
            notiSndSetfn = "def";
        }
        Uri soundP = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.pushoff);
        long[] v = {100,500,300,500,300,500};
        Uri Nosound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.pushoff);


        final NotificationManager mNotificationManager = (NotificationManager) Mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("VERSION.SDK_INT", "최신폰 SDK_INT:" + Build.VERSION.SDK_INT);
        }else{
            Log.e("VERSION.SDK_INT", "이전폰 SDK_INT:" + Build.VERSION.SDK_INT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "장비불러";
            String description = "jbbuller";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("notify_001", "jbbuller", importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            String do_noti = "ON";
            if( notyTypen.equals("Matching")){
                //channel.setSound(Nosound, audioAttributes);
                channel.setSound(null, null);
                mNotificationManager.createNotificationChannel(channel);


                Ringtone r = RingtoneManager.getRingtone(Mcontext, sound);

                if( NOTIOFF.equals("NO")){
                    do_noti = "OFF";
                }else{
                    if( ! textReaded) r.play();
                }
                Log.e("푸시메칭", "정상사운드로 꽁꽝울립니다.. / 브로드 케스트 / use_or_not_sound: " + use_or_not_sound );
            }else{
                if( textReaded){
                    channel.setSound(null, null);
                    mNotificationManager.createNotificationChannel(channel);
                    //Log.e("사운드설정", "노사운드");
                    do_noti = "OFF";
                }else{
                    if( NOTIOFF.equals("NO")){
                        channel.setSound(null, null);
                        mNotificationManager.createNotificationChannel(channel);
                        //Log.e("사운드설정", "노사운드2");
                        do_noti = "OFF";
                    }else{
                        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
                        //Log.e("일반푸시일때소리", "푸시소리 " + notiSndSetfn);
                        if(notiSndSetfn.equals("def") || notiSndSetfn.equals("0")){
                            soundP= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        }else{
                            soundP = getSondFn(notiSndSetfn,"YES");
                        }
                        //Log.e("함수받은값", "푸시소리 " + soundP);
                        if(notyTypen.equals("TalkNotice")){
                            soundP = getSondFn("hellow",use_or_not_sound);
                        }

                        Ringtone r = RingtoneManager.getRingtone(Mcontext, soundP);
                        r.play();

                        channel.setSound(null, null);
                        mNotificationManager.createNotificationChannel(channel);
                        //channel.setSound(soundP, attributes);
                        //Ringtone rr = RingtoneManager.getRingtone(Mcontext, soundP);
                        //rr.play();
                        //Log.e("사운드설정", "기본사운드 DEFAULT_SOUND ");
                    }
                }
            }
            //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent resultIntent = new Intent(Mcontext, MainActivity.class);
            resultIntent.putExtra("linkUrlClick", linkurl);
            resultIntent.putExtra("NotiLInk_send", linkurl);
            resultIntent.putExtra("notiTitle", Title);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.setAction("android.intent.action.MAIN");
            PendingIntent contentIntent = PendingIntent.getActivity(Mcontext, (int) System.currentTimeMillis(),resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(contentIntent);
            //mBuilder.setDefaults(0);
            mNotificationManager.notify(0, mBuilder.build()); //if( do_noti.equals("ON") )
            /*
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(Mcontext, "jbNoTice")
                    .setLargeIcon(remote_picture)
                    .setSmallIcon(R.raw.bigicon)
                    .setColor(Color.parseColor("#000000"))
                    .setContentTitle(Title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle())
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundP)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager)Mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(7788 , notificationBuilder.build());
            */




            bedgeUpdate();
        }else{
            Log.e("노티이전버전", "notyTypen:  " + notyTypen);
            if( notyTypen.equals("Matching")){
                mBuilder.setVibrate(v);
                if( textReaded){
                    mBuilder.setSound(Nosound);
                    mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                    Log.e("푸시메칭", "글시 읽어서 진동만합니다... / " + pushFn + " / " + sound );
                }else{
                    mBuilder.setVibrate(v); //mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

                    //Log.e("푸시메칭", "오레오 이전버전 꽁꽝울립니다.. / " + pushFn + " / " + sound );

                    if(notiSndSetfn.equals("def") || notiSndSetfn.equals("0")){
                        soundP= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }else{
                        soundP = getSondFn(notiSndSetfn,"YES");
                    }
                    sound = getSondFn(pushFn,"YES");
                    if(noticejob.equals("TalkNotice")){
                        sound = getSondFn("hellow","YES");
                    }
                    mBuilder.setSound(sound);
                    //mBuilder.setSound(soundP);
                    Log.e("VXXXSOUND", "매칭소리 GET V10 :  " + soundP + " / pushFn : "+ pushFn);
                    //Ringtone r = RingtoneManager.getRingtone(Mcontext, soundP);
                    //r.play();
                }

            }else{
                if( textReaded){
                    mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                    mBuilder.setSound(Nosound);
                    //Log.e("PushCheckA", "1111111 / " + sound );
                }else{
                    if( NOTIOFF.equals("NO")){
                        //mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                        long[] vs = {0};
                        mBuilder.setVibrate(vs);
                        mBuilder.setSound(Nosound);
                        //Log.e("PushCheckA", "22222 / " + sound );
                    }else{
                        if(notyTypen.equals("TalkNotice")){
                            notiSndSetfn = "hellow";
                        }
                        if(notiSndSetfn.equals("def")){
                            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        }else{
                            sound = getSondFn(notiSndSetfn,"YES");
                            mBuilder.setSound(sound);
                            Log.e("VXXXSOUND", "접수외기타 GET V10 :  " + sound + " / notiSndSetfn : "+ notiSndSetfn);
                        }
                        //Log.e("PushCheckA", "33333 / " + sound );
                        //Ringtone r = RingtoneManager.getRingtone(Mcontext, sound);
                        //r.play();
                    }
                }
                //Log.e("푸시메칭", "오레오 이전버전 일반소리.. / " + notiSndSetfn );
            }
            Log.e("resultIntent", "Title : " + Title + " / body : " + messageBody + "/ notiSndSetfn:" + notiSndSetfn );
            //NotificationCompat.Builder mBuilderx = new NotificationCompat.Builder(Mcontext, "notify_001");

            NotificationManager notificationManager = (NotificationManager) Mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent resultIntent = new Intent(Mcontext, MainActivity.class);
            resultIntent.putExtra("linkUrlClick", linkurl);
            resultIntent.putExtra("NotiLInk_send", linkurl);
            resultIntent.putExtra("notiTitle", Title);
            resultIntent.putExtra("messageBody", messageBody);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.setAction("android.intent.action.MAIN");

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(Mcontext);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1299,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            notificationManager.notify(1191, mBuilder.build());
            bedgeUpdate();
        }


        Log.e("-- TOMAIN ---", "notyTypen : " + notyTypen);
        Log.e("Sending to Main", "메인으로 알리기 notyTypen : " + notyTypen);

        Intent resultIntent = new Intent(MainActivity.ACTION_SHOW_TEXT);
        resultIntent.putExtra("IntentJob", "PushDBUpdated");
        resultIntent.putExtra("noti_type", notyTypen);
        resultIntent.putExtra("updateno", String.valueOf(pushRefNum_Pick));
        resultIntent.putExtra("okstatus", "1");
        LocalBroadcastManager.getInstance(Mcontext).sendBroadcast(resultIntent);
        notiOnce = 1;


        SharedPreferences prefx = Mcontext.getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefx.edit();
        editor.putString("linkURL", linkurl);
        editor.apply();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void gpsUpdate(Context context,String getTime,String noticejob,String samePlaceNo){
        SharedPreferences userDetails = context.getSharedPreferences("userInfo_apset", MODE_PRIVATE); //LoginMember_mbno
        String LoginMember_mbno = userDetails.getString("LoginMember_mbno", "0");
        String last_GPSAVED = userDetails.getString("last_GPSAVED_Value", "0");
        String last_exTime = userDetails.getString("last_applyTime", "0");
        last_exTime = last_exTime.trim();

        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(Mcontext);
        String DB_menoGet = mDbOpenHelper.get_App_member_num();
        if( !TextUtils.isEmpty(DB_menoGet)) {
            Log.e("DBMBNO" , "DB_menoGet :" + DB_menoGet + " / samePlaceNo:" + samePlaceNo);
            if( DB_menoGet.length() > 0 ){
                LoginMember_mbno = DB_menoGet;
            }
        }
        notiOnce = 1;
        userGPS1 = 0;
        userGPS2 = 0;
        try{
            gps = new GPSTracker(context );
        }catch (Exception e){
            e.printStackTrace();
            Log.e("GPS꺼져있음","보고 중단");
            return;
        }
        String userAddress = "";
        int member_no = parseInt(LoginMember_mbno);
        if( last_GPSAVED.length() < 5 ) last_GPSAVED = "";
        double minGab = 1.1;
        if( samePlaceNo.equals("SameOK")) {
            minGab = 0.0;
            if(noticejob.equals("GPSSAVEONLY_NEW_sound")){
                float count=100*.01f;
                final MediaPlayer mp = MediaPlayer.create(Mcontext, R.raw.lalala);
                mp.setVolume(count,count);
                mp.start();
            }
        }
        String mggs = "last_exTime:" + last_exTime + "/getTime" + getTime;
        if( member_no > 0 && !last_exTime.equals(getTime)){ ///////////////// 회원 번호 있고 아직 실행 안한것만
            try {
                userGPS1 = gps.getLatitude();
                userGPS2 = gps.getLongitude();
                userAddress = gps.getAddress(userGPS1, userGPS2);
            } catch (Exception e) {
                return;
            }
            String user_GPS = userGPS1 + "," + userGPS2;
            user_GPS = user_GPS.trim();
            last_GPSAVED = last_GPSAVED.trim();
            String go_UrlNew = URL_Header + "gpssave_by_push.php";
            if(last_GPSAVED.equals(user_GPS)) return;

            if( last_GPSAVED.length() > 5 ) {
                String [] lngs = last_GPSAVED.split(",");
                if( lngs.length > 1){
                    if(lngs[0].length() > 4 && lngs[1].length() > 4 ){
                        DecimalFormat form = new DecimalFormat("#.#################");
                        double gpspld1 = Double.parseDouble(lngs[0]);
                        double gpspld2 = Double.parseDouble(lngs[1]);
                        double GG = distance(gpspld1,gpspld2,userGPS1,userGPS2);
                        Log.e("GPSCOMPGAB ", "회원번호 : " + member_no + "/ GAB1 :" + gpspld1 + "," + gpspld2+ "," + userGPS1+ "," + userGPS2 + " 간격은 = " +  GG);
                        if( GG < minGab  ){
                            Log.e("NOTSAVEGPS", "1KM 이하 이동됨 저장암함 minGab :" + minGab + "KM  / GG :" + GG);
                            return;
                        }
                    }
                }
            }

            if( user_GPS.length() > 5) { /////////////////////////// 다른 위치, 위치값 있을때만 저장
                SharedPreferences.Editor editor = userDetails.edit();
                editor.putString("last_GPSAVED_Value", user_GPS);
                editor.putString("last_applyTime", getTime);
                editor.apply();
                Log.e("last_GPSAVED_Value" , "마지막 저장 위치값 : " + user_GPS );

                mggs += "/ user_GPS : " + user_GPS + " last_GPSAVED : " + last_GPSAVED;
                final String goUrl = go_UrlNew;
                final int mbno = member_no;
                final String gogps = user_GPS;
                final String goadr = userAddress;
                final String mgstatus = mggs;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection mHttpURLConnection = null;
                        DataOutputStream mOutputStream = null;
                        String strLineEnd = "\r\n";
                        String strTwoHyphens = "--";
                        String strBoundary = "*****";
                        int serverResponseCode = 0;
                        try {
                            URL url = new URL(goUrl);
                            mHttpURLConnection = (HttpURLConnection) url.openConnection();
                            mHttpURLConnection.setDoInput(true); // Allow Inputs
                            mHttpURLConnection.setDoOutput(true); // Allow Outputs
                            mHttpURLConnection.setUseCaches(false); // Don't use a Cached Copy
                            mHttpURLConnection.setRequestMethod("POST");
                            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                            mHttpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                            mHttpURLConnection.setRequestProperty("Accept-Encoding", "html/text");
                            mHttpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + strBoundary);
                            mOutputStream = new DataOutputStream(mHttpURLConnection.getOutputStream());
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"mgstatus\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(mgstatus + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"user_num\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(mbno + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"userGPS\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(gogps + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"gps_getype\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes("gps" + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"gps_address\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeUTF(goadr + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            serverResponseCode = mHttpURLConnection.getResponseCode();
                            mOutputStream.flush();
                            mOutputStream.close();
                            Log.e("위치정보저장완료", "코드: " + serverResponseCode + "/ mbno: " + mbno + " + /gogps:" + gogps + "/goadr:" + goadr);
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                            Log.e("위치정보저장실패1", "MalformedURLException: " + ex.getMessage(), ex);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("위치정보저장실패2:" + e.getMessage(), "Exception");
                        }
                    };
                }).start();
            }else{
                Log.e("위치저장안함22","이전위치:" + last_GPSAVED + " / 현재위치:" + user_GPS);
            }
        }else{
            Log.e("위치저장안함","회원번호 없거나 이미적용됨 " + member_no + " / getTime:" + getTime + "/마지막실행한값:" + last_exTime);
        }
    }
    public Uri getSondFn(String pushFn,String use_or_not_sound){
        //Log.e("소리 추출" , "FN : " + pushFn + " 옵션 : " + use_or_not_sound);
        Uri sound = null;
        if( pushFn.equals("chk")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.chk);
        if( pushFn.equals("ding1")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.ding1);
        if( pushFn.equals("ding2")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.ding2);
        if( pushFn.equals("ding3")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.ding3);
        if( pushFn.equals("gllag")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.gllag);
        if( pushFn.equals("mbclog")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.mbclog);
        if( pushFn.equals("notisnd")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.notisnd);
        if( pushFn.equals("ring1")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.ring1);
        if( pushFn.equals("ring2")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.ring2);
        if( pushFn.equals("hellow")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.jbtalk_snd01);
        if( sound==null) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.notisnd);
        if( ! use_or_not_sound.equals("YES")) sound = Uri.parse("android.resource://" + Mcontext.getPackageName() + "/" + R.raw.pushoff);
        return sound;
    }
    public void bedgeUpdate(){
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String NotiBedgeStr = getDbInfo[8] + "";
        int NotiBedgeNum = 0;
        if( NotiBedgeStr.isEmpty()) NotiBedgeStr = "";
        if( NotiBedgeStr.length() < 1 ) NotiBedgeStr = "0";
        if( NotiBedgeStr.length() > 0 ) NotiBedgeNum = parseInt(NotiBedgeStr);
        NotiBedgeNum = NotiBedgeNum + 1;
        String launcherClassName = getLauncherClassName(Mcontext);
        if (launcherClassName == null) {return;}
        Intent intentX = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intentX.putExtra("badge_count", NotiBedgeNum);
        intentX.putExtra("badge_count_package_name", Mcontext.getPackageName());
        intentX.putExtra("badge_count_class_name", launcherClassName);
        mDbOpenHelper.SetSettingvalue("seting_8", String.valueOf(NotiBedgeNum));
        //Log.e("BadgeInfo ", "NotiBedgeNum=" + NotiBedgeNum);
        //Log.e("PakageGet", Mcontext.getPackageName());
        //Log.e("launcherClassName", launcherClassName);
        Mcontext.sendBroadcast(intentX);

    }
    public void alertt(String text) {
        //Log.e("alertt", " text GCM : " + text);
        Toast.makeText(Mcontext, text, Toast.LENGTH_SHORT).show();
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
    private void sendNotification_temp(String Title,String messageBody,String linkurl,String notyTypen, String imageURL) {
        Log.e("PUSHSOUNDPLAY", "브로드케스트");
    }
    public boolean containsOnlyNumbers(String str) {
        if (str == null || str.length() == 0)return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))return false;
        }return true;
    }
}
