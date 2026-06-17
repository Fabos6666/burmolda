package burmalda.visuals.api.mixin.render;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import burmalda.visuals.api.render.world.ProjectionUtils;
import burmalda.visuals.api.render.world.TargetEspRenderer;
import burmalda.visuals.util.minecraft.IMinecraft;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements IMinecraft {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderPrepare(RenderTickCounter tickCounter, CallbackInfo ci) {
        TargetEspRenderer.prepare();
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift = At.Shift.AFTER))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!TargetEspRenderer.isRendering()) {
            return;
        }

        ProjectionUtils.withCamera(mc.gameRenderer.getCamera(), TargetEspRenderer::drawQueued);
        TargetEspRenderer.finish();
    }
}
