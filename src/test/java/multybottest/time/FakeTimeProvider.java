package multybottest.time;

import java.time.Instant;

public final class FakeTimeProvider implements TimeProvider {

  private Instant now;

  public FakeTimeProvider(Instant initialNow) {
    this.now = initialNow;
  }

  @Override
  public Instant now() {
    return now;
  }

  public void setNow(Instant now) {
    this.now = now;
  }
}
