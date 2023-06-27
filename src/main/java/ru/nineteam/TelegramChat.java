package ru.nineteam;

import com.google.gson.annotations.SerializedName;

public class TelegramChat {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @SerializedName("id")
    long id;
}
