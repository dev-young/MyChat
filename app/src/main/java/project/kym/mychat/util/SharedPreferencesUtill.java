package project.kym.mychat.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtill {

    /** 로그인 기록을 저장하여 자동 로그인이 될 수 있도록 한다. */
    public static void saveLoginRecord(Context context, String type, String id, String pw){
        SharedPreferences auto = context.getSharedPreferences(SharedPreferencesKey.LOGIN_RECORD, Activity.MODE_PRIVATE);   //SharedPreferences.Editor를 통해 auto의 loginId와 loginPwd에 값을 저장
        SharedPreferences.Editor autoLogin = auto.edit();
        autoLogin.putString(SharedPreferencesKey.LOGIN_TYPE, type);
        autoLogin.putString(SharedPreferencesKey.LOGIN_ID, id);
        autoLogin.putString(SharedPreferencesKey.LOGIN_PW, pw);
        autoLogin.commit(); // commit()을 해줘야 값이 저장된다.
    }

     /**이전에 로그인 여부를 확인하여 있을 경우 true
     *  로그인 기록이 없을 경우 false */
     public static boolean loadLoginRecord(Context context, LoadLoginRecordCallback callback){
         SharedPreferences auto = context.getSharedPreferences(SharedPreferencesKey.LOGIN_RECORD, Activity.MODE_PRIVATE);   //SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
         int type;
         String id, pw;
         type = auto.getInt(SharedPreferencesKey.LOGIN_TYPE,0); // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값.
         id = auto.getString("id",null);
         pw = auto.getString("pw",null); // 비번을 저장해야 하는가?

         if(type == 0){
             return false;
         }

         if(callback != null)
             callback.success(type, id, pw);
         return true;
     }

    public static void saveAccountProvider(Context context, int provider){
        SharedPreferences auto = context.getSharedPreferences(SharedPreferencesKey.LOGIN_RECORD, Activity.MODE_PRIVATE);   //SharedPreferences.Editor를 통해 auto의 loginId와 loginPwd에 값을 저장
        SharedPreferences.Editor autoLogin = auto.edit();
        autoLogin.putInt(SharedPreferencesKey.ACCOUNT_PROVIDER, provider);
        autoLogin.commit(); // commit()을 해줘야 값이 저장된다.
    }

    public static int loadAccountProvider(Context context){
        SharedPreferences auto = context.getSharedPreferences(SharedPreferencesKey.LOGIN_RECORD, Activity.MODE_PRIVATE);   //SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
        int type;
        type = auto.getInt(SharedPreferencesKey.ACCOUNT_PROVIDER,0); // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값.

        return type;
    }

    public interface SharedPreferencesKey{
        public static final String ACCOUNT_PROVIDER = "account_provider";
        public static final String LOGIN_RECORD = "login_record";
        public static final String LOGIN_TYPE = "login_type";
        public static final String LOGIN_ID = "id";
        public static final String LOGIN_PW = "pw";
    }

    public interface LoadLoginRecordCallback{
        void success(int type, String id, String pw);
    }
}
