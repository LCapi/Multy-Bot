package multybot.infra;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class LogService {
    public static void sendToLog(Guild guild, String channelId, String text) {
        if (guild == null || channelId == null) return;
        TextChannel ch = guild.getTextChannelById(Long.parseLong(channelId));
        if (ch != null) ch.sendMessage(text).queue();
    }
}
