package multybot.infra;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class DiscordGatewayState {

        private final AtomicBoolean started = new AtomicBoolean(false);
        private final AtomicBoolean ready = new AtomicBoolean(false);
        private final AtomicBoolean fatalError = new AtomicBoolean(false);

        public boolean isStarted() {
                return started.get();
        }

        public boolean isReady() {
                return ready.get();
        }

        public boolean hasFatalError() {
                return fatalError.get();
        }

        public void markStarted() {
                started.set(true);
        }

        public void markReady() {
                ready.set(true);
        }

        public void markNotReady() {
                ready.set(false);
        }

        public void markFatalError() {
                fatalError.set(true);
        }
}