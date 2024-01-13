package com.uit.surpic;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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

        String b64Email= GeneralFunc.str2Base64(user.getEmail());

        ImageView userPic=view.findViewById(R.id.iv_profileFrag_profilePic);
        TextView tv_username=view.findViewById(R.id.tv_profileFrag_username),
                tv_follower_follow=view.findViewById(R.id.tv_profileFrag_follower_follow);
        ProgressBar progressBar=view.findViewById(R.id.pb_profileFrag),
                progressBarProfile=view.findViewById(R.id.pb_profileFrag_profile);
        ConstraintLayout cl_profile=view.findViewById(R.id.cl_profileFrag_profile),
                cl_vp=view.findViewById(R.id.cl_profileFrag_viewPic);
        FloatingActionButton fab_vp_back=view.findViewById(R.id.fab_profileFrag_vP_back);
        ScrollView scrollView=view.findViewById(R.id.sv_profileFrag_profile);

        progressBar.setVisibility(View.VISIBLE);
        cl_profile.setVisibility(View.GONE);

        //Tải ảnh đại diện
        mDB.child(b64Email).child(view.getContext().getString(R.string.profile_pic)).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                userPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(String.valueOf(
                        task.getResult().getValue())));
                cl_profile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                view.findViewById(R.id.pb_profileFrag_profile).setVisibility(View.VISIBLE);
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

        //Cập nhật tình trạng theo dõi
        mDB.child(b64Email).child("follower").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            tv_follower_follow.setText(String.valueOf(task.getResult()
                                    .getChildrenCount())+" người theo dõi");
                        }
                    }
                });
        mDB.child(b64Email).child("follow").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            tv_follower_follow.setText(
                                    tv_follower_follow.getText().toString()+
                                            " & đang theo dõi "+ String.valueOf(task
                                            .getResult().getChildrenCount())+" người khác");
                        }
                    }
                });

        //Lấy danh sách ảnh của tài khoản
        mDB.child("fast").child(b64Email).child("pics").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                    ArrayList<Boolean> dummyList=new ArrayList<Boolean>();

                    int picCount=0;
                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                        dummyList.add(true);

                        mDB.child(b64Email).child("pics")
                                .child(childSnapShot.getKey()).child("data").get().addOnCompleteListener(
                                        new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                DataSnapshot dataSnapshot=task.getResult();
                                                objectPic pic=new objectPic(
                                                        childSnapShot.getKey(),
                                                        String.valueOf(dataSnapshot.getValue()),

                                                        String.valueOf(childSnapShot.child(
                                                                "name").getValue()),

                                                        String.valueOf(childSnapShot.child(
                                                                "tags").getValue()),
                                                        b64Email);

                                                LinearLayout linearLayout= GeneralFunc.itemPic(
                                                        view.getContext(), pic);
                                                linearLayoutArrayList.add(linearLayout);

                                                if(linearLayoutArrayList.size()==dummyList.size())
                                                {
                                                    ConstraintLayout constraintLayout= view.findViewById(R.id.cl_profileFrag_listPics);
                                                    constraintLayout.removeAllViews();
                                                    GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                                                            new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    scrollView.setEnabled(false);

                                                                    ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                                                            vpImg = view.findViewById(R.id.iv_profileFrag_vP_img);
                                                                    vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                                                    vpImg.setTag(selectedImg.getTag());
                                                                    vpImg.setTag(R.id.isZip,true);

                                                                    ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(
                                                                            ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                                                                    ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText(
                                                                            tv_username.getText().toString());

                                                                    objectPic pic=(objectPic)selectedImg.getTag();
                                                                    if(pic.getName().length()!=0 || pic.getName().equals("null")){
                                                                        ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText(
                                                                                "Tên ảnh: "+pic.getName());
                                                                    }
                                                                    if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                                                                        ((TextView)view.findViewById(R.id.tv_profileFrag_vP_tagsPic)).setText(
                                                                                "Tags: "+pic.getStrHashtags());
                                                                    }

                                                                    cl_profile.setVisibility(View.GONE);
                                                                    cl_vp.setVisibility(View.VISIBLE);
                                                                    scrollView.setEnabled(true);
                                                                }
                                                            });

                                                    progressBarProfile.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                        picCount++;
                        if(picCount==5)break;
                    }
                }
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
                        if(item.getItemId()==R.id.setting_i_editAcc){
                            Intent intent=new Intent(getActivity().getApplicationContext(),
                                    updateUser.class);
                            intent.putExtra("user",new objectUser(b64Email, true,
                                    GeneralFunc.zipImg2Base64(((BitmapDrawable)((ImageView)view.
                                    findViewById(R.id.iv_profileFrag_profilePic)).getDrawable())
                                    .getBitmap(),100), tv_username.getText().toString()));
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }
                        if(item.getItemId()==R.id.setting_i_changePass){
                            Intent intent=new Intent(getActivity().getApplicationContext(),
                                    updatePass.class);
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


        //Chọn ảnh tải lên
        ((Button)view.findViewById(R.id.b_profileFrag_add)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI);
                takeImg.launch(intent);
            }
        });

        //Sự kiện click quay về
        fab_vp_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv_profileFrag_vP_img=view.findViewById(R.id.iv_profileFrag_vP_img);
                iv_profileFrag_vP_img.setImageBitmap(null);
                iv_profileFrag_vP_img.setTag(R.id.isZip,true);

                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(null);
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText("");

                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText("");
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_tagsPic)).setText("");

                cl_profile.setVisibility(View.VISIBLE);
                cl_vp.setVisibility(View.GONE);
            }
        });

        //Nút more của view pic
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab_profileFrag_vP_more);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo menu setting
                PopupMenu popupMenu=new PopupMenu(view.getContext(),floatingActionButton);

                //Nhận item
                popupMenu.getMenuInflater().inflate(R.menu.profile_vp_more_menu,popupMenu.getMenu());
                if((boolean) view.findViewById(R.id.iv_profileFrag_vP_img).getTag(R.id.isZip)){
                    popupMenu.getMenu().getItem(0).setTitle("Xem ảnh bản đầy đủ");
                }else {
                    popupMenu.getMenu().getItem(0).setTitle("Xem ảnh bản nén");
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        popupMenu.dismiss();

                        objectPic pic = (objectPic) view.findViewById(R.id.iv_profileFrag_vP_img)
                                .getTag();

                        //Lựa chọn chỉnh sửa
                        if(item.getItemId()==R.id.profile_vp_more_i_edit){
                            Intent intent=new Intent(getActivity().getApplicationContext(),
                                    updateImg.class);

                            intent.putExtra("picInfo",pic);
                            intent.putExtra("b64Email", GeneralFunc.str2Base64(user.getEmail()));

                            getActivity().startActivity(intent);

                            return true;
                        }

                        //Lựa chọn tải về
                        if(item.getItemId()==R.id.profile_vp_more_i_download){
                            GeneralFunc.downloadImg(view.getContext(),mDB,b64Email,pic.getKey());

                            Toast.makeText(view.getContext(),"Đang tải...",Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        //Lựa chọn xóa ảnh
                        if (item.getItemId()==R.id.profile_vp_more_i_delete){
                            AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                            builder.setMessage("Bạn có chắc muốn xóa ảnh này?").setTitle("Xác nhận");
                            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GeneralFunc.deleteImg(mDB,b64Email,pic.getKey());
                                    fab_vp_back.performClick();
                                }
                            });
                            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            AlertDialog dialog=builder.create();
                            dialog.show();

                            return true;
                        }

                        //Lựa chọn xem bản đủ
                        if(item.getItemId()==R.id.profile_vp_more_i_full_zip){
                            ImageView iv_profileFrag_vP_img=view.findViewById(R.id.iv_profileFrag_vP_img);
                            if((boolean)iv_profileFrag_vP_img.getTag(R.id.isZip)){
                                iv_profileFrag_vP_img.setTag(R.id.isZip,false);

                                mDB.child(b64Email).child("pics").child(pic.getKey())
                                        .child("full").get().addOnCompleteListener(
                                                new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                iv_profileFrag_vP_img.setImageBitmap(
                                                        GeneralFunc.unzipBase64ToImg(
                                                                String.valueOf(task.getResult().getValue())));

                                                progressBar.setVisibility(View.GONE);
                                                iv_profileFrag_vP_img.setVisibility(View.VISIBLE);
                                            }
                                        });

                                progressBar.setVisibility(View.VISIBLE);
                                iv_profileFrag_vP_img.setVisibility(View.GONE);
                            } else {
                                iv_profileFrag_vP_img.setTag(R.id.isZip,true);

                                iv_profileFrag_vP_img.setImageBitmap(
                                        GeneralFunc.unzipBase64ToImg(pic.getData()));
                            }

                            return true;
                        }

                        return false;
                    }
                });

                //Hiện menu setting
                popupMenu.show();
            }
        });

        //Cập nhật lại danh sách ảnh
        OnCompleteListener<DataSnapshot> onCompleteListener =
                new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            progressBarProfile.setVisibility(View.VISIBLE);

                            ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                            ArrayList<Boolean> dummyList=new ArrayList<Boolean>();
                            for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                                dummyList.add(true);

                                int picCount=0;

                                mDB.child(b64Email).child("pics")
                                        .child(childSnapShot.getKey()).child("data").get().addOnCompleteListener(
                                                new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        DataSnapshot dataSnapshot=task.getResult();
                                                        objectPic pic=new objectPic(
                                                                childSnapShot.getKey(),
                                                                String.valueOf(dataSnapshot.getValue()),

                                                                String.valueOf(childSnapShot.child(
                                                                        "name").getValue()),

                                                                String.valueOf(childSnapShot.child(
                                                                        "tags").getValue()),
                                                                b64Email);

                                                        LinearLayout linearLayout= GeneralFunc.itemPic(
                                                                view.getContext(), pic);
                                                        linearLayoutArrayList.add(linearLayout);

                                                        if(linearLayoutArrayList.size()==dummyList.size())
                                                        {
                                                            ConstraintLayout constraintLayout= view.findViewById(R.id.cl_profileFrag_listPics);
                                                            constraintLayout.removeAllViews();
                                                            GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                                                                    new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            scrollView.setEnabled(false);

                                                                            ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                                                                    vpImg = view.findViewById(R.id.iv_profileFrag_vP_img);
                                                                            vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                                                            vpImg.setTag(selectedImg.getTag());
                                                                            vpImg.setTag(R.id.isZip,true);

                                                                            ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(
                                                                                    ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                                                                            ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText(
                                                                                    tv_username.getText().toString());

                                                                            objectPic pic=(objectPic)selectedImg.getTag();
                                                                            if(pic.getName().length()!=0 || pic.getName().equals("null")){
                                                                                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText(
                                                                                        "Tên ảnh: "+pic.getName());
                                                                            }
                                                                            if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                                                                                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_tagsPic)).setText(
                                                                                        "Tags: "+pic.getStrHashtags());
                                                                            }

                                                                            cl_profile.setVisibility(View.GONE);
                                                                            cl_vp.setVisibility(View.VISIBLE);
                                                                            scrollView.setEnabled(true);
                                                                        }
                                                                    });

                                                            progressBarProfile.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                picCount++;
                                if(picCount==5)break;
                            }

                        }
                    }
                };
        mDB.child(b64Email).child("pics").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //Lấy danh sách ảnh của tài khoản
                mDB.child("fast").child(b64Email).child("pics").get()
                        .addOnCompleteListener(onCompleteListener);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                //Lấy danh sách ảnh của tài khoản
                mDB.child("fast").child(b64Email).child("pics").get()
                        .addOnCompleteListener(onCompleteListener);

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Load thêm ảnh cho danh sách
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY-108 == ((ScrollView)v).getChildAt(0).getMeasuredHeight()-v.getMeasuredHeight()){
                    ConstraintLayout constraintLayout = view.findViewById(R.id.cl_profileFrag_listPics);
                    if(constraintLayout.getChildCount()%5!=0){
                        scrollView.setOnScrollChangeListener(null);
                        return;
                    }

                    progressBarProfile.setVisibility(View.VISIBLE);
                    mDB.child("fast").child(b64Email).child("pics").get().addOnCompleteListener(
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
                                                    ((CardView)
                                                            ((LinearLayout)constraintLayout.getChildAt(i))
                                                                    .getChildAt(0))
                                                            .getChildAt(0))
                                                            .getTag()).getKey());
                                        }

                                        int picCount=0;

                                        for(DataSnapshot childSnapShot : task.getResult().getChildren())
                                        {
                                            if(picKeys.contains(childSnapShot.getKey()))continue;

                                            dummyList.add(true);

                                            mDB.child(b64Email).child("pics")
                                                    .child(childSnapShot.getKey()).child("data")
                                                    .get().addOnCompleteListener(
                                                            new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull
                                                                                       Task<DataSnapshot>
                                                                                               task) {
                                                                    DataSnapshot dataSnapshot=
                                                                            task.getResult();
                                                                    objectPic pic=new objectPic(
                                                                            childSnapShot.getKey(),
                                                                            String.valueOf(
                                                                                    dataSnapshot
                                                                                            .getValue()),

                                                                            String.valueOf(
                                                                                    childSnapShot.child(
                                                                                                    "name")
                                                                                            .getValue()),

                                                                            String.valueOf(
                                                                                    childSnapShot.child(
                                                                                                    "tags")
                                                                                            .getValue()),
                                                                            b64Email);

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
                                                                                new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        scrollView.setEnabled(false);

                                                                                        ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                                                                                vpImg = view.findViewById(R.id.iv_profileFrag_vP_img);
                                                                                        vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                                                                        vpImg.setTag(selectedImg.getTag());
                                                                                        vpImg.setTag(R.id.isZip,true);

                                                                                        ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(
                                                                                                ((BitmapDrawable)userPic.getDrawable()).getBitmap());
                                                                                        ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText(
                                                                                                tv_username.getText().toString());

                                                                                        objectPic pic=(objectPic)selectedImg.getTag();
                                                                                        if(pic.getName().length()!=0 || pic.getName().equals("null")){
                                                                                            ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText(
                                                                                                    "Tên ảnh: "+pic.getName());
                                                                                        }
                                                                                        if(pic.getTags()[0].length() !=0 || pic.getTags()[0].equals("null")){
                                                                                            ((TextView)view.findViewById(R.id.tv_profileFrag_vP_tagsPic)).setText(
                                                                                                    "Tags: "+pic.getStrHashtags());
                                                                                        }

                                                                                        cl_profile.setVisibility(View.GONE);
                                                                                        cl_vp.setVisibility(View.VISIBLE);
                                                                                        scrollView.setEnabled(true);
                                                                                    }
                                                                                });

                                                                        progressBarProfile
                                                                                .setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });

                                            picCount++;
                                            if(picCount==5)break;
                                        }
                                    }
                                }
                            });
                }
            }
        });


        return view;
    }

    //Xử lý sau khi chọn được ảnh
    ActivityResultLauncher<Intent> takeImg=registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()== RESULT_OK && result.getData() != null){
            Uri uri=result.getData().getData();

            Intent intent = new Intent(this.getContext(), uploadImg.class);
            intent.setData(uri);
            intent.putExtra("b64Email", GeneralFunc.str2Base64(user.getEmail()));
            startActivity(intent);
        }
    });

}