package com.example.arsalankhan.lapitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arsalankhan.lapitchatapp.adapter.MessagesAdapter;
import com.example.arsalankhan.lapitchatapp.helper.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar mchatToolbar;
    private String mChatUser;
    private String userName;
    private String thumb_image;
    private RecyclerView mchatRecyclerview;
    private SwipeRefreshLayout mRefreshLayout;
    private MessagesAdapter adapter;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private TextView tv_chatUserStatus;
    private EditText sendMessageView;
    private CircleImageView circleImageView;

    private ArrayList<Messages> arrayList_Messages = new ArrayList<>();

    private static final int ALL_ITEM_TO_LOAD=10;
    private int mCurrentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        sendMessageView = (EditText) findViewById(R.id.chat_editText);
        mchatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);

        setSupportActionBar(mchatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //for Custom Action bar
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View customBar = inflater.inflate(R.layout.custom_app_bar,null);

        actionBar.setCustomView(customBar);

        //getting intent Data
        gettingIntentData();

        // initializing user view
        intCustomBarViewAndSetData();


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId =  mAuth.getCurrentUser().getUid();

        //getting information about user online or offline and thumb image
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.with(ChatActivity.this).load(thumb_image).placeholder(R.drawable.avatar_default).into(circleImageView);

                String lastSeen = dataSnapshot.child("online").getValue().toString();

                if(lastSeen.equals("true")){
                    tv_chatUserStatus.setText("Online");
                }
                else{

                    //converting string into long
                    Long lastTime = Long.parseLong(lastSeen);

                    // creating an instance of GetTimeAgo class
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    tv_chatUserStatus.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //for creating chat object
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+mChatUser, chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!= null){
                                Toast.makeText(ChatActivity.this, "Error: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // Retrieving the chat messages into recyclerview
        LoadMessages();
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                //onRefresh remove the current messages from arraylist and load new messages
                arrayList_Messages.clear();

                // Load message
                LoadMessages();
            }
        });


    }
    // Load all messages from database into recyclerView
    private void LoadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        //Query to load message per page i.e. 10
        /*
           per page load 10 message and onRefresh mCurrentpage is increment by 1
           page 1 => load 10 messages (mCurrentPage = 1 then 1*10 =10)
           page 2 => load 20 messages (mCurrentPage = 2 then 2*10 =20) and so on
         */

        Query messageQuery = messageRef.limitToLast(mCurrentPage * ALL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                arrayList_Messages.add(messages);
                adapter.notifyDataSetChanged();

                mchatRecyclerview.scrollToPosition(arrayList_Messages.size()-1);

                //when data load completely set refreshing
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // send button
    public void chatSendButton(View view){

        sendMessage();
    }

    //sending a message
    private void sendMessage() {

        String message = sendMessageView.getText().toString().trim();

        if(!TextUtils.isEmpty(message)){

            sendMessageView.setText("");
            String current_user_ref="messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref= "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference chat_push_key = mRootRef.child("messages").child(mCurrentUserId).
                    child(mChatUser).push();

            String push_key = chat_push_key.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("type","text");
            messageMap.put("from",mCurrentUserId);
            messageMap.put("seen",false);
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+ "/"+push_key,messageMap);
            messageUserMap.put(chat_user_ref+ "/"+push_key,messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){
                        Log.d("TAG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    //add button
    public void chatAddButton(View view){


    }

    private void intCustomBarViewAndSetData() {
        TextView tv_chatUserName = (TextView) findViewById(R.id.tv_chat_personName);
        tv_chatUserStatus = (TextView) findViewById(R.id.tv_chat_status);
        circleImageView = (CircleImageView) findViewById(R.id.chat_UserThumbImage);
        mchatRecyclerview = (RecyclerView) findViewById(R.id.chat_RecyclerView);
        mchatRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mchatRecyclerview.setHasFixedSize(true);

        adapter = new MessagesAdapter(this,arrayList_Messages);
        mchatRecyclerview.setAdapter(adapter);

        //showing name on toolbar
        tv_chatUserName.setText(userName);


    }

    private void gettingIntentData() {
        Intent intent =getIntent();
        userName = intent.getStringExtra("Username");
        mChatUser = intent.getStringExtra("UserId");
    }
}
