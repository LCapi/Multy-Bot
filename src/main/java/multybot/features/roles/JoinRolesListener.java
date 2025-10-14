package multybot.features.roles;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.Platform;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class JoinRolesListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        var guild = event.getGuild();
        var self  = guild.getSelfMember();

        var cfg = SpaceJoinRoles.of(Platform.DISCORD, guild.getId());
        if (cfg.roleIds == null || cfg.roleIds.isEmpty()) return;
        if (!self.hasPermission(Permission.MANAGE_ROLES)) return;

        List<Role> toAdd = new ArrayList<>();
        for (String roleId : cfg.roleIds) {
            Role r = guild.getRoleById(roleId);
            if (r == null || r.isPublicRole()) continue;
            if (self.canInteract(r)) toAdd.add(r);
        }
        if (toAdd.isEmpty()) return;

        guild.modifyMemberRoles(event.getMember(), toAdd, List.of()).queue();
    }
}
