package me.xiaoying.bot.core.entity;

import me.xiaoying.bot.core.command.CommandSender;

/**
 * Entity User
 */
public class User extends CommandSender {
    private Bot bot;
    private final long id;
    private final String name;

    public User(Bot bot, long id, String name) {
        this.bot = bot;
        this.id = id;
        this.name = name;
    }

    public Bot getBot() {
        return this.bot;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void sendMessage(String message) {
        this.bot.sendMessage(this, message);
    }
}