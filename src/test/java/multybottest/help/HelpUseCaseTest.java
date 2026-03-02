package multybottest.help;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpUseCaseTest {

  @Mock DiscordGateway discord;
  @Mock CommandCatalog catalog;

  @Test
  void replies_with_sorted_stable_help_output() {
    when(catalog.list()).thenReturn(List.of(
        new CommandDescriptor("uptime", "Shows the bot uptime."),
        new CommandDescriptor("ping", "Replies with pong."),
        new CommandDescriptor("help", "Lists available commands."),
        new CommandDescriptor("lang", "Sets the bot language."),
        new CommandDescriptor("poll", "Creates a poll.")
    ));

    var useCase = new HelpUseCase(discord, catalog);
    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

    useCase.handle(ctx);

    var msgCaptor = ArgumentCaptor.forClass(String.class);
    verify(discord).reply(eq("i1"), msgCaptor.capture());
    verifyNoMoreInteractions(discord);

    String expected = """
        Available commands:
        /help - Lists available commands.
        /lang - Sets the bot language.
        /ping - Replies with pong.
        /poll - Creates a poll.
        /uptime - Shows the bot uptime.
        """;

    assertEquals(expected, msgCaptor.getValue());
  }

  @Test
  void replies_with_header_when_catalog_is_empty() {
    when(catalog.list()).thenReturn(List.of());

    var useCase = new HelpUseCase(discord, catalog);
    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());

    useCase.handle(ctx);

    verify(discord).reply("i1", "Available commands:\n");
    verifyNoMoreInteractions(discord);
  }
}
