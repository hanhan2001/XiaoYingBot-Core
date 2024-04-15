package me.xiaoying.bot.core.command;

import java.util.List;

/**
 * Command
 */
public abstract class Command {
    private String name;
    private String description;
    private String usage;
    private List<String> alias;

    public Command(String name) {
        this(name, "a default command", "/" + name, null);
    }

    public Command(String name, String description, String usage, List<String> alias) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.alias = alias;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUsage() {
        return this.usage;
    }

    public List<String> getAlias() {
        return this.alias;
    }
}