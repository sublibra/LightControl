package se.wtm.sublibra.lightControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.json.JSONException;

import java.io.IOException;

import se.wtm.sublibra.myapplication.R;

public class Lights extends AppCompatActivity implements DownloadCallback{

    public static final String TAG = "LightControl";
    String deviceListURL;
    String deviceURL;

    private void retrieveURLFromSettings(){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String baseURL = sharedPrefs.getString("base_url", "NULL");
        String port = sharedPrefs.getString("port", "");
        if (!port.isEmpty()){
            port = ":" + port;
        }
        // Ending based on tellprox REST interface
        deviceURL = baseURL + port + "/json/device/toggle?key=&id=";
        deviceListURL = baseURL + port + "/json/devices/list?key=&supportedMethods=1";

    }

    private void retrieveDeviceListFromNetwork(View view) {
        String message;
        if (deviceListURL == null || !URLUtil.isValidUrl(deviceListURL)) {
            message = "Please set valid base URL setting in settings before updating";
        } else {
            DownloadTask dt = new DownloadTask(Lights.this);
            dt.execute(deviceListURL);
            message = "Update light control list";
        }
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get base url from settings
        retrieveURLFromSettings();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveDeviceListFromNetwork(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lights, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void addAvailableControllers(final DeviceList deviceList){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.controls);
        linearLayout.removeAllViews();

        for (Device device : deviceList.getDevices() ){
            final Switch devSwitch = new Switch(this);
            devSwitch.setText(device.getDeviceName());
            devSwitch.setId(device.getId());
            devSwitch.setChecked(device.isOn());
            linearLayout.addView(devSwitch);
            devSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Toggling " + devSwitch.getText(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    DownloadTask dt = new DownloadTask(Lights.this);
                    dt.execute(deviceURL + view.getId());
                    Log.d(Lights.TAG, deviceURL + view.getId());
                }
            });
        }
    }


    /**
     * Update view based on what has been received from the network call
     * @param netResult NetworkResult object containing command, result or an exception
     */
    @Override
    public void updateFromDownload(Object netResult) {
        NetworkResult result = (NetworkResult) netResult;
        try {
            if (result.getException()!=null || result.getResultValue() == null){
                Log.d(Lights.TAG, "Server communication fail: " + result.getException());
            } else if (result.getCommand().equals(deviceListURL)) {
                Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
                DeviceList deviceList = new DeviceList(result.getResultValue());
                addAvailableControllers(deviceList);
                Log.d(Lights.TAG, deviceList.toString());
            } else if (result.getCommand().contains(deviceURL)){
                // The command was a code switch.
                Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
            } else {
                Log.d(Lights.TAG, "Unknown command received: " + result.getCommand());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        // Not implemented
    }

    @Override
    public void finishDownloading() {
        // Not implemented
    }
}


