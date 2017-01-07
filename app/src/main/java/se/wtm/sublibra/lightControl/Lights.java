package se.wtm.sublibra.lightControl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.json.JSONException;

import java.io.IOException;

import se.wtm.sublibra.myapplication.R;

public class Lights extends AppCompatActivity implements DownloadCallback{

    public static final String TAG = "LightControl";
    final String deviceListURL = "";
    final String deviceURL = ""; 


    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NetworkFragment sc = new NetworkFragment();
        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), deviceListURL);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mDownloading && mNetworkFragment != null) {
                    // Execute the async download.
                    mNetworkFragment.startDownload();
                    mDownloading = true;
                }
                Snackbar.make(view, "SDFGSDFG", Snackbar.LENGTH_LONG)
                       .setAction("Action", null).show();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addAvailableControllers(final DeviceList deviceList){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.controls);
        linearLayout.removeAllViews();

        for (Device device : deviceList.getDevices() ){
            Switch devSwitch = new Switch(this);
            devSwitch.setText(device.getDeviceName());
            devSwitch.setId(device.getId());
            devSwitch.setChecked(device.isOn());
            linearLayout.addView(devSwitch);
            devSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Toggling " + view.getId(),  Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), deviceURL + view.getId());
                    if (!mDownloading && mNetworkFragment != null) {
                        mNetworkFragment.startDownload();
                        mDownloading = true;
                        Log.d(Lights.TAG, deviceURL + view.getId());
                    }
                }
            });
        }
    }

    @Override
    public void updateFromDownload(Object netResult) {
        NetworkResult result = (NetworkResult) netResult;
        try {
            if (result.getCommand().equals(deviceListURL)) {
                DeviceList deviceList = new DeviceList(result.getResultValue());
                addAvailableControllers(deviceList);
                Log.d(Lights.TAG, deviceList.toString());
            } else {
                Log.d(Lights.TAG, result.getCommand() + ":" + result.getResultValue());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };
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
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }
}


