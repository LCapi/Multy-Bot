package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import multybot.core.CommandContext;
import multybot.core.CommandRouter;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Locale; // <-- import necesario

@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    @Inject CommandRouter router;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Usa un Locale seguro; si luego quieres mapear DiscordLocale -> Locale, lo ajustamos
        Locale locale = Locale.getDefault();

        CommandContext ctx = new CommandContext(
                event,
                event.getJDA(),
                event.getGuild(),
                event.getMember(),
                locale,
                event.getHook()
        );

        router.route(ctx);
    }
}