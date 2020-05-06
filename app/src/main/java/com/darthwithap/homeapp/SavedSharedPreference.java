package com.darthwithap.homeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SavedSharedPreference {
    static final String USER_ID = "id";
    static final String CUSTOMER_TYPE = "type";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserId(Context context, String user_id) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USER_ID, user_id);
        editor.commit();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USER_ID, "");
    }

    public static void clearUser(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }
    public static void setCustomerType(Context context, String type) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CUSTOMER_TYPE, type);
        editor.commit();
    }

    public static String getCustomerType(Context context) {
        return getSharedPreferences(context).getString(CUSTOMER_TYPE, "");
    }
}
