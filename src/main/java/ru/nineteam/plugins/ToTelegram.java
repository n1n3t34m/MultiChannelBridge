package ru.nineteam.plugins;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import org.json.simple.parser.ParseException;
import ru.nineteam.TelegramBridge;
import java.io.IOException;
import java.util.Optional;

public class ToTelegram {
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        var config = TelegramBridge.getInstance().getConfig();
        if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            return;
        }
        var server = event.getPlayer().getCurrentServer().get();
        var message = config.getStrings().clientDisconnect
                .replace("{serverName}", server.getServerInfo().getName())
                .replace("{playerName}", event.getPlayer().getUsername());
        var sender = TelegramBridge.getInstance().getSender();
        try {
            sender.sendMessage(
                    config.getTelegramChatId(),
                    message, "html",
                    config.getServers().get(server.getServerInfo().getName()))
            ;
        } catch (ParseException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        var config = TelegramBridge.getInstance().getConfig();

        if (!TelegramBridge.getInstance().getRunning()) {
            return;
        }

        var server = event.getServer();
        System.out.println(server);
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        var message = "";
        //
        // если юзер зашел не из лобби - написать прямо
        if (event.getPreviousServer().isEmpty()) {
            message = config.getStrings().clientJoined
                    .replace("{serverName}", serverName)
                    .replace("{playerName}", playerName);
        } else {
            var previousServerName = event.getPreviousServer().get().getServerInfo().getName();
            message = config.getStrings().clientJoinedVia
                    .replace("{serverName}", serverName)
                    .replace("{playerName}", playerName)
                    .replace("{previousServerName}", previousServerName);
        }
        //
        //


        System.out.println(message);
        try {
            var sender = TelegramBridge.getInstance().getSender();
            var telegramChatId = config.getTelegramChatId();
            String srvName = server.getServerInfo().getName();
            var s = sender.sendMessage(telegramChatId, message, "HTML", config.getServers().get(srvName));
            System.out.println(s);
        } catch (IOException | InterruptedException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }
    @Subscribe
    public void onProxyReload(ProxyReloadEvent ev) {
        var cfg = TelegramBridge.getInstance().getConfig();
        try {
            TelegramBridge.getInstance().getSender().sendMessage(
                    cfg.getTelegramChatId(),
                    cfg.getStrings().masterServerReloaded,
                    "html",
                    cfg.getTelegramLogThread()
            );
        } catch (ParseException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Subscribe
    public void onProxyStop(ProxyShutdownEvent ev) {
        var cfg = TelegramBridge.getInstance().getConfig();
        try {
            TelegramBridge.getInstance().getSender().sendMessage(
                    cfg.getTelegramChatId(),
                    cfg.getStrings().masterServerStopped,
                    "html",
                    cfg.getTelegramLogThread()
            );
        } catch (ParseException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Subscribe
    public void onProxyStarted(ListenerBoundEvent ev) {
        var cfg = TelegramBridge.getInstance().getConfig();
        try {
            TelegramBridge.getInstance().getSender().sendMessage(
                    cfg.getTelegramChatId(),
                    cfg.getStrings().masterServerStarted,
                    "html",
                    cfg.getTelegramLogThread()
            );
        } catch (ParseException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (event.getCommand().equals("tg_answer")) {
            System.out.println(event.getResult());
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!TelegramBridge.getInstance().getRunning()) {
            return;
        }
        var x = event.getResult();


        Optional<ServerConnection> fromServer = event.getPlayer().getCurrentServer();
        if (fromServer.isEmpty()) {
            return;
        }
        var server = fromServer.get();
        var text = event.getMessage();
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        var config = TelegramBridge.getInstance().getConfig();

        String message = config.getStrings().fromMinecraftMessage
                .replace("{serverName}", serverName)
                .replace("{playerName}", playerName)
                .replace("{text}", text);
        try {
            String srvName = fromServer.get().getServerInfo().getName();
            var sender = TelegramBridge.getInstance().getSender();
            var telegramChatId = config.getTelegramChatId();
            var s = sender.sendMessage(telegramChatId, message, "HTML", config.getServers().get(srvName));
            System.out.println(s);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
