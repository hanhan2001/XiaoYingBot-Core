package me.xiaoying.bot.core.event;

import me.xiaoying.bot.core.plugin.Plugin;

/**
 * Event RegisteredListener
 */
public class RegisteredListener {
    private final Listener listener;
    private final EventExecutor executor;
    private final EventPriority priority;
    private final Plugin plugin;

    public RegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, Plugin plugin) {
        this.listener = listener;
        this.executor = executor;
        this.priority = priority;
        this.plugin = plugin;
    }

    public void callEvent(Event event) throws EventException {
        if (event == null || event.isCancelled())
            return;

        this.executor.execute(this.listener, event);
    }

    public Listener getListener() {
        return listener;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}