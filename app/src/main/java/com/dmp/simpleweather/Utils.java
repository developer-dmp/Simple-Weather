package com.dmp.simpleweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.inputmethod.InputMethodManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rtd1p on 3/19/2018.
 */

public class Utils {

    /**
     * Helper method to format the parameters needed to send to the server via URL
     *
     * @param params - mapping of keys to values for the parameters
     * @return - URL encoded parameters
     */
    public static String formatParams(HashMap<String,String> params) throws UnsupportedEncodingException {

        StringBuilder builder = new StringBuilder("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // defense for nulls
            if (key == null) {
                key = "";
            }

            if (value == null) {
                value = "";
            }

            String param =
                    URLEncoder.encode(key, "utf-8") +"="+
                            URLEncoder.encode(value, "utf-8") +"&";
            builder.append(param);
        }
        return builder.toString().substring(0, builder.length()-1);
    }

    /**
     * Helper method to hide the keyboard on command.
     */
    static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // defense
        try {
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    /**
     * Helper to save information to the {@link SharedPreferences}
     *
     * @param prefName - name of the preference to save
     * @param prefValue - value to save to the preference name
     */
    static void saveSharedPreference(Context context, String prefName, String prefValue) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(prefName, prefValue);
        editor.apply();
    }
}
