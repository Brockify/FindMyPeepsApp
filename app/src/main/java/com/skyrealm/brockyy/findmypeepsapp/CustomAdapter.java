package com.skyrealm.brockyy.findmypeepsapp;

/**
 * Created by RockyFish on 8/18/15.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter{
    ArrayList<String> result;
    Context context;
    ArrayList<Bitmap> imageId;
    private static LayoutInflater inflater=null;
    public CustomAdapter(StatusActivity mainActivity, ArrayList<String> prgmNameList, ArrayList<Bitmap> prgmImages) {
        // TODO Auto-generated constructor stub
        result =prgmNameList;
        context=mainActivity;
        imageId = prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.status_list_items, null);
        holder.tv=(TextView) rowView.findViewById(R.id.notificationTextView);
        holder.img=(ImageView) rowView.findViewById(R.id.profilePic);
        holder.tv.setText(result.get(position));
        holder.img.setImageBitmap(imageId.get(position));
        return rowView;
    }

}