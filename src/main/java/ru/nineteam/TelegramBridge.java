package ru.nineteam;


import com.google.gson.Gson;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import ru.nineteam.plugins.PlayerList;
import ru.nineteam.plugins.ServerList;
import ru.nineteam.plugins.ToMinecraft;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id="telegrambridge",
        name = "TelegramBridge",
        version = "0.1.0-SNAPSHOT",
        url = "nineteam.ru",
        description = "test",
        authors = {"RasonGame"}
)
public class TelegramBridge {
    public ProxyServer getProxyServer() {
        return server;
    }

    private final ProxyServer server;

    public TelegramSender getSender() {
        return sender;
    }

    private TelegramSender sender;
    private long TelegramChatId;

    public Config getConfig() {
        return config;
    }

    private Config config;
    Path dataDirectory;
    static TelegramBridge instance;
    public static TelegramBridge getInstance() {
        return instance;
    }
    private Boolean running = false;

    private void createOrLoadConfig() {
        this.config = new Config();
        Gson gson = new Gson();
        var cfgPath = dataDirectory+"/config.json";
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(cfgPath).exists()) {
            config.generate(cfgPath, server);
        } else {
            try (Reader reader = new FileReader(cfgPath)) {
                config = gson.fromJson(reader, Config.class);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Inject
    public TelegramBridge(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.server = server;

        logger.info(String.valueOf(dataDirectory.toAbsolutePath()));
        createOrLoadConfig();
        if (config.getTelegramToken().equals("")) {
            logger.warn("Telegram Token not set; Stopping");
            return;
        }
        logger.info("StartUp Telegram bridge");
        this.sender = new TelegramSender(config.getTelegramToken());
        this.TelegramChatId = config.getTelegramChatId();
        TelegramListener listener = new TelegramListener(config.getTelegramChatId(), config.getTelegramToken(), server, config, this);
        instance = this;
        listener.receivers.add(new PlayerList());
        listener.receivers.add(new ToMinecraft());
        listener.receivers.add(new ServerList());
        new Thread(listener).start();
        this.running = true;
    }
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        if (!running) { return; }
        Optional<ServerConnection> fromServer = event.getPlayer().getCurrentServer();
        if (fromServer.isEmpty()) {
            return;
        }
        var server = fromServer.get();
        var text = event.getMessage();
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        String message = config.getStrings().fromMinecraftMessage.formatted(serverName, playerName, text);
        System.out.println(message);
        try {
            String srvName = fromServer.get().getServerInfo().getName();
            var s = sender.sendMessage(TelegramChatId, message, "HTML", config.getServers().get(srvName));
            System.out.println(s);
        } catch (IOException | InterruptedException | ParseException e) {
            System.err.println(e.getMessage());
        }

    }
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (!running) { return; }

        var server = event.getServer();
        System.out.println(server);
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        var message = "";
        if (event.getPreviousServer().isEmpty()) {
            message = config.getStrings().clientJoined.formatted(serverName, playerName);
        } else {
            var previousServerName = event.getPreviousServer().get().getServerInfo().getName();
            message = config.getStrings().clientJoinedVia.formatted(serverName, playerName, previousServerName);
        }
        System.out.println(message);
        try {
            String srvName = server.getServerInfo().getName();
            var s = sender.sendMessage(TelegramChatId, message, "HTML", config.getServers().get(srvName));
            System.out.println(s);
        } catch (IOException | InterruptedException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }

}
