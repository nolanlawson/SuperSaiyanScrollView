package com.nolanlawson.supersaiyan;

/**
 * Function that converts an object to its section name.
 * @author nolan
 *
 * @param <E>
 */
public interface Sectionizer<E> {

    /**
     * Provide a section name based on the input.  The output cannot be null!
     * @param input
     * @return
     */
    public CharSequence toSection(E input);
    
}
