package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class ComponentRouter {
    @Inject Instance<ComponentHandler> handlers;

    public void route(ComponentContext ctx) throws Exception {
        String id = ctx.event().getComponentId();
        for (ComponentHandler h : handlers) {
            if (h.matches(id)) { h.handle(ctx); return; }
        }
        // sin handler: ignora
    }
}
