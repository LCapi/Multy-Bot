package multybot.infra;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final Pattern CHUNK = Pattern.compile("(\\d+)\\s*([wdhms])", Pattern.CASE_INSENSITIVE);
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Madrid");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    /** Parsea "1w2d3h4m5s" o "1h 30m" → Duration */
    public static Duration parseDuration(String s) {
        if (s == null) return null;
        Matcher m = CHUNK.matcher(s.trim());
        long total = 0;
        int hits = 0;
        while (m.find()) {
            long n = Long.parseLong(m.group(1));
            char u = Character.toLowerCase(m.group(2).charAt(0));
            hits++;
            switch (u) {
                case 'w' -> total += Duration.ofDays(7).getSeconds() * n;
                case 'd' -> total += Duration.ofDays(n).getSeconds();
                case 'h' -> total += Duration.ofHours(n).getSeconds();
                case 'm' -> total += Duration.ofMinutes(n).getSeconds();
                case 's' -> total += n;
            }
        }
        return hits == 0 ? null : Duration.ofSeconds(total);
    }

    /** Parse "YYYY-MM-DD HH:mm" (zona por defecto), "YYYY-MM-DDTHH:mm[+offset|Z]" */
    public static Instant parseAt(String s, ZoneId defaultZone) {
        if (s == null) return null;
        s = s.trim();
        // Con offset o Z
        try { return OffsetDateTime.parse(s).toInstant(); } catch (DateTimeParseException ignore) {}
        try { return ZonedDateTime.parse(s).toInstant(); } catch (DateTimeParseException ignore) {}
        try { return Instant.parse(s); } catch (DateTimeParseException ignore) {}
        // Sin zona → usar zona por defecto
        try {
            DateTimeFormatter f1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime ldt = LocalDateTime.parse(s, f1);
            return ldt.atZone(defaultZone).toInstant();
        } catch (DateTimeParseException ignore) {}
        try {
            DateTimeFormatter f2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime ldt = LocalDateTime.parse(s, f2);
            return ldt.atZone(defaultZone).toInstant();
        } catch (DateTimeParseException ignore) {}
        return null;
    }

    public static String fmtInstant(Instant when, ZoneId zone, Locale locale) {
        return FMT.withLocale(locale).format(when.atZone(zone));
    }
}
