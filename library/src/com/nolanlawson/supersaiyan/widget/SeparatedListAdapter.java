package com.nolanlawson.supersaiyan.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nolanlawson.supersaiyan.R;

/**
 * ListAdapter with headers for each section.  Originally taken from Jeff Sharkey: 
 * http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * 
 * @author nolan
 *
 */
public class SeparatedListAdapter<T extends BaseAdapter> extends BaseAdapter implements SectionIndexer {
	
	// How many sections to require in order to show the fast scroll overlays
	private static final int MIN_NUM_SECTIONS_FOR_SECTION_OVERLAYS = 2;
	// How many items to require in order to show the fast scroll overlays
	private static final int MIN_NUM_ITEMS_FOR_SECTION_OVERLAYS = 10;
	
	public static final int TYPE_SECTION_HEADER = 0;
	public static final int TYPE_SECTION_CONTENT = 1;
	public static final int NUM_TYPES = 2;
	
	public Map<String,T> sections = new LinkedHashMap<String,T>();
	public TypeCheckingArrayAdapter<String> headers;

	private SectionIndexer sectionIndexer;

	public SeparatedListAdapter(Context context) {
		headers = new TypeCheckingArrayAdapter<String>(context, R.layout.list_header);
	}
	
	public T getSection(int position) {
		return sections.get(headers.getItem(position));
	}
	
	public String getSectionName(int position) {
		return headers.getItem(position);
	}
	
	public Collection<T> getSubAdapters() {
		return sections.values();
	}
	
	public void addSection(String section, T adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}
	
	/**
	 * 
	 * Add a section to the specified index of the LinkedHashMap.
	 * @param section
	 * @param adapter
	 */
	public void insertSection(String section, int i, T adapter) {
		this.headers.insert(section, i);
		
		// lame solution... create a new linkedhashmap and fit it in at the index
		Map<String,T> newSections = new LinkedHashMap<String,T>();
		
		if (i == 0) {
			newSections.put(section, adapter);
		}
		int count = 0;
		for (String oldSection : sections.keySet()) {
			newSections.put(oldSection, sections.get(oldSection));
			if (i == ++count) {
				newSections.put(section, adapter);
			}
		}
		sections = newSections;
	}
	
	public ArrayAdapter<String> getSectionHeaders() {
		return headers;
	}
	

	public Object getItem(int position) {
		for(Entry<String, T> entry : sections.entrySet()) {
			String section = entry.getKey();
			T adapter = entry.getValue();
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return section;
			if(position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for(T adapter : this.sections.values()) {
			total += adapter.getCount() + 1;
		}
		return total;
	}

	public int getViewTypeCount() {
		return NUM_TYPES;
	}

	public int getItemViewType(int position) {
		for(T adapter : sections.values()) {
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return TYPE_SECTION_HEADER;
			if(position < size) return TYPE_SECTION_CONTENT;

			// otherwise jump into next section
			position -= size;
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {

		if (getItemViewType(position) == TYPE_SECTION_HEADER) {
			return false;
		} else {
			for(T adapter : this.sections.values()) {
				int size = adapter.getCount() + 1;

				// check if position inside this section
				if(position < size) return adapter.isEnabled(position - 1);

				// otherwise jump into next section
				position -= size;
			}
			return false;
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(T adapter : this.sections.values()) {
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return headers.getView(sectionnum, convertView, parent);
			if(position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}
	
	/**
	 * Return a LinkedHashMap showing all the sections and their sub-adapters.
	 * @return
	 */
	public Map<String, T> getSectionsMap() {
		return sections;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeSection(String section) {
		headers.remove(section);
		sections.remove(section);
	}
	
	public void refreshSections() {
		sectionIndexer = null;
		getSections();
	}

	
	@Override
	public int getPositionForSection(int section) {
		return getSectionIndexer().getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return getSectionIndexer().getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return getSectionIndexer().getSections();
	}
	
	private SectionIndexer getSectionIndexer() {
		if (sectionIndexer == null) {
			sectionIndexer = createSectionIndexer();
		}
		return sectionIndexer;
	}
	
	/**
	 * Build up the section indexer, which just uses the section names.
	 * @return
	 */
	private SectionIndexer createSectionIndexer() {
		
		if (!enoughToShowOverlays()) {
			return createEmptySectionIndexer();
		}
		
		List<String> sectionNames = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();
		
		int runningCount = 0;
		for (Entry<String,T> entry : sections.entrySet()) {
			String section = entry.getKey();
			T subAdapter = entry.getValue();
			
			sectionNames.add(section);
			sectionsToPositions.add(runningCount);
			
			int size = subAdapter.getCount() + 1;
			for (int i = 0; i < size; i++) {
				positionsToSections.add(sectionNames.size() - 1);
			}
			runningCount += size;
		}
		
		final Object[] sectionNamesArray = sectionNames.toArray();
		
		return new SectionIndexer() {
			
			@Override
			public Object[] getSections() {
				return sectionNamesArray;
			}
			
			@Override
			public int getSectionForPosition(int position) {
				return positionsToSections.get(position);
			}
			
			@Override
			public int getPositionForSection(int section) {
				return sectionsToPositions.get(section);
			}
		};
	}
		
	
	private SectionIndexer createEmptySectionIndexer() {
		final Object[] empty = {};
		return new SectionIndexer() {
			
			@Override
			public Object[] getSections() {
				return empty;
			}
			
			@Override
			public int getSectionForPosition(int position) {
				return 0;
			}
			
			@Override
			public int getPositionForSection(int section) {
				return 0;
			}
		};
	}
	
	private boolean enoughToShowOverlays() {
		int numHeaders = headers.getCount();
		return numHeaders >= MIN_NUM_SECTIONS_FOR_SECTION_OVERLAYS && 
				(getCount() - numHeaders) >= MIN_NUM_ITEMS_FOR_SECTION_OVERLAYS;
	}
}
