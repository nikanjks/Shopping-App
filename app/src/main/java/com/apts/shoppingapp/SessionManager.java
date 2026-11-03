package com.apts.shoppingapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void login(String username, String fullName) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getString(KEY_USERNAME, null) != null;
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }
}