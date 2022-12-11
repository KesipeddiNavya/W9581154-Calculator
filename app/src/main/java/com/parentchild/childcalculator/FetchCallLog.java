package com.parentchild.childcalculator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FetchCallLog {

    List<CallLogModel> callLogList = new ArrayList<>();

    String name, number, date, time, type, duration, dateTime;
    int count = 0;

    List<CallLogModel> fetch(Context context){

        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()){
            number= cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            dateTime = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            count++;

            name = (name == "" || name == null) ? "Unknown" : name;

            SimpleDateFormat dateFormet = new SimpleDateFormat("dd MMM yyyy");

            date = dateFormet.format(new Date(Long.parseLong(dateTime)));

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            time = get12Time(timeFormat.format(new Date(Long.parseLong(dateTime))));

            switch (Integer.parseInt(type)){

                case CallLog.Calls.INCOMING_TYPE:
                    type = "Incoming";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    type = "Outgoing";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    type = "Missed";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    type = "Voice Mail";
                    break;
                default:
                    type = "";
                    break;
            }

            callLogList.add(new CallLogModel(name, date, time, duration, type, number));

            if(count == 50){
                break;
            }
        }

        return callLogList;
    }

    String get12Time(String time){

        String finalTime = "";
        char a;
        String suffix = "";
        String t = "";
        int count =0 ;
        for(int i = 0; i < time.length(); i++){
            a = time.charAt(i);
            t += a;

            if(count == 1){
                if(Integer.parseInt(t) >= 12){
                    suffix = " PM";
                }
                else{
                    suffix = " AM";
                }
                finalTime += (Integer.parseInt(t) - 12) +"";
            }
            if(count >= 2 && count <= 4){
                finalTime += a;
            }
            count++;
        }

        return finalTime+suffix;
    }

}
