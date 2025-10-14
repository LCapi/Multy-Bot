package multybot.infra;

import java.time.Instant;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import multybot.core.*;
import multybot.infra.persistence.CooldownDoc;
import net.dv8tion.jda.api.Permission;

@ApplicationScoped
public class PreconditionsExecutor {

    @Inject I18n i18n;

    public Optional<String> evaluate(Command cmd, CommandContext ctx) {
        // Permisos
        RequirePermissions perms = cmd.getClass().getAnnotation(RequirePermissions.class);
        if (perms != null) {
            boolean ok = ctx.member().hasPermission(perms.value());
            if (!ok) return Optional.of(i18n.msg(ctx.locale(), "perm.missing"));
        }
        // Cooldown
       // Cooldown cd = cmd.getClass().getAnnotation(Cooldown.class);
       // if (cd != null) {
       //     String key = ctx.guild().getId()+":"+ctx.member().getId()+":"+cmd.name();
       //     CooldownDoc doc = CooldownDoc.findById(key);
       //     if (doc != null && doc.expiresAt.after(java.util.Date.from(Instant.now()))) {
       //         return Optional.of(i18n.msg(ctx.locale(), "cooldown.active"));
       //     }
       //     // set/update
       //     CooldownDoc newDoc = new CooldownDoc();
       //     newDoc.key = key;
       //     newDoc.guildId = ctx.guild().getId();
       //     newDoc.userId = ctx.member().getId();
       //     newDoc.command = cmd.name();
       //     newDoc.expiresAt = java.util.Date.from(Instant.now().plusSeconds(cd.seconds()));
       //     CooldownDoc.persistOrUpdate(newDoc);
       // }
        return Optional.empty();
    }
}
