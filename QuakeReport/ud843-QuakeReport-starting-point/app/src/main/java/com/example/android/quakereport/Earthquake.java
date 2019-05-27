package com.example.android.quakereport;

public class Earthquake {
    private Double mMag;
    private String mLocation;
    private long mTimeInMilliseconds;
    private String mUrl;

    public Earthquake(Double mag, String location, long timeInMilliseconds,String url) {
        mMag = mag;
        mLocation = location;
        mTimeInMilliseconds = timeInMilliseconds;
        mUrl=url;
    }

    public long getmTimeInMilliseconds() {
        return mTimeInMilliseconds;
    }

    public String getmLocation() {
        return mLocation;
    }

    public Double getmMag() {
        return mMag;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmTimeInMilliseconds(long mTimeInMilliseconds) {
        this.mTimeInMilliseconds = mTimeInMilliseconds;
    }

    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public void setmMag(Double mMag) {
        this.mMag = mMag;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

}
