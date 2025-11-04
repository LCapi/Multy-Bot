package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

public record CommandContext(
        SlashCommandInteractionEvent event,
        JDA jda,
        Guild guild,
        Member member,
        Locale locale,              // <— Locale, no String ni DiscordLocale
        InteractionHook hook
) {
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        Guild g = event.getGuild(); // puede ser null en DMs; aquí trabajamos en guild
        Member m = event.getMember();
        // DiscordLocale -> Locale
        Locale userLocale = event.getUserLocale() != null
                ? event.getUserLocale().getLocale()
                : Locale.ENGLISH;

        // IMPORTANTE: deferReply para asegurar ACK <3s en genérico
        event.deferReply().queue();
        InteractionHook hook = event.getHook();

        return new CommandContext(event, event.getJDA(), g, m, userLocale, hook);
    }
}
