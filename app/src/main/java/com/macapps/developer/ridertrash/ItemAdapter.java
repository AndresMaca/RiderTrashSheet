package com.macapps.developer.ridertrash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Developer on 18/5/2017.
 */

public class ItemAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mStrings;
    public ItemAdapter(Context context,ArrayList<String> strings){
        mContext=context;
        mLayoutInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStrings=strings;

    }
    @Override
    public int getCount() {
        return mStrings.size();
    }

    @Override
    public Object getItem(int position) {
        return mStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView=mLayoutInflater.inflate(R.layout.bus_item,parent,false);
        TextView textView= (TextView)rowView.findViewById(R.id.textView2);
        textView.setText(mStrings.get(position));
        return rowView;
    }
}
