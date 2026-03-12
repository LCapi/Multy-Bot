package multybot.infra.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.DiscordGatewayState;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class BotLivenessCheck implements HealthCheck {

        @Inject
        DiscordGatewayState gatewayState;

        @Override
        public HealthCheckResponse call() {
                if (gatewayState.hasFatalError()) {
                        return HealthCheckResponse.named("bot-liveness")
                                .down()
                                .withData("reason", "fatal gateway error")
                                .build();
                }

                return HealthCheckResponse.named("bot-liveness")
                        .up()
                        .build();        }
}