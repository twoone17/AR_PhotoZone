/**
 * 파이어베이스에 저장할 데이터
 * 위치 : users - uid - posts - postID - {document}
 *
 * -anchorFirebase : latitude, longitude, altitude, angleRadians
 * -imgURL : 촬영한 사진 이미지
 * -userID
 */
package com.google.ar.core.examples.java.geospatial;
public class UploadFirebaseData {

    private String userId;
    private String imgURL;
    private String referBoardDocId;
    private double latitude ;
    private double longitude;
    private double altitude;
    private String anchorID;

    public UploadFirebaseData(String userId, String imgURL, double latitude, double longitude, double altitude, double heading,String anchorID) {
        this.userId = userId;
        this.imgURL = imgURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.heading = heading;
        this.anchorID = anchorID;
    }

    public String getUserId() {
        return userId;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getAnchorID() {
        return anchorID;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getHeading() {
        return heading;
    }

    private double heading;



}


