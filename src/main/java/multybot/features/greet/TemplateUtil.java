package multybot.features.greet;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;

public class TemplateUtil {
    public static String fill(String template, Member member, Guild guild) {
        if (template == null || template.isBlank()) template = "¡Bienvenido {user} a {guild}!";
        return template
                .replace("{user}", member == null ? "" : member.getAsMention())
                .replace("{guild}", guild == null ? "" : guild.getName());
    }
}
