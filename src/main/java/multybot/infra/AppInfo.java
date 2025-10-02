package multybot.infra;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class AppInfo {
    private Instant startedAt;

    void onStart(@Observes StartupEvent ev) { startedAt = Instant.now(); }
    void onStop(@Observes ShutdownEvent ev) { /* no-op, pero queda por si quieres medir */ }

    public Instant startedAt() {
        return startedAt == null ? Instant.now() : startedAt;
    }

    public Duration uptime() {
        return Duration.between(startedAt(), Instant.now());
    }

    public static String human(Duration d) {
        long s = d.getSeconds();
        long days = s / 86_400;  s %= 86_400;
        long hrs  = s / 3_600;   s %= 3_600;
        long mins = s / 60;      s %= 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hrs  > 0) sb.append(hrs).append("h ");
        if (mins > 0) sb.append(mins).append("m ");
        sb.append(s).append("s");
        return sb.toString().trim();
    }
}
