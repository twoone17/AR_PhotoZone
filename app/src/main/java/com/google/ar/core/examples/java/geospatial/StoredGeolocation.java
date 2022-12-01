package com.google.ar.core.examples.java.geospatial;
public class StoredGeolocation {

    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;


    public StoredGeolocation(double latitude, double longitude, double altitude, double heading) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.heading = heading;
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

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }


    @Override
    public String toString() {
        return "StoredGeolocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", heading=" + heading +
                '}';
    }
}
