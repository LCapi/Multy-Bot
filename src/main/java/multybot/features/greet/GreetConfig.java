package multybot.features.greet;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "greet_config")
public class GreetConfig extends PanacheMongoEntityBase {
    @BsonId public String guildId;

    // Welcome
    public String welcomeChannelId;
    public String welcomeMessage;   // usa {user} y {guild}
    public String welcomeImageUrl;  // opcional

    // Goodbye
    public String goodbyeChannelId;
    public String goodbyeMessage;   // usa {user} y {guild}

    public static GreetConfig of(String guildId) {
        GreetConfig c = findById(guildId);
        if (c == null) { c = new GreetConfig(); c.guildId = guildId; }
        return c;
    }

}
