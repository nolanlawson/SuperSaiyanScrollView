package com.nolanlawson.supersaiyan;

import java.util.Collection;

/**
 * A sectionizer that converts an object into multiple sections.
 * @author nolan
 *
 * @param <E>
 */
public interface MultipleSectionizer<E> {

    /**
     * Convert an object into one or more sections.  It's assumed that this list will be non-null and non-empty.
     * @param input
     * @return
     */
    public Collection<? extends CharSequence> toSections(E input);
    
}
