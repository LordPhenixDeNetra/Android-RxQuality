package com.netraapp.rxquality.model;

public class MarkerNetworkInfoWrapper {
    private NMarker nMarker;
    private NetworkInfo networkInfo;

    public NMarker getMarker() {
        return nMarker;
    }

    public void setMarker(NMarker NMarker) {
        this.nMarker = NMarker;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }

    @Override
    public String toString() {
        return "MarkerNetworkInfoWrapper{" +
                "NMarker=" + getMarker().toString() +
                ", networkInfo=" + getNetworkInfo().toString() +
                '}';
    }
}

