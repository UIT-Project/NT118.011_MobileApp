package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<objectUser>list;

    public UserAdapter(Context context,int layout, List<objectUser> list){
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
        ImageView pic;
        TextView username;
        TextView email;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserAdapter.ViewHolder holder;

        if(convertView==null){
            holder=new ViewHolder();
            LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(layout,null);
            holder.pic=(ImageView) convertView.findViewById(R.id.iv_itemUser_pic);
            holder.username=(TextView) convertView.findViewById(R.id.tv_itemUser_username);
            holder.email=(TextView) convertView.findViewById(R.id.tv_itemUser_email);

            convertView.setTag(holder);
        }else {
            holder=(ViewHolder) convertView.getTag();
        }

        holder.pic.setImageBitmap(GeneralFunc.unzipBase64ToImg(list.get(position).getDataUserPic()));
        holder.pic.setTag(list.get(position));
        holder.username.setText(list.get(position).getUsername());
        holder.email.setText(GeneralFunc.base64ToStr(list.get(position).getB64Email()));

        return convertView;
    }

}
