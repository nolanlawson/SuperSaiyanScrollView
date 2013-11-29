package com.example.example2.data;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.example2.R;

public class PocketMonsterAdapter extends ArrayAdapter<PocketMonster> {
    
    private static final int LAYOUT_ID = R.layout.pocket_monster_item;

    private Map<String, GradientDrawable> typesToBackgrounds = new HashMap<String, GradientDrawable>();
    
    public PocketMonsterAdapter(Context context, List<PocketMonster> objects) {
        super(context, LAYOUT_ID, objects);
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(LAYOUT_ID, parent, false);
        }
        
        TextView nameTV = (TextView) view.findViewById(R.id.name);
        TextView uniqueIdTV = (TextView) view.findViewById(R.id.unique_id);
        TextView type1TV = (TextView) view.findViewById(R.id.type1);
        TextView type2TV = (TextView) view.findViewById(R.id.type2);
        
        PocketMonster monster = (PocketMonster)getItem(position);
        
        nameTV.setText(monster.getName());
        uniqueIdTV.setText(monster.getUniqueId());
        type1TV.setText(monster.getType1());
        styleType(type1TV, monster.getType1());
        
        if (!TextUtils.isEmpty(monster.getType2())) {
            // monster has two types
            type2TV.setVisibility(View.VISIBLE);
            type2TV.setText(monster.getType2());
            styleType(type2TV, monster.getType2());
        } else {
            // monsters has one type
            type2TV.setVisibility(View.INVISIBLE);
        }
        return view;
    }
    
    @SuppressWarnings("deprecation")
    private void styleType(TextView textView, String type) {
        
        Resources resources = getContext().getResources();
        // choose a nice color for this type based on what it is
        
        GradientDrawable background = typesToBackgrounds.get(type);
        if (background == null) {
            String colorName = "type_color_" + type.toLowerCase(Locale.US);
            int colorId = resources.getIdentifier(colorName, "color", getContext().getPackageName());
            background = (GradientDrawable)resources.getDrawable(R.drawable.type_background).mutate();
            background.setColor(resources.getColor(colorId));
            typesToBackgrounds.put(type, background);
        }
        
        textView.setBackgroundDrawable(background);
    }
}
