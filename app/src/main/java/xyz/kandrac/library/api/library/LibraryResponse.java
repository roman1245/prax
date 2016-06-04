package xyz.kandrac.library.api.library;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kandrac on 04/02/16.
 */
public class LibraryResponse {

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("authors")
    public String authors;

    @SerializedName("publisher")
    public String publisher;

    @SerializedName("pages")
    public String pages;

    @SerializedName("published")
    public String published;
}
