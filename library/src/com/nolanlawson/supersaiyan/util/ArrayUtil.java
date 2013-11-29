package com.nolanlawson.supersaiyan.util;

public class ArrayUtil {

    public static int[] recycleIfPossible(int[] input, int size) {
        return input != null && input.length == size ? input : new int[size];
    }
    
    public static boolean[] recycleIfPossible(boolean[] input, int size) {
        return input != null && input.length == size ? input : new boolean[size];
    }    
}
