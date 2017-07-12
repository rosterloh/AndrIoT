package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Clouds {

    @SerializedName("all")
    @Expose
    private Integer mAll;

    public Integer getAll() {
        return mAll;
    }

    public void setAll(Integer all) {
        mAll = all;
    }
}
