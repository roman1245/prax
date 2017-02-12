package xyz.kandrac.library.model.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by jan on 11.2.2017.
 */
@IgnoreExtraProperties
public class FirebaseFeedback {

    public static final String KEY_APPROVED = "approved";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TITLE = "title";
    public static final String KEY_VOTES = "votes";
    public static final String KEY_COMMENT = "comment";

    @PropertyName(KEY_APPROVED)
    public boolean approved;
    @PropertyName(KEY_AUTHOR)
    public String author;
    @PropertyName(KEY_DESCRIPTION)
    public String description;
    @PropertyName(KEY_TITLE)
    public String title;
    @PropertyName(KEY_VOTES)
    public int votes;
    @PropertyName(KEY_COMMENT)
    public String comment;

    public FirebaseFeedback() {

    }
}
