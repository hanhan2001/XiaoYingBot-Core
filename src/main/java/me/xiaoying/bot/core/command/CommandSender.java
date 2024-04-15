package me.xiaoying.bot.core.command;

import me.xiaoying.bot.core.Xyb;
import me.xiaoying.bot.core.entity.User;

/**
 * Command Sender
 */
public class CommandSender {
    public void sendMessage(String message) {
        if (this instanceof User) {
            User user = (User) this;
            user.sendMessage(message);
            return;
        }

        Xyb.getLogger().info(message);
    }
}