package multybot.features.automod;

import net.dv8tion.jda.api.entities.Role;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary Automod configuration without Mongo/Panache.
 * Uses an in-memory store keyed by guildId.
 */
public class AutomodConfig {

    // --- In-memory "persistence" ---

    private static final Map<String, AutomodConfig> STORE = new ConcurrentHashMap<>();

    // --- Fields ---

    public String guildId;

    public boolean enabled = false;

    // Basic rules
    public boolean badwordsEnabled = true;
    public List<String> badwords = new ArrayList<>(List.of("idiota", "imbécil", "estúpido"));
    public String badwordsAction = "DELETE";
    public int badwordsTimeoutMinutes = 0;

    public boolean linksEnabled = true;
    public String linksAction = "DELETE";

    public boolean invitesEnabled = true;
    public String invitesAction = "DELETE";

    public boolean capsEnabled = true;
    public int capsPercent = 70;
    public int capsMinLen = 12;
    public String capsAction = "TIMEOUT";
    public int capsTimeoutMinutes = 5;

    public boolean mentionsEnabled = true;
    public int mentionsMax = 5;
    public String mentionsAction = "TIMEOUT";
    public int mentionsTimeoutMinutes = 10;

    // Exemptions
    public Set<String> exemptRoleIds = new HashSet<>();
    public Set<String> exemptChannelIds = new HashSet<>();
    public Set<String> exemptUserIds = new HashSet<>();

    // --- "Persistence" API (replacing Panache) ---

    /** Returns the config for this guild or null if not present. */
    public static AutomodConfig findById(String guildId) {
        if (guildId == null) return null;
        return STORE.get(guildId);
    }

    /**
     * Loads existing config for the guild or creates a new one with defaults.
     */
    public static AutomodConfig loadOrDefault(String guildId) {
        if (guildId == null) {
            return null;
        }
        return STORE.computeIfAbsent(guildId, gid -> {
            AutomodConfig c = new AutomodConfig();
            c.guildId = gid;
            return c;
        });
    }

    /**
     * Saves or updates this config in the in-memory store.
     */
    public void persistOrUpdate() {
        if (guildId == null || guildId.isBlank()) {
            return;
        }
        STORE.put(guildId, this);
    }

    // --- Logic helpers ---

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isExempt(String channelId, String userId, List<Role> roles) {
        if (channelId != null && exemptChannelIds.contains(channelId)) return true;
        if (userId != null && exemptUserIds.contains(userId)) return true;
        if (roles != null) {
            for (Role r : roles) {
                if (exemptRoleIds.contains(r.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}