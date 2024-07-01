package ru.nineteam.plugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.nineteam.*;

import javax.annotation.Nonnull;

public class ToMinecraft implements IMessageReceiver {
    private String formatToMinecraftMessage(Config cfg, TelegramMessage messageObject, Long messageThreadId) {
        return cfg.getStrings().toMinecraftMessage
                    .replace("{first}", messageObject.getFrom().getFirstName())
                    .replace("{last}", messageObject.getFrom().getLastName())
                    .replace("{text}", messageObject.getText())
                    .replace("{thread_id}", String.valueOf(messageThreadId))
                    .replace("{message_to_reply}", String.valueOf(messageObject.getMessageId()));
    }
    private String formatToMinecraftReplyMessage(Config cfg, TelegramMessage messageObject) {
        TelegramMessage reply = messageObject.getReplyToMessage();
        return cfg.getStrings().toMinecraftReplyMessage
                .replace("{first}", reply.getFrom().getFirstName())
                .replace("{last}", reply.getFrom().getLastName())
                .replace("{text}", reply.getText());
    }
    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        var bridge = TelegramBridge.getInstance();
        var cfg = bridge.getConfig();

        cfg.getServers().forEach((serverName, messageThreadId) -> {
            var optServer = bridge.getProxyServer().getServer(serverName);
            boolean is_reply_null = messageObject.getReplyToMessage() == null;
            boolean is_reply_message = !is_reply_null && messageObject.getReplyToMessage().getMessageThreadId().equals(messageThreadId);
            boolean is_single_message = messageObject.getMessageThreadId().equals(messageThreadId);
            if (!is_single_message && !is_reply_message) { return; }
            if (optServer.isEmpty()) { return; }

            String originalString = formatToMinecraftMessage(cfg, messageObject, messageThreadId);

            if (!is_reply_null && !messageObject.getReplyToMessage().getMessageId().equals(messageThreadId)) {
                originalString = formatToMinecraftReplyMessage(cfg, messageObject) + "\n" + originalString;
            }
            final Component textComponent = MiniMessage.miniMessage().deserialize(originalString);
            optServer.get().sendMessage(textComponent);

        });
        return true;
    }
}
