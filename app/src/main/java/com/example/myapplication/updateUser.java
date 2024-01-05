package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityUpdateImgBinding;
import com.example.myapplication.databinding.ActivityUpdateUserBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class updateUser extends AppCompatActivity {
    private ActivityUpdateUserBinding binding;
    private objectUser user;
    private String b64Email, dataUserPic, username;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUpdateUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        if(intent!=null){
            //Nhận data
            user=intent.getParcelableExtra("user");

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Cập nhật ảnh lên giao diện
            dataUserPic=user.getDataUserPic();
            binding.ivUpdateUserPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(dataUserPic));

            //Cập nhật thông tin ra giao diện
            username=user.getUsername();
            binding.etUpdateUserUsername.setText(username);

            //Nút back
            binding.bUpdateUserBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(updateUser.this,MainActivity.class);
                    intent.putExtra("main_frag","profile");
                    startActivity(intent);

                    finish();
                }
            });

            //Nút thay ảnh
            binding.bUpdateUserChangePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
                            .EXTERNAL_CONTENT_URI);
                    takeImg.launch(intent);
                }
            });

            //Nút lưu
            binding.bUpdateUserUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username=binding.etUpdateUserUsername.getText().toString();
                    boolean change=false;

                    if(!dataUserPic.equals(user.getDataUserPic())){
                        mDB.child(user.getB64Email()).child("profile_pic").setValue(dataUserPic);
                        change=true;
                    }
                    if(!username.equals(user.getUsername())){
                        mDB.child(user.getB64Email()).child("username").setValue(username);
                        change=true;
                    }
                    if (!change){
                        Toast.makeText(updateUser.this,"Không có thông tin nào" +
                                " được thay đổi",Toast.LENGTH_LONG).show();
                    }else {
                        binding.bUpdateUserBack.performClick();
                    }
                }
            });

            //Nút go ở keyboard
            binding.etUpdateUserUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_GO){
                        binding.bUpdateUserUpdate.performClick();
                    }
                    return false;
                }
            });
        }
    }

    //Xử lý sau khi chọn được ảnh
    ActivityResultLauncher<Intent> takeImg=registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(), result->{
        if(result.getResultCode()== RESULT_OK && result.getData() != null){
            Uri uri=result.getData().getData();

            binding.ivUpdateUserPic.setImageURI(uri);
            dataUserPic=GeneralFunc.zipImg2Base64(((BitmapDrawable)binding.ivUpdateUserPic
                    .getDrawable()).getBitmap(),0);
            binding.ivUpdateUserPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(dataUserPic));
        }
    });


}