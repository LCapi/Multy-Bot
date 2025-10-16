package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.ComponentContext;
import multybot.core.ComponentRouter;
import multybot.infra.persistence.GuildConfig;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Locale;

@ApplicationScoped
public class ComponentListener extends ListenerAdapter {

    @Inject ComponentRouter router;

    @Override
    public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
        try {
            var guild = event.getGuild();
            var cfg = GuildConfig.of(guild.getId());
            var locale = new Locale(cfg == null ? "es" : cfg.locale);
            var member = event.getMember();
            var ctx = new ComponentContext(event, event.getJDA(), guild, member, locale);
            router.route(ctx);
        } catch (Exception e) {
            event.reply("⚠️ Error").setEphemeral(true).queue();
        }
    }
}
