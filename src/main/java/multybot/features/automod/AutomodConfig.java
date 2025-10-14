package multybot.features.automod;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@MongoEntity(collection = "automod_config")
public class AutomodConfig extends PanacheMongoEntityBase {
    @BsonId public String guildId;

    public boolean enabled = false;

    // Reglas básicas
    public boolean badwordsEnabled = true;
    public List<String> badwords = new ArrayList<>(List.of("idiota","imbécil","estúpido"));
    public String badwordsAction = "DELETE";
    public int badwordsTimeoutMinutes = 0;

    public boolean linksEnabled = true;
    public String linksAction = "DELETE";

    public boolean invitesEnabled = true;
    public String invitesAction = "DELETE";

    public boolean capsEnabled = true;
    public int capsPercent = 70;
    public int capsMinLen = 12;
    public String capsAction = "TIMEOUT";
    public int capsTimeoutMinutes = 5;

    public boolean mentionsEnabled = true;
    public int mentionsMax = 5;
    public String mentionsAction = "TIMEOUT";
    public int mentionsTimeoutMinutes = 10;

    // Exenciones
    public Set<String> exemptRoleIds = new HashSet<>();
    public Set<String> exemptChannelIds = new HashSet<>();
    public Set<String> exemptUserIds = new HashSet<>();

    public static AutomodConfig loadOrDefault(String guildId) {
        AutomodConfig c = findById(guildId);
        if (c == null) { c = new AutomodConfig(); c.guildId = guildId; }
        return c;
    }

    public boolean isEnabled() { return enabled; }

    public boolean isExempt(String channelId, String userId, List<net.dv8tion.jda.api.entities.Role> roles) {
        if (channelId != null && exemptChannelIds.contains(channelId)) return true;
        if (userId != null && exemptUserIds.contains(userId)) return true;
        if (roles != null) for (var r : roles) if (exemptRoleIds.contains(r.getId())) return true;
        return false;
    }
}
