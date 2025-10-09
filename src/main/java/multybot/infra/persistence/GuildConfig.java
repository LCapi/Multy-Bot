package multybot.infra.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

/** Configuración por guild (servidor) */
@MongoEntity(collection = "guild_config")
public class GuildConfig extends PanacheMongoEntityBase {
    @BsonId
    public String guildId;         // Discord Guild ID (clave primaria)

    public String locale = "es";   // es|en (por defecto: es)
    public String logChannelId;    // canal donde enviar logs (opcional)

    /** Carga o crea con valores por defecto (sin persistir hasta persistOrUpdate) */
    public static GuildConfig of(String guildId) {
        GuildConfig cfg = findById(guildId);
        if (cfg == null) {
            cfg = new GuildConfig();
            cfg.guildId = guildId;
        }
        return cfg;
    }
}
