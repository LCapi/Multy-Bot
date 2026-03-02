package multybottest.poll;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePollUseCaseTest {

  @Mock DiscordGateway discord;

  @Test
  void creates_poll_with_two_options() {
    var useCase = new CreatePollUseCase(discord);

    var ctx = new CommandContext("i1", "g1", "c1", "u1",
        Map.of("question", "Best color?", "options", "Blue | Red"));

    useCase.handle(ctx);

    var msg = ArgumentCaptor.forClass(String.class);
    verify(discord).reply(eq("i1"), msg.capture());
    verifyNoMoreInteractions(discord);

    String expected = """
        📊 Best color?
        1) Blue
        2) Red
        """;
    assertEquals(expected, msg.getValue());
  }

  @Test
  void trims_question_and_options_and_ignores_empty_options() {
    var useCase = new CreatePollUseCase(discord);

    var ctx = new CommandContext("i1", "g1", "c1", "u1",
        Map.of("question", "  Lunch?  ", "options", "  Pizza |  |  Sushi  |   "));

    useCase.handle(ctx);

    verify(discord).reply("i1", """
        📊 Lunch?
        1) Pizza
        2) Sushi
        """);
    verifyNoMoreInteractions(discord);
  }

  @Test
  void rejects_empty_question() {
    var useCase = new CreatePollUseCase(discord);

    var ctx = new CommandContext("i1", "g1", "c1", "u1",
        Map.of("question", "   ", "options", "A|B"));

    useCase.handle(ctx);

    verify(discord).reply("i1", "Poll question cannot be empty.");
    verifyNoMoreInteractions(discord);
  }

  @Test
  void rejects_less_than_two_options() {
    var useCase = new CreatePollUseCase(discord);

    var ctx = new CommandContext("i1", "g1", "c1", "u1",
        Map.of("question", "Pick one", "options", "OnlyOne"));

    useCase.handle(ctx);

    verify(discord).reply("i1", "Poll must have at least 2 options.");
    verifyNoMoreInteractions(discord);
  }

  @Test
  void rejects_more_than_max_options() {
    var useCase = new CreatePollUseCase(discord);

    var ctx = new CommandContext("i1", "g1", "c1", "u1",
        Map.of(
            "question", "Pick",
            "options", "1|2|3|4|5|6|7|8|9|10|11"
        ));

    useCase.handle(ctx);

    verify(discord).reply("i1", "Poll cannot have more than 10 options.");
    verifyNoMoreInteractions(discord);
  }
}
