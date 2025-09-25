package multybot.features.moderation;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.Date;

@MongoEntity(collection = "moderation_cases")
public class ModerationCase extends PanacheMongoEntityBase {
    @BsonId public ObjectId id = new ObjectId();

    public String guildId;
    public String targetId;
    public String moderatorId;
    public ModerationType type;
    public String reason;
    public Date createdAt = new Date();
    public Date expiresAt; // para timeout (opcional)
}
