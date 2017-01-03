/*
 * Copyright 2014. Alashov Berkeli
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.alashow.datmusic.rest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import tm.alashow.datmusic.Config;

/**
 * Created by alashov on 27/02/16.
 */
public abstract class ApiService {

    //base url path
    public static String API_BASE_URL = Config.API_ENDPOINT;

    //okHttp goes here
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.HOURS)
        .writeTimeout(1, TimeUnit.HOURS)
        .addInterceptor(
            new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        );

    //converters
    private static Converter.Factory JACKSON_CONVERTER = JacksonConverterFactory.create();
    private static Converter.Factory SCALARS_CONVERTER = ScalarsConverterFactory.create();

    /**
     * Get configured Retrofit.Builder
     *
     * @return builder
     */
    public static Retrofit.Builder getBuilder() {
        return new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(httpClient.build());
    }

    public static <S> S createService(Class<S> serviceClass, Converter.Factory factory) {
        Retrofit retrofit = getBuilder()
            .addConverterFactory(factory)
            .build();
        return retrofit.create(serviceClass);
    }

    /**
     * Create {@link ApiClient} with given converter factory
     *
     * @param factory Converter Factory
     * @return api client
     */
    public static ApiClient getClient(Converter.Factory factory) {
        return createService(ApiClient.class, factory);
    }

    /**
     * Create {@link ApiClient} with Jackson (json) converter factory
     *
     * @return api client
     */
    public static ApiClient getClientJackson() {
        return createService(ApiClient.class, JACKSON_CONVERTER);
    }

    /**
     * Create {@link ApiClient} with Jackson (json) converter factory
     *
     * @return api client
     */
    public static ApiClient getClientScalars() {
        return createService(ApiClient.class, SCALARS_CONVERTER);
    }
}
