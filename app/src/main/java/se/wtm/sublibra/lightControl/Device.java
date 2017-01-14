package se.wtm.sublibra.lightControl;

/**
 * Created by jonash on 2016-12-29.
 */
public class Device {
    String deviceName;
    boolean isDimmable;
    boolean isOn;
    int id;
  int dimLevel;

    public Device(){

    }

    public Device(String deviceName, boolean isDimmable, int dimLevel, int id) {
        this.deviceName = deviceName;
        this.isDimmable = isDimmable;
        this.dimLevel = dimLevel;
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isDimmable() {
        return isDimmable;
    }

    public void setDimmable(boolean dimmable) {
        isDimmable = dimmable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getDimLevel() {
        return dimLevel;
    }

    public void setDimLevel(int dimLevel) {
        this.dimLevel = dimLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (isDimmable != device.isDimmable) return false;
        if (isOn != device.isOn) return false;
        if (id != device.id) return false;
        if (dimLevel != device.dimLevel) return false;
        return deviceName != null ? deviceName.equals(device.deviceName) : device.deviceName == null;

    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceName='" + deviceName + '\'' +
                ", isDimmable=" + isDimmable +
                ", dimLevel=" + dimLevel +
                ", isOn=" + isOn +
                ", id=" + id +
                '}';
    }
}
