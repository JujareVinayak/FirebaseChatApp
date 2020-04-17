package com.vinnu.firebasechat;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class
FireBaseChat extends Application {
    private FirebaseAuth auth;
    private DatabaseReference usersDatabaseReference;
    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
            usersDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        usersDatabaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        //usersDatabaseReference.child("lastSeen").setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
