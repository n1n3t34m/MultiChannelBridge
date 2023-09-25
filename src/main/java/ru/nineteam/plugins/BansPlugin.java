package ru.nineteam.plugins;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import org.json.simple.parser.ParseException;
import ru.nineteam.*;

import javax.annotation.Nonnull;

import space.arim.libertybans.api.*;
import space.arim.libertybans.api.event.PunishEvent;
import space.arim.libertybans.api.punish.DraftPunishment;
import space.arim.libertybans.api.punish.PunishmentRevoker;
import space.arim.libertybans.api.punish.RevocationOrder;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.EventConsumer;
import space.arim.omnibus.events.ListenerPriorities;


import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class BansPlugin implements IMessageReceiver {
    private static Omnibus omnibus;
    private static LibertyBans libertyBans;

    public BansPlugin() {
    }

    boolean isAllowed(long userId) {
        return TelegramBridge.getInstance().getConfig().getOperatorList().contains(userId);
    }
    void reportBan(String opName, Victim victim, String reason, Duration duration) {
        System.out.println(duration);
        try {
            String formattedDuration = libertyBans.getFormatter().formatDuration(duration);
            String draftString = "";
            switch (victim.getType()) {
                case PLAYER -> {
                    var _victim = (PlayerVictim) victim;
                    var victimName = libertyBans.getUserResolver().lookupName(_victim.getUUID()).get().get();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.BannedByNickname
                        .replace("{operatorName}", opName)
                        .replace("{victimName}", victimName)
                        .replace("{duration}", formattedDuration)
                        .replace("{reason}", reason);

                }
                case ADDRESS -> {
                    var _victim = (AddressVictim) victim;
                    var victimIP = _victim.getAddress().toInetAddress();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.BannedByIp
                        .replace("{operatorName}", opName)
                        .replace("{victimIP}", victimIP.toString())
                        .replace("{duration}", formattedDuration)
                        .replace("{reason}", reason);
                }
            }
            TelegramBridge.getInstance().getSender().sendMessage(
                    TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                    draftString, "HTML",
                    1L);

        } catch (ParseException | IOException | ExecutionException | InterruptedException e) {
           e.printStackTrace();
        }
    }
    void reportKick(String opName, Victim victim, String reason) {
        try {
            String draftString = ".";
            switch (victim.getType()) {
                case PLAYER -> {
                    var _victim = (PlayerVictim) victim;
                    var victimName = libertyBans.getUserResolver().lookupName(_victim.getUUID()).get().get();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.KickedByNickname
                            .replace("{operatorName}", opName)
                            .replace("{victimName}", victimName)
                            .replace("{reason}", reason);

                }
                case ADDRESS -> {
                    var _victim = (AddressVictim) victim;
                    var victimIP = _victim.getAddress().toInetAddress();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.KickedByIp
                            .replace("{operatorName}", opName)
                            .replace("{victimIP}", victimIP.toString())
                            .replace("{reason}", reason);
                }
            }
            TelegramBridge.getInstance().getSender().sendMessage(
                    TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                    draftString,
                    "HTML",
                    1L);

        } catch (ExecutionException | InterruptedException | ParseException | IOException e) {
            e.printStackTrace();
        }
    }
    void report(PunishEvent punishEvent) {
        var draft = punishEvent.getDraftPunishment();
        System.out.println(draft.getDuration());
        System.out.println(draft.getOperator());
        System.out.println(draft.getType());
        System.out.println(draft.getVictim());
        var operatorName = "Консоль";

        if (!draft.getOperator().getType().equals(Operator.OperatorType.CONSOLE)) {
            var op = (PlayerOperator) draft.getOperator();
            try {
                operatorName = libertyBans.getUserResolver().lookupName(op.getUUID()).get().get();
            } catch (Exception e) {
                e.printStackTrace();
                operatorName = "Консоль";
            }
        }
        try {
            switch (draft.getType()) {
                case BAN -> reportBan(operatorName, draft.getVictim(), draft.getReason(), draft.getDuration());
                case KICK -> reportKick(operatorName, draft.getVictim(), draft.getReason());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void init() {
        omnibus = OmnibusProvider.getOmnibus();
        libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();
        EventConsumer<PunishEvent> listener = this::report;
        omnibus.getEventBus().registerListener(PunishEvent.class, ListenerPriorities.NORMAL, listener);
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

    @Subscribe()
    public void onSomething(ListenerBoundEvent e) {
        System.out.println("proxy initialize bansplugin");
        if (omnibus == null) {
            System.out.println("initialize");
            init();
        }
    }

    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        String cmd = messageObject.getCommand();

        if(omnibus==null)init();
        List<String> args = messageObject.getArgs();
        switch (cmd) {
            case "/kick" -> {
                if (args.size() >=2 && isAllowed(messageObject.getFrom().getId())) {
                    String reason = "";
                    String playerNickname = args.get(1);
                    if (args.size() >= 3) reason = args.get(2);
                    try {
                        var resolvedNickname = libertyBans.getUserResolver().lookupUUID(playerNickname).get();
                        if (resolvedNickname.isPresent()) {
                            DraftPunishment kickOrder = libertyBans.getDrafter().draftBuilder()
                                    .type(PunishmentType.KICK)
                                    .victim(PlayerVictim.of(resolvedNickname.get()))
                                    .reason(reason)
                                    .build();
                            kickOrder.enactPunishment();
                        }

                    } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); };

                }
            }
            case "/unpunish" -> {
                if (args.size() >= 2 && isAllowed(messageObject.getFrom().getId())) {
                    try {
                        String playerNickname = args.get(1);
                        var resolvedNickname = libertyBans.getUserResolver().lookupUUID(playerNickname).get();
                        if (resolvedNickname.isPresent()) {
                            clearPunishments(resolvedNickname.get(), messageObject.getChat().getId(), messageObject.getMessageThreadId());
                        } else {
                            TelegramBridge.getInstance().getSender().sendMessage(
                                TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                                "Ooops. resolvedNickname.isPresent() == false.", "HTML",
                                messageObject.getMessageThreadId());
                        }

                    } catch (InterruptedException | ExecutionException | ParseException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        return true;
    }
}
