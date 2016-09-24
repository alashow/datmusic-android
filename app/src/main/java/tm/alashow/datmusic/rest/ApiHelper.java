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

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Created by alashov on 15/11/15.
 */
public class ApiHelper {

    public static MediaType IMAGE_MIME_TYPE_DEFAULT = MediaType.parse("application/octet-stream");

    public static boolean isSuccess(Response<?> response) {
        return response.isSuccessful() && response.body() != null;
    }

    public static RequestBody partString(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value);
    }
}