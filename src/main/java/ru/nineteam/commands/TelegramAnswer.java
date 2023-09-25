package ru.nineteam.commands;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.nineteam.Config;
import ru.nineteam.TelegramBridge;
import ru.nineteam.TelegramSender;

import java.util.ArrayList;
import java.util.List;

public class TelegramAnswer implements RawCommand {
    @Override
    public void execute(final Invocation invocation){
        TelegramBridge bridge = TelegramBridge.getInstance();
        TelegramSender sender = bridge.getSender();
        Config config = bridge.getConfig();
        List<String> args = new ArrayList<>(List.of(invocation.arguments().split(" ")));

        if (args.size() < 3) {
            return;
        }
        Long reply_to_message_id = Long.valueOf(args.get(1));
        String playerText = String.join(" ", args.subList(2, args.size()));
        Player ply = (Player) invocation.source();

        String srvName = ply.getCurrentServer().isPresent() ? ply.getCurrentServer().get().getServerInfo().getName() : "";
        String text = config.getStrings().fromMinecraftMessage
                .replace("{serverName}",srvName)
                .replace("{playerName}",ply.getUsername())
                .replace("{text}",playerText);

        if(ply.getCurrentServer().isPresent()) {
            final String fmtString = ("[Answer to <color:blue>T</color><color:red>G</color>] <%s>: %s").formatted(ply.getUsername(), playerText);
            final Component textComponent = MiniMessage.miniMessage().deserialize(fmtString);

            ply.getCurrentServer().get().getServer().sendMessage(textComponent);
        }
        try {
            sender.sendMessage(config.getTelegramChatId(), text, "HTML", reply_to_message_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
