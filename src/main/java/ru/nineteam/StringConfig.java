package ru.nineteam;


public class StringConfig {
    public String clientJoined = "[{serverName}] {playerName} joined.";
    public String clientJoinedVia = "[{serverName}] {playerName} joined via {previousServerName}.";
    public String fromMinecraftMessage = "[{serverName}] &lt;<b>{playerName}</b>&gt;: {text}";
    public BansPluginStringConfig bansPluginMessages;
    public String toMinecraftMessage = "<insert:'/tg_answer {thread_id} {message_to_reply} '>[TG] {first} {last}: {text}</insert>";
    public String toMinecraftReplyMessage = "[TG] <color:aqua>Ответ на: {first} {last}</color>: {text}";

    public String clientDisconnect = "[{serverName}] {playerName} has disconnected.";
    public String masterServerStarted = "[Master server has started.]";
    public String masterServerStopped = "[Master server has stopped.]";
    public String masterServerReloaded = "[Master server has reload.]";

    @Override
    public String toString() {
        return "StringConfig{" +
                "clientJoined='" + clientJoined + '\'' +
                ", clientJoinedVia='" + clientJoinedVia + '\'' +
                ", fromMinecraftMessage='" + fromMinecraftMessage + '\'' +
                ", bansPluginMessages=" + bansPluginMessages +
                ", toMinecraftMessage='" + toMinecraftMessage + '\'' +
                ", toMinecraftReplyMessage='" + toMinecraftReplyMessage + '\'' +
                ", clientDisconnect='" + clientDisconnect + '\'' +
                ", masterServerStarted='" + masterServerStarted + '\'' +
                ", masterServerStopped='" + masterServerStopped + '\'' +
                ", masterServerReloaded='" + masterServerReloaded + '\'' +
                '}';
    }
}
