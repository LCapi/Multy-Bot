package multybot.features.poll;

import java.util.*;

/**
 * In-memory poll document.
 * Replaces the old Mongo/Panache entity for now.
 */
public class PollDoc {

    // Simple in-memory "storage" for this JVM
    private static final List<PollDoc> STORE =
            Collections.synchronizedList(new ArrayList<>());

    // Simple string id instead of ObjectId
    public String id = UUID.randomUUID().toString();

    public String guildId;
    public String channelId;
    public String messageId;

    public String question;
    public List<String> options = new ArrayList<>();      // 2..10
    public Map<String, Integer> votes = new HashMap<>();  // userId -> optionIdx

    public Date createdAt = new Date();
    public boolean closed = false;
    public Date closedAt;

    // --------- Static helpers (instead of Panache) ---------

    /**
     * Create and store a new poll in memory.
     */
    public static PollDoc create(
            String guildId,
            String channelId,
            String messageId,
            String question,
            List<String> options
    ) {
        PollDoc p = new PollDoc();
        p.guildId = guildId;
        p.channelId = channelId;
        p.messageId = messageId;
        p.question = question;
        if (options != null) {
            p.options.addAll(options);
        }
        STORE.add(p);
        return p;
    }

    /**
     * Find a poll by Discord message id (most common use-case).
     */
    public static PollDoc findByMessageId(String messageId) {
        if (messageId == null) return null;
        synchronized (STORE) {
            for (PollDoc p : STORE) {
                if (messageId.equals(p.messageId)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * List all polls for a guild (useful for debugging / future commands).
     */
    public static List<PollDoc> findByGuild(String guildId) {
        if (guildId == null) return List.of();
        List<PollDoc> result = new ArrayList<>();
        synchronized (STORE) {
            for (PollDoc p : STORE) {
                if (guildId.equals(p.guildId)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Ensure the poll is stored (idempotent-ish).
     */
    public static void save(PollDoc poll) {
        if (poll == null) return;
        synchronized (STORE) {
            if (!STORE.contains(poll)) {
                STORE.add(poll);
            }
        }
    }

    /**
     * Clear all polls (mainly for tests / dev).
     */
    public static void clearAll() {
        STORE.clear();
    }
}