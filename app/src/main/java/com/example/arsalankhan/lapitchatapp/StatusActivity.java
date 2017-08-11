package com.example.arsalankhan.lapitchatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout statusInputLayout;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar= (Toolbar) findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        statusInputLayout= (TextInputLayout) findViewById(R.id.status_inputLayout);

        //getting current user id and through that change status
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=currentUser.getUid();

        //getting display name from the intent
        String status=getIntent().getStringExtra("status_value");
        statusInputLayout.getEditText().setText(status);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

    }

    //save the changes
    public void SaveChanges(View view){

        mProgress=new ProgressDialog(StatusActivity.this);
        mProgress.setTitle("Save Changes");
        mProgress.setMessage("Please wait while we save your Changes");
        mProgress.show();

        String status = statusInputLayout.getEditText().getText().toString().trim();
        mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       mProgress.dismiss();
                   }
                   else{
                       Toast.makeText(StatusActivity.this, "There is an error in Saving changes", Toast.LENGTH_SHORT).show();
                   }
            }
        });
    }
}
