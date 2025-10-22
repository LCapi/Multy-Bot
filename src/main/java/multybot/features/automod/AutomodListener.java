package multybot.features.automod;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Pattern;

@ApplicationScoped
public class AutomodListener extends ListenerAdapter {

    private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");
    private static final Pattern INVITE = Pattern.compile("(?i)discord\\.(gg|com/invite)/[A-Za-z0-9-]+");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        var guild  = event.getGuild();
        Member mem = event.getMember();
        if (mem == null) return;

        AutomodConfig cfg = AutomodConfig.findById(guild.getId());
        if (cfg == null || !cfg.isEnabled()) return;

        // EXENCIONES (rol/canal/usuario)
        var channelId = event.getChannel().getId();
        var userId    = event.getAuthor().getId();
        if (cfg.isExempt(channelId, userId, mem.getRoles())) return;

        // === Reglas básicas (ejemplo mínimo) ===
        String content = event.getMessage().getContentStripped();

        // INVITES
        if (cfg.invitesEnabled && INVITE.matcher(content).find()) {
            handleDelete(event, "INVITES");
            return;
        }
        // LINKS
        if (cfg.linksEnabled && URL.matcher(content).find()) {
            handleDelete(event, "LINKS");
            return;
        }
        // BADWORDS
        if (cfg.badwordsEnabled && matchesBadword(content, cfg)) {
            handleDelete(event, "BADWORDS");
            return;
        }
        // MENTIONS
        if (cfg.mentionsEnabled && tooManyMentions(event, cfg)) {
            handleTimeout(event, cfg.mentionsTimeoutMinutes, "MENTIONS");
            return;
        }
        // CAPS
        if (cfg.capsEnabled && isShouting(content, cfg)) {
            handleTimeout(event, cfg.capsTimeoutMinutes, "CAPS");
        }
    }

    private boolean matchesBadword(String s, AutomodConfig cfg) {
        if (cfg.badwords == null || cfg.badwords.isEmpty()) return false;
        String lower = s.toLowerCase();
        for (String w : cfg.badwords) {
            if (w != null && !w.isBlank() && lower.contains(w.toLowerCase())) return true;
        }
        return false;
    }

    private boolean tooManyMentions(MessageReceivedEvent e, AutomodConfig cfg) {
        int users = e.getMessage().getMentions().getUsers().size();
        int roles = e.getMessage().getMentions().getRoles().size();
        boolean everyone = e.getMessage().getMentions().mentionsEveryone();
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
        int pct = (int)Math.round(upper * 100.0 / letters);
        return pct >= cfg.capsPercent;
    }

    private void handleDelete(MessageReceivedEvent e, String rule) {
        var self = e.getGuild().getSelfMember();
        if (!self.hasPermission(e.getGuildChannel(), Permission.MESSAGE_MANAGE)) return;
        e.getMessage().delete().queue();
    }

    private void handleTimeout(MessageReceivedEvent e, int minutes, String rule) {
        var self = e.getGuild().getSelfMember();
        if (!self.hasPermission(Permission.MODERATE_MEMBERS)) return;
        var member = e.getMember();
        if (member == null) return;
        java.time.Duration dur = java.time.Duration.ofMinutes(Math.max(1, minutes));
        member.timeoutFor(dur).reason("AutoMod: " + rule).queue();
    }
}
