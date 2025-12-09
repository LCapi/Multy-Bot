package multybot.features.roles;

import multybot.core.Platform;

import java.util.*;

/**
 * In-memory implementation of SpaceJoinRoles.
 * Removes Mongo/Panache but keeps behavior intact.
 */
public class SpaceJoinRoles {

    /** In-memory storage (key = "discord:<guildId>") */
    private static final Map<String, SpaceJoinRoles> STORE =
            Collections.synchronizedMap(new HashMap<>());

    public String id;            // "discord:<guildId>"
    public Platform platform;    // DISCORD (future-proof)
    public String spaceId;       // guildId
    public Set<String> roleIds = new HashSet<>();

    private SpaceJoinRoles() {}

    /**
     * Load config for a space (guild), or create default one.
     * This replaces Mongo's findById() + auto-create.
     */
    public static SpaceJoinRoles of(Platform pf, String spaceId) {
        if (pf == null || spaceId == null) return null;

        String key = pf.name().toLowerCase() + ":" + spaceId;

        synchronized (STORE) {
            return STORE.computeIfAbsent(key, k -> {
                SpaceJoinRoles cfg = new SpaceJoinRoles();
                cfg.id = k;
                cfg.platform = pf;
                cfg.spaceId = spaceId;
                return cfg;
            });
        }
    }

    /** Saves or updates this config in memory */
    public void save() {
        if (id == null) return;
        STORE.put(id, this);
    }

    /** Retrieve directly by key (optional helper) */
    public static SpaceJoinRoles get(String id) {
        return STORE.get(id);
    }

    /** Remove all data (mainly for tests/dev) */
    public static void clearAll() {
        STORE.clear();
    }
}