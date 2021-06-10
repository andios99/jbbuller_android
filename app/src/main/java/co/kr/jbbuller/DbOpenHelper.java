package co.kr.jbbuller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.firebase.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
public class DbOpenHelper {
    private static final String DATABASE_NAME = "SKYCALLDB.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DataBaseHelper mDBHelper;
    private Context mCtx;
    private String authority = "kr.jbbuller.voicerecorder.myContentProvider";
    public Share_utils Util = null;
    private String [] fds1 = {"num","noti_type","noti_from_unum","noti_from_type","order_level","sub_level","my_num","chr_soo",
            "bonsa","read_cnt","gisa","ref_num","mg_title","mg_body","job_dtype","reg_date","keepit",
            "o_stepcnt","ea_infos","action_doer","order_from","order_fromnum"};
    private String [] fds2 = {"caroderlistnum","order_id","order_type","order_status","status_acter","soojoo_oknum","work_day","serch_keyword","contract_address","car_order_msg","order_member_num",
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
    public DbOpenHelper(Incoming_ready incoming_ready) {}
    public String get_RecordTableList(){
        String DataList = "";
        return DataList;
    }
    private class DataBaseHelper extends SQLiteOpenHelper{
        /**
         * 데이터베이스 헬퍼 생성자
         * @param context   context
         * @param name      Db Name
         * @param factory   CursorFactory
         * @param version   Db Version
         */
        public DataBaseHelper(Context context, String name,SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            if( Util == null) Util = new Share_utils(context);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            //Log.e("DataBaseHelper", "최초 DB를 만들 때 한번만 호출");
            db.execSQL(DataBases.CreateDB._CREATE);
            db.execSQL(DataBases.CreateDB._CREATE2);
        }//버전이 업데이트 되었을 경우 DB를 다시 만들어주는 메소드
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //업데이트를 했는데 DB가 존재할 경우 onCreate를 다시 불러온다
            //db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }
    //DbOpenHelper 생성자
    public void chColumeExists(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        Cursor dbCursor = mDB.query("_car_order_lists", null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        int ok = 0;
        for(int x = 0; x < columnNames.length; x++){
            if( columnNames[x].equals("serch_keyword")){
                ok++;
                break;
            }
        }
        Log.e("chColumeExists", "serch_keyword OK : " + ok );
        if( ok < 1){
            String UpdateSql = "ALTER TABLE _car_order_lists ADD COLUMN serch_keyword varchar(100) DEFAULT NULL";
            try {
                mDB.execSQL(UpdateSql);
                Log.e("Sql 업데이트됨", "UpdateSql : " + UpdateSql);
            }catch(Exception e) {
                Log.e("Sql 에러", "Alter Error : " + UpdateSql);
            }
        }else {
            Log.e("체크결과", "ok : " + ok);
        }
    }
    public DbOpenHelper(Context context) {
        this.mCtx = context;
        if( Util == null) Util = new Share_utils(context);
    }
    private String getNow(){
        // set the format to sql date time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    public void reset_BadCo_list(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        mDB.execSQL("DROP TABLE IF EXISTS skyCall_badCompanyList;");
    }
    public int UpdateDB_push_order(String DBname, ContentValues Datas , int TargetNo) {
        if( mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if( mDB == null) mDB = mDBHelper.getWritableDatabase();
        mDB.update(DBname, Datas, "num="+TargetNo, null);
        close();
        return 1;
    }
    public int save_message_orderList(JSONObject data ){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        ContentValues sendData=new ContentValues();
        ContentValues OrderdData=new ContentValues();
        for (String val : fds1) {
            try {
                String eaval = data.getString(val);
                if( eaval == null) eaval = "";
                if( eaval.length()>0 && !val.equals("num")) sendData.put(val, eaval);
            } catch (JSONException e) {
                //Log.e("RET ERR", "ER:" + e.getCause() );
                //e.printStackTrace();
            }
        }
        String dontsaveOrder="";
        try {
            dontsaveOrder = data.getString(dontsaveOrder);
        } catch (JSONException e) {

        }
        if( ! dontsaveOrder.equals("no") ){
            for (String val : fds2) {
                try {
                    String eaval = data.getString(val);
                    if( eaval == null) eaval = "";
                    if( val.equals("caroderlistnum") || val.equals("ordereanum")) val = "num";
                    if( eaval.length()>0 )OrderdData.put(val, eaval);
                } catch (JSONException e) {
                    //Log.e("RET ERR", "ER:" + val + " / " +  e.getCause() );
                    //e.printStackTrace();
                }
            }
        }

        String numCh = (String) sendData.get("num");
        if( numCh != null ) sendData.remove("num");
        Log.e("푸시오더값", "Data : " + sendData); //_car_order_lists  user_push_messages

        //Log.e("오더장하기", "Data : " + OrderdData); //_car_order_lists  user_push_messages
        String ref_num = (String) sendData.get("ref_num");
        String noti_type = (String) sendData.get("noti_type");
        String chSql1 = "Select num from user_push_messages where ref_num=" + ref_num + " and noti_type='" + noti_type + "'" ;
        Cursor TestCr = mDB.rawQuery(chSql1, null);
        Log.e("PushMsgSerch", "sQL: " + chSql1 );

        int OK=0;
        if( TestCr == null || ! TestCr.moveToFirst()){
            mDB.insert("user_push_messages", null,sendData);
            Log.e("알림 입력", "Data: " + sendData );
            OK = 9;
        }else{
            TestCr.moveToFirst();
            ContentValues sendDataN=new ContentValues();
            //sendDataN.put("reg_date", (String) sendData.get("reg_date"));
            String order_level = (String) sendData.get("order_level");
            String sub_level = (String) sendData.get("sub_level");
            if( order_level==null) order_level="0";
            if( sub_level==null) sub_level="0";
            //sendDataN.put("reg_date", (String) sendData.get("reg_date"));
            sendDataN.put("mg_body", (String) sendData.get("mg_body"));
            sendDataN.put("mg_title", (String) sendData.get("mg_title"));
            sendDataN.put("order_level", order_level);
            sendDataN.put("sub_level", sub_level);
            Date dt = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String check = dateFormat.format(dt);
            sendDataN.put("reg_date", check);


            int oldnum = TestCr.getInt(TestCr.getColumnIndex("num"));
            int rst = mDB.update("user_push_messages", sendDataN, "num="+ oldnum, null);
            Log.e("알림 업데이트", "Data: " + sendDataN + " where num=" + oldnum + " / rst :" + rst);
            OK = 1;
        }

        String OrderNo = (String) OrderdData.get("num");
        if(OrderNo==null ) OrderNo = "0";
        if( ! OrderNo.equals("0") && OrderNo.length() > 1  && ! noti_type.equals("시스템알림") && ! noti_type.equals("새공지사항 알림") ){
            if( OK < 1 ) OK = 1;
            String chSql2 = "Select num from _car_order_lists where num=" + OrderNo ;
            TestCr = mDB.rawQuery(chSql2, null);
            Log.e("오더체크", "SQL: " + chSql2 );
            if( TestCr == null || ! TestCr.moveToFirst()){
                Log.e("없음", "저장 : " + OrderdData);
                mDB.insert("_car_order_lists", null,OrderdData);
            }else{
                Log.e("있음", "업데이트 where num=" + OrderNo);
                mDB.update("_car_order_lists", OrderdData, "num="+OrderNo, null);
            }
        }else{
            Log.e("오더저장안함", "오더번호없음: " + OrderNo + " / noti_type : "+ noti_type );
            Log.e("NoData", "E:" + data);
        }
        close();
        return OK ;
    }
    public int reset_push_order_DB(int d){
        if( mDBHelper == null)mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null)mDB = mDBHelper.getWritableDatabase();
        if( d > 0 ) mDB.execSQL("DROP TABLE IF EXISTS user_push_messages");
        if( d > 1 ) mDB.execSQL("DROP TABLE IF EXISTS _car_order_lists");
        String order_list = make_orderlist_Sql();
        mDB.execSQL(order_list);
        //Log.e("디비시작", order_list);
        String SqlMessage = make_messageList_sql();
        mDB.execSQL(SqlMessage);

        //Log.e("DBRESET" , "회원번호틀려초기화");
        close();
        return 1;
    }
    public String Test_OrderListshow(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from _car_order_lists where num > 0 order by num desc";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("sql", sql);
        String str = "";
        int TCnt = 0;
        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            TCnt = cursor.getInt(0);
            Log.e("Check", "==========================오더리스트 총 수 " + TCnt + "===============================");
            do {
                String onum = cursor.getString(cursor.getColumnIndex("num"));
                String order_type = cursor.getString(cursor.getColumnIndex("order_type"));
                String order_status = cursor.getString(cursor.getColumnIndex("order_status"));
                String addr = cursor.getString(cursor.getColumnIndex("contract_address"));
                String eainfos = cursor.getString(cursor.getColumnIndex("order_handle_by"));
                String serch_keyword = cursor.getString(cursor.getColumnIndex("serch_keyword"));
                str+=onum + " | " + order_type + " | " + order_status + " | " + addr + "|" + eainfos;
                Log.e("EA : ", onum + " | " + order_type + " | " + order_status + " | " + addr + " |KEY: " + serch_keyword );
            } while (cursor.moveToNext());
        }
        close();
        return str;
    }
    public String Test_show_MessageLIst(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from user_push_messages where num > 0 order by reg_date desc";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("sql", sql);
        String str = "";
        int TCnt = 0;


        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            TCnt = cursor.getInt(0);
            Log.e("Check", "==========================오더리스트 총 수 " + TCnt + "===============================");
            do {
                String onum = cursor.getString(cursor.getColumnIndex("num"));
                String noti_type = cursor.getString(cursor.getColumnIndex("noti_type"));
                String mg_title = cursor.getString(cursor.getColumnIndex("mg_title"));
                String ref_num = cursor.getString(cursor.getColumnIndex("ref_num"));
                String reg_date = cursor.getString(cursor.getColumnIndex("reg_date"));
                String order_level = cursor.getString(cursor.getColumnIndex("order_level"));

                String mg_body= cursor.getString(cursor.getColumnIndex("mg_body"));
                str+=onum + " | " + noti_type + " | " + ref_num + " | " + reg_date + "|" + mg_title ;

                Log.e("EA : ", onum + " | " + noti_type + " | " + ref_num + " | " + reg_date + "|" + order_level + "|" + mg_title + " | " + mg_body );
            } while (cursor.moveToNext());
        }
        close();
        return str;
    }

    public String get_App_loginInfo_Each(String tTitle){
        String sql="Select * from LoginInfoSaved where fd_title='" + tTitle +"'";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("sql", sql);
        String valueStr = "";
        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            valueStr = cursor.getString(cursor.getColumnIndex("fd_value"));
        }
        return valueStr;
    }
    public String  getApps_loginInfo(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from LoginInfoSaved";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("sql", sql);
        String[] str = {};
        int TCnt = 0;
        String goStr = "";
        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            TCnt = cursor.getInt(0);
            Log.e("Check", "====================로그인정보 필드수 " + TCnt + "========================"); //
            do {
                String onum = cursor.getString(cursor.getColumnIndex("num"));
                String fd_title = cursor.getString(cursor.getColumnIndex("fd_title"));
                String fd_value = cursor.getString(cursor.getColumnIndex("fd_value"));
                String td_type = cursor.getString(cursor.getColumnIndex("td_type"));
                goStr += fd_title + ":" + fd_value + ":" + td_type + "|";
            } while (cursor.moveToNext());
        }
        close();
        return goStr;
    }
    public String clearLoginInfos(){
        init_LoginTable();
        String Sql = "delete from LoginInfoSaved where num > 0 ";
        mDB.execSQL(Sql);
        return "OK";
    }
    public String LoginDbInputUpdate(String Sql){
        mDB.execSQL(Sql);
        return "OK";
    }
    public String init_LoginTable(){
        if( mDBHelper == null) {
            mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
            mDB = mDBHelper.getWritableDatabase();
        }
        String SqlLogin = "CREATE TABLE IF NOT EXISTS LoginInfoSaved (" +
                "num integer primary key autoincrement, " +
                "fd_title text default '' ," +
                "fd_value text default ''," +
                "td_type text default '')";
        mDB.execSQL(SqlLogin);
        return SqlLogin;
    }

    public Map<String,String>  get_savedLoginInfos(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from LoginInfoSaved";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("sql", sql);
        String[] str = {};
        int TCnt = 0;
        Map<String,String> myMap1 = new HashMap<String, String>();
        /*
        userPhoneInfo:2792,1170,,:array
        member_mygisaList:20,21,22,0,0,0:array
        login_id:010-4451-1212:string
        logintype:driver:string
        member_mybonsa:12:integer
        member_mygisa:20:integer
        member_num:10:integer
        mem_ownercode:10:integer
        login_name:전태현:string
        member_phone:010-4451-1212:string

        myMap1.put("userPhoneInfo", "12");
        myMap1.put("member_mygisaList", "13");
        myMap1.put("login_id", "14");
        myMap1.put("logintype", "15");
        myMap1.put("member_mybonsa", "16");
        myMap1.put("member_num", "17");

        */




        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            TCnt = cursor.getInt(0);

            Log.e("Check", "====================로그인정보 필드수 " + TCnt + "========================"); //
            do {
                String onum = cursor.getString(cursor.getColumnIndex("num"));
                String fd_title = cursor.getString(cursor.getColumnIndex("fd_title"));
                String fd_value = cursor.getString(cursor.getColumnIndex("fd_value"));
                String td_type = cursor.getString(cursor.getColumnIndex("td_type"));
                myMap1.put(fd_title, onum);
                Log.e("EA : ", onum + " | " + fd_title + " | " + fd_value + " | " + td_type );
            } while (cursor.moveToNext());
        }
        close();
        return myMap1;
    }
    public String Test_OrderUpdate(int OrderNo ) {
        if (mDBHelper == null)
            mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if (mDB == null) mDB = mDBHelper.getWritableDatabase();
        ContentValues sendDataN=new ContentValues();
        sendDataN.put("order_status", "4");
        mDB.update("_car_order_lists", sendDataN, "num="+OrderNo, null);
        close();
        return "OK" ;
    }
    public String getCount_DB_Record(String Dbname){
        String sqlC="Select count(num) as totalcnt from " + Dbname;
        Cursor cursor = mDB.rawQuery(sqlC, null);
        String Tot = "0";
        if( cursor != null && cursor.moveToFirst()) Tot = cursor.getString(cursor.getColumnIndex("totalcnt"));
        close();
        return Tot;
    }
    public String getMax_DB_Record(){
        String sqlC="Select max(reg_date) as totalcnt from user_push_messages";
        Cursor cursor = mDB.rawQuery(sqlC, null);
        if( cursor != null && cursor.moveToFirst()){
            String TT = cursor.getString(cursor.getColumnIndex("totalcnt"));
            if( TT == null ) TT = "0";
            close();
            return TT;
        }else{
            close();
            return "2019-01-01 00:00:00";
        }
    }
    public String getMin_DB_Record(){
        String sqlC="Select min(reg_date) as totalcnt , num, reg_date   from user_push_messages";
        Cursor cursor = mDB.rawQuery(sqlC, null);
        if( cursor != null && cursor.moveToFirst()){
            String TT = cursor.getString(cursor.getColumnIndex("totalcnt"));
            String Ta = cursor.getString(cursor.getColumnIndex("num"));
            String Tb = cursor.getString(cursor.getColumnIndex("reg_date"));
            Log.e("MinGetSql" , "TT :" + TT + " / num : " + Ta + " / Tb : " + Tb);
            if( TT == null ) TT = "2000-02-05 00:00:00";
            close();
            return TT;
        }else{
            close();
            return "2000-03-03 00:00:00";
        }
    }
    private void get_count_Db_Order(){
        String sqlC="Select count(num) as totalcnt from user_push_messages";
        Cursor cursor = mDB.rawQuery(sqlC, null);
        String Message_Tot = "0"; String Order_Tot = "0";
        if( cursor != null && cursor.moveToFirst()) Message_Tot = cursor.getString(cursor.getColumnIndex("totalcnt"));
        sqlC="Select count(num) as totalcnt from _car_order_lists";
        cursor = mDB.rawQuery(sqlC, null);
        if( cursor != null && cursor.moveToFirst()) Order_Tot = cursor.getString(cursor.getColumnIndex("totalcnt"));
        //Log.e("Total", "Message :" + Message_Tot + " / Order : " + Order_Tot );
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        close();
    }
    public String get_current_ListDatas(String minDate){
        if( minDate.length() < 10 ) minDate = "2019-01-01 01:00:00";
        String sql="Select a.num as pushnum, b.num as ordernum, a.*, b.* " +
                " from user_push_messages as A " +
                " Left join _car_order_lists as B on A.ref_num=B.num " +
                " where A.reg_date > strftime('%Y-%m-%d %H:%M:%S','"+ minDate +"') " +
                " order by A.reg_date desc limit 200 offset 0";
        Cursor cursor = mDB.rawQuery(sql, null);
        String list = "";

        if (cursor != null && cursor.moveToFirst()) {
            Log.e("GetSQLFirst", "SQL 있음: " + sql );
            list = make_Cursor_list(cursor);
        }else{
            Log.e("GetSQLFirst", "SQL 없음: " + sql );
        }
        close();
        return list;
    }
    public String get_messageList(int offsetInt, int limit){
        String sql="Select a.num as pushnum, b.num as ordernum, a.*, b.* " +
                " from user_push_messages as a " +
                " Left join _car_order_lists as b on A.ref_num=B.num " +
                " order by  a.reg_date desc, a.num desc  limit " + limit + " offset " + offsetInt; //A.reg_date desc,
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("get_messageList_GetSQL", "SQL: " + sql );
        String list = make_Cursor_list(cursor);
        close();
        return list;
    }
    public String get_Updated_on_Row(int Orderno, String notyTypeStr){ // 업데이트 된 오더번호로 검색하기
        String moreOpt = "";
        if( notyTypeStr.length() > 1 ) moreOpt = " and noti_type='" + notyTypeStr + "'";
        String sql="Select A.num as pushnum, B.num as ordernum, A.*, B.* " +
                " from user_push_messages as A " +
                " Left join _car_order_lists as B on A.ref_num=B.num " +
                " where A.ref_num = " + Orderno + moreOpt + " order by A.reg_date desc limit 1";
        Cursor cursor = mDB.rawQuery(sql, null);
        Log.e("GetSQL", "SQL: " + sql );
        String list = make_Cursor_list(cursor);
        close();
        return list;
    }
    private String make_Cursor_list(Cursor cursor) {
        String eaVal = "";
        String list = "";
        String[] Fields = fds1;
        int doIn = 0;
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            do {
                String eaLIne = "";
                for (String val : fds1) {
                    if (val.equals("num")) val = "pushnum";
                    if (val.equals("caroderlistnum")) val = "ordernum";
                    eaVal = cursor.getString(cursor.getColumnIndex(val)); //"†", "‡", "|","↕"
                    eaLIne += val + "†" + eaVal + "↕";
                }
                for (String val : fds2) {
                    if (val.equals("num")) val = "pushnum";
                    if (val.equals("caroderlistnum")) val = "ordernum";
                    eaVal = cursor.getString(cursor.getColumnIndex(val)); //"†", "‡", "|","↕"
                    eaLIne += val + "†" + eaVal + "↕";
                }
                String reg_date = cursor.getString(cursor.getColumnIndex("reg_date"));
                //Log.e("EaLine", "eadata reg_date : " + reg_date + "/ D : " + eaLIne);
                if (!eaLIne.equals("")) {
                    list += eaLIne + "|";
                    doIn++;
                }

            } while (cursor.moveToNext());
        }
        Log.e("리스트 출력완료", "총갯수 : " + doIn);
        close();
        return list;
    }
    public String get_App_member_num(){
        if( mDBHelper == null)mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null)mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from skyCall_settings where seting_25='10'";
        Cursor cursor = mDB.rawQuery(sql, null);
        String mbnoStr = "0";
        if( cursor != null && cursor.moveToFirst()) mbnoStr = cursor.getString(cursor.getColumnIndex("seting_24"));
        if( mbnoStr == null) mbnoStr = "0";
        return mbnoStr;
    }
    public String [] init_messageDb() throws SQLException, JSONException {
        init_DB(0);
        if( mDBHelper == null)mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null)mDB = mDBHelper.getWritableDatabase();
        String order_list = make_orderlist_Sql();
        mDB.execSQL(order_list);
        String SqlMessage = make_messageList_sql();
        mDB.execSQL(SqlMessage);
        //Log.e("디비시작", "메시지DB초기화 생성");
        String sql="Select max(reg_date) as num from user_push_messages";
        Cursor cursor = mDB.rawQuery(sql, null);
        String maxno = "2019-01-01 00:00:00";
        if( cursor != null && cursor.moveToFirst()) {maxno = cursor.getString(cursor.getColumnIndex("num"));}

        sql="Select max(num) as num from _car_order_lists";
        cursor = mDB.rawQuery(sql, null);
        int maxorderno = 0;
        if( cursor != null && cursor.moveToFirst()) {maxorderno = cursor.getInt(cursor.getColumnIndex("num"));}
        sql="Select * from skyCall_settings where seting_25='10'";
        cursor = mDB.rawQuery(sql, null);
        String mbnoStr = "0";
        if( cursor != null && cursor.moveToFirst()) mbnoStr = cursor.getString(cursor.getColumnIndex("seting_24"));
        if( mbnoStr == null) mbnoStr = "0";

        sql="Select min(reg_date) as num from user_push_messages";
        cursor = mDB.rawQuery(sql, null);
        String min_no = "2025-01-01 00:00:00";
        if( cursor != null && cursor.moveToFirst()) {min_no = cursor.getString(cursor.getColumnIndex("num"));}

        sql="Select count(num) as mgCnt from user_push_messages";
        cursor = mDB.rawQuery(sql, null);
        String mgCnt = "0";
        if( cursor != null && cursor.moveToFirst()) {mgCnt = cursor.getString(cursor.getColumnIndex("mgCnt"));}

        chColumeExists();

        String [] datas = {maxno, mbnoStr, String.valueOf(maxorderno), min_no , mgCnt };
        close();
        return datas;

    }

    public DbOpenHelper init_DB(int resetDo) throws SQLException, JSONException {
        mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        if( resetDo == 1){
            mDB.execSQL("DROP TABLE IF EXISTS skyCall_badCompanyList;");
            //Log.e("디비리셋","Reset Table skyCall_badCompanyList");
        }
        init_LoginTable();
        String Sql = "CREATE TABLE IF NOT EXISTS skyCall_badCompanyList (" +
                "num integer primary key autoincrement, " +
                "ref_num integer not null,"+
                "writer_name text default '' ," +
                "writer_phone text default ''," +
                "my_num  integer not null,"+
                "bad_coname text default ''," +
                "is_except text default 0," +
                "bad_co_number text default ''," +
                "bad_car_plate text default ''," +
                "bnd_car_memo text default ''," +
                "driver_num text default ''," +
                "driver_name text default ''," +
                "driver_phone text default ''," +
                "reg_date text default '')";
        mDB.execSQL(Sql);

        String Sqlsettings = "CREATE TABLE IF NOT EXISTS skyCall_settings(" +
                "num integer primary key autoincrement, " +
                "seting_0 text default '0' ," +
                "seting_1 text default '0'," + //문자 읽어줄지
                "seting_2 text default '0'," +  // 문자 읽어줄때 2 : 알림인지, 1 매칭인지
                "seting_3 text default '0'," + //NOTIOFF 노티 안받기
                "seting_4 text default '0'," + // MACHINGOFF 매칭 안받기
                "seting_5 text default '0'," + //pushNoticeSnd 사운드 소리
                "seting_6 text default '0'," + // 읽어주는 속도
                "seting_7 text default '0'," + //읽어주는 피치
                "seting_8 text default '0'," + // 노티 벳지 갯수
                "seting_9 text default '0'," + // 일반 푸시일경우 사운드 소리 def 는 기본소리
                "seting_10 text default '0'," + /// URL 링크시 연결할 주소 임시 저장후 팝업페이지에서 다시 참조 후 연결할 용도
                "seting_11 text default '0'," + // 로그인 정보 전체 저장
                "seting_12 text default '0'," + //
                "seting_13 text default '0'," +
                "seting_14 text default '0'," +
                "seting_15 text default '0'," +
                "seting_16 text default '0'," +
                "seting_17 text default '0'," +
                "seting_18 text default '0'," +
                "seting_20 text default '0'," +
                "seting_21 text default '0'," +
                "seting_22 text default '0'," +
                "seting_23 text default '0'," + /// sql 버전
                "seting_24 text default '0'," + /// 회원고유번호
                "seting_25 text default '0' )";
        mDB.execSQL(Sqlsettings);
        //Log.e("디비시작", Sqlsettings);


        SharedPreferences dbCheck = mCtx.getSharedPreferences("skyCallDbcheck", MODE_PRIVATE);
        String appVersion = BuildConfig.VERSION_NAME;
        SharedPreferences.Editor editor = dbCheck.edit();
        editor.putString("dbTable_badListVer", appVersion);
        editor.apply();
        //Log.e("디비버전","dbTable_badListVer To = " + appVersion);
        String chv = dbCheck.getString("dbTable_badListVer", "0");
        //Log.e("디비버전","dbTable_badListVer Read = " + chv);

        return this;
    }
    public String get_message_not_read(){
        if( mDBHelper == null || mDB == null ){
            mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
            mDB = mDBHelper.getWritableDatabase();
        }
        String ret = "0";
        String sql="Select seting_11 from skyCall_settings where seting_25='10'";
        Cursor cursor = mDB.rawQuery(sql, null);
        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            ret = cursor.getString(cursor.getColumnIndex("seting_11"));
        }
        return ret ;
    }
    public String [] getSettingvalue(){
        mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        String sql="Select * from skyCall_settings where seting_25='10'";
        Cursor cursor = mDB.rawQuery(sql, null);
        String[] xs = new String [] {};
        String getoneInfo = "";
        if( cursor != null && cursor.moveToFirst()) {
            String st_0 = cursor.getString(cursor.getColumnIndex("seting_0"));
            String st_1 = cursor.getString(cursor.getColumnIndex("seting_1"));
            String st_2 = cursor.getString(cursor.getColumnIndex("seting_2"));
            String st_3 = cursor.getString(cursor.getColumnIndex("seting_3"));
            String st_4 = cursor.getString(cursor.getColumnIndex("seting_4"));
            String st_5 = cursor.getString(cursor.getColumnIndex("seting_5"));
            String st_6 = cursor.getString(cursor.getColumnIndex("seting_6"));
            String st_7 = cursor.getString(cursor.getColumnIndex("seting_7"));
            String st_8 = cursor.getString(cursor.getColumnIndex("seting_8"));
            String st_9 = cursor.getString(cursor.getColumnIndex("seting_9"));
            String st_10 = cursor.getString(cursor.getColumnIndex("seting_10")); /// URL 링크시 연결할 주소 임시 저장후 팝업페이지에서 다시 참조 후 연결할 용도
            String st_11 = cursor.getString(cursor.getColumnIndex("seting_11")); /// 메신저 글 읽지 않은 갯수
            String st_12 = cursor.getString(cursor.getColumnIndex("seting_12"));// GPS 마지막 저장한 주소 도로명 값
            String st_13 = cursor.getString(cursor.getColumnIndex("seting_13"));
            String st_14 = cursor.getString(cursor.getColumnIndex("seting_14"));
            String st_15 = cursor.getString(cursor.getColumnIndex("seting_15"));
            String st_16 = cursor.getString(cursor.getColumnIndex("seting_16"));
            String st_17 = cursor.getString(cursor.getColumnIndex("seting_17"));
            String st_18 = cursor.getString(cursor.getColumnIndex("seting_18"));

            xs = new String [] {st_0,st_1,st_2,st_3,st_4,st_5,st_6,st_7,st_8,st_9,st_10,st_11,st_12,st_13,st_14,st_15,st_16,st_17,st_18};
        }else {
            xs = new String [] {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","9"};
            makeDefVal();
        }
        return xs;
    }//seting_5  SetSettingvalue("seting_5",
    public int SetSettingvalue(String filedName, String setVal){
        mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(filedName,setVal); //These Fie
        String SqlX = "update skyCall_settings set " + filedName+ "='"+setVal+"' where seting_25='10'" ;
        try{
            mDB.execSQL(SqlX);
        }catch(Exception e){
            Log.e("RET ERR", "ER:" + e.getCause() );
            String Cc = String.valueOf(e.getCause());
            String pos = filedName + "/" + setVal + " / line 614";
            Util.sendError_to_Server("DbOpenHelper", Cc, SqlX,  pos);
        }

        //Log.e("디비저장" , "저장대상 : " +filedName + " 저장값 :" + setVal + " / " + SqlX  );

        String sql="Select * from skyCall_settings where seting_25='10'";
        Cursor cursor = mDB.rawQuery(sql, null);
        if( cursor != null && cursor.moveToFirst()) {
            String st_0 = cursor.getString(cursor.getColumnIndex("seting_1"));
            String st_1 = cursor.getString(cursor.getColumnIndex("seting_2"));
            String st_2 = cursor.getString(cursor.getColumnIndex("seting_3"));
            String st_5 = cursor.getString(cursor.getColumnIndex("seting_5"));
            String st_9 = cursor.getString(cursor.getColumnIndex("seting_9"));
            //Log.e("테스트읽기" , "st_5 : " +st_5 + " st_9 :" + st_9 );

        }else {
            //Log.e("못읽음" , "값없음" );

        }
        close();
        return 1;
    }
    public void makeDefVal(){
        mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        ContentValues sendData=new ContentValues();
        sendData.put("seting_1", "0");
        sendData.put("seting_25", "10");
        mDB.insert("skyCall_settings", null,sendData);
        close();
    }
    public String[] check_if_this_badCaompany(String pnum){
        if(mDBHelper==null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB==null) mDB = mDBHelper.getWritableDatabase();
        String pnumNo = pnum.replace("-","");
        String sql="Select * from skyCall_badCompanyList where ( driver_phone='" + pnum + "' or driver_phone='" + pnumNo + "') and is_except='0' order by num asc";
        Cursor cursor = mDB.rawQuery(sql, null);
        String[] xs = new String [] {};
        String getoneInfo = "";
        //Log.e("악덕검사중", "sql" + sql);
        if( cursor != null && cursor.moveToFirst()) {
            getoneInfo+= cursor.getString(cursor.getColumnIndex("bad_coname")) + "'";
            getoneInfo+= cursor.getString(cursor.getColumnIndex("driver_name")) + "'";
            getoneInfo+= cursor.getString(cursor.getColumnIndex("driver_phone")) + "'";
            getoneInfo+= cursor.getString(cursor.getColumnIndex("bnd_car_memo")) + "'";
            getoneInfo+= cursor.getString(cursor.getColumnIndex("reg_date"));

            String bad_coname = cursor.getString(cursor.getColumnIndex("bad_coname"));
            String writer_phone = cursor.getString(cursor.getColumnIndex("writer_phone"));
            String bnd_car_memo = cursor.getString(cursor.getColumnIndex("bnd_car_memo"));
            String reg_date = cursor.getString(cursor.getColumnIndex("reg_date"));
            String my_num = cursor.getString(cursor.getColumnIndex("my_num"));
            String ref_num = cursor.getString(cursor.getColumnIndex("ref_num"));

            xs = new String [] {bad_coname,writer_phone,bnd_car_memo,reg_date,my_num,ref_num};
        }
        close();
        return xs;
    }
    public int save_badcompany_listone(String num,String my_num,String bad_coname,
                                       String bad_co_number,String bad_car_plate,String bnd_car_memo,String driver_num,
                                       String driver_name,String driver_phone,String reg_date,String writer_name,String writer_phone ){
        mDB = mDBHelper.getWritableDatabase();
        ContentValues sendData=new ContentValues();
        sendData.put("ref_num", num);
        sendData.put("writer_name", writer_name);
        sendData.put("writer_phone", writer_phone);
        sendData.put("my_num", my_num);
        sendData.put("bad_coname", bad_coname);
        sendData.put("bad_co_number",bad_co_number );
        sendData.put("bad_car_plate", bad_car_plate);
        sendData.put("bnd_car_memo", bnd_car_memo);
        sendData.put("driver_num", driver_num);
        sendData.put("driver_name", driver_name);
        sendData.put("driver_phone", driver_phone);
        sendData.put("reg_date", reg_date);
        mDB.insert("skyCall_badCompanyList", null,sendData);
        close();
        return 1;
    }
    public String getExceopBadList(){
        if(mDBHelper == null) mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if(mDB == null) mDB = mDBHelper.getWritableDatabase();
        String sql="Select ref_num from skyCall_badCompanyList where is_except = '1' order by ref_num asc";
        Cursor cursor = mDB.rawQuery(sql, null);

        String str = "";
        int TCnt = 0;
        if( cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            TCnt = cursor.getInt(0);
            //Log.e("Check", "==========================phone_number_list " + TCnt + "===============================");
            do {
                String ref_num = cursor.getString(cursor.getColumnIndex("ref_num"));
                str+=ref_num+",";
            } while (cursor.moveToNext());
        }
        close();
        return str;
    }
    public int getMax_savedNum_max() {
        int mx = 0;
        try {
            Cursor cursor = mDB.rawQuery("SELECT count(num) as cnt from skyCall_badCompanyList ", new String[]{});
            if (cursor != null){
                if (cursor.moveToFirst()) {
                    mx = cursor.getInt(0);
                }
                close();
                return mx;
            }
        }catch(Exception e){
            return 0;
        }
        return 0;
    }
    public int getMax_savedNum() {
        int mx=0;
        try{
            Cursor cursor=mDB.rawQuery("SELECT max(ref_num) from skyCall_badCompanyList ",new String [] {});
            if (cursor != null){
                if(cursor.moveToFirst()){
                    mx= cursor.getInt(0);
                }
            }
            close();
            return mx;
        }catch(Exception e){
            return 0;
        }

    }
    public int make_except_refDo(int no){
        ContentValues cv = new ContentValues();
        cv.put("is_except","1"); //These Fie
        mDB.update("skyCall_badCompanyList", cv, "ref_num="+no, null);
        close();
        return 1;
    }
    public int make_except_Cancel_Do(int no){
        ContentValues cv = new ContentValues();
        cv.put("is_except","0"); //These Fie
        mDB.update("skyCall_badCompanyList", cv, "ref_num="+no, null);
        close();
        return 1;
    }
    //mDbOpenHelper.Check_input_message(sender_no,sent_phone,sent_message,sent_namecardno,sent_namecardtitle,sent_date,sent_way);
    public void Check_input_message(int sender_no,String sent_phone,String sent_message,String sent_namecardno,String sent_namecardtitle, String sent_date, String sent_way ) {

        String sql = "Select num from g5_member_callaftersend where sent_date='" + sent_date + "' and sent_phone='" + sent_phone + "' and sent_namecardtitle='" + sent_namecardtitle + "'";
        //Log.e("SqlSel", sql );
        Cursor mCount = mDB.rawQuery(sql,null);
        int count = 0;
        if( mCount != null && mCount.moveToFirst()){
            mCount.moveToFirst();
            count= mCount.getInt(0);
        }
        if( count == 0 ){
            //Log.e("없다", "넣기 실행 " );
            //Log.e("SqlSel", sql + "/ " + count);
            String Sql=
                    "Insert into g5_member_callaftersend (sender_no,sent_phone,sent_message,sent_namecardno,sent_namecardtitle,sent_date,sent_way) VALUES("+
                            sender_no + ",'"+sent_phone+"','" + sent_message + "','" + sent_namecardno + "','" + sent_namecardtitle + "','"+sent_date+"','"+sent_way+"')";
           // Log.e("Insering " , Sql);
            mDB.execSQL(Sql);
        }else{
            //Log.e("값있음", "저장안함");
        }
        close();
    }
    //mDbOpenHelper.Check_input_plist(,,,,,,);
    public void insert_Old_memo(String wdate,String phone_no,String phone_number,String memo,String u_name) {
        String Sql=
                "Insert into g5_member_phone_memos (w_date,mb_id,phone_number,memo,phone_no,mb_no) VALUES("+
                        "'" + wdate + "','"+u_name+"','" + phone_number + "','" + memo + "','" + phone_no + "',0)";
        //Log.e("Insering " , Sql);
       mDB.execSQL(Sql);
        close();

    }
    public void dropAll() {
        //Log.e("Drop","DROP TABLE IF EXISTS g5_member_phone_memos");
        //mDB.execSQL("DROP TABLE IF EXISTS g5_member_phone_memos;");
    }
    //Db를 다 사용한 후 닫는 메소드
    public void close() {
        //mDBHelper.close();
    }
    /**
     *  데이터베이스에 사용자가 입력한 값을 insert하는 메소드
     * @param name          이름
     * @param contact       전화번호
     * @param email         이메일
     * @return              SQLiteDataBase에 입력한 값을 insert
     */
    public long insertColumn(String name, String contact, String email) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.CONTACT, contact);
        values.put(DataBases.CreateDB.EMAIL, email);
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
    }
    /**
     * 기존 데이터베이스에 사용자가 변경할 값을 입력하면 값이 변경됨(업데이트)
     * @param id            데이터베이스 아이디
     * @param name          이름
     * @param contact       전화번호
     * @param email         이메일
     * @return              SQLiteDataBase에 입력한 값을 update
     */
    public boolean updateColumn(long id, String name, String contact, String email) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.CONTACT, contact);
        values.put(DataBases.CreateDB.EMAIL, email);
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "_id="+id, null) > 0;
    }
    //입력한 id값을 가진 DB를 지우는 메소드
    public boolean deleteColumn(long id) {
        return mDB.delete(DataBases.CreateDB._TABLENAME, "_id=" + id, null) > 0;
    }
    //입력한 전화번호 값을 가진 DB를 지우는 메소드
    public boolean deleteColumn(String number) {
        return mDB.delete(DataBases.CreateDB._TABLENAME, "contact="+number, null) > 0;
    }
    //커서 전체를 선택하는 메소드
    public Cursor getAllColumns() {
        return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }
    //ID 컬럼 얻어오기
    public Cursor getColumn(long id) {
        Cursor c = mDB.query(DataBases.CreateDB._TABLENAME, null,
                "_id="+id, null, null, null, null);
        //받아온 컬럼이 null이 아니고 0번째가 아닐경우 제일 처음으로 보냄
        if (c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }
    //이름으로 검색하기 (rawQuery)
    public Cursor getMatchName(String name) {
        Cursor c = mDB.rawQuery( "Select * from address where name" + "'" + name + "'", null);
        return c;
    }
    private String make_messageList_sql(){
        String sql="CREATE TABLE IF NOT EXISTS user_push_messages(" +
                "num INTEGER PRIMARY KEY AUTOINCREMENT," +
                "noti_type varchar(20) NOT NULL DEFAULT ''," +
                "noti_from_unum varchar(20) NOT NULL DEFAULT '0'," +
                "noti_from_type varchar(50) DEFAULT NULL," +
                "order_level varchar(20) NOT NULL DEFAULT '0'," +
                "sub_level varchar(20) NOT NULL DEFAULT '0'," +
                "my_num varchar(20) NOT NULL DEFAULT '0'," +
                "chr_soo varchar(50) DEFAULT NULL," +
                "bonsa varchar(20) NOT NULL DEFAULT '0'," +
                "read_cnt varchar(20) NOT NULL DEFAULT '0'," +
                "gisa varchar(20) NOT NULL DEFAULT '0'," +
                "ref_num varchar(20) NOT NULL DEFAULT '0'," +
                "mg_title varchar(500) DEFAULT NULL," +
                "mg_body varchar(3000) DEFAULT NULL," +
                "job_dtype varchar(20) DEFAULT NULL," +
                "reg_date datetime DEFAULT NULL," +
                "keepit varchar(5) DEFAULT '0'," +
                "o_stepcnt varchar(5) DEFAULT '0'," +
                "ea_infos varchar(1500) DEFAULT ''," +
                "action_doer varchar(45) DEFAULT NULL," +
                "order_from varchar(10) DEFAULT NULL ," +
                "order_fromnum varchar(20) DEFAULT '0') ";
        return sql;
    }
    private String make_orderlist_Sql(){
        String sql="CREATE TABLE IF NOT EXISTS _car_order_lists(" +
                "num integer primary key autoincrement," +
                "order_id varchar(50) DEFAULT NULL," +
                "order_type varchar(20) DEFAULT '0'," +
                "order_status varchar(20) NOT NULL DEFAULT '1'," +
                "status_acter varchar(150) DEFAULT ''," +
                "soojoo_oknum varchar(20) DEFAULT '0'," +
                "work_day date DEFAULT NULL," +
                "serch_keyword varchar(100) DEFAULT NULL, " +
                "contract_address varchar(200) DEFAULT NULL," +
                "car_order_msg varchar(100) DEFAULT ''," +
                "order_member_num varchar(20) DEFAULT '0'," +
                "order_mem_ownernum varchar(20) DEFAULT '0'," +
                "worker_num varchar(20) DEFAULT '0'," +
                "worker_ownernum varchar(11) DEFAULT '0'," +
                "work_time datetime DEFAULT NULL," +
                "work_start_h varchar(11) DEFAULT NULL," +
                "work_start_m varchar(11) DEFAULT NULL," +
                "worker_regdate datetime DEFAULT NULL," +
                "car_price varchar(11) DEFAULT NULL," +
                "worker_recivelevel varchar(10) DEFAULT '55'," +
                "worker_recivelevelsub varchar(3) DEFAULT '0'," +
                "orderwork_type varchar(45) DEFAULT NULL," +
                "work_payment varchar(11) DEFAULT '0'," +
                "howtopay varchar(11) DEFAULT NULL," +
                "howto_payorg varchar(5) DEFAULT '0'," +
                "old_paydone varchar(3) DEFAULT '0'," +
                "order_member_name varchar(30) DEFAULT NULL," +
                "order_member_phone varchar(25) DEFAULT NULL," +
                "order_onwer_name varchar(45) DEFAULT NULL," +
                "order_onwer_phone varchar(45) DEFAULT NULL," +
                "worker_name varchar(50) DEFAULT NULL," +
                "worker_owner_name varchar(45) DEFAULT NULL," +
                "worker_carnum varchar(11) DEFAULT '0'," +
                "worker_directphone varchar(30) DEFAULT NULL," +
                "car_uniqnum varchar(5) DEFAULT '-1'," +
                "spcial_got varchar(45) DEFAULT ''," +
                "getorder_phone varchar(25) DEFAULT NULL," +
                "work_onwer_phone varchar(45) DEFAULT NULL," +
                "getorder_name varchar(45) DEFAULT NULL," +
                "worker_dirvernum varchar(11) DEFAULT '0'," +
                "worker_directtype varchar(20) DEFAULT NULL," +
                "worker_carplate varchar(20) DEFAULT NULL," +
                "worker_num2 varchar(11) DEFAULT NULL," +
                "worker_directname2 varchar(50) DEFAULT ' '," +
                "worker_directphone2 varchar(50) DEFAULT ' '," +
                "imui_order_memnum varchar(11) DEFAULT '0'," +
                "imui_order_memname varchar(45) DEFAULT NULL," +
                "imui_order_memphone varchar(45) DEFAULT NULL," +
                "imui_order_mb_no varchar(11) DEFAULT '0'," +
                "imui_order_mb_owner varchar(11) DEFAULT '0'," +
                "imui_order_mb_ownername varchar(45) DEFAULT NULL," +
                "imui_order_mb_ownerphone varchar(45) DEFAULT NULL," +
                "imui_recive_memnum varchar(11) DEFAULT '0'," +
                "imui_recive_memname varchar(45) DEFAULT NULL," +
                "imui_recive_memphone varchar(45) DEFAULT NULL," +
                "imui_recive_mb_no varchar(11) DEFAULT '0'," +
                "imui_recive_mb_owner varchar(11) DEFAULT '0'," +
                "imui_recive_mb_ownername varchar(45) DEFAULT NULL," +
                "imui_recive_mb_ownerphone varchar(45) DEFAULT NULL," +
                "order_regtime datetime DEFAULT NULL," +
                "last_time datetime DEFAULT '2015-01-01 00:01:01'," +
                "last_setSec varchar(11) DEFAULT '10'," +
                "cancel_date datetime DEFAULT NULL," +
                "cancel_status varchar(50) DEFAULT '0'," +
                "order_bonsanum varchar(11) DEFAULT '0'," +
                "order_gisanum varchar(11) DEFAULT '0'," +
                "order_peerate float DEFAULT '10'," +
                "order_member_id varchar(30) DEFAULT NULL," +
                "order_phone varchar(20) DEFAULT NULL," +
                "order_detail varchar(3000) DEFAULT ' '," +
                "car_ownernum varchar(11) DEFAULT '0'," +
                "callhasmade varchar(10) DEFAULT 'NO'," +
                "dex_drivername varchar(100) DEFAULT ' '," +
                "worker_bonsa varchar(11) DEFAULT '0'," +
                "worker_gisa varchar(11) DEFAULT '0'," +
                "worker_levels varchar(11) DEFAULT '0'," +
                "worker_cnt varchar(11) DEFAULT '0'," +
                "worker_exe_tnum varchar(11) DEFAULT '0'," +
                "car_ownername varchar(50) DEFAULT NULL," +
                "job_type varchar(20) DEFAULT 'hour' ," +
                "car_type varchar(50) DEFAULT NULL," +
                "car_typesub varchar(45) DEFAULT NULL," +
                "car_tons varchar(150) DEFAULT NULL," +
                "car_size_from float DEFAULT NULL," +
                "car_size_to float DEFAULT NULL," +
                "floor_sel varchar(100) DEFAULT ' '," +
                "move_distance varchar(50) DEFAULT NULL," +
                "car_option varchar(50) DEFAULT NULL," +
                "car_orderoption varchar(200) DEFAULT ''," +
                "car_height varchar(30) DEFAULT ''," +
                "car_workdaytype varchar(50) DEFAULT NULL," +
                "car_workhour float DEFAULT NULL," +
                "car_workaddhor float DEFAULT '0'," +
                "car_worktype varchar(50) DEFAULT NULL," +
                "car_worktype_more varchar(200) DEFAULT NULL," +
                "car_work_distance varchar(20) DEFAULT NULL," +
                "car_amount varchar(11) DEFAULT NULL," +
                "crain_gorilaset varchar(45) DEFAULT NULL," +
                "sign_howlong_int varchar(5) DEFAULT '0'," +
                "sign_howlong_str varchar(45) DEFAULT ''," +
                "howtopay_done varchar(20) DEFAULT NULL," +
                "paycard_refnum varchar(11) DEFAULT '0'," +
                "pay_user_input varchar(20) DEFAULT '거래처미수'," +
                "pay_user_input2 varchar(30) DEFAULT '거래처미수'," +
                "workextra_type varchar(80) DEFAULT NULL," +
                "workextra_price varchar(11) DEFAULT NULL," +
                "workextra_cnt varchar(11) DEFAULT NULL," +
                "payed_date datetime DEFAULT NULL," +
                "contract_number varchar(50) DEFAULT NULL," +
                "contract_fieldnumber varchar(20) DEFAULT NULL," +
                "contract_name varchar(50) DEFAULT NULL," +
                "contract_message varchar(300) DEFAULT NULL," +
                "jobdonemsg varchar(1000) DEFAULT NULL," +
                "paydonemsg varchar(200) DEFAULT NULL," +
                "zipcode varchar(10) DEFAULT NULL," +
                "mem_basepoint varchar(120) DEFAULT NULL," +
                "isit_myine varchar(11) DEFAULT '0'," +
                "bill_print_note varchar(11) DEFAULT '0'," +
                "bill_print_tax varchar(11) DEFAULT '0'," +
                "ordermade_from varchar(45) DEFAULT NULL," +
                "order_push_cntnum varchar(100) DEFAULT '0'," +
                "order_handle_by text," +
                "order_handle_date datetime DEFAULT NULL," +
                "order_handle_job varchar(200) DEFAULT NULL," +
                "isresendorder varchar(3) DEFAULT '0'," +
                "soojoo_check varchar(3) DEFAULT '0'," +
                "baljoo_check varchar(3) DEFAULT '0'," +
                "ocancel_check varchar(3) DEFAULT '0'," +
                "order_memo varchar(2000) DEFAULT NULL," +
                "order_hideto varchar(150) DEFAULT ''," +
                "togo_now varchar(5) DEFAULT '0'," +
                "togo_all varchar(5) DEFAULT '0'," +
                "togo_gab varchar(5) DEFAULT '0'," +
                "pure_matched varchar(11) DEFAULT '-88'," +
                "ref_photocnt varchar(5) DEFAULT '0'," +
                "tax_printed_to_giver varchar(3) DEFAULT '0'," +
                "tax_printed_ref_num varchar(11) DEFAULT '0'," +
                "order_full_adr varchar(150) DEFAULT NULL," +
                "order_road_adr varchar(150) DEFAULT ''," +
                "search_type varchar(150) DEFAULT ''," +
                "adr_city_dong varchar(100) DEFAULT '-'," +
                "price_changed varchar(3) DEFAULT '0'," +
                "has_note varchar(11) DEFAULT '0'," +
                "pricechange_bal varchar(5) DEFAULT '0'," +
                "pricechange_soo varchar(5) DEFAULT '0'," +
                "order_gijung varchar(100) DEFAULT NULL," +
                "month_code varchar(11) DEFAULT '0'," +
                "okcheksoo varchar(3) DEFAULT '0'," +
                "orderPerson varchar(3) DEFAULT '0'," +
                "ref_push_num varchar(11) DEFAULT '0' ," +
                "ref_push_info varchar(45) DEFAULT NULL ," +
                "orderPerson_who varchar(200) DEFAULT NULL," +
                "is_easy_order varchar(3) DEFAULT NULL," +
                "easy_reforderno varchar(11) DEFAULT NULL," +
                "was_whanwon varchar(3) DEFAULT '0')";
        return sql;
    }

}
