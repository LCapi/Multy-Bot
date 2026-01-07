package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

@ApplicationScoped
public class JdaProducer {

    @ConfigProperty(name = "bot.token")
    String token;

    @Produces
    @ApplicationScoped
    public JDA produceJda() {
        return JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MODERATION
                )
                .setActivity(Activity.playing("Multy-Bot"))
                .build();
    }
}