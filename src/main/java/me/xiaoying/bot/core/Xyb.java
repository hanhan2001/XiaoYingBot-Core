package me.xiaoying.bot.core;

import me.xiaoying.bot.core.plugin.PluginManager;
import me.xiaoying.bot.core.server.Server;
import me.xiaoying.logger.Logger;
import me.xiaoying.logger.LoggerFactory;

public class Xyb {
    private static final Logger logger = new LoggerFactory().getLogger();
    private static Server server;

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        if (Xyb.server != null) {
            throw new UnsupportedOperationException("Cannot redefine singleton Server");
        }

        Xyb.server = server;
        logger.info("This server is running " + getName());
    }

    public static PluginManager getPluginManager() {
        return server.getPluginManager();
    }

    public static String getName() {
        return server.getName();
    }

    public static Logger getLogger() {
        return logger;
    }
}