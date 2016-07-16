package xyz.kandrac.library.billing.util;

import xyz.kandrac.library.BuildConfig;

/**
 * Created by Jan Kandrac on 16.7.2016.
 */
public class IABKeyEncoder {

    private static String MIDDLE = BuildConfig.APP_PUBLIC_KEY;

    private static String START = "NJJCJkBOChlriljH:x1";
    private static String END = "BAQADIwkHcpU8Ubv++";

    public static String getKey() {
        return decrementLetters(START) + MIDDLE + reverseString(END);
    }

    private static String decrementLetters(String text) {
        StringBuilder sb = new StringBuilder();
        char[] x = text.toCharArray();
        for (char dec : x) {
            sb.append((char)(dec - 1));
        }
        return sb.toString();
    }

    private static String reverseString(String text) {
        StringBuilder sb = new StringBuilder();
        char[] x = text.toCharArray();
        for (int i = x.length - 1; i >= 0; i--) {
            sb.append(x[i]);
        }
        return sb.toString();
    }
}
