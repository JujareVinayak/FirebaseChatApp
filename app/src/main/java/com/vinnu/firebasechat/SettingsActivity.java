package com.vinnu.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 5;
    private DatabaseReference userDatabaseReference;
    private FirebaseUser firebaseCurrentUser;
    private final String TAG = "SettingsActivity";
    private TextView displayName, statusTextView;
    private Button changeImageButton, changeStatusButton;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        displayName = (TextView) findViewById(R.id.display_name);
        statusTextView = (TextView) findViewById(R.id.status);
        changeImageButton = (Button) findViewById(R.id.change_image);
        changeStatusButton = (Button) findViewById(R.id.change_status);
        circleImageView = (CircleImageView) findViewById(R.id.circleImageView);
        progressDialog = new ProgressDialog(this);
        firebaseCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseCurrentUser.getUid());
        userDatabaseReference.keepSynced(true);
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.toString());
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                displayName.setText(name);
                statusTextView.setText(status);
                //Picasso.with(SettingsActivity.this).load(image).into(circleImageView);
                if(!isDestroyed())
                Glide.with(SettingsActivity.this).load(image).centerCrop().into(circleImageView)    ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status",statusTextView.getText().toString());
                //statusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(statusIntent);
            }
        });
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT AN IMAGE"),GALLERY_PICK);
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while we upload your profile pic");
                progressDialog.setCancelable(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                final File thumbFilePath = new File(resultUri.getPath());
                try {
                    Bitmap thumbBitmap = new Compressor(this).
                            setMaxHeight(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumbFilePath);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                    final byte[] thumpByte = byteArrayOutputStream.toByteArray();
                    StorageReference filePath= storageReference.child("profile_pics").child(firebaseCurrentUser.getUid()+".jpg");
                    final StorageReference thumbFilePathReference = storageReference.child("profile_pics").child("thumbs").child(firebaseCurrentUser.getUid()+".jpg");
                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    userDatabaseReference.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                UploadTask uploadTask = thumbFilePathReference.putBytes(thumpByte);
                                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                userDatabaseReference.child("thumb_image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Log.d(TAG, "onComplete: Thumbnail upload successfull");
                                                                            Toast.makeText(SettingsActivity.this, "Thumbnail upload successfull", Toast.LENGTH_SHORT).show();
                                                                        }else{
                                                                            Log.d(TAG, "onComplete: Thumbnail upload unsuccessfull");
                                                                            Toast.makeText(SettingsActivity.this, "Thumbnail upload unsuccessfull", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                                Log.d(TAG, "onComplete: Image upload successfull");
                                                Toast.makeText(SettingsActivity.this, "Image upload successfull", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Log.d(TAG, "onComplete: Image upload unsuccessfull");
                                                Toast.makeText(SettingsActivity.this, "Image upload unsuccessfull", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: "+error);
            }
        }
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        userDatabaseReference.child("online").setValue(true);
    }
}
