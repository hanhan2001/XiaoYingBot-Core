package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.event.Event;
import me.xiaoying.bot.core.event.EventExecutor;
import me.xiaoying.bot.core.event.EventPriority;
import me.xiaoying.bot.core.event.Listener;

import java.io.File;

/**
 * Plugin PluginManager
 */
public interface PluginManager {
    void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException;

    Plugin getPlugin(String name);

    Plugin[] getPlugins();

    boolean isPluginEnabled(String name);

    boolean isPluginEnabled(Plugin plugin);

    Plugin loadPlugin(File directory) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException;

    Plugin[] loadPlugins(File directory);

    void disablePlugins();

    void clearPlugins();

    void enablePlugin(Plugin plugin);

    void disablePlugin(Plugin plugin);

    void callEvent(Event event) throws IllegalStateException;

    void registerEvents(Listener listener, Plugin plugin);

    void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin);
}