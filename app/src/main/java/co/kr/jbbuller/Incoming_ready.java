package co.kr.jbbuller;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;

public class Incoming_ready extends BroadcastReceiver {
    Context contextX ;
    private int st = -1;
    private int incomming = -1;
    public String call_type = "";
    public String incomming_num = "";
    private String calling_to_number = "";

    private static final String TAG = "TestDataBase";
    private DbOpenHelper mDbOpenHelper = null;
    private Cursor mCursor;

    private String saved_memos = "";
    private String call_number = "";
    private String Call_person = "";
    private String Last_phone_number = "";
    private int Last_phone_mode = -1;

    @Override
    public void onReceive(Context context, Intent intent) {

        contextX = context;
        final MyPhoneStateListener phoneListener = new MyPhoneStateListener();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String fromNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.e("SkyPSTATUS","state : " + state );
        Log.e("SkyPSTATUS","Incoming_ready : " + intent.getAction() );
        if( mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(contextX);
        if(intent.getAction().equals(ACTION_NEW_OUTGOING_CALL) || intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")  ){ //////////////전화걸기중
            calling_to_number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            incomming_num = "";
            call_type = "calling";
        }else if (telephony.getCallState() == TelephonyManager.CALL_STATE_RINGING || intent.getAction().equals("android.intent.action.PHONE_STATE") ) { // 전화 오는중
            if (fromNumber == null) fromNumber = "";
            if (fromNumber.length() > 5) incomming_num = fromNumber;
            call_type = "incomering";
            incomming_num = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            calling_to_number = "";

            try {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                if( incomingNumber== null) incomingNumber = "";
                Log.e("IncommState","state : " + state + " / incomingNumber : " + incomingNumber + " / outgoingNumber: " + outgoingNumber );
                if( incomingNumber.length() > 5 ) {
                    incomming_num = incomingNumber;
                    call_type = "incomering";
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        Log.e("SkyPSTATUS","call_type : " + call_type + " / incomming_num : " + incomming_num );



        //Toast.makeText(contextX, "리시브 : " + call_type + " / " + incomeNumber , Toast.LENGTH_SHORT).show();
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if(incomingNumber.equals("") || incomingNumber.length()< 5 || call_type.length() < 2) return;
            if( incomming_num == null) incomming_num= "";
            if( incomming_num.isEmpty()) incomming_num = "";
            if( incomming_num.length() < 2 && incomingNumber.length() > 2 ) incomming_num = incomingNumber;
            if( ! incomming_num.equals(incomingNumber) && incomming_num.length() > 3 && incomingNumber.length() > 3 ){
                Log.e("이전번호와같음" , "같은 번호 아니라서 건너뜀");
                return;
            }

            String checkNumber = get_number_withHypen(incomming_num);
            Log.e("InsideStatas" , "call_type : " + call_type + " / state :" + state + " / calling_to_number : " +
                    calling_to_number + " / incomming_num : " + incomming_num + "/ incomingNumber :" + incomingNumber + " / checkNumber :" + checkNumber);

            mDbOpenHelper = new DbOpenHelper(contextX);
            String[] badInfo = mDbOpenHelper.check_if_this_badCaompany(checkNumber);

            Log.e("onCallStateChanged","call_type : " + call_type + " / incomming_num : " + incomingNumber + " /checkNumber : "+ checkNumber + " / " + badInfo.toString() );
            Log.e("검색결과", "리턴값 :" + badInfo + " / " + badInfo.length );

            String isBad = "NO";
            if( badInfo.length >= 4 ) isBad="YES";
            if( Last_phone_number != incomingNumber && state != Last_phone_mode){
                Last_phone_number = incomingNumber;
                Last_phone_mode = state;
                incomming_num = checkNumber;
                if( isBad.equals("YES") && badInfo.length >= 4 ){
                    int togo = 0;
                    if( state== 1 && call_type=="incomering"){
                        Log.e("악덕여부", "악덕 : " + isBad + " | state :" + state + " | CALL TYPE : " + call_type + " / 번호 : " + incomming_num + " /액션 : 악덕 에게 걸려온 전화 알림");
                        togo = 1;

                    }else if(state== 2 && call_type=="calling") {
                        Log.e("악덕여부", "악덕 : " + isBad + " | state :" + state + " | CALL TYPE : " + call_type +  " / 번호 : " + incomming_num + " /액션 : 악덕 에게 전화거는중 알림");
                        togo = 1;
                    }else{
                        //Log.e("악덕여부", "악덕 : " + isBad + " | state :" + state + " | CALL TYPE : " + call_type + " / 액션 : 모름");
                    }
                    if( togo == 1 ){
                        //xs = new String [] {bad_coname,writer_phone,bnd_car_memo,reg_date};
                        Intent pupInt;
                        pupInt = new Intent(contextX, Pupup_notice.class);
                        pupInt.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

                        pupInt.putExtra("callmode", call_type);
                        pupInt.putExtra("pnum", incomming_num);
                        pupInt.putExtra("bad_coname", badInfo[0]);
                        pupInt.putExtra("writer_ph", badInfo[1]);
                        pupInt.putExtra("memo", badInfo[2]);
                        pupInt.putExtra("reg_date", badInfo[3]);
                        pupInt.putExtra("my_num", badInfo[4]);
                        pupInt.putExtra("ref_num", badInfo[5]);

                        Context Ts = contextX.getApplicationContext();
                        PendingIntent pendingIntent = PendingIntent.getActivity(contextX, 0, pupInt, PendingIntent.FLAG_ONE_SHOT);
                        Log.e("걸려온전화 정보보이기" , "번호 :" + incomming_num + "  호출함");
                        AlarmManager alarmManager;
                        alarmManager = (AlarmManager) Ts.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pendingIntent);
                    }
                }
            }
        }

    }
    private String get_number_withHypen(String pnum){
        String kk = pnum;
        kk=kk.replace("-","");
        int sn = kk.length();

        if( sn < 2 ) return "";
        Log.e("StringtoPHNO", "kk : " + kk + " / pnum : " + pnum + " / sn :" + sn);
        String ret = "";
        String hd = kk.substring(0, 2);


        if (hd == "02") {
            if (sn < 7) {
                ret = kk;
            } else if (sn >= 7 && sn < 10) {
                ret = kk.substring(0, 2) + "-" + kk.substring(3, 5) + "-" + kk.substring(6, kk.length());
            } else if (sn >= 10) {
                ret = kk.substring(0, 2) + "-" + kk.substring(3, 6) + "-" + kk.substring(7, kk.length());
            }
        } else {
            if (sn < 7) {
                ret = kk; //kk.substring(0,3) + "-" + kk.substring(3,4) ;
            } else if (sn == 7) {
                ret = kk.substring(0, 3) + "-" + kk.substring(4, kk.length());
            } else if (sn == 8) {
                ret = kk.substring(0, 4) + "-" + kk.substring(5, kk.length());
            } else if (sn > 8 && sn <= 10) {
                ret = kk.substring(0, 3) + "-" + kk.substring(3, 6) + "-" + kk.substring(6, kk.length());
            } else if (sn >= 11) {
                ret = kk.substring(0, 3) + "-" + kk.substring(3, 7) + "-" + kk.substring(7, kk.length());
            }
        }
        kk = kk.replace("--","-");
        Log.e("StringtoPHNO", "AFTERHtpen : " + ret);
       return ret;
    }
    public void init_me(){}
    public static boolean containsOnlyNumbers(String str) {
        if (str == null || str.length() == 0)return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))return false;
        }return true;
    }




}
