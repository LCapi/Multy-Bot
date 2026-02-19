package multybottest.help.commands;

import jakarta.enterprise.context.ApplicationScoped;
import multybottest.help.CommandInfo;

@ApplicationScoped
public class UptimeCommandInfo implements CommandInfo {
  @Override public String name() { return "uptime"; }
  @Override public String description() { return "Shows the bot uptime."; }
}
