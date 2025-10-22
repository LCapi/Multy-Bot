package multybot.infra.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
//import com.mongodb.client.model.Indexes as MIndexes; // alias mental, no compila en Java; usamos el nombre completo abajo
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Indexes {

    @ConfigProperty(name = "infra.persistence.enabled", defaultValue = "true")
    boolean persistenceEnabled;

    @Inject
    MongoClient mongo;

    void onStart(@Observes StartupEvent ev) {
        if (!persistenceEnabled) {
            // No tocamos Mongo en este arranque
            return;
        }
        // A partir de aquí SOLO si tienes Mongo disponible
        // Crea aquí tus índices de forma segura
        // ejemplo:
        // var db = mongo.getDatabase("multybot");
        // MongoCollection<Document> cooldowns = db.getCollection("cooldowns");
        // cooldowns.createIndex(com.mongodb.client.model.Indexes.ascending("guildId", "command", "userId"),
        //         new IndexOptions().expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));
    }
}
