package multybottest.uptime;

import multybottest.time.AppStartTime;
import multybottest.time.FakeTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UptimeUseCaseTest {

  @Test
  void uptime_is_deterministic_and_format_is_valid() {
    var fakeClock = new FakeTimeProvider(Instant.parse("2026-02-16T18:00:00Z"));

    var start = new AppStartTime(fakeClock);
    start.init(); // call @PostConstruct manually for pure unit test

    var useCase = new UptimeUseCase(fakeClock, start);

    fakeClock.setNow(Instant.parse("2026-02-16T18:00:05Z"));
    assertEquals("Uptime: 00h 00m 05s", useCase.execute());

    fakeClock.setNow(Instant.parse("2026-02-16T19:02:03Z"));
    assertEquals("Uptime: 01h 02m 03s", useCase.execute());

    fakeClock.setNow(Instant.parse("2026-02-17T20:03:04Z"));
    assertEquals("Uptime: 1d 02h 03m 04s", useCase.execute());
  }

  @Test
  void uptime_never_negative() {
    var fakeClock = new FakeTimeProvider(Instant.parse("2026-02-16T18:00:10Z"));

    var start = new AppStartTime(fakeClock);
    start.init();

    var useCase = new UptimeUseCase(fakeClock, start);

    fakeClock.setNow(Instant.parse("2026-02-16T18:00:00Z"));
    assertEquals("Uptime: 00h 00m 00s", useCase.execute());
  }
}
