package com.parentchild.childcalculator;

public class CallLogModel {

    String name, date, time, duration, type, number;

    CallLogModel(){

    }

    public CallLogModel(String name, String date, String time, String duration, String type, String number) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.type = type;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
