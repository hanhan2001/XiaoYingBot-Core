package me.xiaoying.bot.core.plugin;

public class IllegalPluginAccessException extends RuntimeException {
    public IllegalPluginAccessException() {
    }

    public IllegalPluginAccessException(String msg) {
        super(msg);
    }
}