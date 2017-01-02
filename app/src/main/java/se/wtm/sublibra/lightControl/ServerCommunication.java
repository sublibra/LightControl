package se.wtm.sublibra.lightControl;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.wtm.sublibra.myapplication.R;

/**
 * Created by jonash on 2016-12-29.
 */ // AsyncTask <Params, progress, results>
public class ServerCommunication extends AsyncTask<String, Void, Device[]> {

    @Override
    protected Device[] doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        try {
            String deviceData = downloadUrl(urls[0]);
            if (deviceData != null) {
                return parseDeviceList(deviceData);
            } else {
                return null;
                // fail
            }
        } catch (JSONException e){
            Log.d(Lights.TAG, "Broken JSON" + e.getMessage());
            return null;
        } catch (SocketTimeoutException e) {
            Log.d(Lights.TAG, "Could not contact server. Socket timeout" + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(Lights.TAG, "Unable to retrieve web page. URL may be invalid." + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Device[] result) {
        if (result != null) {
            Log.d(Lights.TAG, "Content: " + result.toString());
        } else {
            Log.d(Lights.TAG, "No results found");
        }
    }


    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves the data as a string
     * response limited to 5000 characters since sensor input is short
     *
     * @param myurl
     * @return sensor data as string
     * @throws IOException
     * @throws SocketTimeoutException
     */
    private String downloadUrl(String myurl) throws IOException, SocketTimeoutException {
        int len = 50000; // only read 5000 first chars
        InputStream is = null;
        Reader reader = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(Lights.TAG, "HTTP response is: " + response);
            is = conn.getInputStream();
            reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        } finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Parse list of all devices returned as json
     *
     * @param deviceData data returned from the server as json
     * @return Array of device objects containing the device's data
     */
    protected Device[] parseDeviceList(String deviceData) throws JSONException, IOException{
        Device[] deviceArray = null;

        Log.d(Lights.TAG, "JSON: " + deviceData);

        try {
            JSONObject jsonObj = new JSONObject(new String(deviceData));

            // Check if we have a valid sensor. i.e. it contains the data node
            if (jsonObj.has("device")) {
                List<Device> deviceList = new ArrayList<Device>();
                String name;
                int id;
                boolean dimmable = false;
                JSONArray jsonDevice = jsonObj.getJSONArray("device");
                for (int i = 0; i < jsonDevice.length(); i++) {
                    JSONObject obj = jsonDevice.getJSONObject(i);
                    name = obj.getString("name");
                    id = obj.getInt("id");
                    JSONObject parameter = obj.getJSONObject("parameter");
                    if (parameter.getString("fade").equals("true")) {
                        dimmable = true;
                    }
                    Device dev = new Device(name, dimmable, id);
                    Log.d(Lights.TAG, "Device: " + dev.toString());
                    deviceList.add(dev);
                }
                deviceArray = deviceList.toArray(new Device[deviceList.size()]);
            } else {
                Log.d(Lights.TAG, "error: " + jsonObj.getString("error"));
            }
        } catch (NullPointerException e) {
            Log.d(Lights.TAG, e.getMessage());
            throw new IOException(e);
        }
        return deviceArray;
    }
}
