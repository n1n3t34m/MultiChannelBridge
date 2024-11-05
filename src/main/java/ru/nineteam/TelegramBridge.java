package ru.nineteam;


import com.google.gson.Gson;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import ru.nineteam.commands.TelegramAnswer;
import ru.nineteam.commands.TelegramBridgePing;
import ru.nineteam.plugins.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id="telegrambridge",
        name = "TelegramBridge",
        version = "0.1.2-SNAPSHOT",
        url = "nineteam.org",
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

    public Boolean getRunning() {
        return running;
    }

    private Boolean running = false;
    private Boolean libertyBansFound = false;
    private Logger logger;
    public Logger getLogger() {
        return logger;
    }
    public void createOrLoadConfig() {
        boolean is_reload = false;
        if (this.config != null) {
            is_reload = true;
            logger.info("Config reload?");
        }
        this.config = new Config();
        var cfgPath = dataDirectory + "/config.json";
        if (!Files.exists(dataDirectory)) {
            try {
                logger.info("config directory not exists. creating");
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(cfgPath).exists()) {
            logger.info("config not exists. generating");
            config.generate(cfgPath, server);
        } else {
            try (Reader reader = new FileReader(cfgPath)) {
                logger.info("config exists. attempt to read");
                config = new Gson().fromJson(reader, Config.class);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (is_reload) {
            logger.info(config.toString());
        }
    }

    @Inject
    public TelegramBridge(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.server = server;
        this.logger = logger;
        logger.info(String.valueOf(dataDirectory.toAbsolutePath()));
        createOrLoadConfig();
        if (config.getTelegramToken().isEmpty()) {
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
        listener.receivers.add(new BridgeControl());
        try {
            Class cls = Class.forName("space.arim.omnibus.Omnibus");
            libertyBansFound = true;
            // test if LibertyBans Installed

        } catch (ClassNotFoundException e) {
            libertyBansFound = false;
        }
        logger.info("libertybans found: " + libertyBansFound);

        if (libertyBansFound) listener.receivers.add(new BansPlugin(logger));

        new Thread(listener).start();

        this.running = true;

    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("telegram bridge proxy initialize");
        CommandManager cmdManager = getProxyServer().getCommandManager();
        CommandMeta answerCmdMeta = cmdManager.metaBuilder("tg_answer")
                .plugin(this)
                .build();
        CommandMeta pingCmdMeta = cmdManager.metaBuilder("tg_ping")
                .plugin(this)
                .build();

        RawCommand telegramAnswerCmd = new TelegramAnswer();
        RawCommand telegramPingCmd = new TelegramBridgePing();
        cmdManager.register(answerCmdMeta, telegramAnswerCmd);
        cmdManager.register(pingCmdMeta, telegramPingCmd);
        if (libertyBansFound) server.getEventManager().register(this, new BansPlugin(logger));
        server.getEventManager().register(this, new ToTelegram());
    }


}
