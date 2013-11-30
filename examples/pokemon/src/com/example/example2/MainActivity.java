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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

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

public class MainActivity extends ListActivity implements OnCheckedChangeListener {

    private SectionedListAdapter<PocketMonsterAdapter> adapter;
    private SuperSaiyanScrollView scrollView;
    private CheckBox showSectionTitlesCheckBox, showOverlaysCheckBox;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        scrollView = (SuperSaiyanScrollView) findViewById(R.id.scroll);
        showSectionTitlesCheckBox = (CheckBox) findViewById(R.id.checkbox_titles);
        showOverlaysCheckBox = (CheckBox) findViewById(R.id.checkbox_overlays);
        showSectionTitlesCheckBox.setOnCheckedChangeListener(this);
        showOverlaysCheckBox.setOnCheckedChangeListener(this);
        
        List<PocketMonster> monsters = PocketMonsterHelper.readInPocketMonsters(this);
        
        PocketMonsterAdapter subAdapter = new PocketMonsterAdapter(this, monsters);
        
        adapter = SectionedListAdapter.Builder.create(this, subAdapter)
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
        adapter.setValueSorting(Sorting.Explicit);
        adapter.setValueComparator(new Comparator<PocketMonster>(){

                    @Override
                    public int compare(PocketMonster left, PocketMonster right) {
                        return left.getName().compareToIgnoreCase(right.getName());
                    }});
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Small);
        adapter.notifyDataSetChanged();
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
        adapter.setValueSorting(Sorting.InputOrder);
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Normal);
        adapter.notifyDataSetChanged();
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
        adapter.setValueSorting(Sorting.InputOrder);
        scrollView.setOverlaySizeScheme(OverlaySizeScheme.Large);
        adapter.notifyDataSetChanged();
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_titles:
                adapter.setShowSectionTitles(isChecked);
                break;
            case R.id.checkbox_overlays:
                adapter.setShowSectionOverlays(isChecked);
                break;
        }
        adapter.notifyDataSetChanged();
        scrollView.refresh();
    }
}
