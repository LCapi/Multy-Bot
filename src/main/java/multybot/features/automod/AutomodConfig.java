// ... package e imports existentes ...
import java.util.HashSet;
import java.util.Set;

@MongoEntity(collection = "automod_config")
public class AutomodConfig extends PanacheMongoEntityBase {
    @BsonId public String guildId;

    public boolean enabled = false;

    // ... (reglas ya existentes)

    // === EXENCIONES ===
    public Set<String> exemptRoleIds   = new HashSet<>();    // IDs de rol
    public Set<String> exemptChannelIds= new HashSet<>();    // IDs de canal
    public Set<String> exemptUserIds   = new HashSet<>();    // IDs de usuario

    public static AutomodConfig loadOrDefault(String guildId) {
        AutomodConfig c = findById(guildId);
        if (c == null) {
            c = new AutomodConfig();
            c.guildId = guildId;
        }
        return c;
    }

    public boolean isExempt(String channelId, String userId, java.util.List<net.dv8tion.jda.api.entities.Role> roles) {
        if (channelId != null && exemptChannelIds.contains(channelId)) return true;
        if (userId != null && exemptUserIds.contains(userId)) return true;
        if (roles != null) {
            for (var r : roles) {
                if (exemptRoleIds.contains(r.getId())) return true;
            }
        }
        return false;
    }

    public boolean isEnabled() { return enabled; }
}
