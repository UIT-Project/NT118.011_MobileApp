package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
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

        GridView gridView=view.findViewById(R.id.gv_profileFrag_listPics);
        ImageView userPic=view.findViewById(R.id.iv_profileFrag_profilePic);
        TextView tv_username=view.findViewById(R.id.tv_profileFrag_username);

        //Shared Preferences
        SharedPreferences preferences=view.getContext().getSharedPreferences(view.getContext()
                .getString(R.string.share_pref_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        //Tải ảnh đại diện
        mDB.child(b64Email).child(view.getContext().getString(R.string.profile_pic)).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                userPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(String.valueOf(
                        task.getResult().getValue())));
            }
        });

        //Lấy tên tài khoản và email
        mDB.child(GeneralFunc.str2Base64(user.getEmail())).child("username").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String username=String.valueOf(task.getResult().getValue());

                tv_username.setText(username);
                ((TextView)view.findViewById(R.id.tv_profileFrag_email)).setText(user.getEmail());
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
                    ArrayList<objectPic> list=new ArrayList<objectPic>();

                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                        list.add(new objectPic(String.valueOf(childSnapShot.child("data").getValue()),
                                String.valueOf(childSnapShot.child("name").getValue())));
                    }

                    PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,
                            list);
                    gridView.setAdapter(
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

        //Cập nhật lại danh sách ảnh
        mDB.child(b64Email).child("pics").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()){
                                    ArrayList<objectPic> list=new ArrayList<objectPic>();

                                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                        list.add(new objectPic(String.valueOf(childSnapShot.child("data").getValue()),
                                                String.valueOf(childSnapShot.child("name").getValue())));
                                    }

                                    PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,
                                            list);
                                    gridView.setAdapter(
                                            picAdapter);
                                }
                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()){
                                    ArrayList<objectPic> list=new ArrayList<objectPic>();

                                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                        list.add(new objectPic(String.valueOf(childSnapShot.child("data").getValue()),
                                                String.valueOf(childSnapShot.child("name").getValue())));
                                    }

                                    PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,
                                            list);
                                    gridView.setAdapter(
                                            picAdapter);
                                }
                            }
                        });

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()){
                                    ArrayList<objectPic> list=new ArrayList<objectPic>();

                                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                        list.add(new objectPic(String.valueOf(childSnapShot.child("data").getValue()),
                                                String.valueOf(childSnapShot.child("name").getValue())));
                                    }

                                    PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,
                                            list);
                                    gridView.setAdapter(
                                            picAdapter);
                                }
                            }
                        });

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Sự kiện click ảnh
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View imgView, int position, long id) {
                ImageView selectedImg=imgView.findViewById(R.id.iv_itemPic);
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_img)).setImageBitmap(
                        ((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(
                        ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText(tv_username
                        .getText().toString());

                String picName=((objectPic)selectedImg.getTag()).getName();
                if(picName.length()!=0 || picName.equals("null"))
                    ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText(
                            "Tên ảnh: "+picName);

                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                        View.GONE);
                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_viewPic)).setVisibility(
                        View.VISIBLE);

            }
        });

        //Sự kiện click quay về
        ((FloatingActionButton)view.findViewById(R.id.fab_profileFrag_vP_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_img)).setImageBitmap(null);
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(null);
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText("");

                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText("");

                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                        View.VISIBLE);
                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_viewPic)).setVisibility(
                        View.GONE);
            }
        });

        return view;
    }

    //Xử lý sau khi chọn được ảnh
    ActivityResultLauncher<Intent> takeImg=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()== RESULT_OK && result.getData() != null){
            Uri uri=result.getData().getData();

            Intent intent = new Intent(this.getContext(),uploadImg.class);
            intent.setData(uri);
            intent.putExtra("b64Email",GeneralFunc.str2Base64(user.getEmail()));
            startActivity(intent);
        }
    });

}