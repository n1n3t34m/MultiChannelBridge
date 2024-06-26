package ru.nineteam;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;


import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramListener implements Runnable {
    TelegramBridge bridge;
    Logger logger;
    long chatId;
    long lastUpdateId = -1;
    String token;
    Config config;
    ProxyServer proxyServer;
    HttpClient client = HttpClient.newHttpClient();
    final List<IMessageReceiver> receivers = new ArrayList<>();



    public TelegramListener(long chat_id, String token, ProxyServer proxyServer, Config config, TelegramBridge bridge) {

        this.config = config;
        this.token = token;
        this.chatId = chat_id;
        this.proxyServer = proxyServer;
        this.bridge = bridge;
        this.logger = bridge.getLogger();
    }
    static String urlEncodeUTF8(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
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
    private void processUpdate(@Nullable TelegramUpdate update) {
        if (update == null) return;

        TelegramMessage msg = new TelegramMessage();
        if (update.getMessage() != null) msg = update.getMessage();
        if (update.getEditedMessage() != null) msg = update.getEditedMessage();
        if (msg.getChat().getId() != chatId) return;
        for (IMessageReceiver r : receivers) {
            r.onTelegramObjectMessage(msg);
        }

    }
    public void run() {
        Gson parser = new Gson();
        while(true) {

            var args = new HashMap<String, String>();
            args.put("offset", String.valueOf(lastUpdateId));
            args.put("timeout", String.valueOf(config.getTelegramTimeout()));
            var encoded = urlEncodeUTF8(args);
            String api_url = "https://api.telegram.org/bot%s/%s?%s";
            var uri = api_url.formatted(token, "getUpdates", encoded);
            if (config.isLogTelegramRequests()) logger.info(uri.replace(token, "<TOKEN>"));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .build();
            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                String text = resp.body();
                var answer = parser.fromJson(text, TelegramAnswer.class);
                if (!answer.getOk()) {
                    logger.error(text);
                    continue;
                }
                var updates = answer.getResult();

                if (updates != null && !updates.isEmpty()) {

                    for (var update : updates) {
                        if (update.getUpdateId() >= lastUpdateId) lastUpdateId = update.getUpdateId()+1;
                        processUpdate(update);
                    }
                }
            } catch (Exception e) {
                logger.warn("ehhhh? connection reset ??");
                e.printStackTrace();
            }


        }

    }
}
