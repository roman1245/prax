package xyz.kandrac.library;

/**
 * Support back navigation in Views or Fragments
 * <p>
 * Created by jan on 22.1.2017.
 */
public interface BackPressable {

    /**
     * @return true if handled
     */
    boolean onBackPressed();
}
