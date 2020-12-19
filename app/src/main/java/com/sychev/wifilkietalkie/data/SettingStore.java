package com.sychev.wifilkietalkie.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingStore {

    private static final String USER_NAME_STRING = "user_name";
    private static SettingStore mInstance = null;
    private Context mContext = null;

    private final  String settingsApp = "WifilkieTalkie";
    private SharedPreferences mPreference = null;

    public static synchronized SettingStore getInstance() {
        if (mInstance == null)
            mInstance = new SettingStore();
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mPreference = context.getSharedPreferences(settingsApp, 0);
    }

    public boolean isInitialized() {
        return mContext != null;
    }

    public String getName() {
        return mPreference.getString(USER_NAME_STRING, "John");
    }

    public void setUserName(String name) {
        mPreference.edit().putString(USER_NAME_STRING, name).apply();
    }

}
