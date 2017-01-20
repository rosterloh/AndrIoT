package com.rosterloh.andriot.weather;

public class Weather {

    private String temperature;
    private String description;
    private String iconId;
    private String lastUpdated;

    public static class Builder {

        private String temperature;
        private String description;
        private String iconId;
        private String lastUpdated;

        public Builder temperature(String temperature) { this.temperature = temperature; return this; }
        public Builder description(String summary) { this.description = summary; return this; }
        public Builder iconId(String iconId) { this.iconId = iconId; return this;}
        public Builder lastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; return this; }

        public Weather build() {
            return new Weather(this);
        }
    }

    private Weather(Builder builder) {
        this.temperature = builder.temperature;
        this.description = builder.description;
        this.iconId = builder.iconId;
        this.lastUpdated = builder.lastUpdated;
    }

    public String getTemp() {
        return temperature;
    }

    public void setTemp(String temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.substring(0, 1).toUpperCase() + description.substring(1);
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}
