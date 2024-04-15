package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.Xyb;
import me.xiaoying.bot.core.configuration.YamlConfiguration;
import me.xiaoying.bot.core.configuration.serialization.ConfigurationSerializable;
import me.xiaoying.bot.core.configuration.serialization.ConfigurationSerialization;
import me.xiaoying.bot.core.event.Event;
import me.xiaoying.bot.core.event.Listener;
import me.xiaoying.bot.core.event.RegisteredListener;
import me.xiaoying.bot.core.server.Server;
import me.xiaoying.bot.core.utils.Preconditions;
import me.xiaoying.bot.core.utils.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Plugin Loader java
 */
public class JavaPluginLoader implements PluginLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Map<String, PluginClassLoader> loaders = new LinkedHashMap<>();
    private final Pattern[] fileFilters = new Pattern[] {Pattern.compile("\\.jar$")};
    private final List<RegisteredListener> registeredListeners = new ArrayList<>();
    final Server server;

    public JavaPluginLoader(Server instance) {
        Preconditions.checkArgument(instance != null, "Server cannot be null");
        this.server = instance;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException {
        Preconditions.checkArgument(file != null, "File cannot be null");

        PluginDescriptionFile description;
        PluginClassLoader loader;

        if (!file.exists())
            throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exists"));

        try {
            description = this.getPluginDescription(file);
        } catch (InvalidDescription e) {
            throw new RuntimeException(e);
        }

        File parentFile = file.getParentFile();
        File dataFolder = new File(parentFile, description.getName());

        try {
            loader = new PluginClassLoader(this, this.getClass().getClassLoader(), description, dataFolder, file, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return loader.plugin;
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescription {
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(ZipUtil.getFile(file.getPath(), "plugin.yml"));
        String name = yamlConfiguration.getString("name");
        Preconditions.checkNotNull(name, "Plugin name cannot be null");
        String main = yamlConfiguration.getString("main");
        Preconditions.checkNotNull(main, "Main class cannot be null");
        String version = yamlConfiguration.getString("version");
        Preconditions.checkNotNull(version, "Plugin version cannot be null");
        List<String> authors = new ArrayList<>();
        if (yamlConfiguration.getString("author") != null && !yamlConfiguration.getString("author").isEmpty())
            authors.add(yamlConfiguration.getString("author"));

        if (yamlConfiguration.getStringList("authors") != null && !yamlConfiguration.getStringList("authors").isEmpty())
            authors.addAll(yamlConfiguration.getStringList("authors"));
        return new PluginDescriptionFile(name, main, version, authors.toArray(new String[0]), main);
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        return Collections.emptyMap();
    }

    @Override
    public void delRegisteredListener(Listener listener) {
        Iterator<RegisteredListener> iterator = this.registeredListeners.iterator();
        RegisteredListener registeredListener;
        while (iterator.hasNext() && (registeredListener = iterator.next()) != null) {
            if (registeredListener.getListener() != listener)
                continue;

            iterator.remove();
        }
    }

    @Override
    public void delRegisteredListener(Plugin plugin) {
        Iterator<RegisteredListener> iterator = this.registeredListeners.iterator();
        RegisteredListener registeredListener;
        while (iterator.hasNext() && (registeredListener = iterator.next()) != null) {
            if (registeredListener.getPlugin() != plugin)
                continue;

            iterator.remove();
        }
    }

    @Override
    public void delRegisteredListeners() {
        this.registeredListeners.clear();
    }

    @Override
    public List<RegisteredListener> getRegisteredListener() {
        return Collections.emptyList();
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return this.fileFilters;
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        Preconditions.isTrue(plugin instanceof JavaPlugin, "Plugin is not associated with this PluginLoader");

        if (plugin.isEnabled())
            return;

        String message = String.format("Enabling %s %s by %s", plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthors());
        Xyb.getLogger().info(message);

        JavaPlugin jPlugin = (JavaPlugin) plugin;
        String pluginName = jPlugin.getDescription().getName();

        if (!this.loaders.containsKey(pluginName))
            this.loaders.put(pluginName, (PluginClassLoader) jPlugin.getClassLoader());

        try {
            jPlugin.setEnabled(true);
        } catch (Throwable ex) {
            Xyb.getLogger().warn("Error occurred while enabling {} (Is it up to date?)\n{}",plugin.getDescription().getName(), ex.getMessage());
            ex.printStackTrace();
        }
//        this.server.getPluginManager().callEvent((Event) new PluginEnableEvent(plugin));
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        Preconditions.isTrue(plugin instanceof JavaPlugin, "Plugin is not associated with this PluginLoader");

        if (!plugin.isEnabled())
            return;

        String message = String.format("Disabling %s", plugin.getDescription().getName());
        Xyb.getLogger().info(message);

//        this.server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

        JavaPlugin jPlugin = (JavaPlugin) plugin;
        ClassLoader cloader = jPlugin.getClassLoader();
        try {
            jPlugin.setEnabled(false);
        } catch (Throwable ex) {
            Xyb.getLogger().warn("Error occurred while disabling {} (Is it up to date?)\n{}", plugin.getDescription().getName(), ex.getMessage());
        }

        this.loaders.remove(jPlugin.getDescription().getName());
        if (!(cloader instanceof PluginClassLoader))
            return;

        PluginClassLoader loader = (PluginClassLoader) cloader;
        Set<String> names = loader.getClasses();

        for (String name : names)
            removeClass(name);
    }

    private void removeClass(String name) {
        Class<?> clazz = this.classes.remove(name);

        try {
            if (clazz != null && ConfigurationSerializable.class.isAssignableFrom(clazz)) {
                Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
                ConfigurationSerialization.unregisterClass(serializable);
            }
        } catch (NullPointerException nullPointerException) {}
    }

    Class<?> getClassByName(String name) {
        Class<?> cachedClass = this.classes.get(name);
        if (cachedClass != null)
            return cachedClass;

        for (String s : this.loaders.keySet()) {
            PluginClassLoader loader = this.loaders.get(s);

            try { cachedClass = loader.findClass(name, false); } catch (ClassNotFoundException e) {}
            if (cachedClass != null)
                return cachedClass;
        }
        return null;
    }

    void setClass(String name, Class<?> clazz) {
        if (this.classes.containsKey(name))
            return;

        this.classes.put(name, clazz);
        if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
            Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
            ConfigurationSerialization.registerClass(serializable);
        }
    }
}