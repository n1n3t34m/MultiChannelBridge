package ru.nineteam;

import javax.annotation.Nonnull;

/**
 * Use {@link BaseMessageReceiver} instead for filter empty or invalid message
 */
public interface IMessageReceiver {
    boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject);
}