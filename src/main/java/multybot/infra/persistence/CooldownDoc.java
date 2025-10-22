package multybot.infra.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

/** Stub temporal para cooldowns; podrás enriquecerlo más adelante. */
@MongoEntity(collection = "cooldowns")
public class CooldownDoc extends PanacheMongoEntityBase {
    @BsonId public String id;           // ej: guild:cmd:user
    public String key;                  // misma clave
    public long untilEpochMs;           // millis de expiración

    public static CooldownDoc of(String key, long untilMs) {
        CooldownDoc d = new CooldownDoc();
        d.id = key;
        d.key = key;
        d.untilEpochMs = untilMs;
        return d;
    }
}
