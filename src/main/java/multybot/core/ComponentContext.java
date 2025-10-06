package multybot.core;

import java.util.Locale;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public record ComponentContext(
        GenericComponentInteractionCreateEvent event,
        JDA jda,
        Guild guild,
        Member member,
        Locale locale
) {}
