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

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tm.alashow.datmusic.model.Result;

/**
 * Created by alashov on 17/03/16.
 */
public class ResponseHandler {

    private Context mContext;

    private Callback<Result> callback;
    private Call<Result> call;
    private Response<Result> response;

    public ResponseHandler() {
    }

    public ResponseHandler(@NonNull
                               Context context, Callback<Result> callback, Call<Result> call, Response<Result> response) {
        this.mContext = context;
        this.callback = callback;
        this.call = call;
        this.response = response;

        if (ApiHelper.isSuccess(response)) {
            Result result = response.body();
            if (result.isOkay()) {
                onSuccess(result);
            } else {
                if (result.isTokenError()) {
                    onTokenError(result);
                } else if (result.isTooManyRequestsError()) {
                    onTooManyRequestsError(result);
                } else if (result.isCaptchaError()) {
                    onCaptchaError(result);
                } else if (result.isNoResultError()) {
                    onNoResultError(result);
                } else {
                    onUnknownError(result);
                }
            }
        } else {
            callback.onFailure(call, new NetworkErrorException());
        }
    }

    public void onSuccess(Result result) {
    }

    public void onTokenError(Result result) {
        onUnknownError(result);
    }

    public void onTooManyRequestsError(Result result) {
        onUnknownError(result);
    }

    public void onCaptchaError(Result result) {
        onUnknownError(result);
    }

    public void onNoResultError(Result result) {
        onUnknownError(result);
    }

    public void onUnknownError(Result result) {
        callback.onFailure(call, new NetworkErrorException());
    }

    public interface OnSuccessListener {
        void onSuccess(Result result);
    }
}
