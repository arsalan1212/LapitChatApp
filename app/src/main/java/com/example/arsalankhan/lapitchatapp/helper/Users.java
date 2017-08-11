package com.example.arsalankhan.lapitchatapp.helper;

/**
 * Created by Arsalan khan on 7/31/2017.
 */

public class Users {

    public String name;
    public String status;
    public String thumb_image;


    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public Users(){
        //default constructor necessary for firebase
    }
    public Users(String name, String status, String thumb_image) {
        this.name = name;
        this.status = status;
        this.thumb_image=thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
