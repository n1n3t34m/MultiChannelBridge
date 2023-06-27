package ru.nineteam;

import javax.annotation.Nonnull;


public abstract class BaseMessageReceiver implements IMessageReceiver {

    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        String messageText = messageObject.getText();

        if (messageText == null || messageText.length() == 0) {
            return false;
        }
        return onTelegramMessage(messageObject.getFrom(), messageText);
    }

    public abstract boolean onTelegramMessage(TelegramUser userObject, @Nonnull String message);

}