package com.example.example1.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.example1.R;

public class CountryHelper {
    public static List<Country> readInCountries(Context context) {
        List<Country> countries = new ArrayList<Country>();
        BufferedReader buff = null;
        try {
            buff = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.countries)));
            while (buff.ready()) {
                String[] line = buff.readLine().split("\t");

                Country country = new Country();
                country.setContinent(line[0]);
                country.setName(line[1]);

                countries.add(country);
            }
        } catch (IOException e) {
            // ignore
        } finally {
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return countries;
    }
}
