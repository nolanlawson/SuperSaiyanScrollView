package com.example.example2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.example2.data.PocketMonster;
import com.example.example2.data.PocketMonsterAdapter;
import com.example.example2.data.PocketMonsterHelper;
import com.nolanlawson.supersaiyan.MultipleSectionizer;
import com.nolanlawson.supersaiyan.OverlaySizeScheme;
import com.nolanlawson.supersaiyan.SectionedListAdapter;
import com.nolanlawson.supersaiyan.SectionedListAdapter.Sorting;
import com.nolanlawson.supersaiyan.Sectionizer;
import com.nolanlawson.supersaiyan.Sectionizers;
import com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView;

public class MainActivity extends ListActivity {

    private SectionedListAdapter<PocketMonsterAdapter> adapter;
    private SuperSaiyanScrollView scrollView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        scrollView = (SuperSaiyanScrollView) findViewById(R.id.scroll);
        
        List<PocketMonster> monsters = PocketMonsterHelper.readInPocketMonsters(this);
        
        PocketMonsterAdapter subAdapter = new PocketMonsterAdapter(this, monsters);
        
        adapter = new SectionedListAdapter.Builder<PocketMonsterAdapter>(this)
                .setSubAdapter(subAdapter)
                .setSectionizer(Sectionizers.UsingFirstLetterOfToString)
                .sortKeys()
                .sortValues(new Comparator<PocketMonster>(){

                    @Override
                    public int compare(PocketMonster left, PocketMonster right) {
                        return left.getName().compareToIgnoreCase(right.getName());
                    }})
                .build();
        
        setListAdapter(adapter);
    }
    
    private void sortByAz() {
        adapter.setSectionizer(Sectionizers.UsingFirstLetterOfToString);
        adapter.setKeySorting(Sorting.Natural);
        adapter.notifyDataSetChanged();
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Small);
        scrollView.refresh();
    }
    
    private void sortByType() {
        adapter.setMultipleSectionizer(new MultipleSectionizer<PocketMonster>() {

            @Override
            public Collection<? extends CharSequence> toSections(PocketMonster input) {
                if (!TextUtils.isEmpty(input.getType2())) {
                    // two types
                    return Arrays.asList(input.getType1(), input.getType2());
                } else {
                    // one type
                    return Collections.singleton(input.getType1());
                }
            }
        });
        adapter.setKeySorting(Sorting.Natural);
        adapter.notifyDataSetChanged();
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Normal);
        scrollView.refresh();
    }
    
    private void sortByRegion() {
        adapter.setSectionizer(new Sectionizer<PocketMonster>() {

            @Override
            public CharSequence toSection(PocketMonster input) {
                int id = input.getNationalDexNumber();
                
                // see http://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_National_Pok%C3%A9dex_number
                // Kanto region will appear first, followed by those from Johto, Hoenn, Sinnoh, Unova, and Kalos
                if (id <= 151) {
                    return "Kanto (Generation 1)";
                } else if (id <= 251) {
                    return "Johto (Generation 2)";
                } else if (id <= 386) {
                    return "Hoenn (Generation 3)";
                } else if (id <= 493) {
                    return "Sinnoh (Generation 4)";
                } else if (id <= 649) {
                    return "Unova (Generation 5)";
                } else {
                    return "Kalos (Generation 6)";
                }
            }
        });
        adapter.setKeySorting(Sorting.InputOrder); // uses the nat'l pokedex order, since that's the original order
        adapter.notifyDataSetChanged();
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Large);
        scrollView.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case R.id.action_sort_by_az:
                sortByAz();
                return true;
            case R.id.action_sort_by_region:
                sortByRegion();
                return true;
            case R.id.action_sort_by_type:
                sortByType();
                return true;
        }
        
        return false;
    }
    
    

}
