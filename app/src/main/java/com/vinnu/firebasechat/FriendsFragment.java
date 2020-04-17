package com.vinnu.firebasechat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView friendsListRecyclerView;
    private FirebaseAuth auth;
    private DatabaseReference usersDatabaseReference;
    FirebaseRecyclerAdapter<Friend,FriendViewHolder> firebaseRecyclerAdapter;
    private String currentUserId;
    private View mainView;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmentm
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsListRecyclerView = (RecyclerView) mainView.findViewById(R.id.friends_list);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseReference.keepSynced(true);
        friendsListRecyclerView.setHasFixedSize(true);
        friendsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetch();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends").child(currentUserId);
        query.keepSynced(true);
        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(query, new SnapshotParser<Friend>() {
                            @NonNull
                            @Override
                            public Friend parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Friend(snapshot.child("date").getValue().toString());
                            }
                        })
                        .build();
         firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder friendViewHolder, int position, @NonNull Friend friend) {
                friendViewHolder.setDate(friend.getDate());
                final String userId = getRef(position).getKey();
                usersDatabaseReference.keepSynced(true);
                usersDatabaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(isAdded()) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String thumbNail = dataSnapshot.child("thumb_image").getValue().toString();
                            if (dataSnapshot.hasChild("online")) {
                                String isOnline = dataSnapshot.child("online").getValue().toString();
                                friendViewHolder.setUserOnline(isOnline);
                            }
                            friendViewHolder.setName(userName);
                            friendViewHolder.setImage(thumbNail);
                            friendViewHolder.friendView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String[] options = new String[]{"Open User Profile", "Send a message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Please choose option");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int position) {
                                            if (position == 0) {
                                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("userId", userId);
                                                //profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(profileIntent);
                                            }
                                            if (position == 1) {
                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("userId", userId);
                                                chatIntent.putExtra("userName", userName);
                                                //chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new FriendsFragment.FriendViewHolder(view);
            }
        };
        friendsListRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private class FriendViewHolder extends RecyclerView.ViewHolder{
        View friendView;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendView = itemView;
        }
        public void setDate(String date){
            TextView userNameTextView = (TextView)friendView.findViewById(R.id.user_status);
            userNameTextView.setText(date);
        }
        public void setName(String userName){
            TextView userNameTextView = (TextView)friendView.findViewById(R.id.user_name);
            userNameTextView.setText(userName);
        }
        public void setImage(String imageUrl){
            CircleImageView circleImageView = (CircleImageView) friendView.findViewById(R.id.user_image_view);
            Glide.with(getActivity().getApplicationContext()).load(imageUrl).centerCrop().into(circleImageView);
        }
        public void setUserOnline(String isUserOnline){
            ImageView onlineImageView = (ImageView) friendView.findViewById(R.id.user_online);
            if(isUserOnline.equals("true")){
                onlineImageView.setImageResource(R.drawable.online);
            }else {
                onlineImageView.setImageResource(R.drawable.offline);
            }
        }
    }
}
