package com.example.example1;

import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.example1.data.Country;
import com.example.example1.data.CountryHelper;
import com.nolanlawson.supersaiyan.SectionedListAdapter;
import com.nolanlawson.supersaiyan.Sectionizer;
import com.nolanlawson.supersaiyan.Sectionizers;
import com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView;

public class MainActivity extends ListActivity {

    private SuperSaiyanScrollView superSaiyanScrollView;
    private SectionedListAdapter<ArrayAdapter<Country>> sectionedAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<Country> countries = CountryHelper.readInCountries(this);
        
        ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(
                this, android.R.layout.simple_spinner_item, countries);
        
        sectionedAdapter = SectionedListAdapter.Builder.create(this, adapter)
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
        
        superSaiyanScrollView = (SuperSaiyanScrollView) findViewById(R.id.scroll);
    }
    
    private void sortAz() {
        sectionedAdapter.setSectionizer(Sectionizers.UsingFirstLetterOfToString);
        sectionedAdapter.notifyDataSetChanged();
        superSaiyanScrollView.refresh();
    }
    
    private void sortByContinent() {
        sectionedAdapter.setSectionizer(new Sectionizer<Country>(){

                    @Override
                    public CharSequence toSection(Country input) {
                        return input.getContinent();
                    }
                });
        sectionedAdapter.notifyDataSetChanged();
        superSaiyanScrollView.refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case R.id.action_sort_az:
                sortAz();
                break;
            case R.id.action_sort_continent:
                sortByContinent();
                break;
        }
        
        return false;
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }    
    
}
