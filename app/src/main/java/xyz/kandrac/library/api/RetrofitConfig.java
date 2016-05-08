package xyz.kandrac.library.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import xyz.kandrac.library.BuildConfig;
import xyz.kandrac.library.api.google.BooksApi;

/**
 * Created by kandrac on 04/02/16.
 */
public final class RetrofitConfig {

    private static RetrofitConfig instance;
    private BooksApi booksApi;

    public static RetrofitConfig getInstance() {
        return (instance == null) ? instance = new RetrofitConfig() : instance;
    }

    private RetrofitConfig() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // setup logging
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            ).build();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.GOOGLE_BOOKS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        booksApi = retrofit.create(BooksApi.class);
    }

    public BooksApi getBooksApi() {
        return booksApi;
    }

}
