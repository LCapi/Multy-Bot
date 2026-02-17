package multybottest.uptime;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UptimeUseCaseQuarkusTest {

  @Inject UptimeUseCase useCase;

  @Test
  void executes_with_di_and_fake_time() {
    // AppStartTime is initialized during Quarkus boot. This test assumes the start time
    // aligns with the initial NOW value. Then we advance NOW and assert the output.
    TestTimeProvider.NOW = Instant.parse("2026-02-16T18:00:00Z");
    TestTimeProvider.NOW = Instant.parse("2026-02-16T18:00:05Z");

    assertEquals("Uptime: 00h 00m 05s", useCase.execute());
  }
}
