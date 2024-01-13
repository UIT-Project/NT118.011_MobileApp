package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.locks.ReentrantLock;

public class login extends AppCompatActivity {
    //Firebase Authenciation
    FirebaseAuth firebaseAuth;

    //Khóa chống xung đột tiến trình do người dùng
    private ReentrantLock reentrantLock;

    @Override
    public void onStart() {
        super.onStart();

        //Kiểm tra liệu có người dùng nào đang trong phiên đăng nhập hay không
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        //Nếu có người dùng thì đăng xuất
        if(currentUser != null){
            firebaseAuth.signOut();
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

        //Firebase Realtime Database dùng để lưu thông tin tài khoản mới
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference mDB = database.getReference();

        //Firebase Authenciation
        firebaseAuth = FirebaseAuth.getInstance();

        //Khởi tạo khóa
        reentrantLock =new ReentrantLock();

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
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ((TextView)findViewById(R.id.tv_login_err_email)).setText(
                            "*Email không hợp lệ");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_login_err_email)).setText("");
                }
                if(pass.length()<8 || pass.contains(" ")){
                    ((TextView)findViewById(R.id.tv_login_err_pass)).setText(
                            "*Mật khẩu không hợp lệ");
                    return;
                }else {
                    ((TextView)findViewById(R.id.tv_login_err_pass)).setText("");
                }

                //Giao diện chờ
                progressBar.setVisibility(View.VISIBLE);
                reentrantLock.lock();

                //Đăng nhập dựa trên dịch vụ Firebase Auth
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                        login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if(!GeneralFunc.hasInternet(login.this)){
                                    Toast.makeText(login.this,"Không có" +
                                            " internet",Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (task.isSuccessful()) {
                                    //Kiểm tra email đã được xác thực
                                    if(!firebaseAuth.getCurrentUser().isEmailVerified()){
                                        ((TextView)findViewById(R.id.tv_login_err_email)).setText(
                                                "*Tài khoản chưa được kích hoạt, vui lòng thực " +
                                                        "hiện xác thực email trước khi đăng nhập");
                                        firebaseAuth.signOut();
                                        return;
                                    }

                                    //Chuyển đến main act
                                    Intent intent=new Intent(getApplicationContext(),
                                            MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    //Kiểm tra tài khoản có tồn tại/đã đăng ký
                                    mDB.child(GeneralFunc.str2Base64(email)).get()
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(
                                                                @NonNull Task<DataSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                if(task.getResult().getChildrenCount()>0){
                                                                    ((TextView)findViewById(
                                                                            R.id.tv_login_err_pass))
                                                                            .setText("*Mật khẩu không" +
                                                                                    " chính xác");
                                                                }
                                                                else
                                                                {
                                                                    ((TextView)findViewById(
                                                                            R.id.tv_login_err_email))
                                                                            .setText("*Tài khoản không" +
                                                                                    " tồn tại");
                                                                }
                                                            } else {
                                                                Toast.makeText(login.this,
                                                                        "Dăng nhập thất bại",
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });

                                }
                            }
                        });

                //Kết thúc giao diện chờ
                progressBar.setVisibility(View.GONE);
                reentrantLock.unlock();

            }
        });

        //Hiện/không hiện password
        seePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralFunc.showHidPass(seePass,et_pass);
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

        //Nút next trên keyboard
        et_email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_NEXT){
                    et_pass.requestFocus();
                }
                return false;
            }
        });

        //Nút go trên key board
        et_pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_GO){
                    GeneralFunc.hideKeyboard(login.this,et_pass);
                    b_login.performClick();
                }
                return false;
            }
        });
    }
}