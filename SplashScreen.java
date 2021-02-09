package co.kr.skycall;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashScreen extends Activity {
    protected boolean _active = true;
    protected int _splashTime = 2000; // time to display the splash screen in ms
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Log.e("SplashScreen", "SplashScreenSplashScreenSplashScreenSplashScreenSplashScreen###################");
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {waited += 100;}
                    }
                } catch (Exception e) {
                } finally {
                    startActivity(new Intent(SplashScreen.this,MainActivity.class));
                    finish();
                }
            };
        };
        splashTread.start();
    }
}