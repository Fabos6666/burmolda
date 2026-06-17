package burmalda.visuals.api.module.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import burmalda.visuals.api.event.impl.EventAttackEntity;
import burmalda.visuals.api.event.impl.EventWorldRender;
import burmalda.visuals.api.module.Module;
import burmalda.visuals.api.module.ModuleCategory;
import burmalda.visuals.api.module.ModuleInfo;
import burmalda.visuals.api.render.world.TargetEspRenderer;

import java.awt.Color;

@ModuleInfo(name = "Target ESP", category = ModuleCategory.COMBAT)
public final class TargetESP extends Module {
    private static final long TARGET_KEEP_MS = 3_000L;

    private LivingEntity lastTarget;
    private float appear;
    private long lastFrame = -1L;
    private long lastAttackMs;

    public TargetESP() {
        setKey(GLFW.GLFW_KEY_Z);
        setEnabled(true);
    }

    @Subscribe
    public void onWorldRender(EventWorldRender event) {
        if (fullNullCheck() || !isEnabled()) {
            return;
        }

        LivingEntity target = getActiveTarget();

        long now = System.nanoTime();
        float frameSeconds = lastFrame > 0L ? (now - lastFrame) / 1_000_000_000.0F : 1.0F / 60.0F;
        lastFrame = now;

        float appearDelta = frameSeconds / 0.3F;
        appear = MathHelper.clamp(appear + (target != null ? appearDelta : -appearDelta), 0.0F, 1.0F);

        if (target != null) {
            lastTarget = target;
        }

        if (lastTarget == null || appear <= 0.0F) {
            return;
        }

        TargetEspRenderer.drawCrystals(lastTarget, appear, new Color(246, 246, 246, 255));
    }

    @Subscribe
    public void onAttackEntity(EventAttackEntity event) {
        if (!isEnabled() || !(event.getTarget() instanceof LivingEntity livingEntity) || livingEntity == mc.player) {
            return;
        }

        lastTarget = livingEntity;
        lastAttackMs = System.currentTimeMillis();
    }

    private LivingEntity getActiveTarget() {
        if (lastTarget == null || !lastTarget.isAlive() || lastTarget.isRemoved() || System.currentTimeMillis() - lastAttackMs > TARGET_KEEP_MS) {
            return null;
        }

        return lastTarget;
    }

    @Override
    public void onDisable() {
        lastTarget = null;
        appear = 0.0F;
        lastFrame = -1L;
        lastAttackMs = 0L;
    }
}
