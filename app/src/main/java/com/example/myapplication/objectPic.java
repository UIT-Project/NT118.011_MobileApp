package com.example.myapplication;

public class objectPic {
    private String data;
    private String name;
    private String[] Tags;

    public objectPic(String data){
        this.data=data;
    }

    public objectPic(String data, String name){
        this.data=data;
        this.name=name;
    }

    public objectPic(String data, String name, String strTags){
        this.data=data;
        this.name=name;
        this.Tags=strTags.split(" ");
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
}
