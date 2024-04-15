package me.xiaoying.bot.core.plugin;

/**
 * Plugin Exception Invalid
 */
public class InvalidPluginException extends Exception {
    private static final long serialVersionUID = -8242141640709409544L;

    public InvalidPluginException() {
        super();
    }

    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPluginException(Throwable cause) {
        super(cause);
    }
}