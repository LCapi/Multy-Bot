package multybot.features.moderation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Temporary moderation case model with in-memory storage.
 * No Mongo / Panache, but commands can still "save" and "query" cases.
 */
public class ModerationCase {

    // --- Fake persistence (in-memory) ---

    // key = guildId, value = list of cases for that guild
    private static final Map<String, List<ModerationCase>> STORE = new ConcurrentHashMap<>();
    private static final AtomicLong SEQ = new AtomicLong(1);

    // --- Fields ---

    public String id;          // simple sequential id as String
    public String guildId;
    public String targetId;
    public String moderatorId;
    public ModerationType type;
    public String reason;
    public Date createdAt = new Date();
    public Date expiresAt;     // optional, for timeout

    public ModerationCase() {
        // empty ctor for convenience
    }

    // --- "Persistence" API (reemplazo de Panache) ---

    /** Save or update this case in the in-memory store. */
    public void persistOrUpdate() {
        if (id == null) {
            id = String.valueOf(SEQ.getAndIncrement());
        }
        if (guildId == null) {
            // without guild, we can't store it in a grouped map
            return;
        }
        STORE.computeIfAbsent(guildId, g -> new ArrayList<>());

        List<ModerationCase> list = STORE.get(guildId);
        // simple "upsert" by id
        list.removeIf(c -> Objects.equals(c.id, this.id));
        list.add(this);
    }

    /** Returns all cases for a guild. */
    public static List<ModerationCase> findByGuild(String guildId) {
        return STORE.getOrDefault(guildId, Collections.emptyList());
    }

    /** Returns a single case by id (regardless of guild). */
    public static ModerationCase findById(String id) {
        if (id == null) return null;
        return STORE.values().stream()
                .flatMap(List::stream)
                .filter(c -> id.equals(c.id))
                .findFirst()
                .orElse(null);
    }

    /** Returns the latest case number for a guild (or 0 if none). */
    public static long lastCaseNumber(String guildId) {
        return findByGuild(guildId).stream()
                .mapToLong(c -> {
                    try { return Long.parseLong(c.id); }
                    catch (NumberFormatException e) { return 0L; }
                })
                .max()
                .orElse(0L);
    }
}