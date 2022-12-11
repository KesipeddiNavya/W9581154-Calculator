package com.parentchild.childcalculator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FetchMessages {

    int count = 0;
    List<MessageModel> fetch(Context context){
            List<MessageModel> mList = new ArrayList<>();
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            int totalSMS = 0;
            if (c != null) {
                totalSMS = c.getCount();
                while (c.moveToNext()){
//                    for (int j = 0; j < totalSMS; j++) {
                        count++;
                        String date, time;
                        String dateTime = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                        String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                        String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                        String type = "";

                        SimpleDateFormat dateFormet = new SimpleDateFormat("dd MMM yyyy");

                        date = dateFormet.format(new Date(Long.parseLong(dateTime)));

                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                        time = get12Time(timeFormat.format(new Date(Long.parseLong(dateTime))));
                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                type = "inbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                                type = "sent";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                type = "outbox";
                                break;
                            default:
                                break;
                        }

                        MessageModel mm = new MessageModel(body, number, type, date , time);
                        mList.add(mm);
                        if(count == 50){
                            break;
                        }
//                    }

                }

                c.close();

            } else {
                Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
            }
            return  mList;
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
