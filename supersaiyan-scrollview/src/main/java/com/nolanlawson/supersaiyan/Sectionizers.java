package com.nolanlawson.supersaiyan;

/**
 * Common Sectionizers that people might want to use.
 * @author nolan
 *
 */
public class Sectionizers {
    
    /**
     * A basic sectionizer that uses the capitalized first letter of the toString() of the given object.  Spaces,
     * numbers, and symbols are all converted to the character '#' (iOS style).
     */
    public static final Sectionizer<Object> UsingFirstLetterOfToString = new Sectionizer<Object>() {
        
        @Override
        public CharSequence toSection(Object input) {
            if (input != null) {
                String asStr = input.toString();
                if (asStr.length() > 0) {
                    char firstChar = Character.toUpperCase(asStr.charAt(0));
                    if (firstChar >= 'A' && firstChar <= 'Z') {
                        return Character.toString(firstChar);
                    }
                }
            }
            return "#";
        }
    };
    
    /**
     * A sectionizer that simply uses the toString() of the given object, or "null" if it's null.
     */
    public static final Sectionizer<Object> UsingToString = new Sectionizer<Object>() {
        
        @Override
        public CharSequence toSection(Object input) {
            return String.valueOf(input);
        }
    };
}
