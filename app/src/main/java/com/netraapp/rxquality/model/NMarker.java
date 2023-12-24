package com.netraapp.rxquality.model;

public class NMarker {

    private long id;
    private double latitude;
    private double longitude;
    private String title;
    private String snippet;

    public NMarker(long id, double latitude, double longitude, String title, String snippet) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.snippet = snippet;
    }

    public NMarker(double latitude, double longitude, String title, String snippet) {
        this.id = 0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.snippet = snippet;
    }

    public NMarker() {

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @Override
    public String toString() {
        return "NMarker{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}
