package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText et_email = findViewById(R.id.et_register_email),
                et_pass=findViewById(R.id.et_register_password),
                et_repass=findViewById(R.id.et_register_repassword),
                et_name = findViewById(R.id.et_register_username);
        Button b_register=findViewById(R.id.b_register);
        ProgressBar progressBar = findViewById(R.id.pb_register);
        TextView tv_login=findViewById(R.id.tv_register_4);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference mDB = database.getReference();

        mAuth = FirebaseAuth.getInstance();
        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email=et_email.getText().toString(),
                        pass=et_pass.getText().toString(),
                        repass=et_repass.getText().toString();
                if(!email.contains("@") || !email.contains(".")){
                    Toast.makeText(register.this,"Email không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                }
                if((!pass.equals(repass)) || pass.length()<6){
                    Toast.makeText(register.this,"Mật khẩu không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                }
                
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                        register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(register.this,
                                            "Tạo tài khoản thành công",Toast.LENGTH_SHORT)
                                            .show();
                                    mDB.child("users").child(mAuth.getCurrentUser().getUid()).setValue(
                                                    et_name.getText().toString());
                                } else {
                                    Toast.makeText(register.this,
                                            "Tạo tài khoản thất bại", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        }
                );
            }
        });
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}