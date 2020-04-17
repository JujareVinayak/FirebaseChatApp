package com.vinnu.firebasechat;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MessageViewHolder holder, int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        Message message = messageList.get(position);
        String fromUserId = message.getFrom();
        if(fromUserId.equals(currentUserId)){
            databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String displayName = "You";
                    String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    holder.displayName.setText(displayName);
                    Glide.with(context.getApplicationContext()).load(thumbImage).into(holder.profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            holder.messageText.setBackgroundResource(R.drawable.bubble_yellow);
            holder.messageText.setTextColor(Color.BLACK);
        }else {
            databaseReference.child(fromUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String displayName = dataSnapshot.child("name").getValue().toString();
                    String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    holder.displayName.setText(displayName);
                    Glide.with(context.getApplicationContext()).load(thumbImage).into(holder.profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            holder.messageText.setBackgroundResource(R.drawable.bubble_green);
            holder.messageText.setTextColor(Color.BLACK);
        }
        holder.messageText.setText(message.getMessage());
        long time = message.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Date msgSentAt = new Date(time);
        holder.timeTextLayout.setText(dateFormat.format(msgSentAt));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView messageText;
        private CircleImageView profileImage;
        private TextView displayName;
        private TextView timeTextLayout;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            timeTextLayout = (TextView) view.findViewById(R.id.time_text_layout);

        }

    }
}
