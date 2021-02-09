package co.kr.skycall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class soundPlay_Background extends Service implements MediaPlayer.OnPreparedListener{
    private static final String TAG = "BackgroundSoundService";
    MediaPlayer player;
    private Context Mcontext;
    private DbOpenHelper mDbOpenHelper = null;
    private Handler hdlr = new Handler();
    private static int oTime =0, sTime =0, eTime =0, fTime = 5000, bTime = 5000;

    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind()" );
        return null;
    }
    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            sTime = player.getCurrentPosition();
            Log.e("Player", "currentTime:" + sTime);
            hdlr.postDelayed(this, 100);
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        Log.e("SoundPlay", "starting");
        hdlr.postDelayed(UpdateSongTime, 100);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Mcontext = this;
        if (mDbOpenHelper == null) mDbOpenHelper = new DbOpenHelper(Mcontext);
        String[] getDbInfo = mDbOpenHelper.getSettingvalue();
        String goUrl = getDbInfo[10];
        Log.e("SoundPlay", "File name GET:" + goUrl);
        Uri uri = Uri.parse(goUrl);
        File oFile = new File(goUrl);
        try {
            player = new MediaPlayer();
            player.setDataSource(Mcontext, uri);
            player.setLooping(false); // Set looping
            player.setVolume(100,100);
            player.prepare();
            Log.e("SoundPlay", "Prepare() done");

        } catch (IOException e) {
            Log.e("SoundPlay", "Error");
            e.printStackTrace();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        player.start();
        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        Log.i(TAG, "onUnBind()");
        return null;
    }

    public void onStop() {
        player.stop();
        Log.i(TAG, "onStop()");
    }
    public void onPause() {
        Log.i(TAG, "onPause()");
    }
    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        Toast.makeText(this, "Service stopped...", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onCreate() , service stopped...");
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory()");
    }


}
