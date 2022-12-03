package com.google.ar.core.examples.java.retrofit_rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RouteDTO implements Serializable {
    @Expose
    @SerializedName("features")
    private List<Features> features;
    @Expose
    @SerializedName("type")
    private String type;

    public RouteDTO() {
    }

    public List<Features> getFeatures() {
        return features;
    }

    public void setFeatures(List<Features> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Features implements Serializable{
        @Expose
        @SerializedName("properties")
        private Properties properties;
        @Expose
        @SerializedName("geometry")
        private Geometry geometry;
        @Expose
        @SerializedName("type")
        private String type;

        public Features() {
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Properties implements Serializable{
        @Expose
        @SerializedName("pointType")
        private String pointtype;
        @Expose
        @SerializedName("turnType")
        private int turntype;
        @Expose
        @SerializedName("facilityName")
        private String facilityname;
        @Expose
        @SerializedName("facilityType")
        private String facilitytype;
        @Expose
        @SerializedName("intersectionName")
        private String intersectionname;
        @Expose
        @SerializedName("nearPoiY")
        private String nearpoiy;
        @Expose
        @SerializedName("nearPoiX")
        private String nearpoix;
        @Expose
        @SerializedName("nearPoiName")
        private String nearpoiname;
        @Expose
        @SerializedName("direction")
        private String direction;
        @Expose
        @SerializedName("description")
        private String description;
        @Expose
        @SerializedName("name")
        private String name;
        @Expose
        @SerializedName("pointIndex")
        private int pointindex;
        @Expose
        @SerializedName("index")
        private int index;
        @Expose
        @SerializedName("totalTime")
        private int totaltime;
        @Expose
        @SerializedName("totalDistance")
        private int totaldistance;

        @Expose
        @SerializedName("distance")
        private int distance;

        public Properties() {
        }

        public String getPointtype() {
            return pointtype;
        }

        public void setPointtype(String pointtype) {
            this.pointtype = pointtype;
        }

        public int getTurntype() {
            return turntype;
        }

        public void setTurntype(int turntype) {
            this.turntype = turntype;
        }

        public String getFacilityname() {
            return facilityname;
        }

        public void setFacilityname(String facilityname) {
            this.facilityname = facilityname;
        }

        public String getFacilitytype() {
            return facilitytype;
        }

        public void setFacilitytype(String facilitytype) {
            this.facilitytype = facilitytype;
        }

        public String getIntersectionname() {
            return intersectionname;
        }

        public void setIntersectionname(String intersectionname) {
            this.intersectionname = intersectionname;
        }

        public String getNearpoiy() {
            return nearpoiy;
        }

        public void setNearpoiy(String nearpoiy) {
            this.nearpoiy = nearpoiy;
        }

        public String getNearpoix() {
            return nearpoix;
        }

        public void setNearpoix(String nearpoix) {
            this.nearpoix = nearpoix;
        }

        public String getNearpoiname() {
            return nearpoiname;
        }

        public void setNearpoiname(String nearpoiname) {
            this.nearpoiname = nearpoiname;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPointindex() {
            return pointindex;
        }

        public void setPointindex(int pointindex) {
            this.pointindex = pointindex;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getTotaltime() {
            return totaltime;
        }

        public void setTotaltime(int totaltime) {
            this.totaltime = totaltime;
        }

        public int getTotaldistance() {
            return totaldistance;
        }

        public void setTotaldistance(int totaldistance) {
            this.totaldistance = totaldistance;
        }

        public int getDistance() {
            return distance;
        }

        public void setTodistance(int distance) {
            this.distance = distance;
        }
    }

    public static class Geometry implements Serializable{
        @Expose
        @SerializedName("coordinates")
        private Object coordinates;
        @Expose
        @SerializedName("type")
        private String type;

        public Geometry() {
        }

        public Object getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Object coordinates) {
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
