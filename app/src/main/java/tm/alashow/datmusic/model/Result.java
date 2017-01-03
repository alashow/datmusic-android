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

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private ArrayList<Audio> audios;

    public Result() {
    }

    public boolean isOkay() {
        return getStatus().equals("ok") && ! isNoResultError();
    }

    public boolean isNoResultError() {
        return getAudios() != null && getAudios().isEmpty();
    }

    public String getStatus() {
        return status;
    }

    public Result setStatus(String status) {
        this.status = status;
        return this;
    }

    public ArrayList<Audio> getAudios() {
        return audios;
    }

    public Result setAudios(ArrayList<Audio> audios) {
        this.audios = audios;
        return this;
    }
}
