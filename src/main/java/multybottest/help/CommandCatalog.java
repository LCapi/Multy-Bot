package multybottest.help;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class CommandCatalog {

  private final Instance<CommandInfo> commandInfos;

  @Inject
  public CommandCatalog(Instance<CommandInfo> commandInfos) {
    this.commandInfos = commandInfos;
  }

  public List<CommandInfo> listSorted() {
    return commandInfos.stream()
        .sorted(Comparator.comparing(CommandInfo::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }
}
