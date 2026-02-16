package multybottest.core;

import multybottest.ports.DiscordGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock DiscordGateway discord;
    @Mock CommandHandler pingHandler;

    @Test
    void dispatchesToHandler() {
        when(pingHandler.name()).thenReturn("ping");

        var dispatcher = new CommandDispatcher(List.of(pingHandler), discord);
        var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

        dispatcher.dispatch("ping", ctx);

        verify(pingHandler).handle(ctx);
        verifyNoInteractions(discord);
    }

    @Test
    void repliesUnknownCommand() {
        when(pingHandler.name()).thenReturn("ping");

        var dispatcher = new CommandDispatcher(List.of(pingHandler), discord);
        reset(pingHandler); // <-- borra la interacción "name()" del constructor

        var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

        dispatcher.dispatch("nope", ctx);

        verify(discord).reply("i1", "Unknown command: nope");
        verifyNoMoreInteractions(discord);
        verifyNoInteractions(pingHandler); // ahora sí
    }
}