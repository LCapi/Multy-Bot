package multybottest.testdoubles;

import multybottest.lang.Language;
import multybottest.lang.LanguageRepository;

import java.util.HashMap;
import java.util.Map;

public final class FakeLanguageRepository implements LanguageRepository {

  private final Map<String, Language> store = new HashMap<>();

  @Override
  public Language getLanguage(String guildId) {
    return store.get(guildId);
  }

  @Override
  public void setLanguage(String guildId, Language language) {
    store.put(guildId, language);
  }
}
