package tw.kid7.BannerMaker.util;

import org.bukkit.ChatColor;
import tw.kid7.BannerMaker.configuration.Language;

public class MessageUtil {

    public static String format(String message) {
        return format(false, message);
    }

    public static String format(boolean addPrefix, String message) {
        if (addPrefix) {
            message = Language.get("general.prefix") + message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String cutString(String string, int length) {
        if (string.length() > length) {
            string = string.substring(0, length);
        }
        return string;
    }
}
