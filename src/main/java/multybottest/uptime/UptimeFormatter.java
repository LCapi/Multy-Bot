package multybottest.uptime;

import java.time.Duration;

public final class UptimeFormatter {

  private UptimeFormatter() {}

  // Format:
  // "Uptime: 00h 00m 05s"
  // "Uptime: 1d 02h 03m 04s" (days only if > 0)
  public static String format(Duration d) {
    if (d.isNegative()) d = Duration.ZERO;

    long seconds = d.getSeconds();

    long days = seconds / 86_400;
    seconds %= 86_400;

    long hours = seconds / 3_600;
    seconds %= 3_600;

    long minutes = seconds / 60;
    seconds %= 60;

    StringBuilder sb = new StringBuilder("Uptime: ");
    if (days > 0) sb.append(days).append("d ");

    sb.append(String.format("%02dh %02dm %02ds", hours, minutes, seconds));
    return sb.toString();
  }
}
