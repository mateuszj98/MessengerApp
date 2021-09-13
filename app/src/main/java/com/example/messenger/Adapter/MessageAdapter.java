package com.example.messenger.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messenger.MessageActivity;
import com.example.messenger.Model.Chat;
import com.example.messenger.Model.Users;
import com.example.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context context;
    private List<Chat> mChat;
    private String imgURL;

    //Firebase
    FirebaseUser firebaseUser;


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Context context, List<Chat> mChat, String imgURL) {
        this.context = context;
        this.mChat = mChat;
        this.imgURL = imgURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);

        }
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.showMessage.setText(chat.getMessage());

        if(imgURL.equals("default")){
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(context).load(imgURL).into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView showMessage;
        public ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}
