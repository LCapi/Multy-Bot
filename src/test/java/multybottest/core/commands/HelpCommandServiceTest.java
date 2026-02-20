package multybottest.core.commands;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpCommandServiceTest {
    @Mock
    private DiscordGateway gateway;
}