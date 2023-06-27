package ru.nineteam.plugins;

import ru.nineteam.IMessageReceiver;
import ru.nineteam.TelegramBridge;
import ru.nineteam.TelegramMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerList implements IMessageReceiver {
    public static boolean hostAvailabilityCheck(InetAddress serverAddress, int serverPort) {
        try (Socket s = new Socket(serverAddress, serverPort)) {
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }
    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        if (!messageObject.getText().startsWith("/servers")) return false;
        var proxyServer = TelegramBridge.getInstance().getProxyServer();
        var sender = TelegramBridge.getInstance().getSender();
        var servers = proxyServer.getAllServers();
        List<String> lines = new ArrayList<>();
        lines.add("<b>Статус серверов:\n</b>");
        for (var server:servers) {
            var info = server.getServerInfo();
            var address = info.getAddress();
            boolean alive = hostAvailabilityCheck(address.getAddress(), address.getPort());
            lines.add("<b>%s</b>: %d игроков, %s".formatted(info.getName(),server.getPlayersConnected().size(), !alive ? "⚰\uFE0F" : "\uD83D\uDC4B"));

        }
        try {
            sender.sendMessage(messageObject.getChat().getId(), String.join("\n", lines), "HTML", messageObject.getMessageThreadId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
