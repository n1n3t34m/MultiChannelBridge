package ru.nineteam;


import com.google.gson.Gson;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.io.*;
import java.util.HashMap;

public class Config {

    private String TelegramToken;
    private Long TelegramChatId;

    private final HashMap<String, Long> Servers = new HashMap<>(); // ServerName <-> Forum Thread ID

    public StringConfig getStrings() {
        return strings;
    }

    public void setStrings(StringConfig strings) {
        this.strings = strings;
    }

    private StringConfig strings = new StringConfig();
    public void generate(String pathName, ProxyServer proxy) {
        Gson gson = new Gson();
        this.setTelegramToken("");
        this.setTelegramChatId(0L);
        for (RegisteredServer server: proxy.getAllServers()) {
            Servers.put(server.getServerInfo().getName(), 0L);
        }
        try(FileWriter writer = new FileWriter(pathName)) {
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
}