package com.example.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //Widgets
    EditText userEditText, passwordEditText, emailEditText;
    Button registerButton;

    //Firebase auth
    FirebaseAuth auth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initializing Widgets;
        userEditText = findViewById(R.id.usernameText);
        passwordEditText = findViewById(R.id.passwordText);
        emailEditText = findViewById(R.id.emailText);
        registerButton = findViewById(R.id.registerButton);

        auth = FirebaseAuth.getInstance();
        //Adding Event Listener to Register Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = userEditText.getText().toString();
                String passwordText = passwordEditText.getText().toString();
                String emailText = emailEditText.getText().toString();

                if(TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)){
                    Toast.makeText(RegisterActivity.this,"Please fill all fields!", Toast.LENGTH_LONG).show();
                }else{
                    register(usernameText, passwordText, emailText);
                }
            }
        });
    }

    private void register(final String username, String password, String email){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();

                    dbRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);

                    //HashMaps
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");

                    //Opening the Main Activity after successful registration
                    dbRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this,"Invalid Email or Password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}