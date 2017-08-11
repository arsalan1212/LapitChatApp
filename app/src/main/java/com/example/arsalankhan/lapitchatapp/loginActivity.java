package com.example.arsalankhan.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class loginActivity extends AppCompatActivity {

    private Toolbar mLoginToolbar;
    private TextInputLayout layoutEmail,layoutPassword;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");


        layoutEmail= (TextInputLayout) findViewById(R.id.loginEmailLayout);
        layoutPassword= (TextInputLayout) findViewById(R.id.loginPasswordLayout);

        mLoginProgress= new ProgressDialog(this);

        mLoginToolbar= (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(mLoginToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //login button
    public void LoginToAccount(View view){

        String email= layoutEmail.getEditText().getText().toString().trim();
        String password=layoutPassword.getEditText().getText().toString().trim();

        if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

            mLoginToolbar.setTitle("Login to Account");
            mLoginProgress.setMessage("Please wait while Login to your Account");
            mLoginProgress.setCancelable(false);
            mLoginProgress.show();

            SignInAccount(email,password);

        }else{
            Toast.makeText(this, "Please Fill the Required Fields", Toast.LENGTH_SHORT).show();
        }
    }

    //sign in to your account
    private void SignInAccount(String email, String password) {


        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    //getting the device token
                    String device_token= FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mLoginProgress.dismiss();
                            Intent mainIntent=new Intent(loginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                }
                else{
                    mLoginProgress.dismiss();
                    Snackbar.make(findViewById(android.R.id.content),"User not Exist,Create An Account",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Register Now", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent registerIntent=new Intent(loginActivity.this,registerUserActivity.class);
                                    registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(registerIntent);
                                    finish();
                                }
                            }).show();
                }
            }
        });
    }
}
