package burmalda.visuals.api.manager.impl;

import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;
import burmalda.visuals.Burmalda;
import burmalda.visuals.api.event.impl.EventKeyboard;
import burmalda.visuals.api.module.Module;
import burmalda.visuals.api.module.combat.TargetESP;
import burmalda.visuals.api.module.render.Interface;
import burmalda.visuals.util.minecraft.IMinecraft;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager implements IMinecraft {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        Burmalda.getEventBus().register(this);

        modules.add(new TargetESP());
        modules.add(new Interface());

        for (Module module : modules) {
            if (module.isEnabled()) {
                Burmalda.getEventBus().register(module);
                module.onEnable();
            }
        }
    }

    @Subscribe
    public void onKeyboard(EventKeyboard event) {
        if (mc.currentScreen != null || event.getAction() != GLFW.GLFW_PRESS || event.getInput() == null) {
            return;
        }

        int keyCode = event.getInput().getKeycode();
        for (Module module : modules) {
            if (module.getKey() == keyCode) {
                module.toggle();
            }
        }
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module module : modules) {
            if (moduleClass.isInstance(module)) {
                return moduleClass.cast(module);
            }
        }
        return null;
    }

    public List<Module> getModules() {
        return modules;
    }
}
