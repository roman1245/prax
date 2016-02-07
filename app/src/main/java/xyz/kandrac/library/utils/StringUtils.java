package xyz.kandrac.library.utils;

/**
 * Created by VizGhar on 7.2.2016.
 */
public class StringUtils {

    public static String arrayToString(String[] strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(strings[i]);
        }
        return sb.toString();
    }
}
