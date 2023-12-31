package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PicAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<String> listB64Img;

    public PicAdapter(Context context,int layout, List<String> listB64Img){
        this.context=context;
        this.listB64Img=listB64Img;
        this.layout=layout;
    }
    @Override
    public int getCount() {
        return listB64Img.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    private class ViewHolder{
        ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView==null){
            holder=new ViewHolder();
            LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(layout,null);
            holder.imageView=(ImageView) convertView.findViewById(R.id.iv_itemPic);
            convertView.setTag(holder);
        }else {
            holder=(ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageBitmap(GeneralFunc.unzipBase64ToImg(listB64Img.get(position)));

        return convertView;
    }
}
