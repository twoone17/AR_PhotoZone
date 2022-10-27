package com.google.ar.core.examples.java.geospatial;
public class AnchorFirebase {

    private double latitude = 0;
    private double longitude=0;
    private double altitude=0;
    private double angleRadians=0;

    public AnchorFirebase() {
    }

    public AnchorFirebase(double latitude, double longitude, double altitude, double angleRadians) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.angleRadians = angleRadians;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAngleRadians() {
        return angleRadians;
    }

    public void setAngleRadians(double angleRadians) {
        this.angleRadians = angleRadians;
    }

    @Override
    public String toString() {
        return "AnchorFirebase{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", angleRadians=" + angleRadians +
                '}';
    }
}
