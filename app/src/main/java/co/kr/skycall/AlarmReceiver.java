package co.kr.skycall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        Log.e("onReceive", "AlarmReceiver STraing");
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
            return;
        }
        final String GPSGvalue = userGPS1 + "," + userGPS2;
        Intent Inew = new Intent(context, AndroidLocationServices.class);
        Log.e("AlarmReceiver onReceive", "USERNUMBER :" + member_Num + " / gps1" + userGPS1 + " / " + "gps2 :" + userGPS2);
        Inew.putExtra("noticejob", "GPSSAVE");
        Inew.putExtra("userNum", member_Num);
        Inew.putExtra("gpsinfo", GPSGvalue);
        final String userAddress = gps.getAddress(userGPS1, userGPS2);
        final String member_NumGoo = member_Num;
        new Thread(new Runnable(){
            @Override
            public void run() {
                try { //http://jbbuller.kr/
                    String go_UrlNew = "http://jbbuller.kr/_app_location_refresh_save_re2.php";
                    HttpPost httppost = new HttpPost(go_UrlNew);
                    HttpClient httpclient = new DefaultHttpClient();
                    try {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("user_num", member_NumGoo + ""));
                        nameValuePairs.add(new BasicNameValuePair("userGPS", GPSGvalue + ""));
                        nameValuePairs.add(new BasicNameValuePair("gps_getype", "gps"));
                        nameValuePairs.add(new BasicNameValuePair("gps_address", userAddress));
                        nameValuePairs.add(new BasicNameValuePair("isGps", "1"));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                        httppost.setEntity(ent);
                        HttpResponse response = httpclient.execute(httppost);
                        Log.e("AlarmReceiver 에서저장함", "Mbno : " + member_NumGoo + " /  userGPS1 : " + userGPS1 + " / userGPS2 : " + userGPS2 + "/ UserJooso :" + userAddress + "/ 결과:" + response);
                    } catch (ClientProtocolException e) {} catch (IOException e) {
                        Log.e("saveGPSNO", "goStrurl : " + member_NumGoo + " / userGPS : " + GPSGvalue + " URL :" + go_UrlNew );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();


        //context.startService(Inew);
        //Toast.makeText(context, strDate, Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, new Date().toString(),Toast.LENGTH_SHORT).show();.show();
    }

    private String resize(String path) {
        Bitmap b = BitmapFactory.decodeFile(path);
        File file_test = new File(path);

        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            int rotation = 0;
            if (orientation == 6) rotation = 90;
            else if (orientation == 3) rotation = 180;
            else if (orientation == 8) rotation = 270;
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotated = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                // Pretend none of this ever happened!
                b.recycle();
                b = rotated;
                rotated = null;
            }
        } catch (Exception e) {
        }
        int orgw = b.getWidth();
        int orgh = b.getHeight();
        float ww = (float) 1500.00;
        float hh = (float) 1500.00;
        int goww;
        int gohh;
        float r = ww / (float) orgw;
        if (orgw > ww || orgh > hh) {
            if (orgw > orgh) {
                goww = (int) ww;
                gohh = (int) ((float) orgh * r);
            } else {
                r = hh / (float) orgh;
                gohh = (int) hh;
                goww = (int) ((float) orgw * r);
            }
        } else {
            goww = orgw;
            gohh = orgh;
        }
        b = Bitmap.createScaledBitmap(b, goww, gohh, true);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/temp/");
        if (!myDir.exists()) myDir.mkdirs();
        String fnamealltogo = "ImageuploadTempxto_one.jpg";
        String ret_fn = root + "/temp/" + fnamealltogo;
        File file = new File(myDir, fnamealltogo);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return ret_fn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
