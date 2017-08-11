package com.example.arsalankhan.lapitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.arsalankhan.lapitchatapp.adapter.ViewPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar= (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        viewPager= (ViewPager) findViewById(R.id.main_Viewpager);
        mTabLayout= (TabLayout) findViewById(R.id.main_TabLayout);

        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(viewPager);

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            //if the user is not login then send it start Activity
            SendUserToStartActivity();
        }else{
            mUserDatabase.child(currentUser.getUid()).child("online").setValue("true");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() !=null)
        mUserDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    private void SendUserToStartActivity() {

        Intent startActivityIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startActivityIntent);
        finish();
    }


    //setting the Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout){
            if(mAuth.getCurrentUser()!=null){

                mUserDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
                mAuth.signOut();
                SendUserToStartActivity();
            }

        }
        else if(item.getItemId()==R.id.main_account_setting){

            Intent accountIntent=new Intent(this,AccountSettingActivity.class);
            startActivity(accountIntent);
        }
        else if(item.getItemId()==R.id.main_alluser){

            Intent userIntent=new Intent(MainActivity.this,AllUsersActivity.class);
            startActivity(userIntent);
        }
        return true;
    }
}
