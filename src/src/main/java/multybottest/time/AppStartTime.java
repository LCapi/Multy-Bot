package multybottest.time;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class AppStartTime {

  private final TimeProvider timeProvider;
  private Instant start;

  @Inject
  public AppStartTime(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  @PostConstruct
  void init() {
    this.start = timeProvider.now();
  }

  public Instant startInstant() {
    return start;
  }
}
