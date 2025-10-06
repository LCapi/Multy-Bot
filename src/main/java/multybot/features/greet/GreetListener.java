package multybot.features.greet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

@ApplicationScoped
public class GreetListener extends ListenerAdapter {

    @Inject I18n i18n;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        var guild = event.getGuild();
        var cfg = GreetConfig.findById(guild.getId());
        if (cfg == null || cfg.welcomeChannelId == null || cfg.welcomeMessage == null) return;

        MessageChannel ch = guild.getChannelById(MessageChannel.class, cfg.welcomeChannelId);
        if (ch == null) return;

        Member m = event.getMember();
        String msg = TemplateUtil.fill(cfg.welcomeMessage, m, guild);

        var eb = new EmbedBuilder()
                .setColor(new Color(0x57F287))
                .setDescription(msg)
                .setThumbnail(m.getEffectiveAvatarUrl());

        if (cfg.welcomeImageUrl != null && (cfg.welcomeImageUrl.startsWith("http://") || cfg.welcomeImageUrl.startsWith("https://"))) {
            eb.setImage(cfg.welcomeImageUrl);
        }
        ch.sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        var guild = event.getGuild();
        var cfg = GreetConfig.findById(guild.getId());
        if (cfg == null || cfg.goodbyeChannelId == null || cfg.goodbyeMessage == null) return;

        MessageChannel ch = guild.getChannelById(MessageChannel.class, cfg.goodbyeChannelId);
        if (ch == null) return;

        // Member puede ser null en remove; usamos user si está.
        var user = event.getUser();
        String mention = user != null ? user.getAsMention() : "alguien";
        String tmpl = cfg.goodbyeMessage;
        if (tmpl == null || tmpl.isBlank()) tmpl = "¡{user} ha dejado {guild}!";
        String msg = tmpl.replace("{user}", mention).replace("{guild}", guild.getName());

        var eb = new EmbedBuilder()
                .setColor(new Color(0xED4245))
                .setDescription(msg);
        ch.sendMessageEmbeds(eb.build()).queue();
    }
}
