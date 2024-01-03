package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    ArrayList<objectPic> picArrayList;
    Context context;

    public HomeAdapter(ArrayList<objectPic> picArrayList,Context context ) {
        this.picArrayList = picArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.staggered_item, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Glide.with(context).load(picArrayList.get(position).getData()).into(holder.staggeredImages);
    }

    @Override
    public int getItemCount() {
        return picArrayList.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView staggeredImages;
        public HomeViewHolder(@NonNull View itemView){
            super(itemView);
            staggeredImages=itemView.findViewById(R.id.staggeredImages);
        }
    }
}
