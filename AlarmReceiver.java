package co.kr.skycall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    public GPSTracker gps;
    public String user_num;
    public double userGPS1;
    public double userGPS2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        SharedPreferences userDetails = context.getApplicationContext().getSharedPreferences("userInfo_apset", context.MODE_PRIVATE);
        String member_Num=userDetails.getString("USERNUMBER", "");
        String GPSGap=userDetails.getString("USERNUGPSGABMBER", "");

        gps = new GPSTracker(context);
        userGPS1 = 0;
        userGPS2 = 0;

        try {
            userGPS1 = gps.getLatitude();
            userGPS2 = gps.getLongitude();

        } catch (Exception e) {
        }
        String GPSGvalue = userGPS1 + "," + userGPS2;
        Intent Inew = new Intent(context, GCMIntentService.class);

        Inew.putExtra("noticejob", "GPSSAVE");
        Inew.putExtra("userNum", member_Num);
        Inew.putExtra("gpsinfo", GPSGvalue);
        context.startService(Inew);
        Toast.makeText(context, strDate, Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, new Date().toString(),Toast.LENGTH_SHORT).show();.show();
    }

}
