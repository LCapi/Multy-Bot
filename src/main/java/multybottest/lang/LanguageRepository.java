package multybottest.lang;

public interface LanguageRepository {
  Language getLanguage(String guildId);
  void setLanguage(String guildId, Language language);
}
