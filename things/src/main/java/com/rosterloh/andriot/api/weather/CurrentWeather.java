package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrentWeather {

    @SerializedName("id")
    @Expose
    private Integer mId;
    @SerializedName("main")
    @Expose
    private String mMain;
    @SerializedName("description")
    @Expose
    private String mDescription;
    @SerializedName("icon")
    @Expose
    private String mIcon;

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
