package ru.nineteam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Config {
    private Long TelegramTimeout = 60L;
    private String TelegramToken = "";
    private Long TelegramChatId = 0L;
    private Long TelegramLogThread = 0L;
    @NotNull
    private ArrayList<Long> operatorList = new ArrayList<>();


    private boolean logTelegramRequests = false;
    private boolean logPlayerConnectionsToLogThread = false; // если false - логироваться подключения отключения в канал сервера
    private final HashMap<String, Long> Servers = new HashMap<>(); // ServerName <-> Forum Thread ID

    public StringConfig getStrings() {
        return strings;
    }

    public void setStrings(StringConfig strings) {
        this.strings = strings;
    }

    private StringConfig strings = new StringConfig();

    public void generate(String pathName, ProxyServer proxy) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        strings.bansPluginMessages = new BansPluginStringConfig();
        for (RegisteredServer server : proxy.getAllServers()) {
            Servers.put(server.getServerInfo().getName(), 0L);
        }
        try (FileWriter writer = new FileWriter(pathName)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Long> getServers() {
        return Servers;
    }

    public String getTelegramToken() {
        return TelegramToken;
    }

    public void setTelegramToken(String telegramToken) {
        TelegramToken = telegramToken;
    }

    public Long getTelegramChatId() {
        return TelegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        TelegramChatId = telegramChatId;
    }

    public Long getTelegramTimeout() {
        return TelegramTimeout;
    }

    public void setTelegramTimeout(Long telegramTimeout) {
        TelegramTimeout = telegramTimeout;
    }

    public ArrayList<Long> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(ArrayList<Long> operatorList) {
        this.operatorList = operatorList;
    }

    public Long getTelegramLogThread() {
        return TelegramLogThread;
    }

    public void setTelegramLogThread(Long telegramLogThread) {
        TelegramLogThread = telegramLogThread;
    }

    @Override
    public String toString() {
        return "Config{" +
                "TelegramTimeout=" + TelegramTimeout +
                ", TelegramToken='" + TelegramToken + '\'' +
                ", TelegramChatId=" + TelegramChatId +
                ", TelegramLogThread=" + TelegramLogThread +
                ", operatorList=" + operatorList +
                ", Servers=" + Servers +
                ", strings=" + strings +
                '}';
    }
   public boolean isLogPlayerConnectionsToLogThread() {
        return logPlayerConnectionsToLogThread;
    }

    public void setLogPlayerConnectionsToLogThread(boolean logPlayerConnectionsToLogThread) {
        this.logPlayerConnectionsToLogThread = logPlayerConnectionsToLogThread;
    }

    public boolean isLogTelegramRequests() {
        return logTelegramRequests;
    }

    public void setLogTelegramRequests(boolean logTelegramRequests) {
        this.logTelegramRequests = logTelegramRequests;
    }
}