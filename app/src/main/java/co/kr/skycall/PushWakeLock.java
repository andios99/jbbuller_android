package co.kr.skycall;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by junyAsusNoteBook on 2017-08-14.
 */

public class PushWakeLock { //PushWakeLock.acquireCpuWakeLock(this);  PushWakeLock.releaseCpuLock()
    private static PowerManager.WakeLock sCpuWakeLock;
    private static KeyguardManager.KeyguardLock mKeyguardLock;
    private static boolean isScreenLock;

    static void acquireCpuWakeLock(Context context) {
        Log.e("PushWakeLock", "Acquiring cpu wake lock");
        Log.e("PushWakeLock", "wake sCpuWakeLock = " + sCpuWakeLock);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "hello");
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        Log.e("PushWakeLock", "Releasing cpu wake lock");
        Log.e("PushWakeLock", "relase sCpuWakeLock = " + sCpuWakeLock);

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}


