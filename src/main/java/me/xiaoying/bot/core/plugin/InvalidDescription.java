package me.xiaoying.bot.core.plugin;

/**
 * Plugin Exception Description
 */
public class InvalidDescription extends Exception {
    private static final long serialVersionUID = 5721389122281775896L;

    public InvalidDescription(Throwable cause) {
        super(cause);
    }

    public InvalidDescription(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDescription(String message) {
        super(message);
    }

    public InvalidDescription() {
        super();
    }
}