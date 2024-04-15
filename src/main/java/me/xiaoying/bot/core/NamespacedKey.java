package me.xiaoying.bot.core;

import me.xiaoying.bot.core.plugin.Plugin;

import java.util.Locale;

public class NamespacedKey {
    private final String namespace;
    private final String key;

    public NamespacedKey(Plugin plugin, String key) {
        this.namespace = plugin.getDescription().getName().toLowerCase(Locale.ROOT);
        this.key = key;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.key;
    }
}