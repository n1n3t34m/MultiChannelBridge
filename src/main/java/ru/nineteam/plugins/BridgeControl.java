package ru.nineteam.plugins;

import ru.nineteam.IMessageReceiver;
import ru.nineteam.TelegramBridge;
import ru.nineteam.TelegramMessage;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class BridgeControl implements IMessageReceiver {

    @Override
    public boolean onTelegramObjectMessage(@Nonnull TelegramMessage msg) {
        String[] params = msg.getText().split(" ");
        switch (params[0]) {
            case "/topic_id": {
                try { TelegramBridge.getInstance().getSender().sendMessage(msg.getChat().getId(), "Topic ID: " + msg.getMessageThreadId(), "html", msg.getMessageThreadId()); }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            case "/reload": {
                if (!TelegramBridge.getInstance().getConfig().getOperatorList().contains(msg.getFrom().getId())) { return false; }
                TelegramBridge.getInstance().createOrLoadConfig();
                return true;
            }
            case "/ping": {
                try { TelegramBridge.getInstance().getSender().sendMessage(msg.getChat().getId(), "Pong!", "html", msg.getMessageThreadId()); }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            default: return false;
        }
    }

}
