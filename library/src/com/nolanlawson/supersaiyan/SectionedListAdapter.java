package com.nolanlawson.supersaiyan;

import static com.nolanlawson.supersaiyan.util.ExceptionUtil.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nolanlawson.supersaiyan.util.ArrayUtil;

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
    private boolean showSectionTitles = true;
    private Sorting keySorting = Sorting.InputOrder;
    private Sorting valueSorting = Sorting.InputOrder;
    private Comparator<? super CharSequence> keyComparator;
    @SuppressWarnings("rawtypes")
    private Comparator valueComparator;
    

    private SectionTitleAdapter sectionTitleAdapter;
    private Map<CharSequence, List<Integer>> headersToSections;
    private int[] absoluteIndexToRelativeIndex;
    private boolean[] absoluteIndexToIsHeader;
    private int[] sectionsToPositions;
    private int[] positionsToSections;
    private BaseAdapter subAdapter;

    @SuppressWarnings("rawtypes")
    private Sectionizer sectionizer;
    @SuppressWarnings("rawtypes")
    private MultipleSectionizer multipleSectionizer;
    private SectionIndexer sectionIndexer;
    
    /**
     * Create a new separated list adapter, i.e. an adapter with a title and a
     * body for each section.
     * 
     * @param context
     */
    private SectionedListAdapter(Context context) {
        this.sectionTitleAdapter = new SectionTitleAdapter(context, R.layout.list_header);
    }
    
    /**
     * Getters and setters
     * @return
     */
    
    public boolean isShowSectionOverlays() {
        return showSectionOverlays;
    }
    
    public boolean isShowListTitles() {
        return showSectionTitles;
    }

    /**
     * If false, then we won't show the rectangular section overlays.  True by default.
     * 
     * @param showSectionOverlays
     */
    public void setShowSectionOverlays(boolean showSectionOverlays) {
        this.showSectionOverlays = showSectionOverlays;
    }
    
    /**
     * If false, then we won't show the in-line list titles.  True by default.
     * 
     * @param showSectionOverlays
     */
    public void setShowSectionTitles(boolean showSectionTitles) {
        this.showSectionTitles = showSectionTitles;
    }
    
    public BaseAdapter getSubAdapter() {
        return subAdapter;
    }

    public Sectionizer<?> getSectionizer() {
        return sectionizer;
    }

    public Sorting getKeySorting() {
        return keySorting;
    }

    /**
     * Set the key sorting associated with this SectionedListAdapter.  If you use Explicit, we assume you're
     * also calling setKeyComparator().
     * @param keySorting
     */
    public void setKeySorting(Sorting keySorting) {
        this.keySorting = keySorting;
    }

    public Sorting getValueSorting() {
        return valueSorting;
    }

    /**
     * Set the value sorting associated with this SectionedListAdapter.  If you use Explicit, we assume you're
     * also calling setValueComparator().  If you use Natural, we assume that the values implement Comparable.
     * 
     * @param valueSorting
     */
    public void setValueSorting(Sorting valueSorting) {
        this.valueSorting = valueSorting;
    }

    public Comparator<? super CharSequence> getKeyComparator() {
        return keyComparator;
    }

    /**
     * Sets the comparator to use with Sorting.Explicit. 
     * Make sure you've previously set setKeySorting(Sorting.Explicit)!
     * 
     * @param keyComparator
     */
    public void setKeyComparator(Comparator<? super CharSequence> keyComparator) {
        this.keyComparator = keyComparator;
    }

    public Comparator<?> getValueComparator() {
        return valueComparator;
    }
    
    /**
     * Sets the comparator to use with Sorting.Explicit. 
     * Make sure you've previously set setValueSorting(Sorting.Explicit)!
     * 
     * @param valueComparator
     */
    public void setValueComparator(Comparator<?> valueComparator) {
        this.valueComparator = valueComparator;
    }

    public void setSubAdapter(BaseAdapter subAdapter) {
        this.subAdapter = subAdapter;
    }

    public void setSectionizer(Sectionizer<?> sectionizer) {
        this.sectionizer = sectionizer;
        this.multipleSectionizer = null;
    }
    
    public MultipleSectionizer<?> getMultipleSectionizer() {
        return multipleSectionizer;
    }

    /**
     * Sets the multiple sectionizer, which is used instead of the regular sectionizer, if useMultipleSections
     * is true.
     * 
     * @param multipleSectionizer
     */
    public void setMultipleSectionizer(MultipleSectionizer<?> multipleSectionizer) {
        this.multipleSectionizer = multipleSectionizer;
        this.sectionizer = null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        refresh();
    }
    
    private void refresh() {
        // this is called when the adapter is first built or its configuration is changed, so it's a good time
        // to throw some informative exceptions
        checkNotNull(subAdapter, "subAdapter cannot be null! Did you remember to call setSubAdapter()?");
        
        if (sectionizer == null && multipleSectionizer == null) {
            checkNotNull(sectionizer, 
                    "sectionizer cannot be null! Did you remember to call setSectionizer() or setMultipleSectionizer()?");
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
        
        return isHeader ? sectionTitleAdapter.getItem(subPosition) : subAdapter.getItem(subPosition);
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
            return sectionTitleAdapter.getView(subPosition, convertView, parent);
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
        
        
        int numDuplicates = 0;
        for (int i = 0; i < subAdapter.getCount(); i++) {
            Object element = subAdapter.getItem(i);
            
            Collection<CharSequence> sectionNames;
            
            if (multipleSectionizer != null) { // use the multiple sectionizer
                sectionNames = checkNotNull(multipleSectionizer.toSections(element), 
                        "multipleSectionizer.toSections() cannot return null!");
                if (sectionNames.isEmpty()) {
                    throw new IllegalStateException("multipleSectionizer.toSections() cannot return empty!");
                }
            } else { // user the regular sectionizer
                sectionNames = Collections.singleton(checkNotNull(sectionizer.toSection(element),
                        "sectionizer.toSection() cannot return null!"
                        ));
            }
            
            numDuplicates += sectionNames.size() - 1;
            
            for (CharSequence sectionName : sectionNames) {
            
                List<Integer> existingList = sections.get(sectionName);
                
                // multimap logic
                if (existingList == null) {
                    existingList = new ArrayList<Integer>();
                    sections.put(sectionName, existingList);
                }
                existingList.add(i);
            }
        }
        
        // if we're sorting values, then sort 'em!
        sortValuesIfNecessary(sections);
        
        // build up a list of all elements, including section titles
        int totalLength = (showSectionTitles ? sections.size() : 0) + subAdapter.getCount() + numDuplicates;
        absoluteIndexToRelativeIndex = ArrayUtil.recycleIfPossible(absoluteIndexToRelativeIndex, totalLength);
        absoluteIndexToIsHeader = ArrayUtil.recycleIfPossible(absoluteIndexToIsHeader, totalLength);
        sectionsToPositions = ArrayUtil.recycleIfPossible(sectionsToPositions, sections.size());
        positionsToSections = ArrayUtil.recycleIfPossible(positionsToSections, totalLength);
        
        headersToSections = sections;
        int counter = 0;
        int headerCounter = 0;
        for (List<Integer> sectionContent : sections.values()) {
            
            sectionsToPositions[headerCounter] = counter;
            positionsToSections[counter] = headerCounter;
            
            if (showSectionTitles) {
                absoluteIndexToRelativeIndex[counter] = headerCounter;
                absoluteIndexToIsHeader[counter] = true;
                counter++;
            }
            
            for (Integer element : sectionContent) {
                absoluteIndexToRelativeIndex[counter] = element;
                absoluteIndexToIsHeader[counter] = false;
                counter++;
            }
            
            headerCounter++;
        }
        sectionTitleAdapter.clear();
        for (CharSequence section : sections.keySet()) {
            sectionTitleAdapter.add(section);
        }
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
            return EMPTY_SECTION_INDEXER;
        }
        
        Object[] sectionNamesArray = headersToSections.keySet().toArray();

        return new BasicSectionIndexer(sectionNamesArray, sectionsToPositions, positionsToSections);
    }
    
    private static class BasicSectionIndexer implements SectionIndexer {

        private int[] sectionsToPositions;
        private int[] positionsToSections;
        private Object[] sections;
        
        private BasicSectionIndexer(Object[] sections, int[] sectionsToPositions, int[] positionsToSections) {
            this.sectionsToPositions = sectionsToPositions;
            this.positionsToSections = positionsToSections;
            this.sections = sections;
        }

        @Override
        public Object[] getSections() {
            return sections;
        }

        @Override
        public int getSectionForPosition(int position) {
            return positionsToSections[position];
        }

        @Override
        public int getPositionForSection(int section) {
            return sectionsToPositions[section];
        }
    }
    
    private static final SectionIndexer EMPTY_SECTION_INDEXER = new SectionIndexer() {

        private Object[] empty = {};
        
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
    
    public static enum Sorting {
        /**
         * Uses the same order that elements are added to the SectionedListAdapter.
         */
        InputOrder,
        /**
         * Casts objects to Comparable and compares them that way.
         */
        Natural,
        /**
         * Uses an explicit Comparator function to compare objects.
         */
        Explicit;
    }
    
    public static class Builder<T extends BaseAdapter> {

        private SectionedListAdapter<T> adapter;

        private Builder(Context context, T subAdapter) {
            adapter = new SectionedListAdapter<T>(context);
            adapter.setSubAdapter(subAdapter);
        }
        
        /**
         * Start a new SectionedListAdapter.Builder chain with the given context.
         * @param context
         * @param subAdapter Set the subAdapter associated with the SectionedListAdapter, i.e. the adapter to use for all the list
         * items that aren't section headers (i.e. the section content).
         */
        public static <T extends BaseAdapter> Builder<T> create(Context context, T subAdapter) {
            return new Builder<T>(context, subAdapter);
        }

        /**
         * Hides the section overlays, i.e. the rectangular overlays with the names of the sections.  By 
         * default, they are always shown.
         * @return
         */
        public SectionedListAdapter.Builder<T> hideSectionOverlays() {
            adapter.setShowSectionOverlays(false);
            return this;
        }
        
        /**
         * Hides the section titles, i.e. the in-line list titles for each section.  By 
         * default, they are always shown.
         * @return
         */
        public SectionedListAdapter.Builder<T> hideSectionTitles() {
            adapter.setShowSectionTitles(false);
            return this;
        }
        
        /**
         * Set the subAdapter associated with the SectionedListAdapter, i.e. the adapter to use for all the list
         * items that aren't section headers (i.e. the section content).
         * 
         * @param subAdapter
         * @return
         */
        public SectionedListAdapter.Builder<T> setSubAdapter(T subAdapter) {
            adapter.setSubAdapter(subAdapter);
            return this;
        }
        
        /**
         * Same as setSectionizer(), but allows list items to be listed in multiple sections.
         * 
         * @see {@link com.nolanlawson.supersaiyan.Sectionizers} for some commonly-used sectionizers
         * @param sectionizer
         * @return
         */
        public SectionedListAdapter.Builder<T> setMultipleSectionizer(MultipleSectionizer<?> sectionizer) {
            adapter.setMultipleSectionizer(sectionizer);
            return this;
        }        
        /**
         * Set the function that will be called when we need to figure out the section for a given list item.
         * 
         * @see {@link com.nolanlawson.supersaiyan.Sectionizers} for some commonly-used sectionizers
         * @param sectionizer
         * @return
         */
        public SectionedListAdapter.Builder<T> setSectionizer(Sectionizer<?> sectionizer) {
            adapter.setSectionizer(sectionizer);
            return this;
        }
        
        /**
         * Sort the keys, i.e. sort the section titles by their string values.
         * @return
         */
        public SectionedListAdapter.Builder<T> sortKeys() {
            adapter.keySorting = Sorting.Natural;
            return this;
        }
        
        /**
         * Sort the values, i.e. sort the contents of each section.  This assumes that the objects in your
         * subAdapter implement Comparable (if not, we'll throw an exception).
         * @return
         */
        public SectionedListAdapter.Builder<T> sortValues() {
            adapter.valueSorting = Sorting.Natural;
            return this;
        }
        
        /**
         * Sort the keys, i.e. the section titles, using the given comparator.
         * 
         * @see {@link java.lang.String.CASE_INSENSITIVE_ORDER} if you want case-insensitive ordering
         * @param keyComparator
         * @return
         */
        public SectionedListAdapter.Builder<T> sortKeys(Comparator<? super CharSequence> keyComparator) {
            adapter.keySorting = Sorting.Explicit;
            adapter.keyComparator = keyComparator;
            return this;
        }
        
        /**
         * Sort the values, i.e. the contents of each section, using the given comparator.  Assumes that the objects
         * in your subAdapter really do correspond to the type used in the Comparator, else a ClassCastException
         * will be thrown.
         * 
         * @param valueComparator
         * @return
         */
        public SectionedListAdapter.Builder<T> sortValues(Comparator<?> valueComparator) {
            adapter.valueSorting = Sorting.Explicit;
            adapter.valueComparator = valueComparator;
            return this;
        }
        
        /**
         * End the build chain and return the built SectionedListAdapter.
         * @return
         */
        public SectionedListAdapter<T> build() {
            adapter.refresh();
            return adapter;
        }
    }
}
