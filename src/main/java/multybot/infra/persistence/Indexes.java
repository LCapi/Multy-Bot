package multybot.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import jakarta.inject.Inject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

@ApplicationScoped
public class Indexes {
    @Inject MongoClient client;

    void onStart(@Observes StartupEvent ev) {
        var db = client.getDatabase("multybot");
        var cooldowns = db.getCollection("cooldowns");
        cooldowns.createIndex(new Document("expiresAt", 1), new IndexOptions().expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));
    }
}
