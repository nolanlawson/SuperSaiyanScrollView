package com.nolanlawson.supersaiyan.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TypeCheckingArrayAdapter<T> extends ArrayAdapter<T> {

	private static final String UNIQUE_TAG = "MyTag1";
	
	int mFieldId;
	int mResource;
	
	public TypeCheckingArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		init(resource, textViewResourceId);
	}

	public TypeCheckingArrayAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
		init(resource, textViewResourceId);
	}

	public TypeCheckingArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		init(resource, textViewResourceId);
	}

	public TypeCheckingArrayAdapter(Context context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
		init(textViewResourceId, 0);
	}

	public TypeCheckingArrayAdapter(Context context, int textViewResourceId,
			T[] objects) {
		super(context, textViewResourceId, objects);
		init(textViewResourceId, 0);
	}

	public TypeCheckingArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		init(textViewResourceId, 0);
	}
	
    private void init(int resource, int textViewResourceId) {
        mResource = resource;
        mFieldId = textViewResourceId;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// a ClassCastException gets thrown because I'm dynamically changing the types of the items
		// in the ListView.  The code that throws this exception is just an optimization anyway, so 
		// I can get around it by using the tag to check
		return createViewFromResource(position, convertView, parent, mResource);
	}

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        View view;
        TextView text;

        // checking the unique tag confirms that it was US that put the tag there, and therefore it's okay
        // to re-use the view as an optimization
        if (convertView == null || convertView.getTag() == null || !convertView.getTag().equals(UNIQUE_TAG)) {
        	LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
            view.setTag(UNIQUE_TAG);
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        text.setText(getItem(position).toString());

        return view;
    }	
	
	

}
