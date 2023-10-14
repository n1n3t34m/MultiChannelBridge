package ru.nineteam.commands;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class TelegramBridgePing implements RawCommand {
    @Override
    public void execute(final Invocation invocation){
        Player ply = (Player) invocation.source();
        if(ply.getCurrentServer().isPresent()) {
            final String fmtString = ("<color:blue>[Bridge]</color>: Pong!");
            final Component textComponent = MiniMessage.miniMessage().deserialize(fmtString);

            ply.getCurrentServer().get().getServer().sendMessage(textComponent);
        }
    }
}
