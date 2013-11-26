package com.nolanlawson.supersaiyan.example1;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;

import com.nolanlawson.superaiyan.example1.R;
import com.nolanlawson.supersaiyan.example1.data.Country;
import com.nolanlawson.supersaiyan.example1.data.CountryAdapter;
import com.nolanlawson.supersaiyan.example1.data.CountryHelper;
import com.nolanlawson.supersaiyan.example1.data.CountrySorting;
import com.nolanlawson.supersaiyan.widget.SeparatedListAdapter;

public class MainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<Country> countries = CountryHelper.readInCountries(this);
        
        Collections.sort(countries, CountrySorting.ByContinent.getComparator());
        
        CountryAdapter adapter = new CountryAdapter(this, android.R.layout.simple_list_item_2, countries);
        SeparatedListAdapter<CountryAdapter> separatedAdapter = new SeparatedListAdapter<CountryAdapter>(this);
        setListAdapter(separatedAdapter);
    }
}
