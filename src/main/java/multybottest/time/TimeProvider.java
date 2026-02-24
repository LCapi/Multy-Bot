package multybottest.time;

import java.time.Instant;

public interface TimeProvider {
  Instant now();
}
