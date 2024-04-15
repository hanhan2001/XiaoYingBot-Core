package me.xiaoying.bot.core.entity;

public class Friend extends User {
    public Friend(Bot bot, long id, String name) {
        super(bot, id, name);
    }

    public Friend(Bot bot, long id) {
        this(bot, id, bot.getFriend(id).getName());
    }
}