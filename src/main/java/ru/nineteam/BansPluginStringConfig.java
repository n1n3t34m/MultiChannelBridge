package ru.nineteam;

public class BansPluginStringConfig {
    public String KickedByIp = "<b>{operatorName}</b> кикнул(а) игрока(ов) с IP <b>{victimIP}</b>. Причина: <i>{reason}</i>\n" +
            "#op{operatorName} #kickip #{serverName}";
    public String KickedByNickname = "<b>{operatorName}</b> кикнул(а) игрока <b>{victimName}</b>. Причина: <i>{reason}</i>\n" +
            "#op{operatorName} #kick #{serverName}";
    public String BannedByNickname = "<b>{operatorName}</b> забанил(а) игрока <b>{victimName}</b> на {duration}. Причина: <i>{reason}</i>\n" +
            "#op{operatorName} #ban #{serverName}";
    public String BannedByIp = "<b>{operatorName}</b> забанил(а) игрока(ов) с IP <b>{victimIP}</b> на {duration}. Причина: <i>{reason}</i>\n" +
            "#op{operatorName} #banip  #{serverName}";
    public String IpUnban = "<b>{operatorName}</b> разбанил(а) IP <b>{victimIP}</b>.\n" +
            "#op{operatorName} #unbanip #{serverName}";
    public String NicknameUnban = "<b>{operatorName}</b> разбанил(а) игрока <b>{victimName}</b>.\n" +
            "#op{operatorName} #unban #{serverName}";
}
