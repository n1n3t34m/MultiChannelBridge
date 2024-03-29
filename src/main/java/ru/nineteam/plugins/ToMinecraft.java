package ru.nineteam.plugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.nineteam.IMessageReceiver;
import ru.nineteam.TelegramBridge;
import ru.nineteam.TelegramMessage;
import ru.nineteam.TelegramUser;

import javax.annotation.Nonnull;

public class ToMinecraft implements IMessageReceiver {
    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        var bridge = TelegramBridge.getInstance();
        var cfg = bridge.getConfig();
        cfg.getServers().forEach((serverName, messageThreadId) -> {
            if (messageObject.getMessageThreadId().equals(messageThreadId)) {
                var optServer = bridge.getProxyServer().getServer(serverName);
                if (optServer.isPresent()) {
                    TelegramUser user = messageObject.getFrom();
                    String originalString = cfg.getStrings().toMinecraftMessage
                            .replace("{first}", messageObject.getFrom().getFirstName())
                            .replace("{last}", messageObject.getFrom().getLastName())
                            .replace("{text}", messageObject.getText())
                            .replace("{thread_id}", String.valueOf(messageThreadId))
                            .replace("{message_to_reply}", String.valueOf(messageObject.getMessageId()));
                    System.out.println(originalString);
                    final Component textComponent = MiniMessage.miniMessage().deserialize(originalString);
                    System.out.println(optServer.isPresent());
                    optServer.get().sendMessage(textComponent);
                }
            }
        });
        return true;
    }
}
