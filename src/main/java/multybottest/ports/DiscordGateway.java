package multybottest.ports;

public interface DiscordGateway {
    void reply(String interactionId, String message);
    boolean hasPermission(String guildId, String userId, String permission);
}