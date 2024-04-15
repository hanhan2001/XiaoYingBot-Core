package me.xiaoying.bot.core.server;

import me.xiaoying.bot.core.command.CommandManager;
import me.xiaoying.bot.core.plugin.PluginManager;

/**
 * Server
 */
public interface Server {
    String getName();

    PluginManager getPluginManager();
    CommandManager getCommandManager();

    void run();
    void stop();
}