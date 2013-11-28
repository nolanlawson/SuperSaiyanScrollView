package com.example.example2;

import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

import com.example.example2.data.PocketMonster;
import com.example.example2.data.PocketMonsterAdapter;
import com.example.example2.data.PocketMonsterHelper;
import com.nolanlawson.supersaiyan.SectionedListAdapter;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
