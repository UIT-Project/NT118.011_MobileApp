package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class objectUser implements Parcelable{
    private String b64Email;
    private String dataUserPic;
    private String username;

    public objectUser(String string, boolean isBase64){
        if(isBase64){
            this.b64Email=string;
        }else {
            this.b64Email=GeneralFunc.str2Base64(string);
        }
    }

    public objectUser(String string, boolean isBase64, String dataUserPic, String username){
        if(isBase64){
            this.b64Email=string;
        }else {
            this.b64Email=GeneralFunc.str2Base64(string);
        }

        this.dataUserPic=dataUserPic;
        this.username=username;
    }

    public String getB64Email() {
        return b64Email;
    }


    public String getUsername() {
        return username;
    }

    public String getDataUserPic() {
        return dataUserPic;
    }

    protected objectUser(Parcel in){
        b64Email=in.readString();
        dataUserPic=in.readString();
        username=in.readString();
    }
    public static final Parcelable.Creator<objectUser> CREATOR=new Parcelable.Creator<objectUser>() {
        @Override
        public objectUser createFromParcel(Parcel source) {
            return new objectUser(source);
        }

        @Override
        public objectUser[] newArray(int size) {
            return new objectUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(b64Email);
        dest.writeString(dataUserPic);
        dest.writeString(username);
    }

    public void setB64Email(String b64Email) {
        this.b64Email = b64Email;
    }

    public void setDataUserPic(String dataUserPic) {
        this.dataUserPic = dataUserPic;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
