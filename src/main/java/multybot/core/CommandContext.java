package multybot.core;

import java.util.Locale;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

/**
 * Contexto inmutable para manejar un slash-command.
 */
public record CommandContext(
        SlashCommandInteractionEvent event,
        JDA jda,
        Guild guild,
        Member member,
        Locale locale,
        InteractionHook hook
) { }
