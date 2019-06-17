package com.abuTawfeek.diaaldinkr.friendat;

public class groupMessages {
    private String name, message, type, time, sender_id;

public groupMessages(){}
    public groupMessages( String message ,String name, String sender_id,String time , String type) {
        this.name = name;
        this.sender_id = sender_id;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
