package burmalda.visuals.api.module;

import net.minecraft.text.Text;
import burmalda.visuals.Burmalda;
import burmalda.visuals.util.minecraft.IMinecraft;

public abstract class Module implements IMinecraft {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private int key;
    private boolean enabled;

    protected Module() {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            throw new IllegalStateException("Missing @ModuleInfo on " + getClass().getName());
        }

        this.name = info.name();
        this.description = info.description();
        this.category = info.category();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public final void toggle() {
        setEnabled(!enabled);

        if (enabled) {
            onEnable();
            if (Burmalda.getEventBus() != null) {
                Burmalda.getEventBus().register(this);
            }
        } else {
            onDisable();
            if (Burmalda.getEventBus() != null) {
                Burmalda.getEventBus().unregister(this);
            }
        }

        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(name + (enabled ? " enabled" : " disabled")), false);
        }
    }

    public final boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final ModuleCategory getCategory() {
        return category;
    }

    public final int getKey() {
        return key;
    }

    public final void setKey(int key) {
        this.key = key;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
