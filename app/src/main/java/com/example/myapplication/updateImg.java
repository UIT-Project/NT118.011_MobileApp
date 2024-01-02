package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityUpdateImgBinding;
import com.example.myapplication.databinding.ActivityUploadImgBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class updateImg extends AppCompatActivity {

    private ActivityUpdateImgBinding binding;
    private objectPic pic;
    private String b64Email;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUpdateImgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        if(intent!=null){
            //Nhận dữ liệu
            pic=intent.getParcelableExtra("picInfo");
            b64Email=intent.getStringExtra("b64Email");

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Cập nhật ảnh lên giao diện
            binding.ivUpdateImg.setImageBitmap(GeneralFunc.unzipBase64ToImg(pic.getData()));

            //Cập nhật các thông tin
            binding.etUpdateImgName.setText(pic.getName());
            binding.etUpdateImgTags.setText(pic.getStrTags());


            //Nút quay về
            binding.bUpdateImgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //Nút lưu
            binding.bUpdateImgUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Kiểm tra hợp thức Tags
                    Pattern pattern=Pattern.compile("[a-z0-9_]+");
                    String[] strings=binding.etUpdateImgTags.getText().toString().trim()
                            .split(" ");
                    for (String s:strings) {
                        if(!pattern.matcher(s).matches() && !s.isEmpty()){
                            Toast.makeText(updateImg.this,"Tags không hợp lệ",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    DatabaseReference databaseReference = mDB.child(b64Email).child("pics")
                            .child(pic.getKey());
                    databaseReference.child("name").setValue(
                            binding.etUpdateImgName.getText().toString().trim());
                    databaseReference.child("tags").setValue(
                            binding.etUpdateImgTags.getText().toString().trim());

                    finish();
                }
            });

            //Nút xóa
            binding.ibUpdateImgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(updateImg.this);
                    builder.setMessage("Bạn có chắc muốn xóa ảnh này?").setTitle("Xác nhận");
                    builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDB.child(b64Email).child("pics").child(pic.getKey())
                                    .removeValue();
                            finish();
                        }
                    });
                    builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
            });
        }
    }
}