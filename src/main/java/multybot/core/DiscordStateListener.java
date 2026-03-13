package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.DiscordGatewayState;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@ApplicationScoped
public class DiscordStateListener extends ListenerAdapter {

        @Inject
        DiscordGatewayState gatewayState;

        @Override
        public void onReady(ReadyEvent event) {
                gatewayState.markReady();
        }

        @Override
        public void onSessionDisconnect(SessionDisconnectEvent event) {
                gatewayState.markNotReady();
        }

        @Override
        public void onShutdown(ShutdownEvent event) {
                gatewayState.markNotReady();
        }
}