package se.wtm.sublibra.lightControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import se.wtm.sublibra.myapplication.R;

public class Lights extends AppCompatActivity implements DownloadCallback {

    // TellProx API routes
    private static final String listDevicesURL = "/json/devices/list?key=&supportedMethods=1";
    private static final String switchDeviceURL = "/json/device/toggle?key=&id=";
    private static final String offDeviceURL = "/json/device/turnoff?key=&id=";
    private static final String onDeviceURL = "/json/device/turnon?key=&id=";
    private static final String dimDeviceURL = "/json/device/dim?key=&id=%1$d&level=%2$d"; // Require String.format to add id and dim value

    public static final String TAG = "LightControl";
    private String deviceListURL;
    private String deviceURL;
    private String dimURL;
    private String offURL;
    private String onURL;

    private void retrieveURLFromSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String baseURL = sharedPrefs.getString("base_url", "NULL");
        String port = sharedPrefs.getString("port", "");
        if (!port.isEmpty()) {
            port = ":" + port;
        }
        // Ending based on tellprox REST interface
        deviceURL = baseURL + port + switchDeviceURL;
        deviceListURL = baseURL + port + listDevicesURL;
        dimURL = baseURL + port + dimDeviceURL;
        offURL = baseURL + port + offDeviceURL;
        onURL = baseURL + port + onDeviceURL;
    }

    private void retrieveDeviceListFromNetwork() {
        String message;
        if (deviceListURL == null || !URLUtil.isValidUrl(deviceListURL)) {
            message = "Please set valid base URL setting in settings before updating";
        } else {
            DownloadTask dt = new DownloadTask(Lights.this);
            dt.execute(new ServerRequest(ServerRequest.LIST, deviceListURL));
            message = "Update light control list";
        }
        Snackbar.make(findViewById(R.id.app_bar), message, Snackbar.LENGTH_LONG)
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

        // Do initial update
        retrieveDeviceListFromNetwork();
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
            case R.id.action_update_ligts:
                retrieveDeviceListFromNetwork();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    private void createDimDeviceObject(Device device, LinearLayout linearLayout) {
        final TextView tw = new TextView(this);
        tw.setText(device.getDeviceName());
        tw.setTextSize(24);
        linearLayout.addView(tw);

        final SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(255);
        seekBar.setProgress(device.dimLevel);
        seekBar.setPadding(0, 0, 0, 20);
        seekBar.setId(device.getId());
        linearLayout.addView(seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                int percentageProgress = progresValue * 100 / seekBar.getMax();
                DownloadTask dt = new DownloadTask(Lights.this);

                if (percentageProgress<=10){
                    dt.execute(new ServerRequest(ServerRequest.TOGGLE, offURL + seekBar.getId()));
                    seekBar.setProgress(0);
                    percentageProgress = 0;
                    Log.d(Lights.TAG, offURL + seekBar.getId());
                } else {
                    dt.execute(new ServerRequest(ServerRequest.DIM,
                            String.format(dimURL, seekBar.getId(), progresValue)));
                    Log.d(Lights.TAG, deviceURL + seekBar.getId());
                }
                Snackbar.make(seekBar, "Setting dim level to: " + percentageProgress + "%", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // not implemented
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // not implemented
            }
        });
    }

    private void createSwitchObject(Device device, LinearLayout linearLayout) {
        final Switch devSwitch = new Switch(this);
        devSwitch.setText(device.getDeviceName());
        devSwitch.setId(device.getId());
        devSwitch.setChecked(device.isOn());
        devSwitch.setTextSize(24);
        devSwitch.setPadding(0, 0, 0, 20);
        linearLayout.addView(devSwitch);
        devSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String cmd;
                cmd = (isChecked) ? onURL: offURL;
                Snackbar.make(buttonView, "Toggling " + devSwitch.getText(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                DownloadTask dt = new DownloadTask(Lights.this);
                dt.execute(new ServerRequest(ServerRequest.TOGGLE, cmd + buttonView.getId()));
                Log.d(Lights.TAG, cmd + buttonView.getId());
            }
        });
    }


    public void addAvailableControllers(final DeviceList deviceList) {
        if (deviceList.getLength() == 0) {
            snackBarMessage("Error - could not find any devices");
        } else {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.controls);
            linearLayout.removeAllViews();
            for (Device device : deviceList.getDevices()) {
                if (device.isDimmable()) {
                    createDimDeviceObject(device, linearLayout);
                } else {
                    createSwitchObject(device, linearLayout);
                }
            }
        }
    }


    private void snackBarMessage(String message) {
        Snackbar.make(findViewById(R.id.app_bar), message,
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * Update view based on what has been received from the network call
     *
     * @param netResult NetworkResult object containing command, command url and result
     *                  or an exception
     */
    @Override
    public void updateFromDownload(Object netResult) {
        ServerRequest result = (ServerRequest) netResult;

        if (result.getException() != null || result.getResultValue() == null) {
            snackBarMessage("Server communication fail. Check base URL: " + deviceListURL);
            Log.d(Lights.TAG, "Server communication fail: " + result.getException());
        } else {
            try {
                switch (result.getCommand()) {
                    case ServerRequest.LIST:
                        Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
                        DeviceList deviceList = new DeviceList(result.getResultValue());
                        addAvailableControllers(deviceList);
                        Log.d(Lights.TAG, deviceList.toString());
                        break;
                    case ServerRequest.TOGGLE:
                        Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
                        break;
                    case ServerRequest.DIM:
                        Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
                        break;
                    default:
                        Log.d(Lights.TAG, "Unknown command received: " + result.getCommand());
                        break;
                }
            } catch (JSONException | IOException e) {
                snackBarMessage("Server communication fail");
                Log.d(Lights.TAG, "Server communication fail: " + e.getMessage());
            }
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


