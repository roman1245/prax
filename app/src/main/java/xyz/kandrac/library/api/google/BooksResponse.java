package xyz.kandrac.library.api.google;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kandrac on 04/02/16.
 */
public class BooksResponse {

    @SerializedName("items")
    public Book[] books;

    public class Book {

        @SerializedName("volumeInfo")
        public VolumeInfo volumeInfo;

        public class VolumeInfo {

            @SerializedName("title")
            public String title;

            @SerializedName("subtitle")
            public String subtitle;

            @SerializedName("publisher")
            public String publisher;

            @SerializedName("authors")
            public String[] authors;
        }
    }
}
