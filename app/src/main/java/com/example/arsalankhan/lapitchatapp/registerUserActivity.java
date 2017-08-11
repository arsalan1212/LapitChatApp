package com.example.arsalankhan.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class registerUserActivity extends AppCompatActivity {

    private TextInputLayout layoutDisplayName,layoutEmail,layoutPassword;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog mregProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //instiate FireBaseAuth object
        mAuth=FirebaseAuth.getInstance();

        //progress dialog
        mregProgress=new ProgressDialog(this);

        //initialize the TextInputLayout
        layoutDisplayName= (TextInputLayout) findViewById(R.id.registerDisplayNameLayout);
        layoutEmail= (TextInputLayout) findViewById(R.id.registerEmailLayout);
        layoutPassword= (TextInputLayout) findViewById(R.id.registerPasswordLayout);

        mToolbar= (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //create Account for User
    public void CreateUserAccount(View view){

        String displayName=layoutDisplayName.getEditText().getText().toString().trim();
        String email=layoutEmail.getEditText().getText().toString().trim();
        String password=layoutPassword.getEditText().getText().toString().trim();

        if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
           mregProgress.setTitle("Registering User");
            mregProgress.setMessage("Please wait while we create your Account");
            mregProgress.setCancelable(false);
            mregProgress.show();
            registerUserNow(displayName,email,password);
        }
        else{
            Toast.makeText(this, "Fill all the Required Fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUserNow(final String displayName, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    //getting the register user id
                    String uid = mAuth.getCurrentUser().getUid();
                    String device_token= FirebaseInstanceId.getInstance().getToken();
                    //Creating database Reference
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    //Creating HashMap object
                    HashMap<String, String> mUserMap = new HashMap<String, String>();
                    mUserMap.put("name", displayName);
                    mUserMap.put("device_token", device_token);
                    mUserMap.put("status","Hi there I'm using Lapit Chat");
                    mUserMap.put("image", "default");
                    mUserMap.put("thumb_image", "thumbImage");
                    mDatabase.setValue(mUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mregProgress.dismiss();
                                Intent intentMainActivity=new Intent(registerUserActivity.this,MainActivity.class);
                                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intentMainActivity);
                                finish();
                            }else{
                                Toast.makeText(registerUserActivity.this, "Data Not Store to Database", Toast.LENGTH_SHORT).show();
                                mregProgress.dismiss();
                            }
                        }
                    });
                }
                else{
                    mregProgress.dismiss();
                    Toast.makeText(registerUserActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
