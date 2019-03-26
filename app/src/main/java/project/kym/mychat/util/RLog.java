package project.kym.mychat.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class RLog {
    private static boolean PRINT_ENABLED_LOG = true;
    private static boolean SYSTEM_OUT_MODE = false;
    private static final String TAG = "RLog";

    public RLog() {
    }

    /** 디버그 모드 여부를 확인하여 R로그 출력 여부 결정 */
    public static void init(Context context) {
        boolean debuggable = false;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        PRINT_ENABLED_LOG = debuggable;
    }

    /** R로그 출력 여부 */
    public static void setEnable(boolean enable) {
        PRINT_ENABLED_LOG = enable;
    }

    /** log 유틸 사용 여부 */
    public static void setSystemMode(boolean isSystemMode) {
        SYSTEM_OUT_MODE = isSystemMode;
    }

    public static final void e() {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(""));
            } else {
                Log.e(TAG, buildLogMsg(""));
            }
        }
    }

    public static final void e(String message) {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(message));
            } else {
                Log.e(TAG, buildLogMsg(message));
            }
        }
    }

    public static final void w() {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(""));
            } else {
                Log.w(TAG, buildLogMsg(""));
            }
        }
    }

    public static final void w(String message) {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(message));
            } else {
                Log.w(TAG, buildLogMsg(message));
            }
        }
    }

    public static final void i() {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(""));
            } else {
                Log.i(TAG, buildLogMsg(""));
            }
        }
    }

    public static final void i(String message) {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(message));
            } else {
                Log.i(TAG, buildLogMsg(message));
            }
        }
    }

    public static final void d() {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(""));
            } else {
                Log.d(TAG, buildLogMsg(""));
            }
        }
    }

    public static final void d(String message) {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(message));
            } else {
                Log.d(TAG, buildLogMsg(message));
            }
        }
    }

    public static final void v() {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(""));
            } else {
                Log.v(TAG, buildLogMsg(""));
            }
        }
    }

    public static final void v(String message) {
        if (PRINT_ENABLED_LOG) {
            if (SYSTEM_OUT_MODE) {
                System.out.println(buildSystemMsg(message));
            } else {
                Log.v(TAG, buildLogMsg(message));
            }
        }
    }

    private static String buildLogMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);
        return sb.toString();
    }

    private static String buildSystemMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);
        return sb.toString();
    }
}