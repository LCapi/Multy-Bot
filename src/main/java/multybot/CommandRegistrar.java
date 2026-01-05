package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import multybot.core.Command;
import multybot.core.FeatureGate;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Inject Instance<Command> commands;
    @Inject FeatureGate gate;

    @Override
    public void onReady(ReadyEvent event) {
        int guilds = event.getJDA().getGuilds().size();
        LOG.infof("JDA Ready → registering slash commands… Guilds: %d", guilds);

        for (Guild guild : event.getJDA().getGuilds()) {
            registerGuildCommands(guild);
        }

        LOG.info("Command registration done.");
    }

    private void registerGuildCommands(Guild guild) {
        Locale locale = resolveGuildLocale(guild);

        List<SlashCommandData> enabled = new ArrayList<>();
        int total = 0;
        int skipped = 0;

        for (Command c : commands) {
            total++;
            String name = safeLower(c.name());

            if (!gate.isCommandEnabled(name)) {
                skipped++;
                continue;
            }

            try {
                enabled.add(c.slashData(locale));
            } catch (Exception ex) {
                // If one command fails to build, we don't want to break startup
                LOG.errorf(ex, "Failed to build SlashCommandData for command '%s' (guild=%s)", name, guild.getId());
            }
        }

        LOG.infof("Guild %s → commands: total=%d, enabled=%d, skipped=%d",
                guild.getId(), total, enabled.size(), skipped);

        // Push to Discord for THIS guild (fast iteration; no global propagation delay)
        guild.updateCommands()
                .addCommands(enabled)
                .queue(
                        ok -> LOG.infof("Guild %s → slash commands updated (%d).", guild.getId(), enabled.size()),
                        err -> LOG.errorf(err, "Guild %s → failed to update slash commands.", guild.getId())
                );
    }

    private static Locale resolveGuildLocale(Guild guild) {
        // JDA: guild.getLocale() returns DiscordLocale
        DiscordLocale dl = guild != null ? guild.getLocale() : null;
        if (dl == null || dl == DiscordLocale.UNKNOWN) {
            return Locale.ENGLISH;
        }
        // In some JDA versions dl.getLocale() returns a String like "en-US"
        return Locale.forLanguageTag(dl.getLocale());
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}