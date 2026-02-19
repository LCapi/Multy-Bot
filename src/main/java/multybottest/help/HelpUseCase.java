package multybottest.help;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HelpUseCase {

  private final CommandCatalog catalog;

  @Inject
  public HelpUseCase(CommandCatalog catalog) {
    this.catalog = catalog;
  }

  public String execute() {
    var commands = catalog.listSorted();

    StringBuilder sb = new StringBuilder("Available commands:\n");
    for (CommandInfo c : commands) {
      sb.append("/")
        .append(c.name())
        .append(" - ")
        .append(c.description())
        .append("\n");
    }
    return sb.toString();
  }
}
