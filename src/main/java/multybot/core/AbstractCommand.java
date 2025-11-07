package multybot.core;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class AbstractCommand implements Command {

    /** Carga el bundle i18n (baseName = i18n/commands) */
    protected ResourceBundle bundle(Locale locale) {
        try {
            return ResourceBundle.getBundle("i18n.commands", locale);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle("i18n.commands", Locale.ENGLISH);
        }
    }

    /** Descripción por defecto: key = "<name>.desc" */
    @Override
    public String description(Locale locale) {
        var b = bundle(locale);
        var key = name() + ".desc";
        return b.containsKey(key) ? b.getString(key) : "No description.";
    }

    /** Slash por defecto (si no necesitas opciones/subcomandos) */
    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }

    /* ---------- helpers cómodos ---------- */
    protected void reply(CommandContext ctx, String msg)            { ctx.reply(msg); }
    protected void replyEphemeral(CommandContext ctx, String msg)   { ctx.replyEphemeral(msg); }
    protected void ensureAcknowledged(CommandContext ctx)           { ctx.ensureAcknowledged(); }
}