package com.nolanlawson.supersaiyan.example1;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;

import com.nolanlawson.supersaiyan.SectionedListAdapter;
import com.nolanlawson.supersaiyan.Sectionizer;
import com.nolanlawson.supersaiyan.example1.data.Country;
import com.nolanlawson.supersaiyan.example1.data.CountryAdapter;
import com.nolanlawson.supersaiyan.example1.data.CountryHelper;
import com.nolanlawson.supersaiyan.example1.data.CountrySorting;
import com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView;

public class MainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<Country> countries = CountryHelper.readInCountries(this);
        
        Collections.sort(countries, CountrySorting.ByContinent.getComparator());
        
        CountryAdapter adapter = new CountryAdapter(this, android.R.layout.simple_spinner_item, countries);
        
        SectionedListAdapter<CountryAdapter> sectionedAdapter = new SectionedListAdapter.Builder<CountryAdapter>(this)
                .setSubAdapter(adapter)
                .setSectionizer(new Sectionizer<Country>(){

                    @Override
                    public CharSequence toSection(Country input) {
                        return input.getContinent();
                    }
                })
                .sortKeys()
                .sortValues(new Comparator<Country>() {
                    
                    public int compare(Country left, Country right) {
                        return left.getName().compareTo(right.getName());
                    }
                })
                .build();
        
        setListAdapter(sectionedAdapter);
        
        sectionedAdapter.notifyDataSetChanged();
        ((SuperSaiyanScrollView)findViewById(R.id.scroll)).listItemsChanged();
        
    }
}
