package ru.nineteam;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramListener implements Runnable {
    long chatId;
    long lastUpdateId = -1;
    String token;
    Config config;
    ProxyServer proxyServer;
    HttpClient client = HttpClient.newHttpClient();
    final private String api_url = "https://api.telegram.org/bot%s/%s?%s";



    public TelegramListener(long chat_id, String token, ProxyServer proxyServer, Config config) {
        this.config = config;
        this.token = token;
        this.chatId = chat_id;
        this.proxyServer = proxyServer;

    }
    static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
    public static String urlEncodeUTF8(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }

    public void run() {
        Gson parser = new Gson();
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            var args = new HashMap<String, String>();
            args.put("chat_id", String.valueOf(chatId));
            args.put("offset", String.valueOf(lastUpdateId));
            var encoded = urlEncodeUTF8(args);
            var uri = api_url.formatted(token, "getUpdates", encoded);
            System.out.println(uri);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .build();
            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                String text = resp.body();
                System.out.println("ASDAJHSDKGHSDJKGASHJDGJASD+ "+text);
                TelegramAnswer<List<TelegramUpdate>> answer = parser.fromJson(text, new TypeToken<TelegramAnswer<List<TelegramUpdate>>>() {}.getType());
                var updates = answer.getResult();
                for (var update : updates) {
                    if (update.getUpdateId() >= lastUpdateId) lastUpdateId = update.getUpdateId()+1;
                    TelegramMessage msg = new TelegramMessage();
                    if (update.getMessage() != null) msg = update.getMessage();
                    if (update.getEditedMessage() != null) msg = update.getEditedMessage();

                    TelegramMessage finalMsg = msg;
                    config.getServers().forEach((serverName, messageThreadId) -> {
                        if (finalMsg.getMessageThreadId().equals(messageThreadId)) {
                            var optServer = proxyServer.getServer(serverName);

                            if (optServer.isPresent()) {
                                TelegramUser user = finalMsg.getFrom();
                                String fmtString = config.getStrings().toMinecraftMessage.formatted(user.getFirstName(), user.getLastName(), finalMsg.getText());
                                final TextComponent textComponent = Component.text(fmtString);
                                optServer.get().sendMessage(textComponent);
                            }
                        }
                    });
//                    System.out.println(update.getMessage().getText());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }


        }

    }
}
