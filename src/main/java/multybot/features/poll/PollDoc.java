package multybot.features.poll;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.*;

@MongoEntity(collection = "polls")
public class PollDoc extends PanacheMongoEntityBase {
    @BsonId public ObjectId id = new ObjectId();

    public String guildId;
    public String channelId;
    public String messageId;

    public String question;
    public List<String> options = new ArrayList<>();      // 2..10
    public Map<String,Integer> votes = new HashMap<>();   // userId -> optionIdx

    public Date createdAt = new Date();
    public boolean closed = false;
    public Date closedAt;
}
