package com.dmp.simpleweather;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Service used to update the Widget itself with the information that
 * was obtained from the network request.
 *
 * @author Domenic Polidoro
 * @version 1.0
 */

public class UpdateWidgetService extends Service {

    private final String TAG = "* Service *";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        Context context = getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        for (int widgetId : allWidgetIds) {

            RemoteViews views = new RemoteViews(this.getApplicationContext().getPackageName(),
                    R.layout.widget_layout);

            // Register the refresh button to kick off the service again
            Intent refreshIntent = new Intent(this.getApplicationContext(), WeatherWidgetProvider.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent);

            // Register the update button to send the user to the main activity
            PendingIntent updatePendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    new Intent(this.getApplicationContext(), MainActivity.class),
                    0);

            views.setOnClickPendingIntent(R.id.widget_update_button, updatePendingIntent);

            // gather information to present to user
            String temp = "Temp: " + preferences.getString(Constants.PREF_RECENT_TEMP, "N/A") + " F";
            String zip = "Zip: " + preferences.getString(Constants.PREF_RECENT_ZIP, "~~~~~").substring(0, Constants.ZIPCODE_LENGTH);
            String description = "Description: " + preferences.getString(Constants.PREF_RECENT_DESCRIPTION, "N/A");
            String timestamp = "Last Refreshed: " + preferences.getString(Constants.PREF_RECENT_TIMESTAMP, "Never");

            // load info into views
            views.setTextViewText(R.id.temp_widget_textView, temp);
            views.setTextViewText(R.id.zipcode_widget_textView, zip);
            views.setTextViewText(R.id.description_widget_textView, description);
            views.setTextViewText(R.id.last_updated_widget_textView, timestamp);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }
}
