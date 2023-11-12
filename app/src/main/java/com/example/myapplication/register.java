package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private FirebaseAuth mAuth; //Firebase Auth

    //Hàm xử lý ản hiện pass
    public void showHidPass(ImageView seePass, EditText et_pass){
        if(et_pass.getInputType()==InputType.TYPE_TEXT_VARIATION_NORMAL){
            et_pass.setInputType(129); //id của textPassword
            seePass.setImageResource(R.drawable.eye_close);
        }else {
            et_pass.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
            seePass.setImageResource(R.drawable.eye);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Kiểm tra người dùng đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Khởi tạo act
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Kết nới giao diện
        EditText et_email = findViewById(R.id.et_register_email),
                et_pass=findViewById(R.id.et_register_password),
                et_repass=findViewById(R.id.et_register_repassword),
                et_name = findViewById(R.id.et_register_username);
        Button b_register=findViewById(R.id.b_register);
        ProgressBar progressBar = findViewById(R.id.pb_register);
        TextView tv_login=findViewById(R.id.tv_register_4);
        ImageView seePass=findViewById(R.id.iv_register_password),
                seeRepass=findViewById(R.id.iv_register_repassword);

        //Firebase Realtime Database dùng để lưu username
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference mDB = database.getReference();

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Nút đăng ký
        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=et_email.getText().toString(),
                        pass=et_pass.getText().toString(),
                        repass=et_repass.getText().toString();

                //Kiểm tra đầu vào có hợp lệ
                if(!email.contains("@") || !email.contains(".")){
                    Toast.makeText(register.this,"Email không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if((!pass.equals(repass)) || pass.length()<6){
                    Toast.makeText(register.this,"Mật khẩu không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Tạo tài khoản bằng dịch vụ Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                        register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(register.this,
                                            "Tạo tài khoản thành công",Toast.LENGTH_SHORT)
                                            .show();
                                    mDB.child("users").child(
                                            mAuth.getCurrentUser().getUid()).setValue(
                                                    et_name.getText().toString());

                                } else {
                                    Toast.makeText(register.this,
                                            "Tạo tài khoản thất bại", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                );
            }
        });

        //Chuyển sang login
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        //Ẩn/hiện pass
        seePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHidPass(seePass,et_pass);
            }
        });
        seeRepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHidPass(seeRepass,et_repass);
            }
        });
    }
}