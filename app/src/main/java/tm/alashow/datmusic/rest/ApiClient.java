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

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import tm.alashow.datmusic.model.Result;

/**
 * Created by alashov on 27/02/16.
 */
public interface ApiClient {

    @GET("search/?noCount=true&autocomplete=1")
    Call<Result> search(
            @Query("q") String query,
            @Query("sort") int sort,
            @Query("count") int count,
            @Query("performer_only") int performer,
            @Query("offset") int offset
    );

    @GET("search/?noCount=true&autocomplete=1")
    Call<Result> search(
            @Query("q") String query,
            @Query("sort") int sort,
            @Query("count") int count,
            @Query("performer_only") int performer,
            @Query("offset") int offset,
            @Query("captcha_key") String captchaKey,
            @Query("captcha_sid") long captchaSid
    );

    @GET("download.php?getBytes=true")
    Call<String> getBytes(
        @Query("id") String id
    );

    @GET("app/reg_id.php")
    Call<Result> register(
        @Query("reg_id") String regId,
        @Query("id") String deviceId
    );
}
