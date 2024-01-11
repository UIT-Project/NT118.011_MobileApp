package com.example.myapplication;

import static com.example.myapplication.GeneralFunc.base64ToStr;
import static com.example.myapplication.GeneralFunc.unzipBase64ToImg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link careFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class careFrag extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;

    public careFrag() {
        // Required empty public constructor
    }

    public static careFrag newInstance(FirebaseUser user) {
        careFrag fragment = new careFrag();
        Bundle args = new Bundle();
        args.putParcelable("user",user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //FB Auth
            firebaseAuth=FirebaseAuth.getInstance();

            //Kết nối FB Realtime DB
            firebaseDatabase=FirebaseDatabase.getInstance(
                    "https://surpic-324b6-default-rtdb.asia-southeast1.firebasedatabase.app");
            mDB=firebaseDatabase.getReference();

            //Nhận user
            user=getArguments().getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_care, container, false);

        CardView cvByPic=view.findViewById(R.id.cv_careFrag_byPic),
                cvByAcc=view.findViewById(R.id.cv_careFrag_byAcc);
        TextView tvByPic=view.findViewById(R.id.tv_careFrag_byPic),
                tvByAcc=view.findViewById(R.id.tv_careFrag_byAcc);

        String b64Email=GeneralFunc.str2Base64(user.getEmail());

        //Click "Hình ảnh"
        cvByPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isByPic(view.getContext(),cvByPic)){
                    return;
                }

                view.findViewById(R.id.pb_careFrag).setVisibility(View.VISIBLE);

                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                //Danh sách ảnh yêu thích
                mDB.child(b64Email).child("love").get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<LinearLayout>linearLayoutArrayList=new ArrayList<LinearLayout>();

                                for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                                    for (DataSnapshot childSnapshot1: childSnapshot.getChildren()) {
                                        String ownerB64Email=childSnapshot.getKey(),
                                                picKey=childSnapshot1.getKey();

                                        LinearLayout linearLayout=GeneralFunc.itemPic(
                                                view.getContext(),
                                                new objectPic(picKey,"","","",
                                                        ownerB64Email));

                                        mDB.child(ownerB64Email).child("pics")
                                                .child(picKey).get().addOnCompleteListener(
                                                        new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                DataSnapshot dataSnapshot=task.getResult();
                                                                objectPic pic=new objectPic(
                                                                        dataSnapshot.getKey(),
                                                                        String.valueOf(dataSnapshot.child("data").getValue()),
                                                                        String.valueOf(dataSnapshot.child("name").getValue()),
                                                                        String.valueOf(dataSnapshot.child("tags").getValue()),
                                                                        ownerB64Email);

                                                                CardView cardView=(CardView) linearLayout.getChildAt(0);

                                                                ImageView imageView = (ImageView) cardView.getChildAt(0);
                                                                imageView.setImageBitmap(unzipBase64ToImg(pic.getData()));
                                                                imageView.setTag(pic);
                                                            }
                                                        });

                                        linearLayoutArrayList.add(linearLayout);
                                    }
                                }

                                ConstraintLayout constraintLayout= view.findViewById(R.id.cl_careFrag_list);
                                constraintLayout.removeAllViews();
                                GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDB.child(b64Email).get().addOnCompleteListener(
                                                        new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                try{
                                                                    Intent intent=new Intent(view.getContext(),
                                                                            viewPic.class);

                                                                    intent.putExtra("user",
                                                                            new objectUser(user.getEmail(),
                                                                                    false,
                                                                                    task.getResult().child("profile_pic")
                                                                                            .getValue().toString(),
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
                                                                            new objectUser(user.getEmail(),
                                                                                    false,
                                                                                    task.getResult().child("profile_pic")
                                                                                            .getValue().toString(),
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
                                                            }
                                                        });
                                            }
                                        });

                                view.findViewById(R.id.pb_careFrag).setVisibility(View.GONE);
                            }
                        });

            }
        });

        //Click "Tài khoản"
        cvByAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isByPic(view.getContext(),cvByPic)){
                    return;
                }

                view.findViewById(R.id.pb_careFrag).setVisibility(View.VISIBLE);

                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                //Danh sách tk theo dõi
                mDB.child(b64Email).child("follow").get().addOnCompleteListener(
                        new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<LinearLayout>linearLayoutArrayList=new ArrayList<LinearLayout>();

                                for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                                    String targetB64Email=childSnapshot.getKey();

                                    LinearLayout linearLayout=GeneralFunc.itemUser(
                                            view.getContext(),
                                            new objectUser(
                                                    targetB64Email,
                                                    true,
                                                    "",""));

                                    mDB.child(targetB64Email).get().addOnCompleteListener(
                                            new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    DataSnapshot dataSnapshot=task.getResult();
                                                    objectUser user1=new objectUser(
                                                            targetB64Email, true,
                                                            String.valueOf(
                                                                    dataSnapshot
                                                                            .child("profile_pic")
                                                                            .getValue()),
                                                            String.valueOf(
                                                                    dataSnapshot
                                                                            .child("username")
                                                                            .getValue()));

                                                    CardView cardView=(CardView) linearLayout
                                                            .getChildAt(0);
                                                    ImageView imageView=(ImageView)cardView
                                                            .getChildAt(0);

                                                    LinearLayout linearLayout1 =
                                                            (LinearLayout) linearLayout
                                                                    .getChildAt(1);
                                                    TextView name =(TextView) linearLayout1
                                                            .getChildAt(0),
                                                            email =(TextView)linearLayout1
                                                                    .getChildAt(1);

                                                    imageView.setImageBitmap(
                                                            unzipBase64ToImg(user1.getDataUserPic()));
                                                    imageView.setTag(user1);
                                                    name.setText(user1.getUsername());
                                                    email.setText(base64ToStr(user1.getB64Email()));
                                                }
                                            });

                                    linearLayoutArrayList.add(linearLayout);
                                }

                                ConstraintLayout constraintLayout=
                                        view.findViewById(R.id.cl_careFrag_list);
                                constraintLayout.removeAllViews();
                                GeneralFunc.itemsUser2Layout(constraintLayout, linearLayoutArrayList,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDB.child(b64Email).get().addOnCompleteListener(
                                                        new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                Intent intent=new Intent(view.getContext(),
                                                                        viewUser.class);

                                                                intent.putExtra("user",
                                                                        new objectUser(
                                                                                user.getEmail(),
                                                                                false,
                                                                                task.getResult()
                                                                                        .child("profile_pic")
                                                                                        .getValue().toString(),
                                                                                task.getResult()
                                                                                        .child("username")
                                                                                        .getValue().toString()));

                                                                intent.putExtra("targetUserB64Email",
                                                                        GeneralFunc.str2Base64(
                                                                                ((TextView)v.findViewById(
                                                                                        R.id.tv_itemUser_email))
                                                                                        .getText().toString()));

                                                                startActivity(intent);
                                                            }
                                                        });
                                            }
                                        });

                                view.findViewById(R.id.pb_careFrag).setVisibility(View.GONE);
                            }
                        });
            }
        });

        //Danh sách ảnh yêu thích
        mDB.child(b64Email).child("love").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ArrayList<LinearLayout>linearLayoutArrayList=new ArrayList<LinearLayout>();

                for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                    for (DataSnapshot childSnapshot1: childSnapshot.getChildren()) {
                        String ownerB64Email=childSnapshot.getKey(),
                                picKey=childSnapshot1.getKey();

                        LinearLayout linearLayout=GeneralFunc.itemPic(
                                view.getContext(),
                                new objectPic(picKey,"","","",
                                        ownerB64Email));

                        mDB.child(ownerB64Email).child("pics")
                                .child(picKey).get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        DataSnapshot dataSnapshot=task.getResult();
                                        objectPic pic=new objectPic(
                                                dataSnapshot.getKey(),
                                                String.valueOf(dataSnapshot.child("data").getValue()),
                                                String.valueOf(dataSnapshot.child("name").getValue()),
                                                String.valueOf(dataSnapshot.child("tags").getValue()),
                                                ownerB64Email);

                                        CardView cardView=(CardView) linearLayout.getChildAt(0);

                                        ImageView imageView = (ImageView) cardView.getChildAt(0);
                                        imageView.setImageBitmap(unzipBase64ToImg(pic.getData()));
                                        imageView.setTag(pic);
                                    }
                                });

                        linearLayoutArrayList.add(linearLayout);
                    }
                }

                ConstraintLayout constraintLayout= view.findViewById(R.id.cl_careFrag_list);
                constraintLayout.removeAllViews();
                GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDB.child(b64Email).get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                try{
                                                    Intent intent=new Intent(view.getContext(),
                                                            viewPic.class);

                                                    intent.putExtra("user",
                                                            new objectUser(user.getEmail(),
                                                                    false,
                                                                    task.getResult().child("profile_pic")
                                                                            .getValue().toString(),
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
                                                            new objectUser(user.getEmail(),
                                                                    false,
                                                                    task.getResult().child("profile_pic")
                                                                            .getValue().toString(),
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
                                            }
                                        });
                            }
                        });

                view.findViewById(R.id.pb_careFrag).setVisibility(View.GONE);

            }
        });

        return view;
    }

    boolean isByPic(Context context, CardView cv){
        return cv.getBackgroundTintList()==ContextCompat.getColorStateList(context, R.color.orange_brown);
    }

}