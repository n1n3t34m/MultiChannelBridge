package ru.nineteam;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TelegramSender {
    private String token;
    private HttpClient client;
    private JSONParser parser;
    final private String api_url = "https://api.telegram.org/bot%s/%s?%s";
    public TelegramSender(String telegramToken) {
        this.token = telegramToken;
        this.client = HttpClient.newHttpClient();
        parser = new JSONParser();


    }

    public JSONObject getChatAdministrators(long chatId) throws ParseException, IOException, InterruptedException {
        var args = new HashMap<String, String>();
        args.put("chat_id", String.valueOf(chatId));
        return method("getChatAdministrators", urlEncodeUTF8(args));


    }
    public JSONObject method(String method, String args) throws ParseException, IOException, InterruptedException{
        var uri = api_url.formatted(token, method, args);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        TelegramBridge.getInstance().getLogger().info("%d %s ".formatted(resp.statusCode(), method));
        return (JSONObject) parser.parse(resp.body());

    }

    public JSONObject sendMessage(long chatId, String message, String parse_mode, Long reply_to_message_id) throws ParseException, IOException, InterruptedException {
        var args = new HashMap<String, String>();
        args.put("chat_id", String.valueOf(chatId));
        args.put("parse_mode", parse_mode);
        args.put("text", message);
        args.put("reply_to_message_id", String.valueOf(reply_to_message_id));
        var encoded = urlEncodeUTF8(args);
        return method("sendMessage", encoded);

    }

    public JSONObject sendMessage(long chatId, String message, String parse_mode) throws ParseException, IOException, InterruptedException {
        var args = new HashMap<String, String>();
        args.put("chat_id", String.valueOf(chatId));
        args.put("parse_mode", parse_mode);
        args.put("text", message);
        var encoded = urlEncodeUTF8(args);
        return method("sendMessage", encoded);

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
}
