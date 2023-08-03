package ru.nineteam.plugins;

import ru.nineteam.*;

import javax.annotation.Nonnull;

import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.PlayerVictim;
import space.arim.libertybans.api.PunishmentType;
import space.arim.libertybans.api.punish.PunishmentRevoker;
import space.arim.libertybans.api.punish.RevocationOrder;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;


import java.util.List;
import java.util.UUID;

public class BansPlugin implements IMessageReceiver {
    private Omnibus omnibus;
    private LibertyBans libertyBans;

    public BansPlugin() {
    }

    boolean isAllowed(long userId) {
        return TelegramBridge.getInstance().getConfig().getOperatorList().contains(userId);
    }

    void clearPunishments(UUID uuid, Long chatId, Long reply_to) {
        PunishmentRevoker revoker = libertyBans.getRevoker();
        RevocationOrder banOrder = revoker.revokeByTypeAndVictim(PunishmentType.BAN, PlayerVictim.of(uuid));
        RevocationOrder warnOrder = revoker.revokeByTypeAndVictim(PunishmentType.WARN, PlayerVictim.of(uuid));
        RevocationOrder muteOrder = revoker.revokeByTypeAndVictim(PunishmentType.MUTE, PlayerVictim.of(uuid));
        banOrder.undoPunishment();
        muteOrder.undoPunishment();
        warnOrder.undoPunishment();

        String text = "Грехи были отпущены";
        try {
            TelegramBridge.getInstance().getSender().sendMessage(chatId, text, "HTML", reply_to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        if(this.omnibus == null) {
            this.omnibus = OmnibusProvider.getOmnibus();
            this.libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();
        }

        String cmd = messageObject.getCommand();
        List<String> args = messageObject.getArgs();
        if (args.size() >= 2 && isAllowed(messageObject.getFrom().getId()) && cmd.equals("/unpunish")) {
            UUID playerUUID = UUID.fromString(args.get(1));
            clearPunishments(playerUUID, messageObject.getChat().getId(), messageObject.getMessageThreadId());
        }

        return true;
    }
}
