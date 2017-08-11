package com.example.arsalankhan.lapitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

    }


    //Button Listener
    //go to Login Activity
    public void GoToLoginActivity(View view){
        Intent loginIntent=new Intent(StartActivity.this,loginActivity.class);
        startActivity(loginIntent);

    }

    // Move the user to Registration Activity
    public void GoToRegistrationActivity(View view){
        Intent intentRegistration=new Intent(StartActivity.this,registerUserActivity.class);
        startActivity(intentRegistration);
    }
}
