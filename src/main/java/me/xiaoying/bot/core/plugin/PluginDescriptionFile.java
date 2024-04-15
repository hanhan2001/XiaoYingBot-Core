package me.xiaoying.bot.core.plugin;

/**
 * Plugin Description
 */
public class PluginDescriptionFile {
    private String name;
    private String main;
    private String version;
    private String[] authors;
    private String description;

    public PluginDescriptionFile(String name, String main, String version, String[] authors, String description) {
        this.name = name;
        this.main = main;
        this.version = version;
        this.authors = authors;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getMain() {
        return this.main;
    }

    public String getVersion() {
        return this.version;
    }

    public String[] getAuthors() {
        return this.authors;
    }

    public String getDescription() {
        return this.description;
    }
}