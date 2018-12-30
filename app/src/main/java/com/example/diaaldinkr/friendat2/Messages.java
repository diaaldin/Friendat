package com.example.diaaldinkr.friendat2;

public class Messages {
    private String from, message, type, time ,TranslatedMessage;

    public Messages() { }

    public Messages(String from, String message, String type, String time, String TranslatedMessage) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time=time;
        this.TranslatedMessage=TranslatedMessage;

    }
    public String getFrom() {
        return from;
    }

    public String getTranslatedMessage() {
        return TranslatedMessage;
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
