package multybot.features.remind;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.DiscordGateway;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import io.quarkus.scheduler.Scheduled;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class ReminderScheduler {

    @Inject MongoClient mongo;
    @Inject DiscordGateway gw;

    private String nodeId() {
        return System.getenv().getOrDefault("HOSTNAME", "local");
    }

    @Scheduled(every = "5s")
    void tick() {
        var col = mongo.getDatabase("multybot").getCollection("reminders");
        Instant now = Instant.now();

        // Procesa hasta 10 por tick
        for (int i = 0; i < 10; i++) {
            var claimed = col.findOneAndUpdate(
                    Filters.and(
                            Filters.eq("status", "PENDING"),
                            Filters.lte("dueAt", Date.from(now))
                    ),
                    Updates.combine(
                            Updates.set("status", "CLAIMED"),
                            Updates.set("claimedBy", nodeId()),
                            Updates.set("claimedAt", new Date())
                    ),
                    new com.mongodb.client.FindOneAndUpdateOptions().sort(new Document("dueAt", 1)).returnDocument(ReturnDocument.AFTER)
            );
            if (claimed == null) break; // nada que hacer

            try {
                String channelId = claimed.getString("channelId");
                String userId = claimed.getString("userId");
                String text = claimed.getString("text");

                MessageChannel ch = gw.jda().getChannelById(MessageChannel.class, channelId);
                if (ch != null) {
                    ch.sendMessage("⏰ <@" + userId + "> " + text).queue();
                } else {
                    // canal ya no existe → intentar DM
                    gw.jda().retrieveUserById(userId).queue(u -> u.openPrivateChannel().queue(pc -> {
                        pc.sendMessage("⏰ " + text).queue();
                    }));
                }

                col.updateOne(Filters.eq("_id", claimed.getObjectId("_id")),
                        Updates.combine(Updates.set("status","SENT"), Updates.set("sentAt", new Date())));
            } catch (Exception e) {
                // En caso de fallo, re-intentar en siguiente tick: devolver a PENDING
                col.updateOne(Filters.eq("_id", claimed.getObjectId("_id")),
                        Updates.combine(Updates.set("status","PENDING"), Updates.unset("claimedBy"), Updates.unset("claimedAt")));
            }
        }
    }
}
