package multybot.features.automod;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "automod_config")
public class AutomodConfig extends PanacheMongoEntityBase {
    @BsonId public String guildId;

    public boolean enabled = false;

    // Regla: BADWORDS
    public boolean badwordsEnabled = true;
    public List<String> badwords = new ArrayList<>(List.of(
            // ejemplo inicial, ajusta o vacía si prefieres
            "idiota","imbécil","estúpido"
    ));
    public String badwordsAction = "DELETE"; // DELETE | WARN | TIMEOUT
    public int badwordsTimeoutMinutes = 0;

    // Regla: LINKS
    public boolean linksEnabled = true;
    public String linksAction = "DELETE";

    // Regla: INVITES
    public boolean invitesEnabled = true;
    public String invitesAction = "DELETE";

    // Regla: CAPS
    public boolean capsEnabled = true;
    public int capsPercent = 70;   // % de letras en mayúsculas
    public int capsMinLen = 12;    // longitud mínima del mensaje para evaluar
    public String capsAction = "TIMEOUT";
    public int capsTimeoutMinutes = 5;

    // Regla: MENTIONS
    public boolean mentionsEnabled = true;
    public int mentionsMax = 5;    // máximo de menciones totales (users/roles/everyone)
    public String mentionsAction = "TIMEOUT";
    public int mentionsTimeoutMinutes = 10;

    public static AutomodConfig loadOrDefault(String guildId) {
        AutomodConfig c = findById(guildId);
        if (c == null) {
            c = new AutomodConfig();
            c.guildId = guildId;
        }
        return c;
    }

    public boolean isEnabled() { return enabled; }
}
