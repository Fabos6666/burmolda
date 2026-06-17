package burmalda.visuals;

import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ModInitializer;
import burmalda.visuals.api.manager.Manager;
import lombok.Getter;

public final class Burmalda implements ModInitializer {
    @Getter private static EventBus eventBus;
    @Getter private static Manager manager;
    private static boolean initialized;

    @Override
    public synchronized void onInitialize() {
        if (initialized) {
            return;
        }

        eventBus = new EventBus();
        manager = new Manager();
        initialized = true;
    }
}
