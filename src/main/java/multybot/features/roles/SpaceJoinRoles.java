package multybot.features.roles;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import multybot.core.Platform;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.HashSet;
import java.util.Set;

@MongoEntity(collection = "join_roles")
public class SpaceJoinRoles extends PanacheMongoEntityBase {
    @BsonId public String id;     // "discord:<guildId>"
    public Platform platform;     // DISCORD
    public String spaceId;        // guildId
    public Set<String> roleIds = new HashSet<>();

    public static SpaceJoinRoles of(Platform pf, String spaceId) {
        String key = pf.name().toLowerCase() + ":" + spaceId;
        SpaceJoinRoles cfg = findById(key);
        if (cfg == null) { cfg = new SpaceJoinRoles(); cfg.id = key; cfg.platform = pf; cfg.spaceId = spaceId; }
        return cfg;
    }
}
