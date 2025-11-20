package multybot.i18n;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@ApplicationScoped
public class MessageBundle {

    // Base name for ResourceBundle: maps to src/main/resources/messages/messages_*.properties
    private static final String BASENAME = "messages.messages";

    public String get(Locale locale, String key, Object... args) {
        Locale effective = (locale != null ? locale : Locale.ENGLISH);
        ResourceBundle bundle = ResourceBundle.getBundle(BASENAME, effective);

        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException ex) {
            // Fallback: return the key itself if not found
            return "!" + key + "!";
        }

        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }
}