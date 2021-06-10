package co.kr.jbbuller;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class MyTell extends Service implements TextToSpeech.OnInitListener {
    public static TextToSpeech mTts;
    public static final String PREFERENCE_NAME = "noti_Setting";
    public MyTell() {
        //mTts = new TextToSpeech(this, this);
    }

    @Override
    public void onCreate() {
        mTts = new TextToSpeech(this, this);
        mTts.setLanguage(Locale.KOREAN);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private class ToastMessageTask extends AsyncTask<String, String, String> {
        String toastMessage;
        @Override
        protected String doInBackground(String... params) {toastMessage = params[0];return toastMessage;}
        protected void OnProgressUpdate(String... values) {super.onProgressUpdate(values);}
        protected void onPostExecute(String result){Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT);toast.show();}
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        if( intent == null ) return;
        float readspeed_Set = (float) 1.0;
        float readPitch_Set = (float) 1.0;

        String message = intent.getStringExtra("readmessage");
        String readspeed_SetGet = intent.getStringExtra("readspeed_Set");
        String readPitch_SetGet = intent.getStringExtra("readPitch_Set");
        if( message == null ) return;


        Log.e("MyTellOnstart","mTts : " + mTts);
        Log.e("MyTellOnstart","message : " + message);
        Log.e("MyTellOnstart","readspeed_Set : " + readspeed_SetGet);
        Log.e("MyTellOnstart","readPitch_Set : " + readPitch_SetGet);
        mTts.setSpeechRate(readspeed_Set);// 0.995
        mTts.setPitch(readPitch_Set); // 1.0
        if( readspeed_SetGet != null){
            if( readspeed_SetGet.length() > 0 ) readspeed_Set = Float.parseFloat(readspeed_SetGet);
        }
        if( readspeed_SetGet != null){
            if( readPitch_SetGet.length() > 0 ) readPitch_Set = Float.parseFloat(readPitch_SetGet);
        }

        speakOut(message,readspeed_Set,readPitch_Set);



        super.onStart(intent, startId);

    }
    private void speakOut(String txt , float rate, float pitch) {
        boolean ison = mTts.isSpeaking();
        //new MyTell.ToastMessageTask().execute("TTS Speaking : " + ison);
        mTts.setSpeechRate(rate); // 0.995
        mTts.setPitch(pitch); // 1.0
        mTts.speak(txt,TextToSpeech.QUEUE_FLUSH, null);

    }
    public void onInit(int status) {
        // TODO Auto-generated method stub
        if (status == TextToSpeech.SUCCESS) {
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MyTell.this, "오더 읽어주기 서비스가 불가능한 폰입니다",Toast.LENGTH_LONG).show();
        }
    }
}