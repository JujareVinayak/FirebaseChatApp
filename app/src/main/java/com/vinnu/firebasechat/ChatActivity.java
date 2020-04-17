package com.vinnu.firebasechat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity  {

    private static final String TAG = "ChatActivity";
    private static final int NO_OF_MESSAGES_TO_LOAD = 9;
    private int currentPage = 1;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootDatabaseReference;
    private TextView title,lastSeen;
    private EditText inputMessage;
    private ImageButton addButton, sendButton;
    private RecyclerView chatRecyclerView;
    private CircleImageView profilePic;
    private String currentUserId;
    private List<Message> messageList;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout refreshLayout;
    private MessageAdapter messageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String chatUserName = getIntent().getStringExtra("userName");
        final String chatUserId = getIntent().getStringExtra("userId");
        toolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        title = (TextView) findViewById(R.id.custom_bar_title);
        lastSeen = (TextView) findViewById(R.id.custom_bar_seen);
        profilePic = (CircleImageView) findViewById(R.id.custom_bar_image);
        inputMessage = (EditText) findViewById(R.id.chat_message_view);
        sendButton = (ImageButton) findViewById(R.id.chat_send_btn);
        addButton = (ImageButton) findViewById(R.id.chat_add_btn);
        chatRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_messages);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList,this);
        layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(messageAdapter);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        title.setText(chatUserName);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar,null);
        actionBar.setCustomView(actionBarView);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        loadMessages();
        rootDatabaseReference.child("Users").child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String isOnline = dataSnapshot.child("online").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                if(isOnline.equals("true")){
                    lastSeen.setText("Online");
                }else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(isOnline);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    lastSeen.setText(lastSeenTime);
                }
                if(!isDestroyed()) {
                    Glide.with(ChatActivity.this).load(thumbImage).into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        rootDatabaseReference.child("Chats").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUserId)){
                    Map chatMap = new HashMap();
                    chatMap.put("seen",false);
                    chatMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chats/"+currentUserId+"/"+chatUserId,chatMap);
                    chatUserMap.put("Chats/"+chatUserId+"/"+currentUserId,chatMap);
                    rootDatabaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                messageList.clear();
                loadMessages();
            }
        });
    }

    private void loadMessages() {
        String chatUserId = getIntent().getStringExtra("userId");
        DatabaseReference messageReference = rootDatabaseReference.child("Messages").child(currentUserId).child(chatUserId);
        Query messageQuery = messageReference.limitToLast(currentPage * NO_OF_MESSAGES_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString();
        if(message != null && !message.equals("")){
            Map messageMap = new HashMap();
            String chatUserId = getIntent().getStringExtra("userId");
            String currentUserRef = "Messages/"+currentUserId+"/"+chatUserId;
            String chatUserRef =  "Messages/"+chatUserId+"/"+currentUserId;
            DatabaseReference userMessagePush = rootDatabaseReference.child("Messages").child(currentUserId)
                    .child(chatUserId).push();
            String pushId = userMessagePush.getKey();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef+"/"+pushId,messageMap);
            messageUserMap.put(chatUserRef+"/"+pushId,messageMap);
            inputMessage.setText("");
            rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rootDatabaseReference.child("Users").child(currentUserId).child("online").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Task", "onComplete: "+task.isSuccessful());
            }
        });
    }
}
