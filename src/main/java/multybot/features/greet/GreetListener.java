package multybot.features.greet;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;

@ApplicationScoped
public class GreetListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        // ¡IMPORTANTE!: usar mét0do tipado 'of(...)' para no perder los campos
        GreetConfig cfg = GreetConfig.of(event.getGuild().getId());
        if (cfg == null) return;

        if (cfg.welcomeChannelId != null && !cfg.welcomeChannelId.isBlank()
                && cfg.welcomeMessage != null && !cfg.welcomeMessage.isBlank()) {

            TextChannel ch = event.getGuild().getTextChannelById(parseLongSafe(cfg.welcomeChannelId)); // JDA 5: long
            if (ch == null) return;

            String msg = TemplateUtil.fill(cfg.welcomeMessage, event.getMember(), event.getGuild());

            var eb = new EmbedBuilder()
                    .setColor(new Color(0x57F287))
                    .setDescription(msg)
                    .setThumbnail(event.getUser().getEffectiveAvatarUrl());

            if (cfg.welcomeImageUrl != null && !cfg.welcomeImageUrl.isBlank()) {
                eb.setImage(cfg.welcomeImageUrl);
            }
            ch.sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        GreetConfig cfg = GreetConfig.of(event.getGuild().getId());
        if (cfg == null) return;

        if (cfg.goodbyeChannelId != null && !cfg.goodbyeChannelId.isBlank()
                && cfg.goodbyeMessage != null && !cfg.goodbyeMessage.isBlank()) {

            TextChannel ch = event.getGuild().getTextChannelById(parseLongSafe(cfg.goodbyeChannelId)); // JDA 5: long
            if (ch == null) return;

            String msg = TemplateUtil.fill(cfg.goodbyeMessage, event.getMember(), event.getGuild());

            var eb = new EmbedBuilder()
                    .setColor(new Color(0xED4245))
                    .setDescription(msg);

            ch.sendMessageEmbeds(eb.build()).queue();
        }
    }

    private static long parseLongSafe(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1L; }
    }
}
