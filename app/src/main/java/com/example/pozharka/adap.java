package com.example.pozharka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class adap extends BaseAdapter {

    ArrayList<Item> egug = new ArrayList<Item>();
    Context context;

    public adap(Context context, ArrayList<Item> arr) {
        if (arr != null) {
            egug = arr;
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return egug.size();
    }

    @Override
    public Object getItem(int num) {
        return egug.get(num);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int i, View someView, ViewGroup arg2) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (someView == null) {
            someView = inflater.inflate(R.layout.mylist, arg2, false);
        }

        TextView SSID = (TextView) someView.findViewById(R.id.SSID);
        TextView BSSID = (TextView) someView.findViewById(R.id.BSSID);
        TextView strength = (TextView) someView.findViewById(R.id.strength);
        TextView cab = (TextView) someView.findViewById(R.id.cab);

        SSID.setText(egug.get(i).SSID);
        BSSID.setText(egug.get(i).BSSID);
        strength.setText(egug.get(i).strength);
        cab.setText(egug.get(i).cab);
        return someView;
    }
}
