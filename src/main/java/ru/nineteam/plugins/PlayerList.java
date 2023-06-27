package ru.nineteam.plugins;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ru.nineteam.TelegramBridge;
import ru.nineteam.TelegramMessage;
import ru.nineteam.IMessageReceiver;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerList implements IMessageReceiver {

    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        var proxyServer = TelegramBridge.getInstance().getProxyServer();
        var sender = TelegramBridge.getInstance().getSender();
        List<String> cmdArgs = List.of(messageObject.getText().split(" "));
        ArrayList<String> lines = new ArrayList<>();
        if (cmdArgs.isEmpty()) return false;
        long elementCount = cmdArgs.size();
        if (elementCount == 1) { // /players without arguments
            lines.add("<b>Игроки онлайн: </b>\n");
            for (RegisteredServer server : proxyServer.getAllServers()) {
                lines.add(String.format("Сервер <b>%s</b>", server.getServerInfo().getName()));
                for (Player player : server.getPlayersConnected() ) {
                    lines.add(String.format("<b>%s</b> [ %dms ] ", player.getUsername(), player.getPing()));
                }
                lines.add("---");
            }
            var chatId = messageObject.getChat().getId();
            var threadId = messageObject.getMessageThreadId();
            try {
                sender.sendMessage(chatId, String.join("\n", lines), "HTML", threadId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (elementCount == 2) {
            // if /players srvName
            System.out.println(cmdArgs.get(1));
            var srv = proxyServer.getServer(cmdArgs.get(1));
            if(srv.isEmpty()) return false;
            var players = srv.get().getPlayersConnected();
            if (players.size() != 0) {
                lines.add(String.format("<b>Игроки онлайн [<b>%s</b>]</b>\n", srv.get().getServerInfo().getName()));

                for (Player player : players) {
                    lines.add(String.format("<b>%s</b> [ %dms ] ", player.getUsername(), player.getPing()));
                }
                lines.add(String.format("Всего: %d.", players.size()));

            } else {
                lines.add(String.format("Ни одного игрока на <b>%s</b>. Может поиграем? :)", srv.get().getServerInfo().getName()));
            }


            lines.add("---");
            var chatId = messageObject.getChat().getId();
            var threadId = messageObject.getMessageThreadId();
            try {
                var res = sender.sendMessage(chatId, String.join("\n", lines), "HTML", threadId);
                System.out.println(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


}