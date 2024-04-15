package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.Xyb;
import me.xiaoying.bot.core.command.Command;
import me.xiaoying.bot.core.server.Server;
import me.xiaoying.bot.core.utils.Preconditions;
import me.xiaoying.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * Plugin access for other user
 */
public class JavaPlugin extends PluginBase {
    private JavaPluginLoader loader;
    private PluginDescriptionFile description;
    private File dataFolder;
    private File file;
    private ClassLoader classLoader;
    private Server server;
    private boolean isEnabled;

    public JavaPlugin() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof PluginClassLoader))
            throw new IllegalStateException("JavaPlugin requires " + PluginClassLoader.class.getName());
        else
            ((PluginClassLoader) classLoader).initialize(this);
    }

    protected JavaPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        ClassLoader classLoader = this.getClass().getClassLoader();;

        if (classLoader instanceof PluginClassLoader)
            throw new IllegalStateException("Cannot use initialization constructor at runtime");

        this.init(loader, description, dataFolder, file, classLoader);
    }

    final void init(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file, ClassLoader classLoader) {
        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.server = loader.server;
        this.classLoader = classLoader;
    }

    protected final ClassLoader getClassLoader() {
        return this.classLoader;
    }

    protected final void setEnabled(boolean enabled) {
        if (this.isEnabled == enabled)
            return;

        this.isEnabled = enabled;
        if (this.isEnabled)
            this.onEnable();
        else
            this.onDisable();
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void saveConfig() {
        if (new File("config.yml").exists())
            return;

        this.saveResource("config.yml", false);
    }

    @Override
    public void saveDefaultConfig() {
        this.saveResource("config.yml", false);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals(""))
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null)
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.file);

        File outFile = new File(this.dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists())
            outDir.mkdirs();

        try {
            if (outFile.exists() && !replace)
                new LoggerFactory().getLogger().warn("Could not save {} to {} because {} already exists.", outFile.getName(), outFile.toString(), outFile.getName());

            OutputStream out = Files.newOutputStream(outFile.toPath());
            byte[] buf = new byte[in.available()];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            out.close();
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void registerCommand(Command command) {
        Xyb.getServer().getCommandManager().registerCommand(this, command);
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public PluginLoader getPluginloader() {
        return this.loader;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return this.description;
    }

    /**
     * 获取包内文件
     *
     * @param filename 文件名称
     * @return InputStream
     */
    public InputStream getResource(String filename) {
        if (filename == null)
            throw new IllegalArgumentException("Filename cannot be null");

        try {
            URL url = JavaPlugin.class.getClassLoader().getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException iOException) {
            return null;
        }
    }

    public static <T extends JavaPlugin> T getPlugin(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Null class cannot have a plugin");
        if (!JavaPlugin.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException(clazz + " does not extends " + JavaPlugin.class);

        ClassLoader classLoader = clazz.getClassLoader();
        if (!(classLoader instanceof PluginClassLoader))
            throw new IllegalArgumentException(clazz + " is not initialized by " + PluginClassLoader.class);

        JavaPlugin plugin = ((PluginClassLoader) classLoader).plugin;
        if (plugin == null)
            throw new IllegalStateException("Cannot get plugin for " + clazz + " form a static initializer");
        return clazz.cast(plugin);
    }

    public static JavaPlugin getProvidingPlugin(Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "Null class cannot have a plugin");
        ClassLoader classLoader = clazz.getClassLoader();
        if (!(classLoader instanceof PluginClassLoader))
            throw new IllegalArgumentException(clazz + " is not provided by " + PluginClassLoader.class);

        JavaPlugin plugin = ((PluginClassLoader) classLoader).plugin;
        if (plugin == null)
            throw new IllegalStateException("Cannot get plugin for " + clazz + " form a static initializer");
        return plugin;
    }
}