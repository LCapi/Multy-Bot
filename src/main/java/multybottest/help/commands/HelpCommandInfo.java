package multybottest.help.commands;

import jakarta.enterprise.context.ApplicationScoped;
import multybottest.help.CommandInfo;

@ApplicationScoped
public class HelpCommandInfo implements CommandInfo {
  @Override public String name() { return "help"; }
  @Override public String description() { return "Lists available commands."; }
}
