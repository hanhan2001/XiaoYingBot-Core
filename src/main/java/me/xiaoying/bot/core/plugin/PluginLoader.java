package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.event.Event;
import me.xiaoying.bot.core.event.Listener;
import me.xiaoying.bot.core.event.RegisteredListener;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Plugin Loader
 */
public interface PluginLoader {
    Plugin loadPlugin(File file) throws InvalidPluginException;
    PluginDescriptionFile getPluginDescription(File file) throws InvalidDescription;

    Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin);

    void delRegisteredListener(Listener listener);
    void delRegisteredListener(Plugin plugin);
    void delRegisteredListeners();
    List<RegisteredListener> getRegisteredListener();

    Pattern[] getPluginFileFilters();

    void enablePlugin(Plugin plugin);
    void disablePlugin(Plugin plugin);
}