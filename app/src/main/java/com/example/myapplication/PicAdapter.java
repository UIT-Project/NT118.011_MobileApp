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
    private List<objectPic> list;

    public PicAdapter(Context context,int layout, List<objectPic> list){
        this.context=context;
        this.list=list;
        this.layout=layout;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        holder.imageView.setImageBitmap(GeneralFunc.unzipBase64ToImg(list.get(position).getData()));
        holder.imageView.setTag(list.get(position));

        return convertView;
    }
}
