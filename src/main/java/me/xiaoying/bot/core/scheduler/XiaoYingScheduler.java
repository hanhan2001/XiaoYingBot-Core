package me.xiaoying.bot.core.scheduler;

import me.xiaoying.bot.core.plugin.JavaPlugin;

public interface XiaoYingScheduler {
    void cancelTask(int task);

    void runTask(JavaPlugin plugin, Runnable runnable);

    int scheduleSyncDelayedTask(JavaPlugin plugin, Runnable runnable);
    int scheduleSyncDelayedTask(JavaPlugin plugin, Runnable runnable, long delay);
    int scheduleSyncRepeatingTask(JavaPlugin plugin, Runnable runnable, long delay, long period);

    int scheduleAsyncDelayedTask(JavaPlugin plugin, Runnable runnable);
    int scheduleAsyncDelayedTask(JavaPlugin plugin, Runnable runnable, long delay);
    int scheduleAsyncRepeatingTask(JavaPlugin plugin, Runnable runnable, long delay, long period);
}