package multybottest.core;

import java.util.Map;

public record CommandContext(
        String interactionId,
        String guildId,
        String channelId,
        String userId,
        Map<String, String> options
) {}