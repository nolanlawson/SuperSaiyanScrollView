package com.example.example1.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CountryAdapter extends ArrayAdapter<Country> {
    
    private int textViewResourceId;

    public CountryAdapter(Context context, int textViewResourceId,
            List<Country> objects) {
        super(context, textViewResourceId, objects);
        this.textViewResourceId = textViewResourceId;
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(textViewResourceId, parent, false);
        }
        
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        
        Country country = (Country)getItem(position);
        
        // just use the country's name to display in the list
        textView.setText(country.getName());
        
        return view;
    }
}
