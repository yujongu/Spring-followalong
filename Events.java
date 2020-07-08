package com.yujongu.coronatracker.Models;

public class Events {
    String date;
    String event;

    public Events(String date, String event) {
        this.date = date;
        this.event = event;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
