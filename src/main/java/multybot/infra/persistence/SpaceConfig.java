package multybot.infra.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import multybot.core.Platform;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "space_config")
public class SpaceConfig extends PanacheMongoEntityBase {
    @BsonId public String id;            // p.ej. "discord:123456789012345678"
    public Platform platform;            // DISCORD
    public String spaceId;               // "123456789012345678" (guildId)
    public String locale = "es";         // es|en
    public String logChannelRef;         // p.ej. "discord:9876543210"

    public static SpaceConfig of(Platform platform, String spaceId) {
        String key = platform.name().toLowerCase() + ":" + spaceId;
        SpaceConfig cfg = findById(key);
        if (cfg == null) {
            cfg = new SpaceConfig();
            cfg.id = key; cfg.platform = platform; cfg.spaceId = spaceId;
        }
        return cfg;
    }
}
