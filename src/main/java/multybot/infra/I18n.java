package dev.lcapi.multybot.infra;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class I18n {
    public String msg(Locale locale, String key, Object... args) {
        ResourceBundle rb = ResourceBundle.getBundle("messages", locale);
        String pattern = rb.getString(key);
        return MessageFormat.format(pattern, args);
    }
}
