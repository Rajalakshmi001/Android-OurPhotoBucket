package com.example.somasur.weatherpics;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Weather implements Parcelable{

    private String title;
    private String url;
    private String key;
    private String uid;

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    private Bitmap image;

    public Weather() {
        //empty constructor for Firebase deserialization from JSON
    }

    public Weather(String title, String url, String uid) {
        this.title = title;
        this.url = url;
        this.uid = uid;
    }

    protected Weather(Parcel in) {
        title = in.readString();
        url = in.readString();
        key = in.readString();
        uid = in.readString();
    }

    public static final Creator<Weather> CREATOR = new Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel in) {
            return new Weather(in);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValues(Weather weather) {
        this.title = weather.title;
        this.url = weather.url;
        this.uid = weather.uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(uid);
        dest.writeString(key);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
