package ru.nineteam.plugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
                    String fmtString = cfg.getStrings().toMinecraftMessage.formatted(user.getFirstName(), user.getLastName(), messageObject.getText());
                    final TextComponent textComponent = Component.text(fmtString);
                    optServer.get().sendMessage(textComponent);
                }
            }
        });
        return true;
    }
}
