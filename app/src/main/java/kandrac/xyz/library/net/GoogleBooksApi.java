package kandrac.xyz.library.net;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by VizGhar on 2.11.2015.
 */
public interface GoogleBooksApi {

    /**
     * Tu use it with isb pass something like "isbn:1234567891012"
     * @param isbn
     * @return
     */
    @GET("/books/v1/volumes")
    Call<BookResponse> getBooksByIsbn(@Query("q") String isbn);
}
