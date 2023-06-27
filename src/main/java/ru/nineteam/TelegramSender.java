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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TelegramSender {
    private String token = null;
    private HttpClient client = null;
    private JSONParser parser;
    final private String api_url = "https://api.telegram.org/bot%s/%s?%s";
    public TelegramSender(String telegramToken) {
        this.token = telegramToken;
        this.client = HttpClient.newHttpClient();
        parser = new JSONParser();


    }
    public JSONObject sendMessage(long chatId, String message, String parse_mode, Long reply_to_message_id) throws ParseException, IOException, InterruptedException {
        var args = new HashMap<String, String>();
        args.put("chat_id", String.valueOf(chatId));
        args.put("parse_mode", parse_mode);
        args.put("text", message);
        args.put("reply_to_message_id", String.valueOf(reply_to_message_id));
        var encoded = urlEncodeUTF8(args);
        var uri = api_url.formatted(token, "sendMessage", encoded);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return (JSONObject) parser.parse(resp.body());
    }
    public JSONObject sendMessage(long chatId, String message, String parse_mode) throws ParseException, IOException, InterruptedException {
        var args = new HashMap<String, String>();
        args.put("chat_id", String.valueOf(chatId));
        args.put("parse_mode", parse_mode);
        args.put("text", message);
        var encoded = urlEncodeUTF8(args);
        var uri = api_url.formatted(token, "sendMessage", encoded);
        System.out.println(uri.replace(token, "<TOKEN>"));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return (JSONObject) parser.parse(resp.body());
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
