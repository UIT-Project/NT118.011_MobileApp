package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class forgotPass extends AppCompatActivity {

    FirebaseAuth firebaseAuth; //Firebase Authenciation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Khởi tạo act
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        //kết nối giao diện
        EditText et_email=findViewById(R.id.et_forgotPass_email);
        Button resetPass=findViewById(R.id.b_resetPass);
        TextView gotPass=findViewById(R.id.tv_forgotPass_login);
        ProgressBar progressBar=findViewById(R.id.pb_forgotPass);

        //Firebase Authenciation
        firebaseAuth = FirebaseAuth.getInstance();

        //Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference mDB = database.getReference();

        //Nút đặt lại mk
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=et_email.getText().toString();

                //Kiểm tra email hợp lệ
                if(!email.contains("@") || !email.contains(".")){
                    Toast.makeText(forgotPass.this,"Email không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                resetPass.setVisibility(View.GONE);

                //Kiểm tra email đã có tồn tại/đã đăng ký

                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(
                        new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(task.isSuccessful() && task.getResult().getSignInMethods().size()>0){
                            //Gửi mail đặt lại mật khẩu bằng Firebase Auth
                            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            resetPass.setVisibility(View.VISIBLE);
                                            et_email.setText("");

                                            if (task.isSuccessful()) {
                                                Toast.makeText(forgotPass.this,
                                                        "Gửi mail thành công!",
                                                        Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(forgotPass.this,
                                                        "Gửi mail thất bại!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(forgotPass.this,
                                    "Email chưa được đăng ký hoặc không tồn tại!"
                                            +String.valueOf(task.isSuccessful())+String.valueOf(task.getResult().getSignInMethods().isEmpty()),
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            resetPass.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

        //Chuyển đến đăng nhập
        gotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

    }
}