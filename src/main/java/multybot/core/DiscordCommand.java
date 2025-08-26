package multybot.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordCommand {
    String name();
    String descriptionKey(); // clave i18n (p.e. "ping.description")
}
