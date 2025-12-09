package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class ModerationCaseRegistry {

    // Simple in-memory storage (per JVM)
    private final Map<String, ModerationCase> byId = new ConcurrentHashMap<>();

    /** Save or update a case. Generates id if missing. */
    public ModerationCase save(ModerationCase mc) {
        if (mc == null) return null;
        if (mc.id == null || mc.id.isBlank()) {
            mc.id = UUID.randomUUID().toString();
        }
        byId.put(mc.id, mc);
        return mc;
    }

    /** Find a case by its id. */
    public Optional<ModerationCase> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    /** Find all cases for a given guild. */
    public List<ModerationCase> findByGuild(String guildId) {
        if (guildId == null) return List.of();
        return byId.values().stream()
                .filter(mc -> guildId.equals(mc.guildId))
                .collect(Collectors.toList());
    }

    /** Delete a case by id, returning true if something was removed. */
    public boolean delete(String id) {
        if (id == null) return false;
        return byId.remove(id) != null;
    }

    /** Optional helper: remove all cases for a guild (not strictly needed). */
    public void clearGuild(String guildId) {
        if (guildId == null) return;
        byId.values().removeIf(mc -> guildId.equals(mc.guildId));
    }
}