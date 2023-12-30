package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    Button logout;
    TextView tv_username;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;


    String string="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Khởi tạo act
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Tên Shared Preferences file
        String PREF_NAME = getString(R.string.share_pref_name);

        //Shared Preferences
        SharedPreferences preferences=this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //editor.clear();
        //editor.apply();

        //Kết nối giao diện
        logout=findViewById(R.id.b_logout);
        tv_username=findViewById(R.id.tv_username);

        //Kết nối FB Realtime DB
        firebaseDatabase=FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDB=firebaseDatabase.getReference();

        //FB Auth
        firebaseAuth=FirebaseAuth.getInstance();

        //Nhận người dùng hiện tại đăng nhập
        user=firebaseAuth.getCurrentUser();

        //Không có người dùng thì đăng nhập
        if(user==null){
            Intent intent=new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        } else { //Đang đăng nhập
            String b64Email = GeneralFunc.str2Base64(user.getEmail());
            //Lấy username từ FB RDB
            mDB.child(b64Email).child("username").get().addOnCompleteListener(
                    new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,"Get data fail",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                tv_username.setText(String.valueOf(task.getResult().getValue()));
                            }
                        }
                    });

            //img
            mDB.child(b64Email).child(getString(R.string.profile_pic)).get().addOnCompleteListener(
                            new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        string="";
                    } else {
                        string=String.valueOf(task.getResult().getValue());
                        ImageView imageView = findViewById(R.id.img);
                        imageView.setImageBitmap(GeneralFunc.unzipBase64ToImg(string));
                    }
                }
            });

            //Đăng xuất
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseAuth.signOut();
                    Intent intent=new Intent(getApplicationContext(), login.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

    }
}