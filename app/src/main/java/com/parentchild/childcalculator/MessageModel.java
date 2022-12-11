package com.parentchild.childcalculator;

public class MessageModel {

    String msg, number, type, date, time;

    MessageModel(){

    }

    public MessageModel(String msg, String number, String type, String date, String time) {
        this.msg = msg;
        this.number = number;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
