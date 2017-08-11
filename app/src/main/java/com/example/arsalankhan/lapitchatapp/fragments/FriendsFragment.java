package com.example.arsalankhan.lapitchatapp.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.arsalankhan.lapitchatapp.ChatActivity;
import com.example.arsalankhan.lapitchatapp.R;
import com.example.arsalankhan.lapitchatapp.UserProfileActivity;
import com.example.arsalankhan.lapitchatapp.helper.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private View mView;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    private RecyclerView mRecyclerView;
    private String name;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView=inflater.inflate(R.layout.fragment_friends, container, false);

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = mView.findViewById(R.id.FriendRecyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        mAuth= FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);
        mFriendDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Friends, MyViewHolder> adapter =new FirebaseRecyclerAdapter<Friends, MyViewHolder>(
                Friends.class,
                R.layout.single_friend_row,
                MyViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final MyViewHolder viewHolder, final Friends model, int position) {

                final String userId=getRef(position).getKey();

                viewHolder.setDate(model.getDate().toString());

                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String name =  dataSnapshot.child("name").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_status =dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnlineStatus(online_status);
                        }

                        viewHolder.setName(name);
                        viewHolder.setImage(thumb_image,getContext());

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                final AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

                                builder.setTitle("Choose Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if(i==0){
                                            Intent intent =new Intent(getContext(), UserProfileActivity.class);
                                            intent.putExtra("user_id",userId);
                                            startActivity(intent);
                                        }
                                        else if(i==1){

                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("UserId",userId);
                                            intent.putExtra("Username",name);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }

                                });
                                AlertDialog dialog =builder.create();
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("TAG","Error: "+databaseError);
                    }
                });


            }
        };

        mRecyclerView.setAdapter(adapter);
    }

    

    static class MyViewHolder extends RecyclerView.ViewHolder{

        View view;
        public MyViewHolder(View itemView) {
            super(itemView);

            view= itemView;
        }

        public void setDate(String date){

            TextView tv_date= view.findViewById(R.id.friend_status);
            tv_date.setText(date);
        }
        
        public void setName(String name){

            TextView tvName= view.findViewById(R.id.friend_display_name);
            tvName.setText(name);
        }
        
        public void setImage(String image,Context context){

            CircleImageView imageView =view.findViewById(R.id.friend_profile_image);
            Picasso.with(context).load(image).placeholder(R.drawable.avatar_default).into(imageView);
        }

        public void setOnlineStatus(String online_status) {

            ImageView onlineImage_Icon = view.findViewById(R.id.online_Status);

            if(online_status.equals("true")){
                onlineImage_Icon.setVisibility(View.VISIBLE);
            }
            else{
                onlineImage_Icon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
