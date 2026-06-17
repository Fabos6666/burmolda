package burmalda.visuals.api.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL33;

public final class PassThroughShader extends Shader {
    public PassThroughShader() {
        super("effects", "passthrough");
    }

    public static void renderToFramebuffer(Shader shader, GlFramebuffer source, GlFramebuffer target) {
        RenderSystem.assertOnRenderThread();

        if (target != null) {
            target.bindForWrite(true);
        }

        shader.bind();
        shader.setUniform1i("Tex0", 0);
        shader.setUniformBool("Alpha", true);

        GlStateManager._activeTexture(GL33.GL_TEXTURE0);
        GlStateManager._bindTexture(source.getColorTextureId());
        ShaderHelper.drawFullScreenQuad();
        GlStateManager._bindTexture(0);

        shader.unbind();
    }
}
