package com.nolanlawson.supersaiyan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nolanlawson.supersaiyan.util.StringUtil;

/**
 * ListAdapter with headers for each section. Originally taken from Jeff Sharkey:
 * http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * 
 * Do not try to directly instantiate this class; use the Builder() class instead.
 * 
 * @author nolan
 * 
 */
public class SectionedListAdapter< T extends BaseAdapter> extends BaseAdapter implements SectionIndexer {

    public static final int TYPE_SECTION_HEADER = 0;
    
    private boolean showSectionOverlays = true;
    private Sorting keySorting = Sorting.InputOrder;
    private Sorting valueSorting = Sorting.InputOrder;
    private Comparator<? super CharSequence> keyComparator;
    @SuppressWarnings("rawtypes")
    private Comparator valueComparator;
    

    private TypeCheckingArrayAdapter<CharSequence> headers;
    private Map<CharSequence, List<Integer>> headersToSections;
    private int[] absoluteIndexToRelativeIndex;
    private boolean[] absoluteIndexToIsHeader;
    private BaseAdapter subAdapter;

    @SuppressWarnings("rawtypes")
    private Sectionizer sectionizer;
    private SectionIndexer sectionIndexer;
    
    

    /**
     * Create a new separated list adapter, i.e. an adapter with a title and a
     * body for each section.
     * 
     * @param context
     */
    private SectionedListAdapter(Context context) {
        this.headers = new TypeCheckingArrayAdapter<CharSequence>(context, R.layout.list_header);
    }
    
    private void setSubAdapter(T subAdapter) {
        this.subAdapter = subAdapter;
    }
    
    private void setSectionizer(Sectionizer<?> sectionizer) {
        this.sectionizer = sectionizer;
    }
    
    public boolean isShowSectionOverlays() {
        return showSectionOverlays;
    }

    public void setShowSectionOverlays(boolean showSectionOverlays) {
        this.showSectionOverlays = showSectionOverlays;
    }

    public BaseAdapter getSubAdapter() {
        return subAdapter;
    }

    public SectionIndexer getSectionIndexer() {
        return sectionIndexer;
    }

    public void setSectionIndexer(SectionIndexer sectionIndexer) {
        this.sectionIndexer = sectionIndexer;
    }

    public Sectionizer<?> getSectionizer() {
        return sectionizer;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        refresh();
    }
    
    private void refresh() {
        // this is called when the adapter is first built or its configuration is changed, so it's a good time
        // to throw some informative exceptions
        if (subAdapter == null) {
            throw new IllegalStateException("subAdapter cannot be null! Did you remember to call setSubAdapter()?");
        } else if (sectionizer == null) {
            throw new IllegalStateException("sectionizer cannot be null! Did you remember to call setSectionizer()?");
        }
        refreshSections();
        sectionIndexer = createSectionIndexer();
    }

    /**
     * Get the item at the specified position.  Either returns a CharSequence (in the case of section headers)
     * or the object that would normally be in the BaseAdapter (in the case of section content).
     */
    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= absoluteIndexToRelativeIndex.length) {
            return null;
        }
        int subPosition = absoluteIndexToRelativeIndex[position];
        boolean isHeader = absoluteIndexToIsHeader[position];
        
        return isHeader ? headers.getItem(subPosition) : subAdapter.getItem(subPosition);
    }

    @Override
    public int getCount() {
        return absoluteIndexToRelativeIndex.length;
    }

    @Override
    public int getViewTypeCount() {
        return 1 + subAdapter.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= absoluteIndexToIsHeader.length) {
            return -1;
        }
        boolean isHeader = absoluteIndexToIsHeader[position];
        if (isHeader) {
            return TYPE_SECTION_HEADER;
        }
        int subPosition = absoluteIndexToRelativeIndex[position];
        return 1 + subAdapter.getItemViewType(subPosition);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false; // section headers are not enabled
    }

    @Override
    public boolean isEnabled(int position) {

        if (getItemViewType(position) == TYPE_SECTION_HEADER) {
            // section headers are not enabled
            return false;
        } else {
            // section content may or may not be enabled
            int subPosition = absoluteIndexToRelativeIndex[position];
            return subAdapter.isEnabled(subPosition);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (position < 0 || position >= absoluteIndexToIsHeader.length) {
            return convertView; // error - do nothing
        }
        
        boolean isHeader = absoluteIndexToIsHeader[position];
        int subPosition = absoluteIndexToRelativeIndex[position];
        if (isHeader) {
            return headers.getView(subPosition, convertView, parent);
        } else {
            return subAdapter.getView(subPosition, convertView, parent);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPositionForSection(int section) {
        return sectionIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        return sectionIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
        return sectionIndexer.getSections();
    }

    /**
     * Called either when the adapter is first created, or when it's updated with new data or new sortings
     * of that data
     */
    
    @SuppressWarnings("unchecked")
    private void refreshSections() {
        
        Map<CharSequence, List<Integer>> sections = createSectionMap();
        
        for (int i = 0; i < subAdapter.getCount(); i++) {
            Object element = subAdapter.getItem(i);
            CharSequence section = StringUtil.nullToEmpty(sectionizer.toSection(element));
            
            List<Integer> existingList = sections.get(section);
            
            // multimap logic
            if (existingList == null) {
                existingList = new ArrayList<Integer>();
                sections.put(section, existingList);
            }
            existingList.add(i);
        }
        
        // if we're sorting values, then sort 'em!
        sortValuesIfNecessary(sections);
        
        // build up a list of all elements, including section titles
        int totalLength = sections.size() + subAdapter.getCount();
        absoluteIndexToRelativeIndex = new int[totalLength];
        absoluteIndexToIsHeader = new boolean[totalLength];
        headersToSections = sections;
        int counter = 0;
        int headerCounter = 0;
        for (List<Integer> sectionContent : sections.values()) {
            
            absoluteIndexToRelativeIndex[counter] = headerCounter;
            absoluteIndexToIsHeader[counter] = true;
            counter++;
            for (Integer element : sectionContent) {
                absoluteIndexToRelativeIndex[counter] = element;
                absoluteIndexToIsHeader[counter] = false;
                counter++;
            }
            headerCounter++;
        }
        headers.clear();
        headers.addAll(sections.keySet());
    }

    private Map<CharSequence, List<Integer>> createSectionMap() {
        switch (keySorting){
            case InputOrder:
            default:
                return new LinkedHashMap<CharSequence, List<Integer>>();
            case Natural:
                return new TreeMap<CharSequence, List<Integer>>();
            case Explicit:
                return new TreeMap<CharSequence, List<Integer>>(keyComparator);
        }
    }

    private void sortValuesIfNecessary(Map<CharSequence, List<Integer>> sections) {
        
        if (valueSorting == Sorting.InputOrder) {
            return; // not necessary
        }
            
        Comparator<Integer> comparator;
        if (valueSorting == Sorting.Natural) { // natural (i.e. comparable) ordering
            comparator = new Comparator<Integer>() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public int compare(Integer left, Integer right) {
                    try {
                        Object leftObj = subAdapter.getItem(left);
                        Object rightObj = subAdapter.getItem(right);
                        return ((Comparable)leftObj).compareTo(((Comparable)rightObj));
                    } catch (Exception e) {
                        throw new IllegalStateException("you cannot set sortValues() unless the objects " +
                                "are an instance of Comparable", e);
                    }
                }
            };
        } else { // explicit ordering, use the provided comparator
            comparator = new Comparator<Integer>() {

                @SuppressWarnings("unchecked")
                @Override
                public int compare(Integer left, Integer right) {
                    Object leftObj = subAdapter.getItem(left);
                    Object rightObj = subAdapter.getItem(right);
                    return valueComparator.compare(leftObj, rightObj);
                }
            };
        }
        
        for (List<Integer> sectionContent : sections.values()) {
            Collections.sort(sectionContent, comparator);
        }
    }

    /**
     * Build up the section indexer, which just uses the section names.
     * 
     * @return
     */
    private SectionIndexer createSectionIndexer() {

        if (!showSectionOverlays) {
            return createEmptySectionIndexer();
        }

        List<CharSequence> sectionNames = new ArrayList<CharSequence>();
        final List<Integer> sectionsToPositions = new ArrayList<Integer>();
        final List<Integer> positionsToSections = new ArrayList<Integer>();

        int runningCount = 0;
        for (Entry<CharSequence, List<Integer>> entry : headersToSections.entrySet()) {
            CharSequence section = entry.getKey();
            List<Integer> elementIndexes = entry.getValue();

            sectionNames.add(section);
            sectionsToPositions.add(runningCount);

            int size = elementIndexes.size() + 1;
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
    
    private static enum Sorting {
        InputOrder,
        Natural,
        Explicit;
    }
    
    public static class Builder<T extends BaseAdapter> {

        private SectionedListAdapter<T> result;

        public Builder(Context context) {
            result = new SectionedListAdapter<T>(context);
        }

        public SectionedListAdapter.Builder<T> setShowSectionOverlays(boolean showSectionOverlays) {
            result.setShowSectionOverlays(showSectionOverlays);
            return this;
        }
        
        public SectionedListAdapter.Builder<T> setSubAdapter(T subAdapter) {
            result.setSubAdapter(subAdapter);
            return this;
        }
        
        public SectionedListAdapter.Builder<T> setSectionizer(Sectionizer<?> sectionizer) {
            result.setSectionizer(sectionizer);
            return this;
        }
        
        public SectionedListAdapter.Builder<T> sortKeys() {
            result.keySorting = Sorting.Natural;
            return this;
        }
        public SectionedListAdapter.Builder<T> sortValues() {
            result.valueSorting = Sorting.Natural;
            return this;
        }
        public SectionedListAdapter.Builder<T> sortKeys(Comparator<? super CharSequence> keyComparator) {
            result.keyComparator = keyComparator;
            return this;
        }
        public SectionedListAdapter.Builder<T> sortValues(Comparator<?> valueComparator) {
            result.valueComparator = valueComparator;
            return this;
        }
        
        public SectionedListAdapter<T> build() {
            result.refresh();
            return result;
        }
    }
}
