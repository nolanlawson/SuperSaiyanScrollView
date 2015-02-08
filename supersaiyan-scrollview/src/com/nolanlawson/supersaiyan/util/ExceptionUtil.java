package com.nolanlawson.supersaiyan.util;

public class ExceptionUtil {

    public static <T> T checkNotNull(T input, String messageIfNull) {
        if (input == null) {
            throw new NullPointerException(messageIfNull);
        }
        return input;
    }
}
