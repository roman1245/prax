package kandrac.xyz.library.net;


import com.squareup.okhttp.OkHttpClient;

import kandrac.xyz.library.BuildConfig;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Class that holds OkHttp and Retrofit base functionality. Basically is only needed for initializing
 * and accessing Api ({@link GoogleBooksApi}). This can be done via {@link #init()} method.
 * <p/>
 * Created by VizGhar on 29.10.2015.
 */
public class OkHttpConfigurator {

    private static OkHttpConfigurator instance;

    private GoogleBooksApi mApi;

    private OkHttpConfigurator() {

    }

    public static OkHttpConfigurator getInstance() {
        return instance == null ? instance = new OkHttpConfigurator() : instance;
    }

    /**
     * Initialize OkHttp and retrofit
     *
     * @return api for HTTP requests
     */
    public GoogleBooksApi init() {
        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BOOKS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        mApi = retrofit.create(GoogleBooksApi.class);
        return mApi;
    }

    /**
     * @return api for HTTP requests
     */
    public GoogleBooksApi getApi() {
        return mApi;
    }
}
