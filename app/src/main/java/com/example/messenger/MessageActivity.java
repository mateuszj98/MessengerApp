package com.example.messenger;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.messenger.Adapter.MessageAdapter;
import com.example.messenger.Model.Chat;
import com.example.messenger.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    TextView textView;
    ImageView imageView;

    RecyclerView recyclerView;
    EditText messageEditText;
    ImageButton sendButton;


    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    Intent intent;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView chatRecyclerView;
    String userid;

    RecordView recordView;
    RecordButton recordButton;

    MediaRecorder mediaRecorder;
    private String audioPath;

//    // Requesting permission to RECORD_AUDIO
//    private boolean permissionToRecordAccepted = false;
//    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case REQUEST_RECORD_AUDIO_PERMISSION:
//                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//        }
//        if (!permissionToRecordAccepted ) finish();
//
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        imageView = findViewById(R.id.ProfileImageView);
        textView = findViewById(R.id.username);

        sendButton = findViewById(R.id.btnSend);
        messageEditText = findViewById(R.id.textSend);

        //recording voice messages
        recordView = findViewById(R.id.record_view);
        recordButton = findViewById(R.id.record_button);


        //chatRecyclerView
        chatRecyclerView = findViewById(R.id.recyclerView2);
        chatRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);
        storageReference = FirebaseStorage.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                textView.setText(user.getUsername());

                if(user.getImageURL().equals("default")){
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(imageView);
                }

                readMessages(firebaseUser.getUid(),userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
                if(!message.equals("")){
                    sendMessage(firebaseUser.getUid(),userid,message, "text");
                }else{
                    Toast.makeText(MessageActivity.this,"Please send a non empty message!", Toast.LENGTH_SHORT).show();
                }
                messageEditText.setText("");
            }
        });

        initView();
    }

    private void sendRecordingMessage(String sender, String receiver, String audioPath, String type){
        StorageReference filePath = storageReference.child(audioPath);
        Uri audioFile = Uri.fromFile(new File(audioPath));
        filePath.putFile(audioFile).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> audioUrl = taskSnapshot.getStorage().getDownloadUrl();

            audioUrl.addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String url = task.getResult().toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender",sender);
                    hashMap.put("receiver", receiver);
                    hashMap.put("message", url);
                    hashMap.put("type", type);

                    databaseReference.child("Chats").push().setValue(hashMap);

                    //Adding User to chat fragment: Latest Chats with contacts

                    final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(userid);

                    chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatReference.child("id").setValue(userid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        });
    }

    private void sendMessage(String sender, String receiver, String message, String type){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type", type);


        databaseReference.child("Chats").push().setValue(hashMap);

        //Adding User to chat fragment: Latest Chats with contacts

        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(userid);

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatReference.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readMessages(String myId, String userId, String imageUrl){
        mChat = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Chat chat = snapshot1.getValue(Chat.class);

                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageUrl);
                    chatRecyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    public void setSupportActionBar(Toolbar toolbar) {

    }

    private boolean isRecordingOk(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecording(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void initView(){
        recordButton.setRecordView(recordView);
        recordButton.setListenForRecord(false);


//        recordButton.setOnClickListener(v -> {
            if(isRecordingOk(MessageActivity.this)){
                recordButton.setListenForRecord(true);
            }else{
                requestRecording(MessageActivity.this);
            }
//        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                setUpRecording();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messageEditText.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);

                if(file.exists()){
                    file.delete();
                }
                recordView.setVisibility(View.GONE);
                messageEditText.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFinish(long recordTime) {
                Log.d("RecordView", "onFinish");

                mediaRecorder.reset();
                mediaRecorder.release();

                recordView.setVisibility(View.GONE);
                messageEditText.setVisibility(View.VISIBLE);


                sendRecordingMessage(firebaseUser.getUid(),userid,audioPath, "recording");

            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if(file.exists()){
                    file.delete();
                }

                recordView.setVisibility(View.GONE);
                messageEditText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "CHatApp/Media/Recording");
        if(file.exists()){
            file.mkdirs();
        }
        System.out.println();
        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";
        mediaRecorder.setOutputFile(audioPath);
    }

}