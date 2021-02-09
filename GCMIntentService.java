package co.kr.skycall;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public NotificationManager mNotificationManager;
    public static int NoticeCountNum = 0;
    public String NowURL = "";
    public String URL_Header = "http://192.168.1.7/";
    NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra("message");
        String notiTitle = intent.getStringExtra("notiTitle");
        String linkurl = intent.getStringExtra("linkurl");
        String noticejob = intent.getStringExtra("noticejob") + "";
        String userNum = intent.getStringExtra("userNum") + "";
        String gpsinfo = intent.getStringExtra("gpsinfo") + "";
        Log.e("noticejob", "noticejob= : " + noticejob);
        if(noticejob.equals("GPSSAVE")){
            Log.e("GPS SAVE", "SAVESAVESAVESAVESAVESAVE");
            String go_UrlNew = URL_Header + "_app_location_refresh_save_re2.php";
            HttpPost httppost = new HttpPost(go_UrlNew);
            HttpClient httpclient = new DefaultHttpClient();
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_num", userNum + ""));
                nameValuePairs.add(new BasicNameValuePair("userGPS", gpsinfo + ""));
                nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                Log.e("saveGPSOK", "goStrurl : " + userNum + " / userGPS : " + gpsinfo + " URL :" + go_UrlNew );
            } catch (ClientProtocolException e) {} catch (IOException e) {
                Log.e("saveGPSNO", "goStrurl : " + userNum + " / userGPS : " + gpsinfo + " URL :" + go_UrlNew );
            }

        }else{
            NotificationManagerJob(getApplicationContext(), intent);
            GcmBroadcastReceiver.completeWakefulIntent(intent);
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
    public void NotificationManagerJob(Context context, Intent intentx) {
        String message = intentx.getStringExtra("message");
        String notiTitle = intentx.getStringExtra("notiTitle");
        String linkurl = intentx.getStringExtra("linkurl");
        String noticejob = intentx.getStringExtra("noticejob") + "";
        String userNum = intentx.getStringExtra("userNum") + "";
        String gpsinfo = intentx.getStringExtra("gpsinfo") + "";

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder mBuilder = new Notification.Builder(this);

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setTicker(notiTitle); ////null 로 하면 팝업 안됨
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setNumber(10);
        mBuilder.setContentTitle(notiTitle);
        mBuilder.setContentText(message);

        if(noticejob==null) noticejob="";

        //Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.skysnd2);
        if (noticejob.equals("Matching")) {
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notisnd);
            mBuilder.setSound(sound);
        }else{
            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }
        //AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //int streamMaxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        //manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, streamMaxVolume, AudioManager.ADJUST_SAME);



        Intent myIntent = new Intent(this, MainActivity.class); /////////////////////
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        myIntent.putExtra("moveUrl", "Show_NotiActivity");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT > 15) {
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            nm.notify(111, mBuilder.build());
        }else{
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(notiTitle)
                            .setContentText(message);
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            NotificationManager manager_x = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager_x.notify(1222, builder.build());
        }
        new ToastMessageTask().execute(notiTitle);
        if(linkurl==null || noticejob==null){
            linkurl = "";
        }else{
            if(linkurl.length() < 10 || noticejob.indexOf("enW")==0) linkurl = "";
        }

        SharedPreferences pref = getSharedPreferences("noti_order", Context.MODE_PRIVATE);
        String ntcnum = pref.getString("NoticNum", "");
        if (!containsOnlyNumbers(ntcnum)) ntcnum = "0";
        NoticeCountNum = Integer.parseInt(ntcnum);
        NoticeCountNum = NoticeCountNum + 1;

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("linkURL", linkurl);
        editor.putString("NoticNum", String.valueOf(NoticeCountNum));
        Log.e("ChagetoString=",String.valueOf(NoticeCountNum));
        Log.e("(NoticeCountNum)=",String.valueOf(NoticeCountNum));
        ///// Log.e("Notice Link url =",linkurl + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
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