package com.vinnu.firebasechat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText changeStatusEditText;
    private Button saveStatusButton;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseCurrentUser;
    private ProgressDialog progressDialog;
    private static final String TAG = "StatusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        String currentStatus = getIntent().getStringExtra("status");
        firebaseCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseCurrentUser.getUid());
        toolbar = (Toolbar) findViewById(R.id.status_app_bar);
        changeStatusEditText = (EditText) findViewById(R.id.change_status_edit_text);
        changeStatusEditText.setHint(currentStatus);
        saveStatusButton = (Button) findViewById(R.id.save_status_change);
        progressDialog = new ProgressDialog(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("saving Changes");
                progressDialog.setMessage("Please wait while we save changes");
                progressDialog.show();
              String updatedStatus = changeStatusEditText.getText().toString();
              databaseReference.child("status").setValue(updatedStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){
                          progressDialog.dismiss();
                      }else{
                          Log.d(TAG, "onComplete: "+task.toString());
                      }
                  }
              });
            }
        });
    }
}
