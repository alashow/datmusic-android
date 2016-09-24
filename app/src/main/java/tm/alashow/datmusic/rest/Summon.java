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
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by alashov on 17/03/16.
 */
public class  Summon<T> implements Callback<T> {

    public Summon() {
        onStart();
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        onSuccess(call, response);
        onFinish();
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (! call.isCanceled()) {
            onFail(call, t);
        }
        onFinish();
    }

    public void onStart() {
    }

    public void onFinish() {

    }

    public void onSuccess(Call<T> call, Response<T> response) {

    }

    public void onFail(Call<T> call, Throwable t) {

    }
}