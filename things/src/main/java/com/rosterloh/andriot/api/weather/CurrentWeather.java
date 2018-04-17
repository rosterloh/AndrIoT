package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class CurrentWeather {

    @Json(name = "id") private Integer mId;
    @Json(name = "main") private String mMain;
    @Json(name = "description") private String mDescription;
    @Json(name = "icon") private String mIcon;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getMain() {
        return mMain;
    }

    public void setMain(String main) {
        mMain = main;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }
}
