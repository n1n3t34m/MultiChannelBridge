package ru.nineteam.plugins;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
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
    private Logger logger;
    public BansPlugin(Logger logger) {
        this.logger = logger;
    }
    boolean isAllowed(long userId) {
        return TelegramBridge.getInstance().getConfig().getOperatorList().contains(userId);
    }
    void reportBan(String opName, Victim victim, String reason, Duration duration, String serverName) {
        TelegramBridge.getInstance().getLogger().info(String.valueOf(duration));

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
                            .replace("{serverName}", serverName)
                            .replace("{reason}", reason);

                }
                case ADDRESS -> {
                    var _victim = (AddressVictim) victim;
                    var victimIP = _victim.getAddress().toInetAddress();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.BannedByIp
                            .replace("{operatorName}", opName)
                            .replace("{victimIP}", victimIP.toString())
                            .replace("{duration}", formattedDuration)
                            .replace("{reason}", reason)
                            .replace("{serverName}", serverName);
                }
            }
            TelegramBridge.getInstance().getSender().sendMessage(
                    TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                    draftString, "HTML",
                    TelegramBridge.getInstance().getConfig().getTelegramLogThread());

        } catch (ParseException | IOException | ExecutionException | InterruptedException e) {
           e.printStackTrace();
        }
    }
    void reportKick(String opName, Victim victim, String reason, String serverName) {
        try {
            String draftString = ".";
            switch (victim.getType()) {
                case PLAYER -> {
                    var _victim = (PlayerVictim) victim;
                    var victimName = libertyBans.getUserResolver().lookupName(_victim.getUUID()).get().get();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.KickedByNickname
                            .replace("{operatorName}", opName)
                            .replace("{victimName}", victimName)
                            .replace("{reason}", reason)
                            .replace("{serverName}", serverName);

                }
                case ADDRESS -> {
                    var _victim = (AddressVictim) victim;
                    var victimIP = _victim.getAddress().toInetAddress();
                    draftString = TelegramBridge.getInstance().getConfig().getStrings().bansPluginMessages.KickedByIp
                            .replace("{operatorName}", opName)
                            .replace("{victimIP}", victimIP.toString())
                            .replace("{reason}", reason)
                            .replace("{serverName}", serverName);
                }
            }
            TelegramBridge.getInstance().getSender().sendMessage(
                    TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                    draftString,
                    "HTML",
                    TelegramBridge.getInstance().getConfig().getTelegramLogThread());

        } catch (ExecutionException | InterruptedException | ParseException | IOException e) {
            e.printStackTrace();
        }
    }
    void report(PunishEvent punishEvent) {
        var draft = punishEvent.getDraftPunishment();

        var operatorName = "Консоль";
        var serverName = "staff";

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
            TelegramBridge.getInstance().getConfig().getTelegramLogThread();
            switch (draft.getType()) {
                case BAN -> reportBan(operatorName, draft.getVictim(), draft.getReason(), draft.getDuration(), serverName);
                case KICK -> reportKick(operatorName, draft.getVictim(), draft.getReason(), serverName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void init() {
        TelegramBridge.getInstance().getLogger().info("init()");
        omnibus = OmnibusProvider.getOmnibus();
        var libertyBansProvider = omnibus.getRegistry().getProvider(LibertyBans.class);
        if (libertyBansProvider.isPresent()) {
            libertyBans = libertyBansProvider.get();
            EventConsumer<PunishEvent> listener = this::report;
            omnibus.getEventBus().registerListener(PunishEvent.class, ListenerPriorities.HIGHEST, listener);
            logger.error("libertybans provider is alive");
        } else {
            logger.error("libertybans provider is dead");
        }

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
        logger.info("initialize bansplugin");
        if (omnibus == null || libertyBans == null) {
            logger.info("omnibus||libertyballs == null ; init();");
            init();
        }
    }

    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage messageObject) {
        String cmd = messageObject.getCommand();

        if(omnibus==null||libertyBans==null)init();
        List<String> args = messageObject.getArgs();


        switch (cmd) {
            case "/ban" -> {
                if (args.size() < 2) {
                    try {
                        TelegramBridge.getInstance().getSender().sendMessage(
                                TelegramBridge.getInstance().getConfig().getTelegramChatId(),
                                "/ban $nickname $1m1d12h $reason", "HTML",
                                messageObject.getMessageThreadId());
                    } catch (ParseException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String duration = "1d";
                String reason = "";

                String playerNickname = args.get(1);

                if (args.size() >= 3) duration = args.get(2);
                if (args.size() >= 4) reason = String.join(" ", args.subList(3, args.size()));
                try {
                    var resolvedNickname = libertyBans.getUserResolver().lookupUUID(playerNickname).get();
                    if(resolvedNickname.isPresent()) {
                        DraftPunishment banOrder = libertyBans.getDrafter().draftBuilder()
                                .type(PunishmentType.BAN)
                                .victim(PlayerVictim.of(resolvedNickname.get()))
                                .reason(reason)
                                .duration(Duration.parse("P"+duration))
                                .build();
                        banOrder.enactPunishment();
                    }
                } catch (ExecutionException | InterruptedException e) {e.printStackTrace(); }
            }
            case "/kick" -> {
                if (args.size() >=2 && isAllowed(messageObject.getFrom().getId())) {
                    String reason = "";
                    String playerNickname = args.get(1);
                    if (args.size() >= 3) reason = String.join(" ", args.subList(2, args.size()));
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
