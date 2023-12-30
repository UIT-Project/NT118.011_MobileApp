package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
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

import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class register extends AppCompatActivity {
    //Firebase Auth
    private FirebaseAuth mAuth;

    //Khóa chống xung đột tiến trình do người dùng
    private ReentrantLock reentrantLock;

    //Hàm xử lý ẩn hiện pass
    public void showHidPass(ImageView seePass, EditText et_pass){
        if(et_pass.getInputType()==131073){ //id của normal text
            et_pass.setInputType(129); //id của password text
            seePass.setImageResource(R.drawable.eye_close);
        }else {
            et_pass.setInputType(131073);
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

        //Khởi tạo khóa
        reentrantLock =new ReentrantLock();

        //Tên Shared Preferences file
        String PREF_NAME = getString(R.string.share_pref_name);

        //Kết nới giao diện
        EditText et_email = findViewById(R.id.et_register_email),
                et_pass=findViewById(R.id.et_register_password),
                et_repass=findViewById(R.id.et_register_repassword),
                et_name = findViewById(R.id.et_register_username);
        Button b_register=findViewById(R.id.b_register),
                b_register_success_login=findViewById(R.id.b_register_success_login);
        ProgressBar progressBar = findViewById(R.id.pb_register);
        TextView tv_login=findViewById(R.id.tv_register_4);
        ImageView seePass=findViewById(R.id.iv_register_password),
                seeRepass=findViewById(R.id.iv_register_repassword);

        //Firebase Realtime Database dùng để lưu thông tin tài khoản mới
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference mDB = database.getReference();

        //Shared Preferences
        SharedPreferences preferences=this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Giao diện chờ
        progressBar.setVisibility(View.VISIBLE);
        reentrantLock.lock();

        //Kiểm tra ảnh mặc định
        String defUserImg;
        if(preferences.getString("default_user_img","").length() == 0){
            defUserImg=GeneralFunc.defaultImg2Base64(this);
            if(defUserImg.length()>0){
                editor.putString("default_user_img",defUserImg);
                editor.apply();
            }
        }else {
            defUserImg=preferences.getString("default_user_img","");
        }

        //Kết thúc giao diện chờ
        progressBar.setVisibility(View.GONE);
        reentrantLock.unlock();

        //Nút đăng ký
        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=et_email.getText().toString().trim(),
                        pass=et_pass.getText().toString().trim(),
                        repass=et_repass.getText().toString().trim(),
                        username=et_name.getText().toString().trim();

                //Kiểm tra đầu vào có hợp lệ
                if(username.isEmpty() || username.length() < 4){
                    ((TextView)findViewById(R.id.tv_register_err_username)).setText(
                            "*Tên tài khoản không hợp lệ, tên tài khoản phải có ít nhất 4 kí tự");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_register_err_username)).setText("");
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ((TextView)findViewById(R.id.tv_register_err_email)).setText(
                            "*Email không hợp lệ");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_register_err_email)).setText("");
                }
                if(pass.length()<8 || pass.contains(" ")){
                    ((TextView)findViewById(R.id.tv_register_err_pass)).setText(
                            "*Mật khẩu không hợp lệ, mật khẩu phải có ít nhất 8 kí tự " +
                                    "và không chứa khoảng trắng \" \"");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_register_err_pass)).setText("");
                }
                if(!pass.equals(repass)){
                    ((TextView)findViewById(R.id.tv_register_err_repass)).setText(
                    "*Mật khẩu nhập lại không trùng khớp");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_register_err_repass)).setText("");
                }

                //Giao diện chờ
                progressBar.setVisibility(View.VISIBLE);
                reentrantLock.lock();

                //Tạo tài khoản bằng dịch vụ Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                        register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Tạo dữ liệu cho tài khoản trên realtime db
                                    String b64Email=GeneralFunc.str2Base64(email);
                                    mDB.child(b64Email)
                                            .child("username").setValue(username);
                                    mDB.child(b64Email)
                                            .child(getString(R.string.profile_pic)).setValue(defUserImg);

                                    //Kết thúc giao diện chờ
                                    progressBar.setVisibility(View.GONE);
                                    reentrantLock.unlock();

                                    //Cập nhật giao diện thành công
                                    ((ConstraintLayout)findViewById(R.id.cl_act_register))
                                            .setVisibility(View.GONE);
                                    ((ConstraintLayout)findViewById(R.id.cl_act_register_success))
                                            .setVisibility(View.VISIBLE);

                                    //Giao diện chờ
                                    progressBar.setVisibility(View.VISIBLE);
                                    reentrantLock.lock();

                                    mAuth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(register.this,
                                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful()){
                                                Toast.makeText(register.this,
                                                        "Gửi email xác thực thất bại",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                    //Kết thúc giao diện chờ
                                    progressBar.setVisibility(View.GONE);
                                    reentrantLock.unlock();

                                } else {
                                    Toast.makeText(register.this,
                                            "Tạo tài khoản thất bại", Toast.LENGTH_SHORT)
                                            .show();
                                }

                            }
                        }
                );

                //Đăng xuất
                mAuth.signOut();
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
        b_register_success_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        //Gửi lại
        ((TextView)findViewById(R.id.tv_register_success_resend)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Giao diện chờ
                progressBar.setVisibility(View.VISIBLE);
                reentrantLock.lock();

                mAuth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(register.this,
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()){
                                            Toast.makeText(register.this,
                                                    "Gửi email xác thực thất bại",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                //Kết thúc giao diện chờ
                progressBar.setVisibility(View.GONE);
                reentrantLock.unlock();

                //Chạy thread cd gửi lại
                CountDownThread countDownThread=new CountDownThread();
                countDownThread.start();
            }
        });
    }

    class CountDownThread extends Thread{
        @Override
        public void run() {
            super.run();
            startTimer();
        }

        private void startTimer(){
            ((TextView)findViewById(R.id.tv_register_success_resend)).setTextColor(
                    getColor(R.color.black));
            ((TextView)findViewById(R.id.tv_register_success_resend)).setClickable(false);
            CountDownTimer countDownTimer=new CountDownTimer(60000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    ((TextView)findViewById(R.id.tv_register_success_cd)).setText(
                            String.valueOf((int)(millisUntilFinished/1000)));
                }

                @Override
                public void onFinish() {
                    ((TextView)findViewById(R.id.tv_register_success_resend)).setTextColor(
                            getColor(R.color.orange_brown));
                    ((TextView)findViewById(R.id.tv_register_success_resend)).setClickable(true);
                    ((TextView)findViewById(R.id.tv_register_success_cd)).setText("");
                }
            }.start();
        }
    }
}