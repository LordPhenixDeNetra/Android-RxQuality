package com.netraapp.rxquality.model;

import com.netraapp.rxquality.enums.SignalQuality;

public class NetworkInfo {
    private long id;
    private long markerId;
    private String networkType;
    private float signalStrength;
    private float frequency;
    private float linkSpeed;
    private float coverage;
    private SignalQuality signalQuality;

    public NetworkInfo(long id, long markerId, String networkType, float signalStrength, float frequency, float linkSpeed, float coverage, SignalQuality signalQuality) {
        this.id = id;
        this.markerId = markerId;
        this.networkType = networkType;
        this.signalStrength = signalStrength;
        this.frequency = frequency;
        this.linkSpeed = linkSpeed;
        this.coverage = coverage;
        this.signalQuality = signalQuality;
    }

    public NetworkInfo(String networkType, float signalStrength, float frequency, float linkSpeed, float coverage, SignalQuality signalQuality) {
        this.id = 0;
        this.markerId = 0;
        this.networkType = networkType;
        this.signalStrength = signalStrength;
        this.frequency = frequency;
        this.linkSpeed = linkSpeed;
        this.coverage = coverage;
        this.signalQuality = signalQuality;
    }

    public NetworkInfo() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMarkerId() {
        return markerId;
    }

    public void setMarkerId(long markerId) {
        this.markerId = markerId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public float getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(float signalStrength) {
        this.signalStrength = signalStrength;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getLinkSpeed() {
        return linkSpeed;
    }

    public void setLinkSpeed(float linkSpeed) {
        this.linkSpeed = linkSpeed;
    }

    public float getCoverage() {
        return coverage;
    }

    public void setCoverage(float coverage) {
        this.coverage = coverage;
    }

    public SignalQuality getSignalQuality() {
        return signalQuality;
    }

    public void setSignalQuality(SignalQuality signalQuality) {
        this.signalQuality = signalQuality;
    }

    @Override
    public String toString() {
        return "NetworkInfo{" +
                "id=" + id +
                ", markerId=" + markerId +
                ", networkType='" + networkType + '\'' +
                ", signal_strength=" + signalStrength +
                ", frequency=" + frequency +
                ", linkSpeed=" + linkSpeed +
                ", coverage=" + coverage +
                ", signalQuality=" + signalQuality +
                '}';
    }
}
