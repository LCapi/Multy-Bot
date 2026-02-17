package multybottest.uptime;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import multybottest.time.TimeProvider;

import java.time.Instant;

@Alternative
@Priority(1)
@ApplicationScoped
public class TestTimeProvider implements TimeProvider {

  public static volatile Instant NOW = Instant.parse("2026-02-16T18:00:00Z");

  @Override
  public Instant now() {
    return NOW;
  }
}
