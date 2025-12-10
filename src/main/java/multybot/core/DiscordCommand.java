package multybot.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface DiscordCommand {

    /**
     * Slash command name (ej: "ping", "ban", "poll"...)
     */
    String name();

    /**
     * Key en el bundle de i18n para la descripción base.
     */
    String descriptionKey();

    /**
     * Allow to activate/disable comands.
     * if false the command won't be available.
     */
    boolean enabled() default true;
}