package com.parentchild.childcalculator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FetchContact {

    public List<ContactModel> getContacts(Context ctx) {
        List<ContactModel> cList = new ArrayList<>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        int count = 0;

        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";
        Cursor cursor = ctx.getContentResolver().query(
                uri, null, null, null, sort
        );
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()){
                count++;
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";

            Cursor phoneCursor = ctx.getContentResolver().query(
                    uriPhone, null, selection, new String[]{id}, null
            );

            if (phoneCursor.moveToNext()) {
                String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                ContactModel cm = new ContactModel(id, name, number);
                cList.add(cm);
                phoneCursor.close();
            }

            if(count == 100){
                break;
            }
        }
        }
        cursor.close();

        return cList;
    }

}
