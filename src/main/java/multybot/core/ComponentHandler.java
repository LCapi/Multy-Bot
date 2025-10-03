package multybot.core;

public interface ComponentHandler {
    boolean matches(String customId);
    void handle(ComponentContext ctx) throws Exception;
}
