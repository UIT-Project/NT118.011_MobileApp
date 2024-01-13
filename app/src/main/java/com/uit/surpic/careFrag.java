package com.uit.surpic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
        ScrollView scrollView=view.findViewById(R.id.sv_careFrag);
        ConstraintLayout constraintLayout= view.findViewById(R.id.cl_careFrag_list);
        ProgressBar progressBar=view.findViewById(R.id.pb_careFrag);

        String b64Email= GeneralFunc.str2Base64(user.getEmail());
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
                },
                onClickAccListener=
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDB.child("fast").child(b64Email).get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                Intent intent=new Intent(view.getContext(),
                                                        viewUser.class);

                                                intent.putExtra("user",
                                                        new objectUser(b64Email,
                                                                true,
                                                                null,
                                                                task.getResult().child("username")
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
                        };

        //Danh sách ảnh yêu thích
        OnCompleteListener imgListListener=new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                progressBar.setVisibility(View.VISIBLE);

                ArrayList<LinearLayout>linearLayoutArrayList=new ArrayList<LinearLayout>();
                ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                int picCount=0;

                for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                    for (DataSnapshot childSnapshot1: childSnapshot.getChildren()) {
                        String ownerB64Email=childSnapshot.getKey(),
                                picKey=childSnapshot1.getKey();

                        dummyList.add(true);

                        mDB.child("fast").child(ownerB64Email).child("pics")
                                .child(picKey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        DataSnapshot snapshot=task.getResult();

                                        mDB.child(ownerB64Email).child("pics")
                                                .child(picKey).child("data")
                                                .get().addOnCompleteListener(
                                                        new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                DataSnapshot dataSnapshot=task.getResult();
                                                                objectPic pic=new objectPic(
                                                                        picKey,
                                                                        String.valueOf(dataSnapshot
                                                                                .getValue()),
                                                                        String.valueOf(snapshot
                                                                                .child("name").getValue()),
                                                                        String.valueOf(snapshot
                                                                                .child("tags").getValue()),
                                                                        ownerB64Email);

                                                                LinearLayout linearLayout= GeneralFunc.itemPic(
                                                                        view.getContext(), pic);
                                                                linearLayoutArrayList.add(linearLayout);

                                                                if(linearLayoutArrayList.size()==dummyList.size()){
                                                                    constraintLayout.removeAllViews();
                                                                    GeneralFunc.items2Layout(
                                                                            constraintLayout,
                                                                            linearLayoutArrayList,
                                                                            onClickImgListener);

                                                                    progressBar.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                    }
                                });

                        picCount++;
                        if(picCount==10)break;
                    }
                    if (picCount==10)break;
                }
            }
        };
        mDB.child(b64Email).child("love").get().addOnCompleteListener(imgListListener);


        //Click "Hình ảnh"
        cvByPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isByPic(view.getContext(),cvByPic)){
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                //Danh sách ảnh yêu thích
                mDB.child(b64Email).child("love").get().addOnCompleteListener(imgListListener);

            }
        });

        //Click "Tài khoản"
        cvByAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isByPic(view.getContext(),cvByPic)){
                    return;
                }
                constraintLayout.removeAllViews();

                progressBar.setVisibility(View.VISIBLE);

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
                                if(task.isSuccessful()){
                                    ArrayList<LinearLayout>linearLayoutArrayList=new ArrayList<LinearLayout>();
                                    ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                                    int userCount=0;

                                    for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                                        String targetB64Email=childSnapshot.getKey();

                                        dummyList.add(true);

                                        mDB.child("fast").child(targetB64Email).get()
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot>
                                                                                           task)
                                                            {
                                                                if(task.isSuccessful()){
                                                                    DataSnapshot dataSnapshot=task.getResult();

                                                                    String username =
                                                                            String.valueOf(dataSnapshot.child(
                                                                                    "username").getValue());

                                                                    mDB.child(targetB64Email).child("profile_pic")
                                                                            .get().addOnCompleteListener(
                                                                                    new OnCompleteListener<DataSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DataSnapshot> task)
                                                                                        {
                                                                                            if(task.isSuccessful()){
                                                                                                DataSnapshot dataSnapshot1=task.getResult();
                                                                                                objectUser user1=new objectUser(
                                                                                                        targetB64Email, true,
                                                                                                        String.valueOf(
                                                                                                                dataSnapshot1
                                                                                                                        .getValue()),
                                                                                                        username);

                                                                                                LinearLayout linearLayout= GeneralFunc.itemUser(
                                                                                                        view.getContext(), user1);
                                                                                                linearLayoutArrayList.add(linearLayout);

                                                                                                if(linearLayoutArrayList.size()==dummyList.size()){
                                                                                                    constraintLayout.removeAllViews();
                                                                                                    GeneralFunc.itemsUser2Layout(
                                                                                                            constraintLayout,
                                                                                                            linearLayoutArrayList,
                                                                                                            onClickAccListener);
                                                                                                    progressBar.setVisibility(View.GONE);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    });
                                                                }

                                                            }
                                                        });

                                        userCount++;
                                        if(userCount>20) break;
                                    }

                                    if(dummyList.size()==0)progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        //Tải thêm item
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY == ((ScrollView)v).getChildAt(0).getMeasuredHeight()-v.getMeasuredHeight()){
                    if(constraintLayout.getChildCount()%10!=0){
                        scrollView.setOnScrollChangeListener(null);
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    if(isByPic(view.getContext(),cvByPic)){
                        //Lấy danh sách ảnh yêu thích
                        mDB.child(b64Email).child("love").get().addOnCompleteListener(
                                new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(task.isSuccessful()){
                                            ArrayList<LinearLayout> linearLayoutArrayList =
                                                    new ArrayList<LinearLayout>();
                                            ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                                            ArrayList<String> picKeys=new ArrayList<String>();

                                            //Danh sách key của ảnh đã đc tải trc đó
                                            int c=constraintLayout.getChildCount();
                                            for (int i=0;i<c;i++) {
                                                objectPic pic=(objectPic) ((ImageView)
                                                        ((LinearLayout)constraintLayout.getChildAt(i))
                                                                .getChildAt(0)).getTag();
                                                picKeys.add(pic.getKey());
                                            }

                                            int picCount=0;

                                            for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                                                for (DataSnapshot childSnapshot1: childSnapshot.getChildren()) {
                                                    if(picKeys.contains(childSnapshot1.getKey()))continue;

                                                    String ownerB64Email=childSnapshot.getKey(),
                                                            picKey=childSnapshot1.getKey();

                                                    dummyList.add(true);

                                                    mDB.child("fast").child(ownerB64Email)
                                                            .child("pics")
                                                            .child(picKey).get()
                                                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    DataSnapshot snapshot=task.getResult();

                                                                    mDB.child(ownerB64Email).child("pics")
                                                                            .child(picKey).child("data")
                                                                            .get().addOnCompleteListener(
                                                                                    new OnCompleteListener<DataSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                                            DataSnapshot dataSnapshot=task.getResult();
                                                                                            objectPic pic=new objectPic(
                                                                                                    picKey,
                                                                                                    String.valueOf(dataSnapshot
                                                                                                            .getValue()),
                                                                                                    String.valueOf(snapshot
                                                                                                            .child("name").getValue()),
                                                                                                    String.valueOf(snapshot
                                                                                                            .child("tags").getValue()),
                                                                                                    ownerB64Email);

                                                                                            LinearLayout linearLayout= GeneralFunc.itemPic(
                                                                                                    view.getContext(), pic);
                                                                                            linearLayoutArrayList.add(linearLayout);

                                                                                            if(linearLayoutArrayList.size()==dummyList.size()){
                                                                                                constraintLayout.removeAllViews();
                                                                                                GeneralFunc.moreItems2Layout(
                                                                                                        constraintLayout,
                                                                                                        linearLayoutArrayList,
                                                                                                        onClickImgListener);

                                                                                                progressBar.setVisibility(View.GONE);
                                                                                            }
                                                                                        }
                                                                                    });
                                                                }
                                                            });

                                                    picCount++;
                                                    if(picCount==10)break;
                                                }
                                                if (picCount==10)break;
                                            }
                                            if (dummyList.size()==0)
                                                progressBar.setVisibility(View.GONE);

                                        }

                                    }
                                });
                    }else {
                        //Lấy danh sách tk theo dõi
                        mDB.child(b64Email).child("follow").get().addOnCompleteListener(
                                new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(task.isSuccessful()){
                                            ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                                            ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                                            ArrayList<String> accs=new ArrayList<String>();

                                            //Danh sách key của ảnh đã đc tải trc đó
                                            int c=constraintLayout.getChildCount();
                                            for (int i=0;i<c;i++) {
                                                String tgB64Email=String.valueOf( ((ImageView)
                                                        ((LinearLayout)constraintLayout.getChildAt(i))
                                                                .getChildAt(0)).getTag());
                                                if(!accs.contains(tgB64Email)){
                                                    accs.add(tgB64Email);
                                                }
                                            }

                                            int userCount=0;

                                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                                if(accs.contains(childSnapShot.getKey()))continue;

                                                String targetB64Email=childSnapShot.getKey();

                                                dummyList.add(true);

                                                mDB.child("fast").child(targetB64Email).get()
                                                        .addOnCompleteListener(
                                                                new OnCompleteListener<DataSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                        DataSnapshot dataSnapshot=task.getResult();

                                                                        String username =
                                                                                String.valueOf(dataSnapshot.child(
                                                                                        "username").getValue());

                                                                        mDB.child(targetB64Email).child("profile_pic")
                                                                                .get().addOnCompleteListener(
                                                                                        new OnCompleteListener<DataSnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                                                DataSnapshot dataSnapshot1=task.getResult();
                                                                                                objectUser user1=new objectUser(
                                                                                                        targetB64Email, true,
                                                                                                        String.valueOf(
                                                                                                                dataSnapshot1
                                                                                                                        .getValue()),
                                                                                                        username);

                                                                                                LinearLayout linearLayout= GeneralFunc.itemUser(
                                                                                                        view.getContext(), user1);
                                                                                                linearLayoutArrayList.add(linearLayout);

                                                                                                if(linearLayoutArrayList.size()==dummyList.size()){
                                                                                                    constraintLayout.removeAllViews();
                                                                                                    GeneralFunc.moreItemsUser2Layout(
                                                                                                            constraintLayout,
                                                                                                            linearLayoutArrayList,
                                                                                                            onClickAccListener);
                                                                                                    progressBar.setVisibility(View.GONE);
                                                                                                }
                                                                                            }
                                                                                        });

                                                                    }
                                                                });
                                                userCount++;
                                                if(userCount>20) break;
                                            }
                                            if (dummyList.size()==0)
                                                progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }
        });

        return view;
    }

    boolean isByPic(Context context, CardView cv){
        return cv.getBackgroundTintList()==ContextCompat.getColorStateList(context, R.color.orange_brown);
    }

}