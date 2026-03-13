package multybot.infra.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.DiscordGatewayState;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class BotReadinessCheck implements HealthCheck {

        @Inject
        DiscordGatewayState gatewayState;

        @Override
        public HealthCheckResponse call() {
                if (!gatewayState.isReady()) {
                        return HealthCheckResponse.named("bot-readiness")
                                .down()
                                .build();
                }

                return HealthCheckResponse.named("bot-readiness")
                        .up()
                        .build();
        }
}