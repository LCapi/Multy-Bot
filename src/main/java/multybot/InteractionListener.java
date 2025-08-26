package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import multybot.core.*;
import multybot.infra.persistence.GuildConfig;

import java.util.Locale;

@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    @Inject CommandRouter router;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            String gid = event.getGuild().getId();
            GuildConfig cfg = GuildConfig.findById(gid);
            Locale locale = new Locale(cfg == null ? "es" : cfg.locale);
            CommandContext ctx = new CommandContext(
                    event, event.getJDA(), event.getGuild(), event.getMember(),
                    locale, event.deferReply().setEphemeral(true).complete()
            );
            router.route(ctx);
        } catch (Exception e) {
            event.reply("⚠️ Error").setEphemeral(true).queue();
        }
    }
}