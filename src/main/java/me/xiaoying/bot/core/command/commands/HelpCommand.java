package me.xiaoying.bot.core.command.commands;

import me.xiaoying.bot.core.Xyb;
import me.xiaoying.bot.core.command.Command;
import me.xiaoying.bot.core.command.CommandSender;

/**
 * Command Help
 */
public class HelpCommand extends Command {
    public HelpCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        Xyb.getLogger().info("");
    }
}