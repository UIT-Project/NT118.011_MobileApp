package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileFrag extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;

    public profileFrag() {
        // Required empty public constructor
    }

    //Truyền tham số
    public static profileFrag newInstance(FirebaseUser firebaseUser) {
        profileFrag fragment = new profileFrag();
        Bundle args = new Bundle();
        args.putParcelable("fb_user",firebaseUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Nhận tham số
        if (getArguments() != null) {
            //FB Auth
            firebaseAuth=FirebaseAuth.getInstance();

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Nhận user
            user=getArguments().getParcelable("fb_user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile,container,false);

        String b64Email=GeneralFunc.str2Base64(user.getEmail());

        //Tải ảnh đại diện
        mDB.child(b64Email).child(view.getContext().getString(R.string.profile_pic)).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ((ImageView)view.findViewById(R.id.iv_profileFrag_profilePic))
                        .setImageBitmap(GeneralFunc.unzipBase64ToImg(String.valueOf(
                                task.getResult().getValue())));
            }
        });

        //Lấy tên tài khoản
        mDB.child(GeneralFunc.str2Base64(user.getEmail())).child("username").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String username=String.valueOf(task.getResult().getValue());

                ((TextView)view.findViewById(R.id.tv_profileFrag_username)).setText(username);
            }
        });

        //Nút setting
        ImageButton imageButton = view.findViewById(R.id.ib_profile_setting);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo menu setting
                PopupMenu popupMenu=new PopupMenu(view.getContext(),imageButton);

                //Nhận item
                popupMenu.getMenuInflater().inflate(R.menu.setting_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //Lựa chọn logout
                        if(item.getItemId()==R.id.setting_i_logout){
                            firebaseAuth.signOut();
                            Intent intent=new Intent(getActivity().getApplicationContext(),
                                    login.class);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }
                        return true;
                    }
                });

                //Hiện menu setting
                popupMenu.show();
            }
        });

        //Lấy danh sách ảnh của tài khoản
        mDB.child(b64Email).child("pics").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<String> listB64Img=new ArrayList<String>();

                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                        listB64Img.add(String.valueOf(childSnapShot.child("data").getValue()));
                    }

                    PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,
                            listB64Img);
                    ((GridView)view.findViewById(R.id.gv_profileFrag_listPics)).setAdapter(
                            picAdapter);
                }
            }
        });

        //Chọn ảnh
        ((Button)view.findViewById(R.id.b_profileFrag_add)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI);
                takeImg.launch(intent);
            }
        });

        return view;
    }

    //Xử lý sau khi chọn được ảnh
    ActivityResultLauncher<Intent> takeImg=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()== RESULT_OK && result.getData() != null){
            Uri uri=result.getData().getData();

            ImageView imageView=new ImageView(getView().getContext());
            imageView.setVisibility(View.GONE);
            imageView.setImageURI(uri);

            mDB.child(GeneralFunc.str2Base64(user.getEmail())).child("pics")
                    .push().child("data").setValue(GeneralFunc.zipImg2Base64(
                            ((BitmapDrawable)imageView.getDrawable()).getBitmap()));
        }
    });

}