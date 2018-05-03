package com.dmp.simpleweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Class used to handle the user interaction when they are
 * selecting a zip code they would like to see the weather for.
 *
 * @author Domenic Polidoro
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    public static ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, Constants.ADMOB_APP_ID);

        // configure global dialog
        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage("Fetching weather ...");

        configure();

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /**
     * Setup the views for user interaction.
     */
    private void configure() {
        // listen for the 'Go' button press
        findViewById(R.id.go_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goButtonPressed();
            }
        });

        // listen for the 'Go' button press on the keyboard
        ((EditText)findViewById(R.id.zipcode_editText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    goButtonPressed();
                    return true;
                }
                return false;
            }
        });

        (findViewById(R.id.zipcode_editText)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }

    /**
     * Method to determine the logic that is to be performed when the user
     * selects the 'Go' button or presses 'Go' on the keyboard.
     */
    private void goButtonPressed() {
        String zip = ((EditText)findViewById(R.id.zipcode_editText)).getText().toString().trim();

        // simple error handling
        if (zip.length() != Constants.ZIPCODE_LENGTH) {
            Toast.makeText(MainActivity.this, "Invalid zipcode", Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.hideKeyboard(MainActivity.this);
        // save the user's last zip code queried
        Utils.saveSharedPreference(MainActivity.this, Constants.PREF_RECENT_ZIP, zip+",us");

        // gather the parameters
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.API_APPID_KEY, Constants.API_APPID_VALUE);
        params.put(Constants.API_ZIP_KEY, zip+",us");

        // perform network request
        try {
            mDialog.show();
            getWeather(Constants.BASE_API_URL+Utils.formatParams(params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mDialog.dismiss();
            Toast.makeText(MainActivity.this, "Invalid parameters", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to perform the network request to the Weather endpoint.
     * Kicks off background processing of the resulting JSON response.
     *
     * @param mUrl - url encoded endpoint to hit for JSON
     */
    private void getWeather(String mUrl) {
        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                mUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("JSON",response.toString());
                        new ParseWeatherResponse(MainActivity.this).execute(response);
                        ((TextView)findViewById(R.id.weather_info_textView)).setText(R.string.weather_success_message);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mDialog.dismiss();
                    }
                }
        );
        Volley.newRequestQueue(this).add(weatherRequest);
    }
}
