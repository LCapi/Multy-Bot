package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.infra.persistence.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@ApplicationScoped
public class LogService {

    /** Método que llaman tus comandos */
    public void log(Guild guild, String text) {
        if (guild == null || text == null || text.isBlank()) return;
        // Lee el canal desde la configuración del guild
        var cfg = GuildConfig.of(guild.getId());
        String chId = (cfg != null) ? cfg.logChannelId : null;
        if (chId == null || chId.isBlank()) return;

        TextChannel ch = guild.getTextChannelById(parseLongSafe(chId));
        if (ch != null) ch.sendMessage(text).queue();
    }

    /** Helper estático por si lo usas en otros sitios */
    public static void sendToLog(Guild guild, String channelId, String text) {
        if (guild == null || channelId == null || text == null || text.isBlank()) return;
        TextChannel ch = guild.getTextChannelById(parseLongSafe(channelId));
        if (ch != null) ch.sendMessage(text).queue();
    }

    private static long parseLongSafe(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1L; }
    }
}
