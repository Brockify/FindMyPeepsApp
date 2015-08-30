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
    ArrayList<String> usernames;
    ArrayList<String> dates;
    ArrayList<String> times;
    private static LayoutInflater inflater=null;
    public CustomAdapter(StatusActivity mainActivity, ArrayList<String> prgmNameList, ArrayList<Bitmap> prgmImages, ArrayList<String> prgrmUsernames, ArrayList<String> dates, ArrayList<String> times) {
        // TODO Auto-generated constructor stub
        result =prgmNameList;
        context=mainActivity;
        imageId = prgmImages;
        usernames = prgrmUsernames;
        this.dates = dates;
        this.times = times;
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
        TextView username;
        TextView date;
        TextView time;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.status_list_items, null);
        holder.tv=(TextView) rowView.findViewById(R.id.notificationTextView);
        holder.img=(ImageView) rowView.findViewById(R.id.profilePic);
        holder.username = (TextView) rowView.findViewById(R.id.status_usernameTextView);
        holder.date = (TextView) rowView.findViewById(R.id.dateTextView);
        holder.time = (TextView) rowView.findViewById(R.id.timeTextView);

        holder.username.setText(usernames.get(position));
        holder.tv.setText(result.get(position));
        holder.img.setImageBitmap(imageId.get(position));
        holder.date.setText(dates.get(position));
        holder.time.setText(times.get(position));
        return rowView;
    }

}