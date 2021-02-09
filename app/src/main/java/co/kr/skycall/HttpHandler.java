package co.kr.skycall;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class HttpHandler extends MainActivity {
    private DbOpenHelper mDbOpenHelper = null;
    private Context MainContext;
    private String message_LastDate = "2019-01-01 00:00:00";
    private String DB_MBNO = "0";
    private String orderListMaxno = "0";
    private String message_MinDate = "2025-01-01 00:00:00";
    private Activity mActivity;
    String [] fds1 = {"num","noti_type","noti_from_unum","noti_from_type","order_level","sub_level","my_num","chr_soo",
            "bonsa","read_cnt","gisa","ref_num","mg_title","mg_body","job_dtype","reg_date","keepit",
            "o_stepcnt","ea_infos","action_doer","order_from","order_fromnum"};
    String [] fds2 = {"orderNo"," order_id","order_type","order_status","status_acter","soojoo_oknum","work_day","contract_address","car_order_msg","order_member_num",
            "order_mem_ownernum","worker_num","worker_ownernum","work_time","work_start_h","work_start_m","worker_regdate","car_price","worker_recivelevel","worker_recivelevelsub",
            "orderwork_type","work_payment","howtopay","howto_payorg","old_paydone","order_member_name","order_member_phone","order_onwer_name","order_onwer_phone","worker_name",
            "worker_owner_name","worker_carnum","worker_directphone","car_uniqnum","spcial_got","getorder_phone","work_onwer_phone","getorder_name","worker_dirvernum","worker_directtype",
            "worker_carplate","worker_num2","worker_directname2","worker_directphone2","imui_order_memnum","imui_order_memname","imui_order_memphone","imui_order_mb_no","imui_order_mb_owner",
            "imui_order_mb_ownername","imui_order_mb_ownerphone","imui_recive_memnum","imui_recive_memname","imui_recive_memphone","imui_recive_mb_no","imui_recive_mb_owner",
            "imui_recive_mb_ownername","imui_recive_mb_ownerphone","order_regtime","last_time","last_setSec","cancel_date","cancel_status","order_bonsanum","order_gisanum","order_peerate",
            "order_member_id","order_phone","order_detail","car_ownernum","callhasmade","dex_drivername","worker_bonsa","worker_gisa","worker_levels","worker_cnt","worker_exe_tnum",
            "car_ownername","job_type","car_type","car_typesub","car_tons","car_size_from","car_size_to","floor_sel","move_distance","car_option","car_orderoption","car_height","car_workdaytype",
            "car_workhour","car_workaddhor","car_worktype","car_worktype_more","car_work_distance","car_amount","crain_gorilaset","sign_howlong_int","sign_howlong_str","howtopay_done",
            "paycard_refnum","pay_user_input","pay_user_input2","workextra_type","workextra_price","workextra_cnt","payed_date","contract_number","contract_fieldnumber","contract_name",
            "contract_message","jobdonemsg","paydonemsg","zipcode","mem_basepoint","isit_myine","bill_print_note","bill_print_tax","ordermade_from","order_push_cntnum","order_handle_by",
            "order_handle_date","order_handle_job","isresendorder","soojoo_check","baljoo_check","ocancel_check","order_memo","order_hideto","togo_now","togo_all","togo_gab","pure_matched",
            "ref_photocnt","tax_printed_to_giver","tax_printed_ref_num","order_full_adr","order_road_adr","search_type","adr_city_dong","price_changed","has_note","pricechange_bal",
            "pricechange_soo","order_gijung","month_code","okcheksoo","orderPerson","ref_push_num","ref_push_info","orderPerson_who","is_easy_order","easy_reforderno","was_whanwon"};
    private String actType = "";
    private String RefrashRecentDate = "2019-01-01 00:00:00";
    private int start_getDBCnt = 0;
    private int dbReadMode = 0;
    public HttpHandler(Context context, Activity activity) {
        MainContext = context;
        mActivity = activity;
        mDbOpenHelper = new DbOpenHelper(MainContext);
    }
    public void reset_DBOnce(){
        start_getDBCnt = 0;
        dbReadMode = 1;
    }
    public void get_and_makeMessageList(int xx){
        if(mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(MainContext);
        if(start_getDBCnt > 0 ) {
            Log.e("한번호출됨" , "DB다시 읽지않음");
            return;
        }
        String [] pre_Data = {};
        try {
            pre_Data = mDbOpenHelper.init_messageDb();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String Test_DB_MBNO = pre_Data[1];
        message_LastDate = pre_Data[0];
        orderListMaxno = pre_Data[2];
        message_MinDate = pre_Data[0];
        String mgCnt = pre_Data[4];

        if( Test_DB_MBNO == null) Test_DB_MBNO = "0";
        Log.e("MemberInfoGetDB" , "회원번호:" + Test_DB_MBNO + " /마지막날짜  :" + message_LastDate + " / OrderMaxNo : "+ orderListMaxno) ;
        if(xx==99) return;
        if( !Test_DB_MBNO.equals("0")){
            DB_MBNO = pre_Data[1];
            if(!DB_MBNO.equals("0") && !DB_MBNO.equals(Test_DB_MBNO)){
                mDbOpenHelper.reset_push_order_DB(2);
                Log.e("처음값 호출함", "get_and_makeMessageList 회원번호 달라서 실행됨 !!!!!!!!!!!!!!!!!!!!!" );
                My_messageList_get("notmatchmbno", "", MainContext);
            }else{
                if( mgCnt == null) mgCnt = "0";
                if( mgCnt.length() < 1 ) mgCnt = "0";
                int mgCntInt = parseInt(mgCnt);
                if ( mgCntInt < 50 ) {
                    Log.e("처음값 호출함", "get_and_makeMessageList 50개 미만이여서 실행됨 !!!!!!!!!!!!!!!!!!!!!" );
                    My_messageList_get("firstLoading", "", MainContext);
                }else {
                    Log.e("처음값 호출함", "이도저도 아님 서버 호출 안함!");
                }
            }

            start_getDBCnt++;
            //if( xx < 1 ) messageMaxno = 0;
            //My_messageList_get("", "", MainContext);
        }
    }


    public void My_messageList_get(String act_type, String Refresh_recentDate , final Context  context){
        if( act_type == "recentListGet" && Refresh_recentDate.length() > 10 ) message_LastDate = Refresh_recentDate;
        if( message_LastDate== null ) message_LastDate = "";
        if( act_type.equals("refresh") && Refresh_recentDate.length() >= 10 ){
            Log.e("검색 조건", " reg_date < " + Refresh_recentDate);
        }else{
            if( message_LastDate.length() >= 10){
                Log.e("검색 조건", " reg_date > " + message_LastDate);
            }
        }
        String url = "http://jbbuller.kr/_sky_m_01_java_get_message_list.php?act=" + act_type + "&lowerno=" +Refresh_recentDate+ "&mbno=" + DB_MBNO +"&maxno=" + message_LastDate;
        Log.e("My_messageList_get :  ", url);
        actType = act_type;
        RefrashRecentDate = Refresh_recentDate;

        final Context myCont = context;
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
                String pushNo = "";
                Log.e("My_messageList_get", "RESULT :" + result);
                try {
                    JSONObject json_data = new JSONObject(result);
                    JSONArray son_data_list = json_data.getJSONArray("result");
                    int size = son_data_list.length();
                    ArrayList<JSONObject> arrays = new ArrayList<JSONObject>();
                    Log.e("푸시메시지갰수", "Count :" + size + " / LowerNo : " + RefrashRecentDate);
                    int OKNUM = 0;
                    for (int i = 0; i < size; i++) {
                        JSONObject another_json_object = son_data_list.getJSONObject(i);
                        arrays.add(another_json_object);

                        int orderNo = 0;
                        String push_regDate = another_json_object.getString("reg_date");
                        String ordernoStr = another_json_object.getString("caroderlistnum");
                        if( ordernoStr.equals("") || ordernoStr.equals("_") ) ordernoStr= "0";
                        if(ordernoStr == null || ordernoStr.trim().equals("")) ordernoStr = "0";
                        if( push_regDate.length() >= 10 && pushNo.equals("")) pushNo = push_regDate;
                        int ok = mDbOpenHelper.save_message_orderList(another_json_object);
                        if( ok > 0){
                            OKNUM++;
                        }
                    }
                    String mmg = "actType :" + actType + " / RefrashRecentDate :" + RefrashRecentDate + "/ dbReadMode: " + dbReadMode + " /pushNo : " + pushNo ;
                    Log.e("PushOrderDone", mmg + " | orderNo : " + orderListMaxno + " TotalAdded : " + OKNUM );
                    if( ( (actType.equals("recentListGet") ) && RefrashRecentDate.length() >= 10) || dbReadMode == 1 || actType.equals("firstLoading") || actType.equals("notmatchmbno") ){
                        dbReadMode = 0;
                        Log.e("새로고침임", "로컬에새로고침알림");
                        new MainActivity.Push_UpdateDo(mActivity, MainContext).execute(RefrashRecentDate);
                    }
                } catch (JSONException e) {
                    Log.e("리스트받기에러", "ERR : " + e);
                    e.printStackTrace();
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }





}
