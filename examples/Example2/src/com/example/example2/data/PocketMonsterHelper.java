package com.example.example2.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.example2.R;

public class PocketMonsterHelper {
    public static List<PocketMonster> readInPocketMonsters(Context context) {
        List<PocketMonster> monsters = new ArrayList<PocketMonster>();
        BufferedReader buff = null;
        try {
            buff = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.pokemon), 
                    Charset.forName("UTF-8")));
            while (buff.ready()) {
                String[] line = buff.readLine().split(",");

                PocketMonster monster = new PocketMonster();
                monster.setUniqueId(line[0]);
                monster.setNationalDexNumber(Integer.parseInt(line[0].replaceAll("[^0-9]", "")));
                monster.setName(line[1]);
                monster.setType1(line[2]);
                monster.setType2(line.length > 3 ? line[3] : null);

                monsters.add(monster);
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
        return monsters;
    }
}
