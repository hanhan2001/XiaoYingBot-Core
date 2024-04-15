package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.utils.Preconditions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin ClassLoader
 */
public class PluginClassLoader extends URLClassLoader {
    private final JavaPluginLoader loader;
    private final ClassLoader classLoader;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private ClassLoader libraryLoader;
    JavaPlugin plugin;
    private JavaPlugin pluginInit;
    private IllegalStateException pluginState;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public PluginClassLoader(JavaPluginLoader loader, ClassLoader classLoader, PluginDescriptionFile description, File dataFolder, File file, ClassLoader libraryLoader) throws IOException, InvalidPluginException {
        super(new URL[]{file.toURI().toURL()}, classLoader);
        this.loader = loader;
        this.classLoader = classLoader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.libraryLoader = libraryLoader;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            Class<?> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException e) {
                throw new InvalidPluginException("main class `" + description.getMain() + "` dose not extends JavaPlugin", e);
            }

            this.plugin = (JavaPlugin) pluginClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("No public constructor", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Abnormal plugin type", e);
        }
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        if (name.startsWith("me.xiaoying.bot."))
            throw new ClassNotFoundException(name);

        Class<?> result = this.classes.get(name);
        if (result != null)
            return result;
        if (checkGlobal)
            result = this.loader.getClassByName(name);
        if (result == null) {
            result = super.findClass(name);
            if (result != null) this.loader.setClass(name, result);
        }
        this.classes.put(name, result);
        return result;
    }

    Set<String> getClasses() {
        return this.classes.keySet();
    }

    synchronized void initialize(JavaPlugin javaPlugin) {
        Preconditions.checkArgument(javaPlugin != null, "Initializing plugin cannot be null");
        Preconditions.checkArgument(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
        if (this.plugin != null || this.pluginInit != null)
            throw new IllegalArgumentException("Plugin already initialized!", this.pluginState);

        this.pluginState = new IllegalStateException("Initial initialization");
        this.pluginInit = javaPlugin;
    }
}