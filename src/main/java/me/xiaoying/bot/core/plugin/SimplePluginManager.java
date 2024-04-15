package me.xiaoying.bot.core.plugin;

import me.xiaoying.bot.core.Xyb;
import me.xiaoying.bot.core.event.*;
import me.xiaoying.bot.core.server.Server;
import me.xiaoying.bot.core.utils.Preconditions;
import me.xiaoying.logger.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plugin PluginManager Simple
 */
public class SimplePluginManager implements PluginManager {
    private final Server server;
    private final List<Plugin> plugins = new ArrayList<>();
    private final Map<String, Plugin> lookupNames = new HashMap<>();
    private final Map<Pattern, PluginLoader> fileAssociations = new HashMap<>();

    public SimplePluginManager(Server instance) {
        this.server = instance;
    }

    @Override
    public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException {
        PluginLoader instance;
        if (!PluginLoader.class.isAssignableFrom(loader))
            throw new IllegalArgumentException(String.format("Class %s does not implement interface PluginLoader", loader.getName()));

        try {
            Constructor<? extends PluginLoader> constructor = loader.getConstructor(Server.class);
            instance = constructor.newInstance(this.server);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        Pattern[] patterns = instance.getPluginFileFilters();

        synchronized (this) {
            for (Pattern pattern : patterns)
                this.fileAssociations.put(pattern, instance);
        }
    }

    @Override
    public Plugin getPlugin(String name) {
        return this.lookupNames.get(name.replace(' ', '_'));
    }

    @Override
    public Plugin[] getPlugins() {
        return this.plugins.toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(String name) {
        Plugin plugin = this.getPlugin(name);
        return this.isPluginEnabled(plugin);
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && this.plugins.contains(plugin) ? plugin.isEnabled() : false;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        Preconditions.checkArgument(file != null, "File cannot be null");
        Set<Pattern> filters = this.fileAssociations.keySet();
        Plugin result = null;

        for (Pattern filter : filters) {
            String name = file.getName();

            Matcher match = filter.matcher(name);
            if (match.find()) {
                PluginLoader loader = this.fileAssociations.get(filter);
                result = loader.loadPlugin(file);
            }
        }

        if (result != null) {
            this.plugins.add(result);
            this.lookupNames.put(result.getDescription().getName(), result);
        }

        return result;
    }

    @Override
    public Plugin[] loadPlugins(File directory) {
        Preconditions.checkArgument(directory != null, "Directory cannot be null");
        Preconditions.checkArgument(directory.isDirectory(), "Directory must be a directory");

        Set<Pattern> filters = this.fileAssociations.keySet();
        File[] files;
        int count = Objects.requireNonNull(files = directory.listFiles()).length;

        for (int i = 0; i < count; i++) {
            File file = files[i];

            PluginLoader pluginLoader = null;
            Iterator<Pattern> iterator = filters.iterator();

            Pattern description;
            while (iterator.hasNext()) {
                description = iterator.next();
                Matcher match = description.matcher(file.getName());
                if (match.find())
                    pluginLoader = this.fileAssociations.get(description);
            }

            if (pluginLoader == null)
                continue;

            PluginDescriptionFile descriptionFile = null;

            try {
                descriptionFile = pluginLoader.getPluginDescription(file);
            } catch (InvalidDescription e) {
                new LoggerFactory().getLogger().warn("Could not load '{}' in folder '{}'", file.getPath(), directory.getPath());
                e.printStackTrace();
            }

            try {
                Plugin plugin = new JavaPluginLoader(this.server).loadPlugin(file);
                this.plugins.add(plugin);
            } catch (InvalidPluginException e) {
                throw new RuntimeException(e);
            }
        }

        return this.plugins.toArray(new Plugin[0]);
    }

    @Override
    public void disablePlugins() {
        Plugin[] plugins = this.getPlugins();

        for (Plugin plugin : plugins)
            this.disablePlugin(plugin);
    }

    @Override
    public void clearPlugins() {
        synchronized(this) {
            this.disablePlugins();
            this.plugins.clear();
            HandlerList.unregisterAll();
        }
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin.isEnabled())
            return;

        plugin.getPluginloader().enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (!plugin.isEnabled())
            return;

        plugin.getPluginloader().disablePlugin(plugin);
        HandlerList.unregisterAll(plugin);
    }

    @Override
    public void callEvent(Event event) throws IllegalStateException {
        synchronized (this) {
            this.fireEvent(event);
        }
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListener();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled())
                continue;

            try {
                registration.callEvent(event);
            } catch (AuthorNagException e) {
                Xyb.getLogger().warn(e.getMessage());
            } catch (Throwable throwable) {
                Xyb.getLogger().warn("Could not pass event " + event.getClass().getName() + " to " + registration.getPlugin().getDescription().getName());
                throwable.printStackTrace();
            }
        }
    }


    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginloader().createRegisteredListeners(listener, plugin).entrySet())
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
    }

    @Override
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) {

    }

    private HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = this.getRegistrationClass(type).getDeclaredMethod("getHandlerList", new Class[0]);
            method.setAccessible(true);
            if (!Modifier.isStatic(method.getModifiers()))
                throw new IllegalAccessException("getHandlerList must be static");

            return (HandlerList)method.invoke(null);
        } catch (Exception var3) {
            throw new IllegalPluginAccessException("Error while registering listener for event type " + type.toString() + ": " + var3);
        }
    }

    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList", new Class[0]);
            return clazz;
        } catch (NoSuchMethodException noSuchMethodException) {
            if (clazz.getSuperclass() != null &&
                    !clazz.getSuperclass().equals(Event.class) &&
                    Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            }
            throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
        }
    }
}