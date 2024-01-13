package com.uit.surpic;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        String b64Email= GeneralFunc.str2Base64(user.getEmail());
        ScrollView scrollView=view.findViewById(R.id.sv_homeFrag);
        View.OnClickListener onClickImgListener=
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.setEnabled(false);
                v.setEnabled(false);

                mDB.child("fast").child(b64Email).get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                try{
                                    Intent intent=new Intent(view.getContext(),
                                            viewPic.class);

                                    intent.putExtra("user",
                                            new objectUser(b64Email,
                                                    true,
                                                    null,
                                                    task.getResult().child("username")
                                                            .getValue().toString()));

                                    intent.putExtra("viewPic",
                                            (objectPic)v.findViewById(R.id.iv_itemPic)
                                                    .getTag());

                                    startActivity(intent);
                                }catch (Exception e){ //Lỗi do kích thước ảnh lớn
                                    Intent intent=new Intent(view.getContext(),
                                            viewPic.class);

                                    intent.putExtra("user",
                                            new objectUser(b64Email,
                                                    true,
                                                    null,
                                                    task.getResult().child("username")
                                                            .getValue().toString()));

                                    objectPic pic=(objectPic)v.findViewById(R.id.iv_itemPic)
                                            .getTag();

                                    intent.putExtra("viewPicMin",
                                            new objectPic(pic.getKey(),
                                                    pic.getName(), pic.getStrHashtags(),
                                                    pic.getB64EmailOwner()));

                                    startActivity(intent);
                                }

                                v.setEnabled(true);
                                scrollView.setEnabled(true);
                            }
                        });
            }
        };


        //Lấy danh sách ảnh ngẫu nhiên
        mDB.child("fast").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                            ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                            int picCount=0;

                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                String ownerB64Email=childSnapShot.getKey();
                                if(ownerB64Email.equals(b64Email)) continue;

                                Random random = new Random();
                                for(DataSnapshot childSnapShot1 : childSnapShot.child("pics")
                                        .getChildren()){
                                    if(random.nextBoolean())continue;

                                    dummyList.add(true);

                                    mDB.child(ownerB64Email).child("pics")
                                            .child(childSnapShot1.getKey()).child("data")
                                            .get().addOnCompleteListener(
                                                    new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    DataSnapshot dataSnapshot=task.getResult();
                                                    objectPic pic=new objectPic(
                                                            childSnapShot1.getKey(),
                                                            String.valueOf(dataSnapshot.getValue()),

                                                            String.valueOf(childSnapShot1.child(
                                                                    "name").getValue()),

                                                            String.valueOf(childSnapShot1.child(
                                                                    "tags").getValue()),
                                                            ownerB64Email);

                                                    LinearLayout linearLayout= GeneralFunc.itemPic(
                                                            view.getContext(), pic);
                                                    linearLayoutArrayList.add(linearLayout);

                                                    if(linearLayoutArrayList.size()==dummyList.size())
                                                    {
                                                        ConstraintLayout constraintLayout =
                                                                view.findViewById(R.id.cl_homeFrag_listPics);
                                                        constraintLayout.removeAllViews();
                                                        GeneralFunc.items2Layout(constraintLayout,
                                                                linearLayoutArrayList,
                                                                onClickImgListener);

                                                        view.findViewById(R.id.pb_homeFrag)
                                                                .setVisibility(View.GONE);
                                                    }
                                                }
                                            });

                                    picCount++;
                                    if(picCount==10)break;
                                }
                                if (picCount==10)break;
                            }
                        }
                    }
                });

        //Load thêm ảnh cho danh sách
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY == ((ScrollView)v).getChildAt(0).getMeasuredHeight()-v.getMeasuredHeight()){
                    ConstraintLayout constraintLayout = view.findViewById(R.id.cl_homeFrag_listPics);
                    if(constraintLayout.getChildCount()%10!=0){
                        scrollView.setOnScrollChangeListener(null);
                        return;
                    }

                    view.findViewById(R.id.pb_homeFrag).setVisibility(View.VISIBLE);

                    mDB.child("fast").get().addOnCompleteListener(
                            new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                                        ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                                        ArrayList<String> picKeys=new ArrayList<String>();

                                        //Danh sách key của ảnh đã đc tải trc đó
                                        int c=constraintLayout.getChildCount();
                                        for (int i=0;i<c;i++) {
                                            picKeys.add(((objectPic)
                                                    ((ImageView)
                                                    ((LinearLayout)constraintLayout.getChildAt(i))
                                                            .getChildAt(0))
                                                            .getTag()).getKey());
                                        }

                                        int picCount=0;

                                        for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                            String ownerB64Email=childSnapShot.getKey();
                                            if(ownerB64Email.equals(b64Email)) continue;

                                            Random random = new Random();
                                            for(DataSnapshot childSnapShot1 : childSnapShot.child("pics")
                                                    .getChildren()){
                                                if(picKeys.contains(childSnapShot1.getKey()))continue;
                                                if(random.nextBoolean())continue;

                                                dummyList.add(true);

                                                mDB.child(ownerB64Email).child("pics")
                                                        .child(childSnapShot1.getKey()).child("data")
                                                        .get().addOnCompleteListener(
                                                                new OnCompleteListener<DataSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull
                                                                                           Task<DataSnapshot>
                                                                                                   task) {
                                                                        DataSnapshot dataSnapshot=
                                                                                task.getResult();
                                                                        objectPic pic=new objectPic(
                                                                                childSnapShot1.getKey(),
                                                                                String.valueOf(
                                                                                        dataSnapshot
                                                                                                .getValue()),

                                                                                String.valueOf(
                                                                                        childSnapShot1.child(
                                                                                        "name")
                                                                                        .getValue()),

                                                                                String.valueOf(
                                                                                        childSnapShot1.child(
                                                                                        "tags")
                                                                                                .getValue()),
                                                                                ownerB64Email);

                                                                        LinearLayout linearLayout=
                                                                                GeneralFunc.itemPic(
                                                                                view.getContext(), pic);
                                                                        linearLayoutArrayList.add(linearLayout);

                                                                        if(linearLayoutArrayList.size()
                                                                                == dummyList.size())
                                                                        {
                                                                            GeneralFunc.moreItems2Layout(
                                                                                    constraintLayout,
                                                                                    linearLayoutArrayList,
                                                                                    onClickImgListener);

                                                                            view.findViewById(R.id.pb_homeFrag)
                                                                                    .setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                });

                                                picCount++;
                                                if(picCount==10)break;
                                            }
                                            if (picCount==10)break;
                                        }
                                    }
                                }
                            });
                }
            }
        });

        return view;
    }
}