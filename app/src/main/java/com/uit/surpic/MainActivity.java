package com.uit.surpic;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.uit.surpic.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.allowThreadDiskReads();

        //Khởi tạo act
        super.onCreate(savedInstanceState);

        //Khởi tạo giao diện
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(!GeneralFunc.hasInternet(this)){
            binding.tvMainNoInternet.setVisibility(View.VISIBLE);
            return;
        }
        //Kết nối FB Realtime DB
        firebaseDatabase=FirebaseDatabase.getInstance(
                "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDB=firebaseDatabase.getReference();

        //FB Auth
        firebaseAuth=FirebaseAuth.getInstance();

        //Nhận người dùng hiện tại đăng nhập
        user=firebaseAuth.getCurrentUser();

        //Tên Shared Preferences file
        String PREF_NAME = getString(R.string.share_pref_name);

        //Shared Preferences
        SharedPreferences preferences=this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //editor.clear();
        //editor.apply();

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

            //Kết nối các fragment với menu item
            Intent intent=getIntent();
            if(intent!=null){
                String frag=intent.getStringExtra("main_frag");
                if(frag!=null){
                    if(frag.equals("home")){
                        binding.bottomNavView.findViewById(R.id.bn_i_home).setSelected(true);
                        replaceFrag(homeFrag.newInstance(user));
                    }
                    if(frag.equals("search")){
                        binding.bottomNavView.findViewById(R.id.bn_i_search).setSelected(true);
                        replaceFrag(searchFrag.newInstance(user));
                    }
                    if(frag.equals("care")){
                        binding.bottomNavView.setSelectedItemId(R.id.bn_i_care);
                        replaceFrag(careFrag.newInstance(user));
                    }
                    if(frag.equals("profile")){
                        binding.bottomNavView.setSelectedItemId(R.id.bn_i_profile);
                        replaceFrag(profileFrag.newInstance(user));
                    }
                }else {
                    replaceFrag(homeFrag.newInstance(user));
                }
            }
            binding.bottomNavView.setOnItemSelectedListener(item -> {
                if(item.getItemId()==R.id.bn_i_home){
                    replaceFrag(homeFrag.newInstance(user));
                }
                if(item.getItemId()==R.id.bn_i_search){
                    replaceFrag(searchFrag.newInstance(user));
                }
                if(item.getItemId()==R.id.bn_i_care){
                    replaceFrag(careFrag.newInstance(user));
                }
                if(item.getItemId()==R.id.bn_i_profile){
                    replaceFrag(profileFrag.newInstance(user));
                }

                return true;
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