package com.uit.surpic;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class objectPic implements Parcelable {
    private String key;
    private String data;
    private String name;
    private String[] Tags;
    private String b64EmailOwner;

    public objectPic(String key, String data, String name, String strTags,String b64EmailOwner){
        this.key=key;
        this.data=data;
        this.name=name;
        this.Tags=strTags.split(" ");
        this.b64EmailOwner=b64EmailOwner;
    }
    public objectPic(String key, String name, String strTags,String b64EmailOwner){
        this.key=key;
        this.name=name;
        this.Tags=strTags.split(" ");
        this.b64EmailOwner=b64EmailOwner;
    }

    public void setB64EmailOwner(String b64EmailOwner) {
        this.b64EmailOwner = b64EmailOwner;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String[] getTags() {
        return Tags;
    }

    public String getB64EmailOwner() {
        return b64EmailOwner;
    }

    public String getStrTags(){return String.join(" ",Tags);}
    public String getStrHashtags(){
        String hashtags="#"+String.join(" #",Tags);
        return hashtags.substring(0,hashtags.length());
    }


    protected objectPic(Parcel in){
        key=in.readString();
        data=in.readString();
        name=in.readString();
        b64EmailOwner=in.readString();

        Tags=in.createStringArray();
    }
    public static final Creator<objectPic> CREATOR=new Creator<objectPic>() {
        @Override
        public objectPic createFromParcel(Parcel source) {
            return new objectPic(source);
        }

        @Override
        public objectPic[] newArray(int size) {
            return new objectPic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(data);
        dest.writeString(name);
        dest.writeString(b64EmailOwner);

        if(Tags!=null)
            dest.writeStringArray(Tags);
    }
}
