// src/main/java/multybot/features/poll/PollRegistry.java
package multybot.features.poll;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class PollRegistry {

    private final Map<String, PollDoc> polls = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1L);

    public String nextId() {
        return Long.toString(seq.getAndIncrement());
    }

    public void save(PollDoc poll) {
        if (poll.id == null || poll.id.isBlank()) {
            poll.id = nextId();
        }
        polls.put(poll.id, poll);
    }

    public Optional<PollDoc> findById(String id) {
        return Optional.ofNullable(polls.get(id));
    }
}