package me.xiaoying.bot.core.event.bot;

import me.xiaoying.bot.core.entity.Bot;
import me.xiaoying.bot.core.event.Event;
import me.xiaoying.bot.core.event.HandlerList;

/**
 * Event bot
 */
public class BotEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Bot bot;

    public BotEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return this.bot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}