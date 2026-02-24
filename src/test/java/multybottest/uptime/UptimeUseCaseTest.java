package multybottest.uptime;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import multybottest.time.FakeTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UptimeUseCaseTest {

  @Mock DiscordGateway discord;

  @Test
  void uptime_is_deterministic_and_formatted() {
    Instant startedAt = Instant.parse("2026-02-16T18:00:00Z");
    var clock = new FakeTimeProvider(Instant.parse("2026-02-16T18:00:05Z"));

    var useCase = new UptimeUseCase(discord, clock, startedAt);
    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

    useCase.handle(ctx);

    var msgCaptor = ArgumentCaptor.forClass(String.class);
    verify(discord).reply(eq("i1"), msgCaptor.capture());
    verifyNoMoreInteractions(discord);

    assertEquals("Uptime: 00h 00m 05s", msgCaptor.getValue());
  }

  @Test
  void uptime_includes_days_when_needed() {
    Instant startedAt = Instant.parse("2026-02-16T18:00:00Z");
    var clock = new FakeTimeProvider(Instant.parse("2026-02-17T20:03:04Z")); // +1d 2h 3m 4s

    var useCase = new UptimeUseCase(discord, clock, startedAt);
    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

    useCase.handle(ctx);

    verify(discord).reply("i1", "Uptime: 1d 02h 03m 04s");
    verifyNoMoreInteractions(discord);
  }

  @Test
  void uptime_is_never_negative() {
    Instant startedAt = Instant.parse("2026-02-16T18:00:10Z");
    var clock = new FakeTimeProvider(Instant.parse("2026-02-16T18:00:00Z")); // before start

    var useCase = new UptimeUseCase(discord, clock, startedAt);
    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

    useCase.handle(ctx);

    verify(discord).reply("i1", "Uptime: 00h 00m 00s");
    verifyNoMoreInteractions(discord);
  }
}
