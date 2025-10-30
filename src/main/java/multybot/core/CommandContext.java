package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;
import java.util.function.Consumer;

public record CommandContext(
        SlashCommandInteractionEvent event,
        JDA jda,
        Guild guild,
        Member member,
        Locale locale
) {
    public static CommandContext from(SlashCommandInteractionEvent e) {
        Locale loc = e.getUserLocale() != null ? Locale.forLanguageTag(e.getUserLocale()) : Locale.ENGLISH;
        return new CommandContext(e, e.getJDA(), e.getGuild(), e.getMember(), loc);
    }

    /** Acknowledge diferido (útil si el comando tarda). */
    public void defer(boolean ephemeral) {
        if (!event.isAcknowledged()) {
            event.deferReply().setEphemeral(ephemeral).queue();
        }
    }

    /** Respuesta de texto simple. Si ya hay defer, edita el original; si no, reply. */
    public void reply(String content) {
        if (event.isAcknowledged()) {
            event.getHook().editOriginal(content).queue();
        } else {
            event.reply(content).queue();
        }
    }

    /** Respuesta de texto con configuración extra. */
    public void reply(String content, Consumer<net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction> customizer) {
        if (event.isAcknowledged()) {
            event.getHook().editOriginal(content).queue();
        } else {
            var action = event.reply(content);
            if (customizer != null) customizer.accept(action);
            action.queue();
        }
    }

    /** Acceso al hook (solo válido después de deferReply). */
    public InteractionHook hook() {
        return event.getHook();
    }
}
