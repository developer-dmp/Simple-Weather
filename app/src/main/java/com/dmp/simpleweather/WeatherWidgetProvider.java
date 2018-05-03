package com.dmp.simpleweather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Class that will be invoked by the widget.  Either invoked at the
 * interval set in the xml definition for the widget or when the
 * user selects the 'Refresh' button on the widget itself.
 *
 * @see res/xml/widget_info.xml
 * @author Domenic Polidoro
 * @version 1.0
 */

public class WeatherWidgetProvider extends AppWidgetProvider {

    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("******", "onUpdate");

        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);

        // gather the parameters
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.API_APPID_KEY, Constants.API_APPID_VALUE);
        params.put(Constants.API_ZIP_KEY, preferences.getString(Constants.PREF_RECENT_ZIP, "08831"));

        // perform network request
        String mUrl = "";
        try {
            mUrl = Constants.BASE_API_URL+Utils.formatParams(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                mUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("JSON",response.toString());
                        new ParseWeatherResponse(context).execute(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        Volley.newRequestQueue(context).add(weatherRequest);
    }
}
