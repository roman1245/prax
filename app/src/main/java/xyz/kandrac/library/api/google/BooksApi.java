package xyz.kandrac.library.api.google;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by kandrac on 04/02/16.
 */
public interface BooksApi {

    @GET("/books/v1/volumes")
    Call<BooksResponse> getBookByIsbn(@Query("q") String isbn);
}
