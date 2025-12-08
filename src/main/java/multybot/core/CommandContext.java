package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

/**
 * Contexto por interacción para slash commands.
 * Proporciona acceso cómodo a JDA, guild, miembro, locale y helpers de respuesta.
 */
public class CommandContext {

    private final SlashCommandInteractionEvent event;
    private final Locale locale;

    private CommandContext(SlashCommandInteractionEvent event, Locale locale) {
        this.event = event;
        this.locale = locale;
    }

    /** Fábrica principal */
    public static CommandContext from(SlashCommandInteractionEvent event) {
        return new CommandContext(event, resolveLocale(event));
    }

    /** Alias por legibilidad / compatibilidad */
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        return from(event);
    }

    /**
     * Resolución de locale con esta prioridad:
     * 1) Locale del usuario (event.getUserLocale())
     * 2) Locale de la guild (event.getGuild().getLocale())
     * 3) Fallback: Locale.ENGLISH
     */
    private static Locale resolveLocale(SlashCommandInteractionEvent e) {
        // 1) Locale del usuario
        DiscordLocale user = e.getUserLocale();
        if (user != null && user != DiscordLocale.UNKNOWN) {
            return Locale.forLanguageTag(user.getLocale());
        }

        // 2) Locale de la guild
        if (e.getGuild() != null) {
            DiscordLocale guildLocale = e.getGuild().getLocale();
            if (guildLocale != null && guildLocale != DiscordLocale.UNKNOWN) {
                return Locale.forLanguageTag(guildLocale.getLocale());
            }
        }

        // 3) Fallback
        return Locale.ENGLISH; // o Locale.getDefault() si prefieres
    }

    // ------------- Getters de contexto -------------

    public SlashCommandInteractionEvent event() {
        return event;
    }

    public Locale locale() {
        return locale;
    }

    public JDA jda() {
        return event.getJDA();
    }

    public Guild guild() {
        return event.getGuild();
    }

    public Member member() {
        return event.getMember();
    }

    // ------------- Helpers de respuesta -------------

    /**
     * Reply “normal”.
     * - Si la interacción no está reconocida: event.reply(...)
     * - Si ya está reconocida (defer/reply previo): hook().sendMessage(...)
     */
    public void reply(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content).queue();
        } else {
            event.reply(content).queue();
        }
    }

    /**
     * Reply efímero.
     * - Si la interacción no está reconocida: event.reply(...).setEphemeral(true)
     * - Si ya está reconocida: hook().sendMessage(...).setEphemeral(true)
     */
    public void replyEphemeral(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content)
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply(content)
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Asegura que la interacción está reconocida (deferReply si hace falta)
     * y devuelve el InteractionHook.
     *
     * IMPORTANTE:
     * - Si tu InteractionListener ya hace event.deferReply() siempre,
     *   esto simplemente devuelve el hook.
     */
    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    /** Enviar un mensaje adicional después del primero (follow-up). */
    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    /**
     * Si la interacción aún no está reconocida (sin reply ni defer),
     * llama a deferReply() para evitar el timeout de 3s.
     *
     * Si ya está reconocida, no hace nada (no-op).
     */
    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }
}