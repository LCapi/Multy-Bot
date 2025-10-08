package multybot.features.automod;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.I18n;
import multybot.infra.LogService;
import multybot.features.moderation.ModerationCase;
import multybot.features.moderation.ModerationType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class AutomodListener extends ListenerAdapter {

    @Inject I18n i18n;
    @Inject LogService logs;

    private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");
    private static final Pattern INVITE = Pattern.compile("(?i)discord\\.(gg|com/invite)/[A-Za-z0-9-]+");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        var msg = event.getMessage();
        var author = event.getAuthor();
        if (author.isBot() || author.isSystem()) return; // ignorar bots y sistema

        var guild = event.getGuild();
        var member = event.getMember();
        if (member == null) return;

        AutomodConfig cfg = AutomodConfig.findById(guild.getId());
        if (cfg == null || !cfg.isEnabled()) return; // desactivado

        String content = msg.getContentStripped();
        // PRIORIDAD: invites > links > badwords > mentions > caps
        if (cfg.invitesEnabled && matchesInvite(content)) {
            handleAction("INVITES", cfg.invitesAction, 0, event);
            return;
        }
        if (cfg.linksEnabled && matchesUrl(content)) {
            handleAction("LINKS", cfg.linksAction, 0, event);
            return;
        }
        if (cfg.badwordsEnabled && matchesBadword(content, cfg)) {
            int minutes = Math.max(0, cfg.badwordsTimeoutMinutes);
            handleAction("BADWORDS", cfg.badwordsAction, minutes, event);
            return;
        }
        if (cfg.mentionsEnabled && tooManyMentions(msg, cfg)) {
            handleAction("MENTIONS", cfg.mentionsAction, cfg.mentionsTimeoutMinutes, event);
            return;
        }
        if (cfg.capsEnabled && isShouting(content, cfg)) {
            handleAction("CAPS", cfg.capsAction, cfg.capsTimeoutMinutes, event);
        }
    }

    private boolean matchesUrl(String s) {
        return URL.matcher(s).find();
    }

    private boolean matchesInvite(String s) {
        return INVITE.matcher(s).find();
    }

    private boolean matchesBadword(String s, AutomodConfig cfg) {
        if (cfg.badwords == null || cfg.badwords.isEmpty()) return false;
        String lower = s.toLowerCase();
        // búsqueda simple contiene; si prefieres límites de palabra, compila regex por palabra
        for (String w : cfg.badwords) {
            if (w == null || w.isBlank()) continue;
            if (lower.contains(w.toLowerCase())) return true;
        }
        return false;
    }

    private boolean tooManyMentions(Message m, AutomodConfig cfg) {
        int users = m.getMentions().getUsers().size();
        int roles = m.getMentions().getRoles().size();
        boolean everyone = m.getMentions().isMentioned(m.getGuild().getPublicRole(), Message.MentionType.ROLE)
                || m.getMentions().mentionsEveryone();
        int total = users + roles + (everyone ? 1 : 0);
        return total >= cfg.mentionsMax;
    }

    private boolean isShouting(String s, AutomodConfig cfg) {
        int letters = 0, upper = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) upper++;
            }
        }
        if (s.length() < cfg.capsMinLen || letters == 0) return false;
        int pct = (int) Math.round((upper * 100.0) / letters);
        return pct >= cfg.capsPercent;
    }

    private void handleAction(String rule, String action, int minutes, MessageReceivedEvent event) {
        var guild = event.getGuild();
        var member = event.getMember();
        var channel = event.getChannel();
        Locale locale = new Locale("es"); // si tienes locale por guild, cárgalo aquí

        String reason = "AutoMod: " + rule;

        switch (action.toUpperCase()) {
            case "DELETE" -> {
                if (!guild.getSelfMember().hasPermission(channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE)) {
                    channel.sendMessage(i18n.msg(locale, "automod.no_perms.delete")).queue(m -> m.delete().queueAfter(java.time.Duration.ofSeconds(5)));
                    break;
                }
                event.getMessage().delete().queue(
                        ok -> logs.log(guild, "**[AutoMod]** DELETE (" + rule + ") msg " + event.getMessageId() + " by <@" + member.getId() + ">"),
                        err -> {}
                );
            }
            case "WARN" -> {
                // Guardar caso WARN + DM si es posible
                ModerationCase mc = new ModerationCase();
                mc.guildId = guild.getId();
                mc.moderatorId = guild.getSelfMember().getId();
                mc.targetId = member.getId();
                mc.type = ModerationType.WARN;
                mc.reason = reason;
                mc.persist();

                logs.log(guild, "**[AutoMod]** WARN (" + rule + ") <@" + member.getId() + ">");
                member.getUser().openPrivateChannel().queue(pc -> {
                    pc.sendMessage(i18n.msg(locale, "automod.warn.dm", guild.getName(), reason)).queue(
                            ok -> {},
                            err -> {}
                    );
                }, err -> {});
            }
            case "TIMEOUT" -> {
                if (!guild.getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
                    channel.sendMessage(i18n.msg(locale, "automod.no_perms.timeout")).queue(m -> m.delete().queueAfter(java.time.Duration.ofSeconds(5)));
                    break;
                }
                int mins = Math.max(1, minutes);
                member.timeoutFor(Duration.ofMinutes(mins)).reason(reason).queue(
                        ok -> {
                            ModerationCase mc = new ModerationCase();
                            mc.guildId = guild.getId();
                            mc.moderatorId = guild.getSelfMember().getId();
                            mc.targetId = member.getId();
                            mc.type = ModerationType.TIMEOUT;
                            mc.reason = reason;
                            mc.expiresAt = new Date(System.currentTimeMillis() + Duration.ofMinutes(mins).toMillis());
                            mc.persist();
                            logs.log(guild, "**[AutoMod]** TIMEOUT " + mins + "m (" + rule + ") <@" + member.getId() + ">");
                        },
                        err -> {}
                );
            }
            default -> {}
        }
        // Mensaje informativo (ephemeral no aplica a mensajes), opcionalmente nada.
        // channel.sendMessage(i18n.msg(locale,"automod.hit", rule, reason)).queue(m -> m.delete().queueAfter(Duration.ofSeconds(5)));
    }
}
