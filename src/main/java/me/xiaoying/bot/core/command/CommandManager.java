package me.xiaoying.bot.core.command;

import me.xiaoying.bot.core.plugin.Plugin;

/**
 * Command commandMap
 */
public interface CommandManager {
    void registerCommand(String fallbackPrefix, Command command);

    void registerCommand(Plugin plugin, Command command);

    void unregisterCommand(Plugin plugin, Command command);

    void unregisterCommands(Plugin plugin);

    boolean dispatch(CommandSender sender, String command);

    Command getCommand(String command);
}