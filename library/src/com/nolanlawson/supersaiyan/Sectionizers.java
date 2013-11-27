package com.nolanlawson.supersaiyan;

/**
 * Common Sectionizers that people might want to use.
 * @author nolan
 *
 */
public class Sectionizers {
    
    public static final Sectionizer<Object> UsingFirstLetterOfToString = new Sectionizer<Object>() {
        
        @Override
        public CharSequence toSection(Object input) {
            return input.toString().substring(0, 1);
        }
    };
}
