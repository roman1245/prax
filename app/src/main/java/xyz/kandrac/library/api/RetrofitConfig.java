package xyz.kandrac.library.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.kandrac.library.BuildConfig;
import xyz.kandrac.library.api.google.GoogleApi;
import xyz.kandrac.library.api.library.LibraryApi;

/**
 * Created by kandrac on 04/02/16.
 */
public final class RetrofitConfig {

    private static RetrofitConfig instance;
    private GoogleApi googleApi;
    private LibraryApi libraryApi;

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

        Retrofit google = new Retrofit.Builder()
                .baseUrl(BuildConfig.GOOGLE_BOOKS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        googleApi = google.create(GoogleApi.class);

        Retrofit library = new Retrofit.Builder()
                .baseUrl(BuildConfig.LIBRARY_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        libraryApi = library.create(LibraryApi.class);
    }

    public GoogleApi getGoogleApi() {
        return googleApi;
    }

    public LibraryApi getLibraryApi() {
        return libraryApi;
    }

}
