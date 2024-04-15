package me.xiaoying.bot.core.command;

import me.xiaoying.bot.core.NamespacedKey;
import me.xiaoying.bot.core.command.commands.HelpCommand;
import me.xiaoying.bot.core.plugin.Plugin;

import java.util.*;

/**
 * Command commandMap SimpleCommandMap
 */
public class SimpleCommandManager implements CommandManager {
    private final Map<String, Command> knownCommand = new HashMap<>();

    public SimpleCommandManager() {
        this.registerCommand("xiaoyingbot", new HelpCommand("help"));
    }

    @Override
    public void registerCommand(String fallbackPrefix, Command command) {
        this.knownCommand.put(fallbackPrefix + ":" + command.getName(), command);
    }

    public void registerCommand(Plugin plugin, Command command) {
        this.knownCommand.put(new NamespacedKey(plugin, command.getName()).toString(), command);

        for (String alias : command.getAlias())
            this.knownCommand.put(new NamespacedKey(plugin, alias).toString(), command);
    }

    public void unregisterCommand(Plugin plugin, Command command) {
        this.knownCommand.remove(new NamespacedKey(plugin, command.getName()).toString());

        if (command.getAlias().isEmpty())
            return;

        for (String alias : command.getAlias())
            this.knownCommand.remove(new NamespacedKey(plugin, alias).toString());
    }

    public void unregisterCommands(Plugin plugin) {
        Iterator<String> iterator = this.knownCommand.keySet().iterator();
        String string;
        while (iterator.hasNext() && (string = iterator.next()) != null) {
            if (string.isEmpty())
                continue;

            if (!string.startsWith(plugin.getDescription().getName() + ":"))
                continue;

            iterator.remove();
        }
    }

    @Override
    public boolean dispatch(CommandSender sender, String command) {
        boolean result = false;
        if (command == null || command.isEmpty())
            return result;

        String[] split = command.split(" ");

        if (split.length == 0)
            return result;

        String head = split[0];
        head = this.matchCommand(head);
        if (head == null)
            return result;

        Command cmd = this.knownCommand.get(head);
        if (cmd == null)
            return false;

        String[] args = null;
        if (split.length != 1)
            args = new ArrayList<>(Arrays.asList(split).subList(1, split.length)).toArray(new String[0]);
        cmd.execute(sender, args);
        return true;
    }

    @Override
    public Command getCommand(String command) {
        return null;
    }

    private String matchCommand(String command) {
        if (command.contains(":"))
            return command;

        for (String s : this.knownCommand.keySet()) {
            if (!s.endsWith(":" + command))
                continue;
            if (s.startsWith("xiaoying:"))
                continue;

            return s;
        }

        if (this.knownCommand.get("xiaoying:" + command) != null)
            return "xiaoying:" + command;
        return null;
    }
}