package xyz.kandrac.library.net;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tools that can be used in relation to Google Books API
 * <p/>
 * Created by VizGhar on 11.11.2015.
 */
public final class GoogleBooksUtils {

    /**
     * Return result for standard query not related to any particular field in book data
     */
    public static final int QUERY_STANDARD = 1;

    /**
     * Returns results where the text following this keyword is found in the title
     */
    private static final int QUERY_IN_TITLE = 2;

    /**
     * Returns results where the text following this keyword is found in the author
     */
    private static final int QUERY_IN_AUTHOR = 3;

    /**
     * Returns results where the text following this keyword is found in the publisher
     */
    private static final int QUERY_IN_PUBLISHER = 4;

    /**
     * Returns results where the text following this keyword is listed in the category list of the volume
     */
    private static final int QUERY_SUBJECT = 5;

    /**
     * Returns results where the text following this keyword is the ISBN number.
     */
    public static final int QUERY_ISBN = 6;

    /**
     * Returns results where the text following this keyword is the Library of Congress Control Number
     */
    public static final int QUERY_LCCN = 7;

    /**
     * Returns results where the text following this keyword is the Online Computer Library Center number
     */
    public static final int QUERY_OCLC = 8;

    @IntDef({QUERY_STANDARD, QUERY_IN_TITLE, QUERY_IN_AUTHOR, QUERY_IN_PUBLISHER,
            QUERY_SUBJECT, QUERY_ISBN, QUERY_LCCN, QUERY_OCLC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QueryMethod {
    }

    private GoogleBooksUtils() {
    }

    public static String getSearchQuery(@QueryMethod int method, String query) {
        String queryPrefix = "";
        switch (method) {
            case QUERY_IN_TITLE:
                queryPrefix = "intitle:";
                break;
            case QUERY_IN_AUTHOR:
                queryPrefix = "inauthor:";
                break;
            case QUERY_IN_PUBLISHER:
                queryPrefix = "inpublisher:";
                break;
            case QUERY_SUBJECT:
                queryPrefix = "subject:";
                break;
            case QUERY_ISBN:
                queryPrefix = "isbn:";
                break;
            case QUERY_LCCN:
                queryPrefix = "lccn:";
                break;
            case QUERY_OCLC:
                queryPrefix = "oclc:";
                break;
            default:
        }
        return queryPrefix + query;
    }
}
