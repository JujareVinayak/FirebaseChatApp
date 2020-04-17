package com.vinnu.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private ImageView logo, signIn;
    private AutoCompleteTextView username, password;
    private TextView forgotPassword, signInTv;
    private Button buttonSignIn;
    private ProgressDialog progressDialog;
    private Toolbar startToolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;
    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: "+android.os.Build.MODEL);
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        startToolbar = (Toolbar) findViewById(R.id.start_app_bar);
        setSupportActionBar(startToolbar);
        getSupportActionBar().setTitle(R.string.welcome);
        logo = (ImageView) findViewById(R.id.ivLogLogo);
        signIn = (ImageView) findViewById(R.id.ivSignIn);
        username = (AutoCompleteTextView) findViewById(R.id.atvEmailLog);
        password = (AutoCompleteTextView) findViewById(R.id.atvPasswordLog);
        forgotPassword = (TextView) findViewById(R.id.tvForgotPass);
        signInTv = (TextView) findViewById(R.id.tvSignIn);
        buttonSignIn = (Button) findViewById(R.id.btnSignIn);
        progressDialog = new ProgressDialog(this);
        signInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                //regIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(regIntent);
            }
        });
        //signInTv.setEnabled(false);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = username.getText().toString();
                String pwd = password.getText().toString();
                if((email != null && !email.isEmpty()) && (pwd != null && !pwd.isEmpty())){
                    progressDialog.setTitle("Login User");
                    progressDialog.setMessage("Please wait while we finish login.");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    loginUser(email,pwd);
                }else {
                    Toast.makeText(StartActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkPermissions();
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String currentUserId = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    userDatabaseReference.child(currentUserId).child("deviceToken").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent mainIntent = new Intent(StartActivity.this,MainActivity.class);
                            //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                }else {
                    progressDialog.hide();
                    Log.d(TAG, "Authentication is failed: ");
                    Toast.makeText(StartActivity.this, "Authentication is failed: ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }
}
