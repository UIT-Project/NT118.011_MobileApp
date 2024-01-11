package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityUploadImgBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class uploadImg extends AppCompatActivity {

    private ActivityUploadImgBinding binding;
    private Uri uri;
    private String b64Email;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUploadImgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        if(intent!=null){
            //Nhận Uri
            uri=intent.getData();
            b64Email=intent.getStringExtra("b64Email");

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Cập nhật ảnh lên giao diện
            binding.ivUploadImg.setImageURI(uri);

            //Nút quay về
            binding.bUploadImgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //Nút tải lên
            binding.bUploadImgUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Kiểm tra hợp thức Tags
                    Pattern pattern=Pattern.compile("[a-z0-9_]+");
                    String[] strings=binding.etUploadImgTags.getText().toString().trim().split(" ");
                    for (String s:strings) {
                        if(!pattern.matcher(s).matches() && !s.isEmpty()){
                            Toast.makeText(uploadImg.this,"Tags không hợp lệ",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    //Upload dữ liệu
                    DatabaseReference databaseReference;
                    try{
                        databaseReference=mDB.child(b64Email).child("pics")
                                .push();
                        databaseReference.child("data").setValue(GeneralFunc.zipImg2Base64(
                                ((BitmapDrawable)binding.ivUploadImg.getDrawable()).getBitmap(),0));
                        databaseReference.child("full").setValue(GeneralFunc.zipImg2Base64(
                                ((BitmapDrawable)binding.ivUploadImg.getDrawable()).getBitmap(),100));
                    }
                    catch (Exception e){
                        Toast.makeText(uploadImg.this,"Tải ảnh lên thất bại",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    databaseReference.child("name").setValue(binding.etUploadImgName.getText()
                            .toString().trim());
                    databaseReference.child("tags").setValue(binding.etUploadImgTags.getText()
                            .toString().trim());

                    mDB.child("fast").child(b64Email).child("pics")
                            .child(databaseReference.getKey()).child("name")
                            .setValue(binding.etUploadImgName.getText().toString().trim());
                    mDB.child("fast").child(b64Email).child("pics")
                            .child(databaseReference.getKey()).child("tags")
                            .setValue(binding.etUploadImgTags.getText().toString().trim());

                    finish();
                }
            });
        }
    }

}