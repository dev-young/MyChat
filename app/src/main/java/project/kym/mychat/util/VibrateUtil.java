package project.kym.mychat.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateUtil {
    private static Vibrator vibrator;
    private static void init(Context context){
        if(vibrator == null)
            vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static  void startVibrate(Context context){
        init(context);
        vibrator.vibrate(800); // 1초간 진동

    }

    public static  void startEmergencyVibrate(Context context){
        init(context);
        long[] pattern = {500, 200, 250, 100, 500, 200, 250, 100, 500, 200, 250};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1); // 1초간 진동
        }

    }
}
