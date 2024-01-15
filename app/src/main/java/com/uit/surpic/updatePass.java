package com.uit.surpic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.uit.surpic.databinding.ActivityUpdatePassBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class updatePass extends AppCompatActivity {
    private ActivityUpdatePassBinding binding;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUpdatePassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        if(intent!=null){
            //FB Auth
            firebaseAuth=FirebaseAuth.getInstance();

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Nút back
            binding.bUpdatePassBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(updatePass.this, MainActivity.class);
                    intent.putExtra("main_frag","profile");
                    startActivity(intent);

                    finish();
                }
            });

            //Nút đặt lại
            binding.bUpdatePassUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Kiểm tra đầu vào có hợp lệ
                    String oldPass=binding.etUpdatePassOldPass.getText().toString(),
                            newPass=binding.etUpdatePassNewPass.getText().toString(),
                            rePass=binding.etUpdatePassRePass.getText().toString();
                    if(oldPass.length()<8 || oldPass.contains(" ")){
                        binding.tvUpdatePassErrOldPass.setText(
                                "*Mật khẩu không chính xác");
                        return;
                    }else {
                        binding.tvUpdatePassErrOldPass.setText("");
                    }
                    if(newPass.length()<8 || newPass.contains(" ")){
                        binding.tvUpdatePassErrNewPass.setText(
                                "*Mật khẩu mới không hợp lệ, mật khẩu mới phải có ít nhất 8 kí tự " +
                                        "và không chứa khoảng trắng \" \"");
                        return;
                    }else {
                        binding.tvUpdatePassErrNewPass.setText("");
                    }
                    if(!newPass.equals(rePass)){
                        binding.tvUpdatePassErrRePass.setText(
                                "*Mật khẩu nhập lại không trùng khớp");
                        return;
                    }else {
                        binding.tvUpdatePassErrRePass.setText("");
                    }

                    AuthCredential authCredential= EmailAuthProvider.getCredential(firebaseAuth
                                    .getCurrentUser().getEmail(), oldPass);

                    //Kiểm tra mật khẩu
                    firebaseAuth.getCurrentUser().reauthenticate(authCredential)
                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Đặt lại mk
                                firebaseAuth.getCurrentUser().updatePassword(newPass).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            binding.bUpdatePassBack.performClick();
                                        }else {
                                            Toast.makeText(updatePass.this,"Đặt lại" +
                                                    " mật khẩu thất bại",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                binding.tvUpdatePassErrRePass.setText(
                                        "*Mật khẩu nhập lại không trùng khớp");
                            }
                        }
                    });
                }
            });

            //Chuỗi chuyển tiếp
            binding.etUpdatePassOldPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_NEXT){
                        binding.etUpdatePassNewPass.requestFocus();
                        return  true;
                    }
                    return false;
                }
            });
            binding.etUpdatePassNewPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_NEXT){
                        binding.etUpdatePassRePass.requestFocus();
                        return  true;
                    }
                    return false;
                }
            });
            binding.etUpdatePassRePass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_GO){
                        ((InputMethodManager)updatePass.this.getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(binding.etUpdatePassRePass.getWindowToken(), 0);
                        binding.bUpdatePassUpdate.performClick();
                        return  true;
                    }
                    return false;
                }
            });

            //Ẩn/hiện pass
            binding.ivUpdatePassNewPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneralFunc.showHidPass(binding.ivUpdatePassNewPass,binding.etUpdatePassNewPass);
                }
            });
            binding.ivUpdatePassOldPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneralFunc.showHidPass(binding.ivUpdatePassOldPass,binding.etUpdatePassOldPass);
                }
            });
            binding.ivUpdatePassRePass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneralFunc.showHidPass(binding.ivUpdatePassRePass,binding.etUpdatePassRePass);
                }
            });


        }
    }
}