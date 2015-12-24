package xyz.kandrac.library;

/**
 * Implement this interface in all classes that provides searching capabilities (For example
 * {@code ListView}s that requires filtering based on search).
 * <p/>
 * Created by VizGhar on 23.10.2015.
 */
public interface Searchable {

    /**
     * Invoke search based on given {@code query}
     *
     * @param query based on which searching is invoked
     * @return true if search is handled
     */
    boolean requestSearch(String query);
}
