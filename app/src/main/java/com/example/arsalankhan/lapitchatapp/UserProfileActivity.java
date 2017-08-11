package com.example.arsalankhan.lapitchatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.N;

public class UserProfileActivity extends AppCompatActivity {

    private TextView user_DisplayName,profile_status,profile_FriendCount;
    private ImageView profileImage;
    private ProgressDialog mProgress;
    private Button friendRequestBtn,declineButton;

    private DatabaseReference mRootRef;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDb;

    private String mCurrent_state;
    private String user_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        user_DisplayName= (TextView) findViewById(R.id.userprofile_displayName);
        profile_FriendCount= (TextView) findViewById(R.id.profile_FriendCount);
        profile_status= (TextView) findViewById(R.id.profile_status);
        profileImage= (ImageView) findViewById(R.id.UserProfile_image);
        friendRequestBtn = (Button) findViewById(R.id.profile_SendRequest_btn);
        declineButton= (Button) findViewById(R.id.profile_DeclineRequest_btn);

        declineButton.setVisibility(View.GONE);
        declineButton.setEnabled(false);

        // initializing the progress dialog
         intProgress();

        // getting the user key to which we visited
        user_key = getIntent().getStringExtra("user_id");


        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDb= FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        //FOR OFFLINE
        mUserDatabase.keepSynced(true);
        mFriendDatabase.keepSynced(true);
        mFriendRequestDatabase.keepSynced(true);
        mRootRef.keepSynced(true);



        mCurrent_state="not_friend";


        //retrieving User data and display it
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                user_DisplayName.setText(name);
                profile_status.setText(status);


                Picasso.with(UserProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.avatar_default)
                        .into(profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(UserProfileActivity.this).load(image).placeholder(R.drawable.avatar_default)
                                        .into(profileImage);
                            }
                        });



                // --------- FRIEND LIST/ FRIEND REQUEST--------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_key)){

                            String requestType=dataSnapshot.child(user_key).child("request_type")
                                    .getValue().toString();

                            if(requestType.equals("received")){

                                friendRequestBtn.setText("ACCEPT FRIEND REQUEST");
                                mCurrent_state="request_received";


                                declineButton.setVisibility(View.VISIBLE);
                                declineButton.setEnabled(true);
                            }
                            else if(requestType.equals("sent")){
                                friendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                mCurrent_state="request_sent";

                                declineButton.setVisibility(View.INVISIBLE);
                                declineButton.setEnabled(false);
                            }

                            mProgress.dismiss();
                        }
                        else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_key)){
                                        friendRequestBtn.setText("UNFRIEND "+user_DisplayName.getText());
                                        mCurrent_state="friends";

                                        declineButton.setVisibility(View.INVISIBLE);
                                        declineButton.setEnabled(false);
                                    }

                                    mProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mProgress.dismiss();
                        Toast.makeText(UserProfileActivity.this, "There is an Error Occur: "+databaseError, Toast.LENGTH_SHORT).show();
                    }

                });

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                Toast.makeText(UserProfileActivity.this, "Error while Loading user data: "+databaseError, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser!=null){
            mUserDb.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }

    // initializing the progress dialog
    private void intProgress() {
        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Please wait while we load User Data");
        mProgress.setCancelable(false);
        mProgress.show();
    }


    //sending friend request Button
    @RequiresApi(api = N)  //Annotation for getting current date
    public void SendFriendRequest(View view){

        friendRequestBtn.setEnabled(false);

        //------------NOT FRIEND  SENDING FRIEND REQUEST------------------------------------------
        if(mCurrent_state.equals("not_friend")){

            //Sending Friend Request
            SendFriendRequest();
        }

        //................ CANCELLING THE FRIEND REQUEST..........................
        if(mCurrent_state.equals("request_sent")){

            // removing the friends request
            CancelFriendRequest();
        }

        //*************** ACCEPTING FRIENDS REQUEST *******************************
        if(mCurrent_state.equals("request_received")){

           // final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd, HH:mm:ss");
            String currentDate = mdformat.format(calendar.getTime());
            AcceptFriendRequest(currentDate);
        }


        //------------- UN-FRIEND A USER ------------------------------------

        if(mCurrent_state.equals("friends")){

            mFriendDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    mFriendDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendRequestBtn.setText("SEND FRIEND REQUEST");
                            mCurrent_state="not_friend";
                            friendRequestBtn.setEnabled(true);

                            declineButton.setVisibility(View.INVISIBLE);
                            declineButton.setEnabled(false);
                        }
                    });
                }
            });
        }
    }


    //Sending Friend Request
    private void SendFriendRequest() {

//        //getting Notification key by using push
//        DatabaseReference newNotification =  mRootRef.child("notifications").child(user_key).push();
//        String notificationKey =  newNotification.getKey();
//
//        //Sending Notification to which we are sending Request
//        HashMap<String,String> notificationData=new HashMap<String, String>();
//
//        notificationData.put("from",mCurrentUser.getUid());
//        notificationData.put("type","request");
//
//
//        Map requestMap =  new HashMap();
//
//        requestMap.put("Friend_request/"+mUserDatabase.getKey()+"/"+user_key + "/request_type","sent");
//        requestMap.put("Friend_request/"+user_key +"/"+mUserDatabase.getKey() + "/request_type","received");
//        requestMap.put("notifications/"+user_key +"/" +notificationKey,notificationData);
//
//        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                if(databaseError == null){
//
//                    mCurrent_state="request_sent";
//                    friendRequestBtn.setText("CANCEL FRIEND REQUEST");
//
//                    declineButton.setVisibility(View.INVISIBLE);
//                    declineButton.setEnabled(false);
//                }
//                else{
//
//                    String errorMessage = databaseError.getMessage();
//                    Toast.makeText(UserProfileActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
//                }
//
//                friendRequestBtn.setEnabled(true);
//            }
//        });


        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_key).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mFriendRequestDatabase.child(user_key).child(mCurrentUser.getUid()).child("request_type")
                            .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //Sending Notification to which we are sending Request
                            HashMap<String,String> notificationData=new HashMap<String, String>();

                            notificationData.put("from",mCurrentUser.getUid());
                            notificationData.put("type","request");

                            mNotificationDatabase.child(user_key).push().setValue(notificationData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mCurrent_state="request_sent";
                                            friendRequestBtn.setText("CANCEL FRIEND REQUEST");

                                            declineButton.setVisibility(View.INVISIBLE);
                                            declineButton.setEnabled(false);
                                        }
                                    });
                        }
                    });

                }
                else{
                    Toast.makeText(UserProfileActivity.this, "Failed Sending Friend Request", Toast.LENGTH_SHORT).show();
                }
                friendRequestBtn.setEnabled(true);
            }
        });

    }


    //Accepting Friend Request
    private void AcceptFriendRequest(final String currentDate) {
/*        Map friendMap =  new HashMap();
        friendMap.put("Friends/"+mCurrentUser.getUid() + "/" +user_key+"/date" ,currentDate);
        friendMap.put("Friends/"+user_key +"/" +mCurrentUser.getUid()+"/date",currentDate);

        //DELETING THE FRIEND REQUEST DATABASE

        friendMap.put("Friend_request/"+ mCurrentUser.getUid() + "/"+user_key,null);
        friendMap.put("Friend_request/"+ user_key+ "/" + mCurrentUser.getUid(),null);

        mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    mCurrent_state="friends";
                    friendRequestBtn.setText("UnFriend "+user_DisplayName.getText());

                    declineButton.setVisibility(View.INVISIBLE);
                    declineButton.setEnabled(false);
                }
                else{
                    String errorMessage = databaseError.getMessage();
                    Toast.makeText(UserProfileActivity.this,errorMessage, Toast.LENGTH_SHORT).show();
                }
                friendRequestBtn.setEnabled(true);
            }
        });
    }*/
        mFriendDatabase.child(mCurrentUser.getUid()).child(user_key).child("date").setValue(currentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendDatabase.child(user_key).child(mCurrentUser.getUid()).child("date").setValue(currentDate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            //if the both users become friend then delete friend request Database
                                            DeleteFriendRequestDatabase();
                                        }
                                        else{
                                            Toast.makeText(UserProfileActivity.this, "Friend Request Database Not deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }

    // when the user Accept the Friend Request then Delete Friend Request Database
    //This method is called from AcceptFriendRequest method
    private void DeleteFriendRequestDatabase() {

        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendRequestDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            friendRequestBtn.setEnabled(true);
                            mCurrent_state="friends";
                            friendRequestBtn.setText("UnFriend "+user_DisplayName.getText());

                            declineButton.setVisibility(View.INVISIBLE);
                            declineButton.setEnabled(false);
                        }
                        else{
                            Toast.makeText(UserProfileActivity.this, "Friend Request Database Not deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //Canceling the Friend Request
    private void CancelFriendRequest() {

        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               mFriendRequestDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {

                       if(task.isSuccessful()){

                           friendRequestBtn.setEnabled(true);
                           mCurrent_state="not_friend";
                           friendRequestBtn.setEnabled(true);
                           friendRequestBtn.setText("SEND FRIEND REQUEST");

                           declineButton.setVisibility(View.INVISIBLE);
                           declineButton.setEnabled(false);
                       }
                   }
               });
                //Enabling the button either the request is Successful or not
                friendRequestBtn.setEnabled(true);
            }
        });

    }


    //decline friend request button
    public void DeclineFriendRequest(View view){

        Map declineMap = new HashMap();

        declineMap.put("Friend_request/"+ mCurrentUser.getUid()+"/"+user_key,null);
        declineMap.put("Friend_request/" + user_key +"/"+ mCurrentUser.getUid(),null);

        mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){
                    mCurrent_state="not_friend";
                    Toast.makeText(UserProfileActivity.this, "Decline Friend Request", Toast.LENGTH_SHORT).show();
                }
                else {
                    String errorMessage = databaseError.getMessage();
                    Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
