package multybottest.core.commands;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PingCommandServiceTest {

    @Mock DiscordGateway discord;

    @Test
    void repliesPong() {
        var service = new PingCommandService(discord);

        var ctx = new CommandContext(
                "interaction-1",
                "guild-1",
                "channel-1",
                "user-1",
                Map.of()
        );

        service.handle(ctx);

        verify(discord).reply("interaction-1", "pong");
        verifyNoMoreInteractions(discord);
    }
}