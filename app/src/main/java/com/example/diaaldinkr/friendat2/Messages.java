package com.example.diaaldinkr.friendat2;

public class Messages {
    private String from, to, message, type, time ;

    public Messages() { }

    public Messages(String from, String to ,String message, String type, String time) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.type = type;
        this.time=time;

    }
    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }


    public String getMessage() {
        return message;
    }



    public String getType() {
        return type;
    }



    public String getTime() {
        return time;
    }


}
