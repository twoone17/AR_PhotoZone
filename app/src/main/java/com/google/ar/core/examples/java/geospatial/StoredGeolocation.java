package com.google.ar.core.examples.java.geospatial;
public class StoredGeolocation {

    private double latitude;
    private double longitude;
    private double horizontalAccuracy;
    private double altitude;
    private double verticalAccuracy;
    private double heading;
    private double headingAccuracy;


    public StoredGeolocation(double latitude, double longitude,double horizontalAccuracy, double altitude, double verticalAccuracy, double heading, double headingAccuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.verticalAccuracy = verticalAccuracy;
        this.heading = heading;
        this.headingAccuracy = headingAccuracy;
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

    public double getVerticalAccuracy() {
        return verticalAccuracy;
    }

    public void setVerticalAccuracy(double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getHeadingAccuracy() {
        return headingAccuracy;
    }

    public void setHeadingAccuracy(double headingAccuracy) {
        this.headingAccuracy = headingAccuracy;
    }

    @Override
    public String toString() {
        return "StoredGeolocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", horizontalAccuracy=" + horizontalAccuracy +
                ", altitude=" + altitude +
                ", verticalAccuracy=" + verticalAccuracy +
                ", heading=" + heading +
                ", headingAccuracy=" + headingAccuracy +
                '}';
    }
}
