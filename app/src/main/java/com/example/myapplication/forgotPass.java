package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.locks.ReentrantLock;

public class forgotPass extends AppCompatActivity {
    //Firebase Authenciation
    FirebaseAuth firebaseAuth;

    //Khóa chống xung đột tiến trình do người dùng
    private ReentrantLock reentrantLock;

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

        //Khởi tạo khóa
        reentrantLock =new ReentrantLock();

        //Nút gửi mail reset pass
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=et_email.getText().toString();

                //Kiểm tra email hợp lệ
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ((TextView)findViewById(R.id.tv_forgotPass_err_email)).setText(
                            "*Email không hợp lệ");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_forgotPass_err_email)).setText("");
                }

                //Giao diện chờ
                progressBar.setVisibility(View.VISIBLE);
                reentrantLock.lock();

                //Kiểm tra tài khoản có tồn tại/đã đăng ký
                mDB.child(GeneralFunc.str2Base64(email)).get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getChildrenCount()>0){
                                //Gửi mail đặt lại mật khẩu bằng Firebase Auth
                                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ((ConstraintLayout)findViewById(R.id.cl_act_forgotPass))
                                                        .setVisibility(View.GONE);
                                                ((ConstraintLayout)findViewById(
                                                        R.id.cl_act_forgotPass_after_first))
                                                        .setVisibility(View.VISIBLE);

                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(forgotPass.this,
                                                            "Gửi mail thất bại!",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                //Chạy cd gửi lại
                                GeneralFunc.startTimer(forgotPass.this,(TextView)findViewById(
                                        R.id.tv_forgotPass_af_resend),(TextView)findViewById(
                                        R.id.tv_forgotPass_af_resend_cd),60);
                            }
                            else
                            {
                                ((TextView)findViewById(R.id.tv_forgotPass_err_email)).setText(
                                        "*Tài khoản không tồn tại");
                            }
                        } else {
                            Toast.makeText(forgotPass.this,"Kiểm tra sự tồn tại của" +
                                    " tài khoản thất bại",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                //Kết thúc giao diện chờ
                progressBar.setVisibility(View.GONE);
                reentrantLock.unlock();
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
        ((Button)findViewById(R.id.b_forgotPass_af_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        //Gửi lại
        ((TextView)findViewById(R.id.tv_forgotPass_af_resend)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Giao diện chờ
                        progressBar.setVisibility(View.VISIBLE);
                        reentrantLock.lock();

                        //Gửi mail reset pass
                        firebaseAuth.sendPasswordResetEmail(et_email.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(forgotPass.this,
                                                    "Gửi mail thất bại!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        //Kết thúc giao diện chờ
                        progressBar.setVisibility(View.GONE);
                        reentrantLock.unlock();

                        //Chạy cd gửi lại
                        GeneralFunc.startTimer(forgotPass.this,(TextView)findViewById(
                                R.id.tv_forgotPass_af_resend),(TextView)findViewById(
                                        R.id.tv_forgotPass_af_resend_cd),60);
                    }
                });

        //Nút go trên keyboard
        et_email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO){
                    GeneralFunc.hideKeyboard(forgotPass.this,et_email);
                    resetPass.performClick();
                }
                return false;
            }
        });
    }
}