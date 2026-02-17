package multybottest.time;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class SystemTimeProvider implements TimeProvider {
  @Override
  public Instant now() {
    return Instant.now();
  }
}
