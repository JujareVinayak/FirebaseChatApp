package com.vinnu.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private TextView userName, userStatus;
    private Button sendFriendRequest,declineFriendRequest;
    private DatabaseReference userDatabaseReference, friendRequestDb, friendsDatabase;
    private String currentState;
    private FirebaseUser firebaseUser;
    private DatabaseReference rootDatabaseReference;
    private static final String TAG = ProfileActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        friendRequestDb = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userName = (TextView)findViewById(R.id.profile_display_name);
        userStatus = (TextView)findViewById(R.id.profile_user_status);
        profilePic = (ImageView) findViewById(R.id.profile_user_pic);
        sendFriendRequest = (Button) findViewById(R.id.profile_send_request_button);
        declineFriendRequest = (Button) findViewById(R.id.profile_decline_button);
        declineFriendRequest.setVisibility(View.INVISIBLE);
        currentState = "NotFriends";
        final String userId = getIntent().getStringExtra("userId");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("name").getValue().toString());
                Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(profilePic);
                userStatus.setText(dataSnapshot.child("status").getValue().toString());

                friendRequestDb.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String reqType = dataSnapshot.child(userId).child("requestType").getValue().toString();
                            if(reqType.equals("received")){
                                currentState = "RequestReceived";
                                sendFriendRequest.setText("Accept Friend Request");
                                declineFriendRequest.setVisibility(View.VISIBLE);
                            }
                            else if(reqType.equals("sent")){
                                currentState = "RequestSent";
                                sendFriendRequest.setText("Cancel Friend Request");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(true);
                            }
                        }else {
                            friendsDatabase.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        currentState = "Friends";
                                        sendFriendRequest.setText("UnFriend");
                                        declineFriendRequest.setVisibility(View.INVISIBLE);
                                        declineFriendRequest.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  sendFriendRequest.setEnabled(false);
                  if(currentState.equals("NotFriends")) {
                      DatabaseReference newNotiRef = rootDatabaseReference.child("notifications").child(userId).push();
                      String newNotiId = newNotiRef.getKey();
                      HashMap<String,String> notificationData = new HashMap<>();
                      notificationData.put("from",firebaseUser.getUid());
                      notificationData.put("type","request");
                      Map reqMap = new HashMap<>();
                      reqMap.put("FriendRequests/"+firebaseUser.getUid()+"/"+userId+ "/requestType","sent");
                      reqMap.put("FriendRequests/"+userId+"/"+firebaseUser.getUid()+"/requestType","received");
                      reqMap.put("notifications/"+userId+"/"+newNotiId,notificationData);
                      rootDatabaseReference.updateChildren(reqMap, new DatabaseReference.CompletionListener() {
                          @Override
                          public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                              currentState = "RequestSent";
                              sendFriendRequest.setText("Cancel friend request");
                              sendFriendRequest.setEnabled(true);
                              declineFriendRequest.setVisibility(View.INVISIBLE);
                              declineFriendRequest.setEnabled(false);
                              Log.d(TAG, "Friend Request sent.");
                          }
                      });
                  }else if(currentState.equals("RequestSent")){
                      friendRequestDb.child(firebaseUser.getUid()).child(userId).removeValue();
                      friendRequestDb.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){
                                  sendFriendRequest.setEnabled(true);
                                  currentState = "NotFriends";
                                  sendFriendRequest.setText("Send Friend Request");
                                  declineFriendRequest.setVisibility(View.INVISIBLE);
                                  declineFriendRequest.setEnabled(false);
                                  Log.d(TAG, "Friend request cancelled.");
                              }else {
                                  Log.d(TAG, "Friend request cancel failed.");
                              }

                          }
                      });

                }else if(currentState.equals("RequestReceived")){
                      final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                      Map friendMap = new HashMap();
                      friendMap.put("Friends/"+firebaseUser.getUid()+"/"+userId+"/date",currentDate);
                      friendMap.put("Friends/"+userId+"/"+firebaseUser.getUid()+"/date",currentDate);
                      friendMap.put("FriendRequests/"+firebaseUser.getUid()+"/"+userId,null);
                      friendMap.put("FriendRequests/"+userId+"/"+firebaseUser.getUid(),null);
                      rootDatabaseReference.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                          @Override
                          public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                              sendFriendRequest.setEnabled(true);
                              currentState = "Friends";
                              sendFriendRequest.setText("UnFriend");
                              declineFriendRequest.setVisibility(View.INVISIBLE);
                              declineFriendRequest.setEnabled(false);
                              Log.d(TAG, "Friend request cancelled.");
                          }
                      });
                  }else if(currentState.equals("Friends")){
                      friendsDatabase.child(firebaseUser.getUid()).child(userId).removeValue();
                      friendsDatabase.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()) {
                                  currentState = "NotFriends";
                                  sendFriendRequest.setText("Send Friend Request");
                                  sendFriendRequest.setEnabled(true );
                                  declineFriendRequest.setVisibility(View.INVISIBLE);
                              }
                          }
                      });
                  }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        userDatabaseReference.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
