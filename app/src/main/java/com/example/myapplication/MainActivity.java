package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

//MainActivity
//-login
//--forgotPass
//--register
//-fragments
//--profile
//---upload
//--home
//--search

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;
    ActivityMainBinding binding;

    String string="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Khởi tạo act
        super.onCreate(savedInstanceState);

        //Yêu cầu cấp quyền
        GeneralFunc.askPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        GeneralFunc.askPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        GeneralFunc.askPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        //Kết nối giao diện
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFrag(new homeFrag());

        //Kết nối các fragment với menu item
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.bn_i_home){
                replaceFrag(new homeFrag());
            }
            if(item.getItemId()==R.id.bn_i_search){
                replaceFrag(new searchFrag());
            }
            if(item.getItemId()==R.id.bn_i_profile){
                binding.pbMain.setVisibility(View.VISIBLE);
                replaceFrag(profileFrag.newInstance(user));
            }

            return true;
        });

        //Tên Shared Preferences file
        String PREF_NAME = getString(R.string.share_pref_name);

        //Shared Preferences
        SharedPreferences preferences=this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //editor.clear();
        //editor.apply();

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
            Intent intent=new Intent(this, login.class);
            startActivity(intent);
            finish();
        } else { //Đang đăng nhập
            //Kiểm tra thời gian đăng nhập (hạn đăng nhập là 1 ngày)
            user.getIdToken(false).addOnCompleteListener(
                    new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if(!task.isSuccessful()){
                        firebaseAuth.signOut();
                        Intent intent=new Intent(MainActivity.this, login.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    if((long)(Calendar.getInstance().getTimeInMillis()/1000) -
                            task.getResult().getAuthTimestamp() > (60*60*24)){
                        firebaseAuth.signOut();
                        Intent intent=new Intent(MainActivity.this, login.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }

    }
    private void replaceFrag(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main,fragment);
        fragmentTransaction.commit();
    }
}