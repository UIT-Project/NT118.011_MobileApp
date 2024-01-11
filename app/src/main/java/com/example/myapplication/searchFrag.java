package com.example.myapplication;

import static androidx.core.content.ContextCompat.getSystemService;

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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link searchFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class searchFrag extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDB;
    FirebaseUser user;
    public searchFrag() {
        // Required empty public constructor
    }

    //Truyền tham số
    public static searchFrag newInstance(FirebaseUser firebaseUser) {
        searchFrag fragment = new searchFrag();
        Bundle args = new Bundle();
        args.putParcelable("fb_user",firebaseUser);
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
            user=getArguments().getParcelable("fb_user");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search,container,false);

        TextView tvCancel=view.findViewById(R.id.tv_searchFrag_cancel),
                tvByPic=view.findViewById(R.id.tv_searchFrag_byPic),
                tvByAcc=view.findViewById(R.id.tv_searchFrag_byAcc),
                tvNoRes=view.findViewById(R.id.tv_searchFrag_noRes);
        EditText etSearchBox=view.findViewById(R.id.et_searchFrag_searchBox);
        ImageView ivIcon=view.findViewById(R.id.iv_searchFrag_icon);
        CardView cvByPic=view.findViewById(R.id.cv_searchFrag_byPic),
                cvByAcc=view.findViewById(R.id.cv_searchFrag_byAcc);
        ConstraintLayout constraintLayout=view.findViewById(R.id.cl_searchFrag_listPics);

        String b64Email=GeneralFunc.str2Base64(user.getEmail());

        tvCancel.setVisibility(View.GONE);

        //Cập nhật giao diện khi nhập text
        etSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0 && !s.toString().equals("null")){
                    ivIcon.setVisibility(View.GONE);
                    tvCancel.setVisibility(View.VISIBLE);
                }else {
                    ivIcon.setVisibility(View.VISIBLE);
                    tvCancel.setVisibility(View.GONE);
                }
            }
        });

        //Click hủy
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchBox.setText("");

                ((InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(etSearchBox.getWindowToken(), 0);

                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                //listView.setAdapter(null);
                constraintLayout.removeAllViews();
            }
        });

        //Click "Hình ảnh"
        cvByPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isByPic(view.getContext(),cvByPic)){
                    return;
                }

                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                etSearchBox.post(new Runnable() {
                    @Override
                    public void run() {
                        etSearchBox.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
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

                cvByAcc.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));
                cvByPic.setBackgroundTintList(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));

                tvByAcc.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.white));
                tvByPic.setTextColor(ContextCompat.getColorStateList(view.getContext(),
                        R.color.orange_brown));

                etSearchBox.post(new Runnable() {
                    @Override
                    public void run() {
                        etSearchBox.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
                    }
                });
            }
        });

        //Nút tìm kiếm
        etSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    tvNoRes.setVisibility(View.GONE);

                    if(v.getText().toString().equals("") || v.getText()==null)
                        return false;

                    if(constraintLayout.getChildCount()>0)
                        constraintLayout.removeAllViews();

                    view.findViewById(R.id.pb_searchFrag).setVisibility(View.VISIBLE);

                    String keyword=v.getText().toString();
                    String[] keywords=keyword.split(" ");

                    if(isByPic(view.getContext(),cvByPic)){
                        //Lấy danh sách ảnh cần tìm
                        mDB.child("fast").get().addOnCompleteListener(
                                new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(task.isSuccessful()){
                                            ArrayList<LinearLayout> linearLayoutArrayList =
                                                    new ArrayList<LinearLayout>();
                                            //int picCount=0;

                                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                                String ownerB64Email=childSnapShot.getKey();
                                                if(ownerB64Email.equals(b64Email)) continue;

                                                for(DataSnapshot childSnapShot1 : childSnapShot.child("pics")
                                                        .getChildren()){

                                                    String name=String.valueOf(childSnapShot1.child("name")
                                                            .getValue()), strTags=String.valueOf(
                                                            childSnapShot1.child("tags")
                                                                    .getValue());
                                                    String[] tags=strTags.split(" ");
                                                    boolean ok=false;

                                                    for (String k: keywords) {
                                                        if(name.contains(k)){
                                                            ok=true;
                                                            break;
                                                        }
                                                    }

                                                    if(ok){
                                                        LinearLayout linearLayout=GeneralFunc.itemPic(
                                                                view.getContext(),
                                                                new objectPic(
                                                                        childSnapShot1.getKey(),
                                                                        "","","",
                                                                        ownerB64Email));

                                                        mDB.child(ownerB64Email).child("pics")
                                                                .child(childSnapShot1.getKey()).get()
                                                                .addOnCompleteListener(
                                                                        new OnCompleteListener<DataSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(
                                                                                    @NonNull Task<DataSnapshot>
                                                                                            task)
                                                                            {
                                                                                DataSnapshot dataSnapshot=task.getResult();
                                                                                objectPic pic=new objectPic(
                                                                                        dataSnapshot.getKey(),
                                                                                        String.valueOf(
                                                                                                dataSnapshot
                                                                                                        .child(
                                                                                                                "data")
                                                                                                        .getValue()),
                                                                                        String.valueOf(
                                                                                                dataSnapshot
                                                                                                        .child("name")
                                                                                                        .getValue()),
                                                                                        String.valueOf(
                                                                                                dataSnapshot
                                                                                                        .child("tags")
                                                                                                        .getValue()),
                                                                                        ownerB64Email);

                                                                                CardView cardView=(CardView) linearLayout.getChildAt(0);

                                                                                ImageView imageView = (ImageView) cardView.getChildAt(0);
                                                                                imageView.setImageBitmap(unzipBase64ToImg(pic.getData()));
                                                                                imageView.setTag(pic);
                                                                            }
                                                                        });

                                                        linearLayoutArrayList.add(linearLayout);
                                                        //picCount++;
                                                        //if(picCount==10)break;

                                                    }else {
                                                        ok=true;
                                                        for (String k: keywords) {
                                                            boolean allTag=false;
                                                            for (String tag:tags) {
                                                                if(tag.equals(k)){
                                                                    allTag=true;
                                                                    break;
                                                                }
                                                            }
                                                            if(!allTag){
                                                                ok=false;
                                                                break;
                                                            }
                                                        }
                                                        if(ok){
                                                            LinearLayout linearLayout=GeneralFunc.itemPic(
                                                                    view.getContext(),
                                                                    new objectPic(
                                                                            childSnapShot1.getKey(),
                                                                            "","","",
                                                                            ownerB64Email));

                                                            mDB.child(ownerB64Email).child("pics")
                                                                    .child(childSnapShot1.getKey()).get()
                                                                    .addOnCompleteListener(
                                                                            new OnCompleteListener<DataSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(
                                                                                        @NonNull Task<DataSnapshot>
                                                                                                task)
                                                                                {
                                                                                    DataSnapshot dataSnapshot=task.getResult();
                                                                                    objectPic pic=new objectPic(
                                                                                            dataSnapshot.getKey(),
                                                                                            String.valueOf(
                                                                                                    dataSnapshot
                                                                                                            .child(
                                                                                                                    "data")
                                                                                                            .getValue()),
                                                                                            String.valueOf(
                                                                                                    dataSnapshot
                                                                                                            .child("name")
                                                                                                            .getValue()),
                                                                                            String.valueOf(
                                                                                                    dataSnapshot
                                                                                                            .child("tags")
                                                                                                            .getValue()),
                                                                                            ownerB64Email);

                                                                                    CardView cardView=(CardView) linearLayout.getChildAt(0);

                                                                                    ImageView imageView = (ImageView) cardView.getChildAt(0);
                                                                                    imageView.setImageBitmap(unzipBase64ToImg(pic.getData()));
                                                                                    imageView.setTag(pic);
                                                                                }
                                                                            });

                                                            linearLayoutArrayList.add(linearLayout);
                                                            //picCount++;
                                                            //if(picCount==10)break;
                                                        }
                                                    }
                                                }
                                                //if (picCount==10)break;
                                            }

                                            GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList, new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            mDB.child(b64Email).get().addOnCompleteListener(
                                                                    new OnCompleteListener<DataSnapshot>() {
                                                                        @Override
                                                                        public void
                                                                        onComplete(@NonNull
                                                                                   Task<DataSnapshot> task) {
                                                                            try{
                                                                                Intent intent =
                                                                                        new Intent(
                                                                                                view.getContext(),
                                                                                                viewPic.class);

                                                                                intent.putExtra(
                                                                                        "user",
                                                                                        new objectUser(
                                                                                                user.getEmail(),
                                                                                                false,
                                                                                                task.getResult().child("profile_pic")
                                                                                                        .getValue().toString(),
                                                                                                task.getResult().child("username")
                                                                                                        .getValue().toString()));

                                                                                intent.putExtra(
                                                                                        "viewPic",
                                                                                        (objectPic)v
                                                                                                .findViewById(R.id.iv_itemPic)
                                                                                                .getTag());

                                                                                startActivity(intent);
                                                                            }catch (Exception e){ //Lỗi do kích thước ảnh lớn
                                                                                Intent intent =
                                                                                        new Intent(
                                                                                                view.getContext(),
                                                                                                viewPic.class);

                                                                                intent.putExtra(
                                                                                        "user",
                                                                                        new objectUser(user.getEmail(),
                                                                                                false,
                                                                                                task.getResult()
                                                                                                        .child("profile_pic")
                                                                                                        .getValue().toString(),
                                                                                                task.getResult()
                                                                                                        .child("username")
                                                                                                        .getValue().toString()));

                                                                                objectPic pic =
                                                                                        (objectPic)v
                                                                                                .findViewById(R.id.iv_itemPic)
                                                                                                .getTag();

                                                                                intent.putExtra(
                                                                                        "viewPicMin",
                                                                                        new objectPic(
                                                                                                pic.getKey(),
                                                                                                pic.getName(),
                                                                                                pic.getStrHashtags(),
                                                                                                pic.getB64EmailOwner()));

                                                                                startActivity(intent);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });

                                            if(linearLayoutArrayList.size()==0)
                                                tvNoRes.setVisibility(View.VISIBLE);
                                            else
                                                tvNoRes.setVisibility(View.GONE);
                                        }

                                        view.findViewById(R.id.pb_searchFrag).setVisibility(View.GONE);
                                    }
                                });
                    }else {
                        //Lấy danh sách tk cần tìm
                        mDB.child("fast").get().addOnCompleteListener(
                                new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(task.isSuccessful()){
                                            ArrayList<LinearLayout> list=new ArrayList<LinearLayout>();
                                            //int userCount=0;

                                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                                String targetB64Email=childSnapShot.getKey();
                                                if(targetB64Email.equals(b64Email)) continue;

                                                String username =
                                                        String.valueOf(childSnapShot.child(
                                                                "username").getValue()),
                                                        email =
                                                                base64ToStr(targetB64Email);

                                                if(username.contains(keyword) || email.contains(keyword)) {
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

                                                    list.add(linearLayout);
                                                    //userCount++;
                                                    //if(userCount>20) break;
                                                }
                                                //if (userCount>20)break;
                                            }

                                            ConstraintLayout constraintLayout=
                                                    view.findViewById(R.id.cl_searchFrag_listPics);
                                            constraintLayout.removeAllViews();
                                            GeneralFunc.itemsUser2Layout(constraintLayout, list,
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

                                            if(list.size()==0)
                                                tvNoRes.setVisibility(View.VISIBLE);
                                            else
                                                tvNoRes.setVisibility(View.GONE);
                                        }

                                        view.findViewById(R.id.pb_searchFrag).setVisibility(View.GONE);
                                    }
                                });
                    }

                    return true;
                }

                return false;
            }
        });

        return view;
    }

    boolean isByPic(Context context, CardView cv){
        return cv.getBackgroundTintList()==ContextCompat.getColorStateList(context, R.color.orange_brown);
    }

}