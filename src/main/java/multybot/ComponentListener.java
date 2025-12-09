package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.ComponentContext;
import multybot.core.ComponentRouter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Locale;

@ApplicationScoped
public class ComponentListener extends ListenerAdapter {

    @Inject
    ComponentRouter router;

    @Override
    public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
        try {
            Guild guild = event.getGuild();
            Member member = event.getMember();
            Locale locale = resolveLocale(event);

            var ctx = new ComponentContext(event, guild, member, locale);
            router.route(ctx);
        } catch (Exception e) {
            event.reply("⚠️ Error").setEphemeral(true).queue();
        }
    }

    /**
     * Resolve locale for component interactions:
     * 1) User locale
     * 2) Guild locale
     * 3) English as fallback
     */
    private static Locale resolveLocale(GenericComponentInteractionCreateEvent event) {
        DiscordLocale userLocale = event.getUserLocale();
        if (userLocale != null && userLocale != DiscordLocale.UNKNOWN) {
            return Locale.forLanguageTag(userLocale.getLocale());
        }

        Guild guild = event.getGuild();
        if (guild != null && guild.getLocale() != null && guild.getLocale() != DiscordLocale.UNKNOWN) {
            return Locale.forLanguageTag(guild.getLocale().getLocale());
        }

        return Locale.ENGLISH;
    }
}