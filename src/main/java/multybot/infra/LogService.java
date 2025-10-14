package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.infra.persistence.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@ApplicationScoped
public class LogService {

    public void log(Guild guild, String message) {
        if (guild == null) return;
        GuildConfig cfg = GuildConfig.findById(guild.getId());
        if (cfg == null || cfg.logChannelId == null) return;

        var ch = guild.getTextChannelById(id);
        if (ch != null) {
            ch.sendMessage(message).queue();
        }
    }
}
