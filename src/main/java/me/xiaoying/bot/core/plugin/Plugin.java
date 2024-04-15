package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.command.Command;
import me.xiaoying.bot.core.server.Server;

import java.io.File;

/**
 * Plugin
 */
public interface Plugin {
    File getDataFolder();

    void onLoad();
    void onEnable();
    void onDisable();
    boolean isEnabled();

    void saveConfig();
    void saveDefaultConfig();
    void saveResource(String filename, boolean replace);
    void registerCommand(Command command);

    Server getServer();

    PluginLoader getPluginloader();
    PluginDescriptionFile getDescription();
}