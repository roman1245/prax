package xyz.kandrac.library.api.library;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by kandrac on 04/02/16.
 */
public interface LibraryApi {

    @GET("/libraryapi")
    Call<LibraryResponse> getBookByIsbn(@Query("isbn") String isbn);
}
