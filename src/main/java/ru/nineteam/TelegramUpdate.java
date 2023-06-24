package ru.nineteam;

import com.google.gson.annotations.SerializedName;

public class TelegramUpdate {
    @SerializedName("update_id")
    private Long updateId = 0L;
    @SerializedName("message")
    private TelegramMessage message = null;
    @SerializedName("edited_message")
    private TelegramMessage editedMessage = null;

    public Long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    public TelegramMessage getMessage() {
        return message;
    }

    public void setMessage(TelegramMessage message) {
        this.message = message;
    }

    public TelegramMessage getEditedMessage() {
        return editedMessage;
    }

    public void setEditedMessage(TelegramMessage editedMessage) {
        this.editedMessage = editedMessage;
    }
}
