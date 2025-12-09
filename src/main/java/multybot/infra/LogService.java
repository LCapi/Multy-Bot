package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.entities.Guild;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LogService {

    private static final Logger LOG = Logger.getLogger(LogService.class);

    /**
     * Legacy API used by commands.
     * For now it just logs to application logs, without any persistence or Discord channel.
     */
    public void log(Guild guild, String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        String guildId = (guild != null) ? guild.getId() : "null";
        String guildName = (guild != null) ? guild.getName() : "unknown";

        LOG.infof("[GuildLog] guildId=%s name='%s' :: %s", guildId, guildName, text);
    }

    /**
     * Static helper kept for backward-compatibility.
     * Delegates to the instance-style logging when possible.
     */
    public static void sendToLog(Guild guild, String channelId, String text) {
        // For now we ignore channelId and just log.
        if (text == null || text.isBlank()) {
            return;
        }

        String guildId = (guild != null) ? guild.getId() : "null";
        String guildName = (guild != null) ? guild.getName() : "unknown";

        Logger.getLogger(LogService.class)
                .infof("[GuildLog-static] guildId=%s name='%s' :: %s", guildId, guildName, text);
    }
}