package multybottest.uptime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybottest.time.AppStartTime;
import multybottest.time.TimeProvider;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class UptimeUseCase {

  private final TimeProvider timeProvider;
  private final AppStartTime appStartTime;

  @Inject
  public UptimeUseCase(TimeProvider timeProvider, AppStartTime appStartTime) {
    this.timeProvider = timeProvider;
    this.appStartTime = appStartTime;
  }

  public String execute() {
    Instant start = appStartTime.startInstant();
    Instant now = timeProvider.now();

    Duration uptime = Duration.between(start, now);
    return UptimeFormatter.format(uptime);
  }
}
