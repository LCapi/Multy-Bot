// src/main/java/multybot/features/greet/GreetConfig.java
package multybot.features.greet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GreetConfig {

    public String guildId;

    // Welcome
    public String welcomeChannelId;
    public String welcomeMessage;   // usa {user} y {guild}
    public String welcomeImageUrl;  // opcional

    // Goodbye
    public String goodbyeChannelId;
    public String goodbyeMessage;   // usa {user} y {guild}

    private static final Map<String, GreetConfig> STORE = new ConcurrentHashMap<>();

    public static GreetConfig of(String guildId) {
        return STORE.computeIfAbsent(guildId, id -> {
            GreetConfig c = new GreetConfig();
            c.guildId = id;
            return c;
        });
    }

    public static void save(GreetConfig cfg) {
        if (cfg == null || cfg.guildId == null) return;
        STORE.put(cfg.guildId, cfg);
    }
}