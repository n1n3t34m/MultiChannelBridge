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
import javax.inject.Inject;
import java.io.*;


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
    private final ProxyServer server;
    private TelegramSender sender;
    private TelegramListener listener;
    private long TelegramChatId;
    private Config config;
    private final Logger logger;
    String regex = "(?s)`([^`]*)`|(\\*\\*|[_~])((?:(?!\\2).)*)\\2";
    Path dataDirectory;
    @Inject
    public TelegramBridge(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.server = server;
        String token = "";
        long chat_id = 0L;
        logger.info(String.valueOf(dataDirectory.toAbsolutePath()));
        //private long TelegramChatId = -1001629242284L;
        config = new Config();
        Gson gson = new Gson();
        if (!new File(dataDirectory+".json").exists()) {
            config.generate(dataDirectory+ ".json", server);
        } else {
           try (Reader reader = new FileReader(dataDirectory+".json")) {
               config = gson.fromJson(reader, Config.class);

           } catch (IOException e) {
               e.printStackTrace();
           }
        }

        logger.info("StartUp Telegram bridge");
        this.sender = new TelegramSender(config.getTelegramToken());
        this.TelegramChatId = config.getTelegramChatId();
        this.listener = new TelegramListener(config.getTelegramChatId(), config.getTelegramToken(), server, config);
        new Thread(this.listener).start();
    }
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {

        Optional<ServerConnection> fromServer = event.getPlayer().getCurrentServer();
        if (fromServer.isEmpty()) {
            return;
        }
        var server = fromServer.get();
        var text = event.getMessage();
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        String message = String.format("[%s] &lt;<b>%s</b>&gt;: %s", serverName, playerName, text);
        System.out.println(message);
        try {
            var s = sender.sendMessage(TelegramChatId, message, "HTML", config.getServers().get("lobby"));
            System.out.println(s);
        } catch (IOException | InterruptedException | ParseException e) {
            System.err.println(e.getMessage());
        }

    }
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        var server = event.getServer();
        System.out.println(server);
        var playerName = event.getPlayer().getUsername();
        var serverName = server.getServerInfo().getName();
        var message = "";
        if (event.getPreviousServer().isEmpty()) {
            message = String.format("[%s] <b>%s</b> joined.", serverName, playerName);
        } else {
            var srv = event.getPreviousServer().get().getServerInfo();
            message = String.format("[%s] <b>%s</b> joined via %s.", serverName, playerName, srv.getName());
        }
        System.out.println(message);
        try {
            var s = sender.sendMessage(TelegramChatId, message, "HTML", config.getServers().get("lobby"));
            System.out.println(s);
        } catch (IOException | InterruptedException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }

}
