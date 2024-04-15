package me.xiaoying.bot.core.event;

/**
 * Event executor
 */
public interface EventExecutor {
    void execute(Listener listener, Event event) throws EventException;
}