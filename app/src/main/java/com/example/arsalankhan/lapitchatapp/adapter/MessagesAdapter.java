package com.example.arsalankhan.lapitchatapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.arsalankhan.lapitchatapp.R;
import com.example.arsalankhan.lapitchatapp.helper.Messages;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arsalan khan on 8/10/2017.
 */

public class MessagesAdapter  extends RecyclerView.Adapter<MessagesAdapter.MyMessageViewHolder>{

    private ArrayList<Messages> arrayListMessages= new ArrayList<>();
    private Context mcontext;
    FirebaseAuth mAuth;

    public MessagesAdapter(Context context,ArrayList<Messages> arrayListMessages){
        this.arrayListMessages = arrayListMessages;
        mcontext = context;
    }
    @Override
    public MyMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_row, parent,false);

        return new MyMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyMessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String mCurrentUser = mAuth.getCurrentUser().getUid();

        Messages messages = arrayListMessages.get(position);

        if(mCurrentUser.equals(messages.getFrom())){
            holder.textViewMessage.setBackgroundResource(R.drawable.custom_message_bg_primary);
            holder.textViewMessage.setTextColor(Color.WHITE);
        }else{
            holder.textViewMessage.setBackgroundResource(R.drawable.custom_message_bg_white);
            holder.textViewMessage.setTextColor(Color.BLACK);
        }
        holder.textViewMessage.setText(messages.getMessage());

    }

    @Override
    public int getItemCount() {
        return arrayListMessages.size();
    }

    class MyMessageViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;
        CircleImageView UserProfile;
        public MyMessageViewHolder(View itemView) {
            super(itemView);

            textViewMessage = itemView.findViewById(R.id.messageTextView);
            UserProfile = itemView.findViewById(R.id.messageUserProfile);
        }


    }
}
