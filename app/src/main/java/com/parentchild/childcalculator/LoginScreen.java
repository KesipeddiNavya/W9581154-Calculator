package com.parentchild.childcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    EditText email_field, pass_field, username_field;
    Button loginBtn;
    String email, pass, username;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    boolean isValid;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Childrens");
    String permissionsList[] = new String[]{
            Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        getSupportActionBar().hide();

        username_field = findViewById(R.id.username_field);
        pass_field = findViewById(R.id.pass_field);
        loginBtn = findViewById(R.id.login_btn);

        ActivityCompat.requestPermissions(this, permissionsList, 19);

        if(isLoggedIn()){
            startActivity(new Intent(LoginScreen.this, MainActivity2.class));
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = username_field.getText().toString();
                pass = pass_field.getText().toString();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot s : snapshot.getChildren()){
                            String childrenPass = s.child("pass").getValue().toString();
                            String childrenName = s.child("name").getValue().toString();
                            String childrenUsername = s.child("username").getValue().toString();
                            String parentUid = s.child("parentUid").getValue().toString();

                            if(username.equals(childrenUsername) && pass.equals(childrenPass)){
                                storeDataAndLogIn(email, childrenName, parentUid, username);
                            }
                            else{
                                Toast.makeText(LoginScreen.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    void storeDataAndLogIn(String email, String name, String parentUid, String username){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);

        SharedPreferences.Editor data = sharedPreferences.edit();

        data.putString("name", name);
        data.putString("parentUid", parentUid);
        data.putString("email", email);
        data.putString("username", username);
        data.commit();


        startActivity(new Intent(LoginScreen.this, MainActivity2.class));
    }

    public boolean isLoggedIn(){
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return (sh.getString("username", "") != "");
    }


}