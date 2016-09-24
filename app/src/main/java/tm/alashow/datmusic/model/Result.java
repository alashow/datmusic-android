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

package tm.alashow.datmusic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by alashov on 17/03/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    @JsonProperty("error")
    private Error error;

    @JsonProperty("response")
    private ArrayList<Audio> audios;

    public Result() {
    }

    public boolean isOkay() {
        return isErrorNull() && !isNoResultError();
    }

    public boolean isErrorNull() {
        return getError() == null;
    }

    public boolean isTokenError() {
        return !isErrorNull() && (getError().getCode() == 5);
    }

    public boolean isTooManyRequestsError() {
        return !isErrorNull() && (getError().getCode() == 6);
    }

    public boolean isCaptchaError() {
        return !isErrorNull() && (getError().getCode() == 14);
    }

    public boolean isNoResultError() {
        return isErrorNull() && getAudios().isEmpty();
    }

    public Error getError() {
        return error;
    }

    public Result setError(Error error) {
        this.error = error;
        return this;
    }

    public ArrayList<Audio> getAudios() {
        return audios;
    }

    public Result setAudios(ArrayList<Audio> audios) {
        this.audios = audios;
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Error {

        @JsonProperty("error_code")
        private int code;

        @JsonProperty("error_msg")
        private String message;

        @JsonProperty("captcha_img")
        private String captchaImage;

        @JsonProperty("captcha_sid")
        private long captchaSid;

        public Error() {
        }

        public int getCode() {
            return code;
        }

        public Error setCode(int code) {
            this.code = code;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Error setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getCaptchaImage() {
            return captchaImage;
        }

        public Error setCaptchaImage(String captchaImage) {
            this.captchaImage = captchaImage;
            return this;
        }

        public long getCaptchaSid() {
            return captchaSid;
        }

        public Error setCaptchaSid(long captchaSid) {
            this.captchaSid = captchaSid;
            return this;
        }
    }
}
