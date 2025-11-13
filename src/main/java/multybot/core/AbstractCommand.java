package multybot.core;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Implementación base que:
 *  - Resuelve description(locale) vía ResourceBundle i18n/commands*.properties
 *  - Construye slashData simple por defecto
 *  - Solo te obliga a implementar name() y execute(ctx)
 */
public abstract class AbstractCommand implements Command {

    @Override
    public String description(Locale locale) {
        String key = name() + ".desc";
        try {
            var bundle = ResourceBundle.getBundle("i18n.commands", locale);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "No description";
        }
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }
}