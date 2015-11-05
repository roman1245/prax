package kandrac.xyz.library.net;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kandrac on 04/11/15.
 */
public class BookResponse {

    @SerializedName("totalItems")
    public int totalItems;

    @SerializedName("items")
    public Book[] books;

    public static class Book {

        @SerializedName("volumeInfo")
        public Info volumeInfo;

        public static class Info {
            @SerializedName("title")
            public String title;
            @SerializedName("publisher")
            public String publisher;
            @SerializedName("description")
            public String description;

            @Override
            public String toString() {
                return "Info{" +
                        "title='" + title + '\'' +
                        ", publisher='" + publisher + '\'' +
                        ", description='" + description + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Book{" +
                    "volumeInfo=" + volumeInfo.toString() +
                    '}';
        }
    }
}
