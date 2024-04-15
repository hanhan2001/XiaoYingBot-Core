package me.xiaoying.bot.core.entity;

/**
 * Entity bot
 */
public interface Bot {
    long getId();

    String getName();
    Friend getFriend(long id);
    Stranger getStranger(long id);
    Group getGroup(long id);

    boolean isOnline();

    void sendMessage(Group group, String message);
    void sendMessage(User user, String message);
}