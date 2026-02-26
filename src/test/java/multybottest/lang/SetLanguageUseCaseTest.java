package multybottest.lang;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import multybottest.testdoubles.FakeLanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetLanguageUseCaseTest {

  @Mock DiscordGateway discord;

  @Test
  void without_param_shows_current_and_available_defaulting_to_en() {
    var repo = new FakeLanguageRepository();
    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());
    useCase.handle(ctx);

    verify(discord).reply("i1", "Current language: en\nAvailable: es, en");
    verifyNoMoreInteractions(discord);
  }

  @Test
  void without_param_shows_current_when_set() {
    var repo = new FakeLanguageRepository();
    repo.setLanguage("g1", Language.ES);

    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of());
    useCase.handle(ctx);

    verify(discord).reply("i1", "Current language: es\nAvailable: es, en");
    verifyNoMoreInteractions(discord);
  }

  @Test
  void valid_language_sets_and_confirms() {
    var repo = new FakeLanguageRepository();
    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of("lang", "es"));
    useCase.handle(ctx);

    verify(discord).reply("i1", "Language set to: es");
    verifyNoMoreInteractions(discord);

    // state persisted in repo
    assert repo.getLanguage("g1") == Language.ES;
  }

  @Test
  void trims_and_is_case_insensitive() {
    var repo = new FakeLanguageRepository();
    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of("lang", " EN "));
    useCase.handle(ctx);

    verify(discord).reply("i1", "Language set to: en");
    verifyNoMoreInteractions(discord);
    assert repo.getLanguage("g1") == Language.EN;
  }

  @Test
  void invalid_language_replies_error_and_does_not_set() {
    var repo = new FakeLanguageRepository();
    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of("lang", "xx"));
    useCase.handle(ctx);

    verify(discord).reply("i1", "Unsupported language: xx\nAvailable: es, en");
    verifyNoMoreInteractions(discord);

    assert repo.getLanguage("g1") == null;
  }

  @Test
  void blank_language_treated_as_missing_param() {
    var repo = new FakeLanguageRepository();
    var useCase = new SetLanguageUseCase(discord, repo);

    var ctx = new CommandContext("i1", "g1", "c1", "u1", Map.of("lang", "   "));
    useCase.handle(ctx);

    verify(discord).reply("i1", "Current language: en\nAvailable: es, en");
    verifyNoMoreInteractions(discord);
  }
}
