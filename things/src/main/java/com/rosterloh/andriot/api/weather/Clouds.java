package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class Clouds {

    @Json(name = "all") private Integer mAll;

    public Integer getAll() {
        return mAll;
    }

    public void setAll(Integer all) {
        mAll = all;
    }
}
