package com.example.example1;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.example1.data.Country;
import com.example.example1.data.CountryHelper;
import com.nolanlawson.supersaiyan.widget.SuperSaiyanRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity {

    private SuperSaiyanRecyclerView superSaiyanRecyclerView;

    // Dataset
    private List<Country> countries;

    // Helpers for alphabetical sorting
    private final Comparator<Country> mAlphaComparator = new Comparator<Country>() {
        @Override
        public int compare( Country lhs, Country rhs ) {
            return getFirstChar( lhs ) <  getFirstChar( rhs ) ? -1 : ( getFirstChar( lhs ) ==  getFirstChar( rhs ) ? 0 : 1);
        }
    };
    private final char getFirstChar( Country country ) {
        char ret = '#';
        if ( country != null ) {
            char firstChar = Character.toUpperCase( country.getName().charAt( 0 ) );
            if ( firstChar >= 'A' && firstChar <= 'Z' ) {
                ret = firstChar;
            }
        }
        return ret;
    }

    // Helpers for continent sorting
    private final Comparator<Country> mContinentComparator = new Comparator<Country>() {
        @Override
        public int compare( Country lhs, Country rhs ) {
            return lhs.getContinent().toUpperCase().compareTo( rhs.getContinent().toUpperCase() );
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countries = CountryHelper.readInCountries(this);
        superSaiyanRecyclerView = (SuperSaiyanRecyclerView) findViewById(R.id.scroll);

        // Default sort by alpha
        sortAz();

        RecyclerView.Adapter<RecyclerView.ViewHolder> adp = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            class CountryViewHolder extends RecyclerView.ViewHolder {
                private TextView mTextView;
                public CountryViewHolder(View v) {
                    super(v);
                    mTextView = (TextView) v.findViewById( android.R.id.text1 );
                    mTextView.setTextColor( getResources().getColor( android.R.color.black ) );
                }
                TextView getTextView() { return mTextView; }
            }

            @Override
            public CountryViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
                return new CountryViewHolder( LayoutInflater.from( getApplicationContext() ).inflate( android.R.layout.simple_spinner_item, parent, false ) );
            }

            @Override
            public void onBindViewHolder( RecyclerView.ViewHolder holder, int position ) {
                TextView tv = ((CountryViewHolder)holder).getTextView();
                tv.setText( countries.get(position).getName() );
            }

            @Override
            public int getItemCount() {
                return countries.size();
            }
        };

        RecyclerView rv = (RecyclerView)superSaiyanRecyclerView.findViewById( android.R.id.list );
        rv.setHasFixedSize( true );
        rv.setLayoutManager( new LinearLayoutManager( this ) );
        rv.setAdapter( adp );
    }

    private void sortAz() {
        List<String> sectionNames = new ArrayList<>();
        List<Integer> sectionPositions = new ArrayList<>();

        if ( countries != null  &&  countries.size() > 0 ) {
            // Sort data by alpha
            Collections.sort( countries, mAlphaComparator );

            // Compute sections
            for ( char c = 'A'; c <= 'Z'; ++c ) {
                sectionNames.add( String.valueOf( c ) );
            }
            int i = (int) ('A') - 1;
            for ( Country country : countries ) {
                char firstChar = getFirstChar( country );
                while ( (int) firstChar > i ) {
                    if ( sectionPositions.size() < sectionNames.size() )
                        sectionPositions.add( countries.indexOf( country ) );
                    ++i;
                }
            }
        }

        superSaiyanRecyclerView.setSections( sectionNames, sectionPositions );
    }
    
    private void sortByContinent() {
        List<String> sectionNames = new ArrayList<>();
        List<Integer> sectionPositions = new ArrayList<>();

        if ( countries != null  &&  countries.size() > 0 ) {
            // Sort data by continent
            Collections.sort( countries, mContinentComparator );

            // Compute sections
            Set<String> continents = new HashSet<>();
            for ( Country country : countries ) {
                continents.add( country.getContinent() );
            }
            sectionNames.addAll( continents );
            Collections.sort( sectionNames );

            int i = 0;
            for ( String continent : sectionNames ) {
                if ( sectionPositions.size() < sectionNames.size() )
                    sectionPositions.add( i );
                while ( countries.get(i).getContinent().compareTo( continent ) < 0 ) {
                    ++i;
                }
            }
        }

        superSaiyanRecyclerView.setSections( sectionNames, sectionPositions );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case R.id.action_sort_continent:
                sortByContinent();
                break;
            case R.id.action_sort_az:
            default:
                sortAz();
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