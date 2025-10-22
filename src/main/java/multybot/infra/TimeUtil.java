package multybot.infra;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtil {
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Madrid");
    public static String fmtInstant(Instant instant, ZoneId zone, Locale locale) {
        if (instant == null) return "-";
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withLocale(locale).withZone(zone);
        return fmt.format(instant);
    }
}
