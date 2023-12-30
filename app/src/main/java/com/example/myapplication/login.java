package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

public class login extends AppCompatActivity {
    FirebaseAuth firebaseAuth; //Firebase Authenciation
    @Override
    public void onStart() {
        super.onStart();

        //Kiểm tra liệu có người dùng nào đang trong phiên đăng nhập hay không
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        //Nếu có người dùng thì chuyển về main act
        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Khởi động hoạt động
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Kết nối giao diện
        EditText et_email=findViewById(R.id.et_login_email),
                et_pass=findViewById(R.id.et_login_password);
        Button b_login=findViewById(R.id.b_login);
        TextView tv_register=findViewById(R.id.tv_login_register),
                tv_forgotPass=findViewById(R.id.tv_login_forgotPass);
        ProgressBar progressBar=findViewById(R.id.pb_login);
        ImageView seePass=findViewById(R.id.iv_login_password);

        //Firebase Authenciation
        firebaseAuth = FirebaseAuth.getInstance();

        //Nút chuyển sang đăng ký
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),register.class);
                startActivity(intent);
                finish();
            }
        });

        //Nút đăng nhập
        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=et_email.getText().toString(),
                        pass=et_pass.getText().toString();

                //Kiểm tra đầu vào hợp lệ
                if(!email.contains("@") || !email.contains(".")){
                    Toast.makeText(login.this,"Email không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pass.length()<6){
                    Toast.makeText(login.this,"Mật khẩu không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Đăng nhập dựa trên dịch vụ Firebase Auth
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(login.this,
                                new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    Toast.makeText(login.this,"Đăng nhập thành công",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getApplicationContext(),
                                            MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(login.this, "Đăng nhập thất bại",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //Hiện/không hiện password
        seePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_pass.getInputType()==131073){ //id của normal text
                    et_pass.setInputType(129); //id của textPassword
                    seePass.setImageResource(R.drawable.eye_close);
                }else {
                    et_pass.setInputType(131073);
                    seePass.setImageResource(R.drawable.eye);
                }
            }
        });

        //Quên mật khẩu
        tv_forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), forgotPass.class);
                startActivity(intent);
                finish();
            }
        });
    }
}