package multybottest.help.commands;

import jakarta.enterprise.context.ApplicationScoped;
import multybottest.help.CommandInfo;

@ApplicationScoped
public class PingCommandInfo implements CommandInfo {
  @Override public String name() { return "ping"; }
  @Override public String description() { return "Replies with pong."; }
}
