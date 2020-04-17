package com.vinnu.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView usersRecyclerViewList;
    private DatabaseReference usersDatabaseReference;
    FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        toolbar = (Toolbar) findViewById(R.id.users_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        usersRecyclerViewList = (RecyclerView) findViewById(R.id.users_recycler_view);
        usersRecyclerViewList.setHasFixedSize(true);
        usersRecyclerViewList.setLayoutManager(new LinearLayoutManager(this));
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetch();
        firebaseRecyclerAdapter.startListening();
        usersDatabaseReference.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
        usersDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, new SnapshotParser<User>() {
                            @NonNull
                            @Override
                            public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new User(snapshot.child("name").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("status").getValue().toString());
                            }
                        })
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int position, @NonNull User user) {
                        usersViewHolder.setName(user.getName());
                        usersViewHolder.setStatus(user.getStatus());
                        usersViewHolder.setImage(user.getImage());
                        final String userId = getRef(position).getKey();
                        usersViewHolder.userView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("userId",userId);
                                //profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(profileIntent);
                            }
                        });
            }
        };
        usersRecyclerViewList.setAdapter(firebaseRecyclerAdapter);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View userView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userView = itemView;
        }
        public void setName(String userName){
            TextView userNameTextView = (TextView)userView.findViewById(R.id.user_name);
            userNameTextView.setText(userName);
        }
        public void setStatus(String status){
            TextView statusTextView = (TextView)userView.findViewById(R.id.user_status);
            statusTextView.setText(status);
        }
        public void setImage(String imageUrl){
            CircleImageView circleImageView = (CircleImageView) userView.findViewById(R.id.user_image_view);
            Glide.with(getApplicationContext()).load(imageUrl).centerCrop().into(circleImageView);
        }
    }

}
