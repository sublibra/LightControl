package se.wtm.sublibra.lightControl;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jonash on 2017-01-05.
 */

public class DeviceList {

    Device[] devices;

    public DeviceList(){
        devices=null;
    }

    public DeviceList(String deviceData) throws JSONException, IOException{
        devices = parseDeviceList(deviceData);
    }

    public int getLength(){
      if (devices == null){
          return 0;
      } else {
          return devices.length;
      }
    }

    public Device[] getDevices() {
        return devices;
    }

    /**
     * Parse list of all devices returned as json
     *
     * @param deviceData data returned from the server as json
     * @return Array of device objects containing the device's data
     */
    protected Device[] parseDeviceList(String deviceData) throws JSONException, IOException {
        Device[] deviceArray = null;

        //Log.d(Lights.TAG, "JSON: " + deviceData);

        try {
            JSONObject jsonObj = new JSONObject(new String(deviceData));

            // Check if we have a valid sensor. i.e. it contains the data node
            if (jsonObj.has("device")) {
                List<Device> deviceList = new ArrayList<Device>();
                String name;
                int id;
                boolean isOn;
                boolean dimmable = false;
                JSONArray jsonDevice = jsonObj.getJSONArray("device");
                for (int i = 0; i < jsonDevice.length(); i++) {
                    JSONObject obj = jsonDevice.getJSONObject(i);
                    name = obj.getString("name");
                    id = obj.getInt("id");
                    isOn = obj.getInt("state") == 1;
                    JSONObject parameter = obj.getJSONObject("parameter");
                    if (parameter.getString("fade").equals("true")) {
                        dimmable = true;
                    }
                    Device dev = new Device(name, dimmable, id);
                    dev.setOn(isOn);
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

    @Override
    public String toString() {
        return "DeviceList{" +
                "devices=" + Arrays.toString(devices) +
                '}';
    }
}
