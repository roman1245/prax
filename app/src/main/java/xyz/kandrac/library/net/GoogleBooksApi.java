package xyz.kandrac.library.net;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Retrofit client API for communication with Google Books API.
 * Created by VizGhar on 2.11.2015.
 */
public interface GoogleBooksApi {

    /**
     * Search Google Books Api for books based on given query. To use this method properly, use
     * either {@link GoogleBooksUtils#getSearchQuery(int, String)} method or simply pass you query
     * text. To see how to build correct query, please have a look at
     * <a href="https://developers.google.com/books/docs/v1/using?hl=en">Books Api Specification</a>
     *
     * @param query to search for
     * @return retrofit call
     */
    @GET("/books/v1/volumes")
    Call<BookResponse> getBooksByQuery(@Query("q") String query);
}
