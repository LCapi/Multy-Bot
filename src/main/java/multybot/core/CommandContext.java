package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

/**
 * Contexto único por interacción SlashCommand.
 * Permite acceso seguro a JDA, Guild, Member y respuesta encapsulada.
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

    /** Alias de compatibilidad */
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        return from(event);
    }

    /** Resolver locale en orden: usuario → guild → default */
    private static Locale resolveLocale(SlashCommandInteractionEvent e) {
        DiscordLocale user = e.getUserLocale();
        if (user != null) return Locale.forLanguageTag(user.getLocale());

        Guild g = e.getGuild();
        if (g != null && g.getLocale() != null) {
            return Locale.forLanguageTag(g.getLocale().getLocale());
        }

        return Locale.getDefault();
    }

    /* ----------------- Getters de contexto ----------------- */

    public SlashCommandInteractionEvent event() { return event; }
    public Locale locale() { return locale; }
    public JDA jda() { return event.getJDA(); }
    public Guild guild() { return event.getGuild(); }
    public Member member() { return event.getMember(); }

    /* ----------------- Helpers de respuesta ----------------- */

    /**
     * Responde de forma segura. Si la interacción ya fue reconocida,
     * se usa el hook; si no, hace reply normal o deferReply automático.
     */
    public void reply(String content) {
        if (event.isAcknowledged()) {
            // ya fue deferReply() o reply()
            hook().sendMessage(content).queue();
        } else {
            event.reply(content).queue();
        }
    }

    /** Respuesta efímera */
    public void replyEphemeral(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content).setEphemeral(true).queue();
        } else {
            event.reply(content).setEphemeral(true).queue();
        }
    }

    /** Asegura que el interaction esté reconocido y devuelve el hook */
    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    /** Enviar mensaje adicional tras defer/reply */
    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    /** Si no se ha reconocido aún, hacer deferReply() */
    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }
}