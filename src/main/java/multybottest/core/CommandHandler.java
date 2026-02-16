package multybottest.core;

public interface CommandHandler {
  String name();                 // e.g. "ping"
  void handle(CommandContext ctx);
}