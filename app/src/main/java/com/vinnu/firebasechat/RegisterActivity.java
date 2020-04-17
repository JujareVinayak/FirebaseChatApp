package com.vinnu.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText eUsername, eEmail, ePassword;
    private Button register;
    private FirebaseAuth firebaseAuth;
    private final String  TAG = "RegisterActivity";
    private Toolbar regToolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        regToolbar = (Toolbar) findViewById(R.id.register_tool_bar);
        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle(R.string.create_an_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        eUsername = (EditText) findViewById(R.id.username);
        eEmail = (EditText) findViewById(R.id.email);
        ePassword = (EditText) findViewById(R.id.password);
        register = (Button) findViewById(R.id.create_acc);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = eUsername.getText().toString();
                String email = eEmail.getText().toString();
                String password = ePassword.getText().toString();
                if( (username != null && !username.isEmpty()) && (email != null && !email.isEmpty()) && (password != null && !password.isEmpty())){
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we finish registration.");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    registerUser(username,email,password);
                }
            }
        });
    }

    private void registerUser(final String username, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            HashMap user = new HashMap();
                            user.put("name",username);
                            user.put("image","");
                            user.put("status","Status: "+username);
                            user.put("thumb_image","");
                            user.put("deviceToken",deviceToken);
                            databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.cancel();
                                    Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                    //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                            Log.d(TAG, "createUserWithEmail:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.hide();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
