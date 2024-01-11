package com.example.myapplication;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.makeMainSelectorActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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

        ImageView userPic=view.findViewById(R.id.iv_profileFrag_profilePic);
        TextView tv_username=view.findViewById(R.id.tv_profileFrag_username),
                tv_follower_follow=view.findViewById(R.id.tv_profileFrag_follower_follow);
        ((ProgressBar)view.findViewById(R.id.pb_profileFrag)).setVisibility(View.VISIBLE);
        ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(View.GONE);

        //Tải ảnh đại diện
        mDB.child(b64Email).child(view.getContext().getString(R.string.profile_pic)).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                userPic.setImageBitmap(GeneralFunc.unzipBase64ToImg(String.valueOf(
                        task.getResult().getValue())));
                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                        View.VISIBLE);
                ((ProgressBar)view.findViewById(R.id.pb_profileFrag)).setVisibility(View.GONE);
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
        mDB.child(b64Email).child("pics").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                        LinearLayout linearLayout=GeneralFunc.itemPic(view.getContext(),new objectPic(
                                childSnapShot.getKey(),
                                String.valueOf(childSnapShot.child("data").getValue()),
                                String.valueOf(childSnapShot.child("name").getValue()),
                                String.valueOf(childSnapShot.child("tags").getValue()),
                                b64Email));
                        linearLayoutArrayList.add(linearLayout);
                    }

                    ConstraintLayout constraintLayout= view.findViewById(R.id.cl_profileFrag_listPics);
                    constraintLayout.removeAllViews();
                    GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                            new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                    vpImg = view.findViewById(R.id.iv_profileFrag_vP_img);
                            vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                            vpImg.setTag(selectedImg.getTag());

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

                            ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                                    View.GONE);
                            ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_viewPic)).setVisibility(
                                    View.VISIBLE);
                        }
                    });
                }
                view.findViewById(R.id.pb_profileFrag_profile).setVisibility(View.GONE);
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
        ((FloatingActionButton)view.findViewById(R.id.fab_profileFrag_vP_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_img)).setImageBitmap(null);
                ((ImageView)view.findViewById(R.id.iv_profileFrag_vP_profilePic)).setImageBitmap(null);
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_username)).setText("");

                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_namePic)).setText("");
                ((TextView)view.findViewById(R.id.tv_profileFrag_vP_tagsPic)).setText("");

                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                        View.VISIBLE);
                ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_viewPic)).setVisibility(
                        View.GONE);
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
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //Lựa chọn chỉnh sửa
                        if(item.getItemId()==R.id.profile_vp_more_i_edit){
                            Intent intent=new Intent(getActivity().getApplicationContext(),
                                    updateImg.class);

                            intent.putExtra("picInfo",(objectPic)((ImageView)view
                                    .findViewById(R.id.iv_profileFrag_vP_img)).getTag());
                            intent.putExtra("b64Email",GeneralFunc.str2Base64(user.getEmail()));

                            getActivity().startActivity(intent);
                        }

                        //Lựa chọn tải về
                        if(item.getItemId()==R.id.profile_vp_more_i_download){
                            mDB.child(b64Email).child("pics").child(
                                    ((objectPic)((ImageView)view.findViewById(
                                            R.id.iv_profileFrag_vP_img)).getTag()).getKey())
                                    .child("full").get()
                                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    File directory = new File(Environment.getExternalStorageDirectory(),
                                            Environment.DIRECTORY_PICTURES+"/surpic");
                                    if (!directory.exists()) {
                                        directory.mkdirs();
                                    }

                                    //Tự động tạo file dựa vào thời gian
                                    String timeStamp = DateFormat.format("yyyyMMdd_HHmmss",
                                            new Date()).toString();
                                    String fileName = "image_" + timeStamp + ".jpeg";

                                    File file = new File(directory, fileName);

                                    try {
                                        //Lưu ảnh vào file
                                        FileOutputStream fos = new FileOutputStream(file);
                                        GeneralFunc.unzipBase64ToImg(task.getResult().getValue()
                                                .toString()).compress(Bitmap.CompressFormat.JPEG,
                                                100, fos);
                                        fos.close();

                                        //Thông báo để GALLERY cập nhật
                                        view.getContext().sendBroadcast( makeMainSelectorActivity(
                                                android.content.Intent.ACTION_MAIN,
                                                android.content.Intent.CATEGORY_APP_GALLERY));

                                        Toast.makeText(view.getContext(),"Tải xuống thành công",
                                                Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        return true;
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
                view.findViewById(R.id.pb_profileFrag_profile).setVisibility(View.VISIBLE);
                if(task.isSuccessful()){
                    ArrayList<LinearLayout> linearLayoutArrayList=new ArrayList<LinearLayout>();
                    for(DataSnapshot childSnapShot : task.getResult().getChildren()){
                        LinearLayout linearLayout=GeneralFunc.itemPic(view.getContext(),new objectPic(
                                childSnapShot.getKey(),
                                String.valueOf(childSnapShot.child("data").getValue()),
                                String.valueOf(childSnapShot.child("name").getValue()),
                                String.valueOf(childSnapShot.child("tags").getValue()),
                                b64Email));
                        linearLayoutArrayList.add(linearLayout);
                    }

                    ConstraintLayout constraintLayout= view.findViewById(R.id.cl_profileFrag_listPics);
                    constraintLayout.removeAllViews();
                    GeneralFunc.items2Layout(constraintLayout, linearLayoutArrayList,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImageView selectedImg=v.findViewById(R.id.iv_itemPic),
                                            vpImg = view.findViewById(R.id.iv_profileFrag_vP_img);
                                    vpImg.setImageBitmap(((BitmapDrawable)selectedImg.getDrawable()).getBitmap());
                                    vpImg.setTag(selectedImg.getTag());

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

                                    ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_profile)).setVisibility(
                                            View.GONE);
                                    ((ConstraintLayout)view.findViewById(R.id.cl_profileFrag_viewPic)).setVisibility(
                                            View.VISIBLE);
                                }
                            });
                }
                view.findViewById(R.id.pb_profileFrag_profile).setVisibility(View.GONE);
            }
        };
        mDB.child(b64Email).child("pics").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(onCompleteListener);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(onCompleteListener);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                //Lấy danh sách ảnh của tài khoản
                mDB.child(b64Email).child("pics").get().addOnCompleteListener(onCompleteListener);

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    //Xử lý sau khi chọn được ảnh
    ActivityResultLauncher<Intent> takeImg=registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()== RESULT_OK && result.getData() != null){
            Uri uri=result.getData().getData();

            Intent intent = new Intent(this.getContext(),uploadImg.class);
            intent.setData(uri);
            intent.putExtra("b64Email",GeneralFunc.str2Base64(user.getEmail()));
            startActivity(intent);
        }
    });

}