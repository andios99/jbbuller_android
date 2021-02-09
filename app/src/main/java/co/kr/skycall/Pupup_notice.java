package co.kr.skycall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Pupup_notice extends Activity {
    private Context mContext;
    private String call_number;
    private String call_mode;
    private String my_num_reg_badone ;
    private String Call_person;
    private String my_mbno;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_notice);
        this.setFinishOnTouchOutside(false);
        Bundle extras = getIntent().getExtras();

        SharedPreferences pref = getSharedPreferences("noti_Setting", Context.MODE_PRIVATE);
        my_mbno = pref.getString("SkyMbno", "");
        Log.e("악덕알림 시작", "내 회원번호 :" +my_mbno  );

        if (extras != null) {
            call_number = extras.getString("pnum");
            call_mode = extras.getString("callmode");
            call_mode = extras.getString("callmode");
            my_num_reg_badone = extras.getString("my_num");
            String ref_num = extras.getString("ref_num");

            String bad_coname = extras.getString("bad_coname");
            final String writer_ph = extras.getString("writer_ph");
            String bad_message = extras.getString("memo");
            //bad_message="작년 8월달에 3일 작업후 입금 약속을 차잏 피일 미루다 수신거부로 전화를 받지않음 열받아서 소액 재판를 신청했는데 휴대전화번호가 차명이라서 주소 수배가안됨 카톡보면 놀러다니며 여유로운 생활함 돈은 안받아도 되는데 괴롭히고 싶습니다 작년 8월달에 3일 작업후 입금 약속을 차잏 피일 미루다 수신거부로 전화를 받지않음 열받아서 소액 재판를 신청했는데 휴대전화번호가 차명이라서 주소 수배가안됨 카톡보면 놀러다니며 여유로운 생활함 돈은 안받아도 되는데 괴롭히고 싶습니다작년 8월달에 3일 작업후 입금 약속을 차잏 피일 미루다 수신거부로 전화를 받지않음 열받아서 소액 재판를 신청했는데 휴대전화번호가 차명이라서 주소 수배가안됨 카톡보면 놀러다니며 여유로운 생활함 돈은 안받아도 되는데 괴롭히고 싶습니다";
            String reg_date = extras.getString("reg_date");
            String br = System.getProperty("line.separator");
            String show_message = bad_message;
            if( bad_coname.length() > 0 ){
                show_message = bad_coname + br + br + bad_message;
            }
            String regDate = "등록일 : " + reg_date;
            Log.e("악덕알림", "콜 유형 :" + call_mode + "/ my_num_reg_badone :" + my_num_reg_badone  );
            if( call_mode.equals("incomering")){
                Log.e("걸려온전화", "푸시보내고 디비등록하기 : " + my_num_reg_badone + " / " + my_mbno  + " / " + ref_num);
                report_badCopany_Called(my_mbno,my_num_reg_badone,ref_num);
            }

            show_message = "장비불러에 등록된 악덕업체 입니다." + br + br + show_message;
            TextView Tv2 = (TextView)findViewById(R.id.textView2);
            TextView Tv3 = (TextView)findViewById(R.id.textView3);
            Tv2.setMovementMethod(new ScrollingMovementMethod());
            Tv2.setText(show_message);
            Tv3.setText(regDate);
            //Tv3.setText(call_number);

            Button BTN1 = (Button) findViewById(R.id.button1);
            Button BTN2 = (Button) findViewById(R.id.button2);
            BTN1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + writer_ph));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            BTN2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }
    public void report_badCopany_Called(String mynum, String regd_num, String ref_num){ //
        String url="http://jbbuller.kr/_badcompany_call_report.php?mynum=" + mynum + "&reg_mbno=" + regd_num + "&ref_num=" + ref_num ;
        //http://6n4.co.kr/mobile/_pnumber_info_get_all.php?gtype=memos&mb_no=" + This_mb_no + "&w_date=" + w_date;
        Log.e("악덕보고시작 ", url);
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
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }
                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result){
                Log.e("RRR", "RESULT :"  + result);
                try {
                    JSONObject json_data = new JSONObject(result);
                    String resultMg = json_data.getString("result");
                    Log.e("악덕알림결과","Value:" + resultMg );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("악덕알림", "onStart");
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.e("악덕알림", "onStop");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("악덕알림", "onResume");

    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.e("악덕알림", "onPostResume");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("악덕알림", "onDestroy");
    }
}
