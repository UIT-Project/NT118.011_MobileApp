package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.TransactionTooLargeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFrag extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;

    public homeFrag() {
        // Required empty public constructor
    }
    //Truyền tham số
    public static homeFrag newInstance(FirebaseUser firebaseUser) {
        homeFrag fragment = new homeFrag();
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
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        GridView gridView=view.findViewById(R.id.gv_homeFrag_listPics);
        String b64Email=GeneralFunc.str2Base64(user.getEmail());
        view.findViewById(R.id.pb_homeFrag).setVisibility(View.VISIBLE);

        //Lấy danh sách ảnh ngẫu nhiên trừ ảnh của tài khoản
        mDB.get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<objectPic> list=new ArrayList<objectPic>();
                            int picCount=0;

                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                String ownerB64Email=childSnapShot.getKey();
                                if(ownerB64Email.equals(b64Email)) continue;
                                Random random = new Random();
                                for(DataSnapshot childSnapShot1 : childSnapShot.child("pics")
                                        .getChildren()){
                                    if(random.nextBoolean())continue;
                                    list.add(new objectPic(childSnapShot1.getKey(),
                                            String.valueOf(childSnapShot1.child("data").getValue()),
                                            String.valueOf(childSnapShot1.child("name").getValue()),
                                            String.valueOf(childSnapShot1.child("tags").getValue()),
                                            ownerB64Email));
                                    picCount++;
                                    if(picCount==50)break;
                                }
                                if (picCount==50)break;
                            }

                            PicAdapter picAdapter=new PicAdapter(view.getContext(),R.layout.item_pic,list);
                            gridView.setAdapter(picAdapter);

                            view.findViewById(R.id.pb_homeFrag).setVisibility(View.GONE);
                        }
                    }
                });

        //Sự kiện click ảnh
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View imgView, int position, long id) {
                mDB.child(b64Email).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        try{
                            Intent intent=new Intent(view.getContext(),viewPic.class);
                            intent.putExtra("user",new objectUser(user.getEmail(),false,
                                    task.getResult().child("profile_pic").getValue().toString(),
                                    task.getResult().child("username").getValue().toString()));
                            intent.putExtra("viewPic",(objectPic)imgView.findViewById(
                                    R.id.iv_itemPic).getTag());

                            startActivity(intent);
                        }catch (Exception e){ //Lỗi do kích thước ảnh lớn
                            Intent intent=new Intent(view.getContext(),viewPic.class);
                            intent.putExtra("user",new objectUser(user.getEmail(),false,
                                    task.getResult().child("profile_pic").getValue().toString(),
                                    task.getResult().child("username").getValue().toString()));
                            objectPic pic=(objectPic)imgView.findViewById(R.id.iv_itemPic).getTag();
                            intent.putExtra("viewPicMin",new objectPic(pic.getKey(),
                                    pic.getName(),pic.getStrHashtags(),pic.getB64EmailOwner()));

                            startActivity(intent);
                        }
                    }
                });
            }
        });

        return view;
    }
}