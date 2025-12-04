package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

@ApplicationScoped
public class MessageBundle {

    private static final Logger LOG = Logger.getLogger(MessageBundle.class);

    // Base name: src/main/resources/messages/messages*.properties
    private static final String BUNDLE_BASE_NAME = "messages/messages";

    // Custom Control to read properties as UTF-8
    private static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(
                String baseName,
                Locale locale,
                String format,
                ClassLoader loader,
                boolean reload
        ) throws IllegalAccessException, InstantiationException, IOException {

            // Default implementation but forcing UTF-8
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream is = loader.getResourceAsStream(resourceName)) {
                if (is == null) {
                    return null;
                }
                try (InputStreamReader reader = new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }
    };

    public String msg(Locale locale, String key, Object... args) {
        Locale effectiveLocale = (locale != null) ? locale : Locale.getDefault();

        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, effectiveLocale, UTF8_CONTROL);
        } catch (MissingResourceException e) {
            // If the bundle is completely missing, this is a configuration error
            String msg = "Message bundle not found for base '" + BUNDLE_BASE_NAME +
                    "' and locale '" + effectiveLocale + "'";
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }

        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException e) {
            String msg = "Missing i18n key '" + key + "' for locale '" + effectiveLocale + "'";
            LOG.error(msg, e);
            // Explicit exception if key is missing (as requested)
            throw new IllegalArgumentException(msg, e);
        }

        if (args == null || args.length == 0) {
            return pattern;
        }

        return MessageFormat.format(pattern, args);
    }
}