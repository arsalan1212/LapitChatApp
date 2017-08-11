package com.example.arsalankhan.lapitchatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.arsalankhan.lapitchatapp.helper.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserRecyclerView;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Users, MyViewHolder> recyclerAdapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar= (Toolbar) findViewById(R.id.user_Appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserRecyclerView= (RecyclerView) findViewById(R.id.userRecyclerView);
        mUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUserRecyclerView.setHasFixedSize(true);

        //Firebase Database
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
    }

    //onstart METHOD

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!= null){
            mDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        }

        recyclerAdapter = new FirebaseRecyclerAdapter<Users, MyViewHolder>(
                Users.class,
                R.layout.user_single_row,
                MyViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getThumb_image(),AllUsersActivity.this);


                final String user_id=getRef(position).getKey();

                viewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent=new Intent(AllUsersActivity.this,UserProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUserRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cleaning up the adapter data
        recyclerAdapter.cleanup();
    }

    // Holder class for firbaseRecyclerAdapter
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;
        public MyViewHolder(View itemView) {
            super(itemView);
            myView=itemView;
        }

        public void setName(String name) {

            TextView displayName=myView.findViewById(R.id.user_display_name);
            displayName.setText(name);
        }


        public void setStatus(String status){

            TextView tv_status=myView.findViewById(R.id.user_status);
            tv_status.setText(status);
        }

        public void setImage(final String image, final Context context){

            final CircleImageView imageView=myView.findViewById(R.id.friend_profile_image);

            if(!image.equals("default")){

                Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_default)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                                Picasso.with(context).load(image).placeholder(R.drawable.avatar_default)
                                        .into(imageView);
                            }
                        });
            }

        }
    }
}
