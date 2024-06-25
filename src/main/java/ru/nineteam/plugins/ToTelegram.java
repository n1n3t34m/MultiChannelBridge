package ru.nineteam.plugins;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
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
    private static boolean _serverStarted = false;
    private final int maxTries = 3;
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        var config = TelegramBridge.getInstance().getConfig();

        if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            return;
        }
        var server = event.getPlayer().getCurrentServer();
        var serverName = (server.isPresent()) ? server.get().getServerInfo().getName() : "lobby";
        var message = config.getStrings().clientDisconnect
                .replace("{serverName}", serverName)
                .replace("{playerName}", event.getPlayer().getUsername());
        var sender = TelegramBridge.getInstance().getSender();
        int count = 0;
        while(true) {
            try {
                sender.sendMessage(
                        config.getTelegramChatId(),
                        message, "html",
                        config.getServers().get(serverName));
                break;
            } catch (ParseException | IOException | InterruptedException e) {
                if (++count == maxTries) e.printStackTrace();
            }
        }

    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        var config = TelegramBridge.getInstance().getConfig();

        if (!TelegramBridge.getInstance().getRunning()) {
            return;
        }

        var server = event.getServer();
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


        TelegramBridge.getInstance().getLogger().info(message);
        int count = 0;
        while (true) {
            try {
                var sender = TelegramBridge.getInstance().getSender();
                var telegramChatId = config.getTelegramChatId();
                String srvName = server.getServerInfo().getName();
                var s = sender.sendMessage(telegramChatId, message, "HTML", config.getServers().get(srvName));
                break;
            } catch (IOException | InterruptedException | ParseException e) {
                if (++count == maxTries)  e.printStackTrace();
            }
        }

    }
    @Subscribe
    public void onProxyReload(ProxyReloadEvent ev) {
        var cfg = TelegramBridge.getInstance().getConfig();
        TelegramBridge.getInstance().getLogger().info("telegram bridge loaded. reloading config.");
        try {
            TelegramBridge.getInstance().createOrLoadConfig();

            TelegramBridge.getInstance().getSender().sendMessage(
                    cfg.getTelegramChatId(),
                    cfg.getStrings().masterServerReloaded,
                    "html",
                    cfg.getTelegramLogThread()
            );
        } catch (Exception e) {
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
    public void onServerStarted(ListenerBoundEvent ev) {
        if(_serverStarted) { return; } //
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
        _serverStarted = true;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!TelegramBridge.getInstance().getRunning()) {
            return;
        }
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
        int count = 0;
        while (true) {
            try {
                String srvName = fromServer.get().getServerInfo().getName();
                var sender = TelegramBridge.getInstance().getSender();
                var telegramChatId = config.getTelegramChatId();
                var s = sender.sendMessage(telegramChatId, message, "HTML", config.getServers().get(srvName));
                break;
            } catch (Exception e) {
                if (++count == maxTries) e.printStackTrace();
            }
        }
    }
}
