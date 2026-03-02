package multybottest.lang;

public enum Language {
  ES("es"),
  EN("en");

  private final String code;

  Language(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static Language fromCode(String raw) {
    if (raw == null) return null;
    String c = raw.trim().toLowerCase();
    for (Language l : values()) {
      if (l.code.equals(c)) return l;
    }
    return null;
  }
}
