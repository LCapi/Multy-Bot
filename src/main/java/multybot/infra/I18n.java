package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;

/** Carga bundles messages_*.properties en UTF-8.
 *  Si falta una clave en el locale actual, cae a EN.
 */
@ApplicationScoped
public class I18n {

    private static final String BUNDLE_BASE = "messages";

    public String msg(Locale locale, String key, Object... args) {
        if (locale == null) locale = Locale.ENGLISH;

        ResourceBundle current = getBundle(locale);
        String pattern = null;
        if (current != null && current.containsKey(key)) {
            pattern = current.getString(key);
        } else {
            ResourceBundle en = getBundle(Locale.ENGLISH);
            if (en != null && en.containsKey(key)) {
                pattern = en.getString(key);
            }
        }
        if (pattern == null) {
            // última red: devuelve la clave
            pattern = key;
        }

        MessageFormat mf = new MessageFormat(pattern, locale);
        return mf.format(args);
    }

    private ResourceBundle getBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(BUNDLE_BASE, locale, new UTF8Control());
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /** Control para leer properties como UTF-8 (no ISO-8859-1). */
    public static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            var stream = loader.getResourceAsStream(resourceName);
            if (stream == null) return null;
            try (var reader = new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
                var props = new Properties();
                props.load(reader);
                return new ResourceBundle() {
                    @Override protected Object handleGetObject(String key) { return props.getProperty(key); }
                    @Override public Enumeration<String> getKeys() {
                        return Collections.enumeration(props.stringPropertyNames());
                    }
                    @Override public boolean containsKey(String key) { return props.containsKey(key); }
                };
            }
        }
    }
}
