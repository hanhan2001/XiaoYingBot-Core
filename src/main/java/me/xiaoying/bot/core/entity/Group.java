package me.xiaoying.bot.core.entity;

public class Group {
    private final Bot bot;
    private final long id;
    private final String name;

    public Group(Bot bot, long id, String name) {
        this.bot = bot;
        this.id = id;
        this.name = name;
    }

    public Bot getBot() {
        return bot;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) {
        bot.sendMessage(this, message);
    }
}