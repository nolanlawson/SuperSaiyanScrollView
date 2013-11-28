package com.example.example1.util;

/**
 * Simple input/output function for functional programming.
 * @author nolan
 *
 * @param <E>
 * @param <T>
 */
public interface Function<E,T> {

    public T apply(E input);
    
}
