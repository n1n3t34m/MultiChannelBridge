package ru.nineteam;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TelegramAnswer {
    @SerializedName("ok")
    private Boolean ok = false;
    @SerializedName("result")
    private List<TelegramUpdate> result = null;

    @SerializedName("error_code")
    private String errorCode = "";
    @SerializedName("description")
    private String description = "";

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public List<TelegramUpdate> getResult() {
        return result;
    }

    public void setResult(List<TelegramUpdate> result) {
        this.result = result;
    }
}