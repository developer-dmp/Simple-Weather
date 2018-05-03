package com.dmp.simpleweather;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to parse the weather response from the server.
 * Saves the information to the {@link android.content.SharedPreferences}
 * for future use.  We also then kick off the service to update the
 * widget for the user.
 *
 * @author Domenic Polidoro
 * @version 1.0
 */

class ParseWeatherResponse extends AsyncTask<JSONObject, Void, Void> {

    private Context context;

    ParseWeatherResponse(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(JSONObject... params) {
        JSONObject response = params[0];
        try {
            JSONObject main = response.getJSONObject("main");
            double kelvin = Double.valueOf(main.getString("temp"));
            double nineFifths = 9/5;

            // conversion from Kelvin to F
            String temp_f = String.valueOf(round((nineFifths*(kelvin - 273.15)) + 32, 2));
            Utils.saveSharedPreference(
                    context,
                    Constants.PREF_RECENT_TEMP,
                    temp_f
            );

            JSONArray array = response.getJSONArray("weather");
            String description = array.getJSONObject(0).getString("description");
            Utils.saveSharedPreference(
                    context,
                    Constants.PREF_RECENT_DESCRIPTION,
                    description
            );

            Utils.saveSharedPreference(
                    context,
                    Constants.PREF_RECENT_TIMESTAMP,
                    new SimpleDateFormat("hh:mm a", Locale.US).format(new Date())
            );

            Utils.saveSharedPreference(
                    context,
                    Constants.PREF_TOWN_NAME,
                    response.getString("name")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            MainActivity.mDialog.dismiss();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // Kick off the service to update the widget

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, WeatherWidgetProvider.class);
        int[] allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);
    }

    /**
     * Helper method to round a given number to a particular number of decimal places.
     *
     * @param value - the number to round
     * @param places - the number of places after the decimal
     * @return - newly rounded double
     */
    private double round(double value, int places) {
        return new BigDecimal(value)
                        .setScale(places, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
    }
}
