package burmalda.visuals.api.mixin.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import burmalda.visuals.Burmalda;
import burmalda.visuals.api.event.impl.Event2DRender;
import burmalda.visuals.api.render.Render2D;
import burmalda.visuals.util.minecraft.IMinecraft;

@Mixin(InGameHud.class)
public class InGameHudMixin implements IMinecraft {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Burmalda.getEventBus() != null) {
            Burmalda.getEventBus().post(new Event2DRender(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
        }

        Render2D.get().drawBuffer();
    }
}
