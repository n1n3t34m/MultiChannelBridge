package ru.nineteam;


public class StringConfig {
    public String clientJoined = "[{serverName}] {playerName} joined.";
    public String clientJoinedVia = "[{serverName}] {playerName} joined via {previousServerName}.";
    public String fromMinecraftMessage = "[{serverName}] &lt;<b>{playerName}</b>&gt;: {text}";
    public BansPluginStringConfig bansPluginMessages;
    public String toMinecraftMessage = "<insert:'/tg_answer {thread_id} {message_to_reply} '>[TG] {first} {last}: {text}</insert>";


}
