package xyz.kandrac.library.api.library;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by kandrac on 04/02/16.
 */
@SuppressWarnings("SpellCheckingInspection")
public interface LibraryApi {

    @GET("/library/api/book/{isbn}")
    Call<LibraryResponse> getBookByIsbn(@Path("isbn") String isbn);
}
