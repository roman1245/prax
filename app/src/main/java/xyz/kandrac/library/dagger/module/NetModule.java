package xyz.kandrac.library.dagger.module;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.kandrac.library.BuildConfig;

/**
 * Module for providing Network operations
 * </br>
 * Created by jan on 5.10.2016.
 */
@Module
public final class NetModule {

    private String mBaseUrl;

    // Constructor needs one parameter to instantiate.
    public NetModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // setup logging
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            ).build();
        }

        return builder.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(mBaseUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

}
