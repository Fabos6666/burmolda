package burmalda.visuals.api.mixin.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.gl.RenderPassImpl;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import burmalda.visuals.api.render.world.WorldOverrideUtils;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {
    @Inject(method = "drawBoundObjectWithRenderPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlCommandEncoder;drawObjectWithRenderPass(Lnet/minecraft/client/gl/RenderPassImpl;IIILcom/mojang/blaze3d/vertex/VertexFormat$IndexType;Lnet/minecraft/client/gl/CompiledShaderPipeline;I)V", shift = At.Shift.BEFORE))
    private void applyWorld(RenderPassImpl pass, int start, int firstIndex, int indexCount, VertexFormat.IndexType indexType, int instanceCount, CallbackInfo ci) {
        WorldOverrideUtils.PassOverride override = WorldOverrideUtils.get();
        if (override == null) {
            return;
        }

        if (override.additive()) {
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        }
        if (override.noDepth()) {
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GL11.GL_ALWAYS);
            GlStateManager._depthMask(false);
        }
    }

    @Inject(method = "drawBoundObjectWithRenderPass", at = @At("TAIL"))
    private void resetWorld(RenderPassImpl pass, int start, int firstIndex, int indexCount, VertexFormat.IndexType indexType, int instanceCount, CallbackInfo ci) {
        WorldOverrideUtils.PassOverride override = WorldOverrideUtils.get();
        if (override == null) {
            return;
        }

        if (override.noDepth()) {
            GlStateManager._depthMask(true);
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GL11.GL_LEQUAL);
        }
        if (override.additive()) {
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        }
    }
}
