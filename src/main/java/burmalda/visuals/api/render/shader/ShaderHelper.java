package burmalda.visuals.api.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;
import burmalda.visuals.util.minecraft.IMinecraft;

import java.nio.FloatBuffer;

public final class ShaderHelper implements IMinecraft {
    private static GlFramebuffer copyFbo;
    private static int framebufferWidth = -1;
    private static int framebufferHeight = -1;
    private static int fullscreenVao = -1;
    private static int fullscreenVbo = -1;

    private ShaderHelper() {
    }

    public static void checkFramebuffers() {
        RenderSystem.assertOnRenderThread();

        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        if (copyFbo != null && framebufferWidth == width && framebufferHeight == height) {
            return;
        }

        if (copyFbo != null) {
            copyFbo.delete();
        }

        framebufferWidth = width;
        framebufferHeight = height;
        copyFbo = new GlFramebuffer("copy", width, height, true);
    }

    public static void drawFullScreenQuad() {
        RenderSystem.assertOnRenderThread();
        ensureFullscreenQuad();

        GlStateManager._glBindVertexArray(fullscreenVao);
        GlStateManager._drawArrays(GL33.GL_TRIANGLE_STRIP, 0, 4);
        GlStateManager._glBindVertexArray(0);
    }

    public static GlFramebuffer getCopyFbo() {
        return copyFbo;
    }

    private static void ensureFullscreenQuad() {
        if (fullscreenVao >= 0) {
            return;
        }

        float[] vertices = new float[]{
                -1.0F, -1.0F, 0.0F,
                1.0F, -1.0F, 0.0F,
                -1.0F, 1.0F, 0.0F,
                1.0F, 1.0F, 0.0F
        };

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        fullscreenVao = GlStateManager._glGenVertexArrays();
        fullscreenVbo = GlStateManager._glGenBuffers();

        GlStateManager._glBindVertexArray(fullscreenVao);
        GlStateManager._glBindBuffer(GL33.GL_ARRAY_BUFFER, fullscreenVbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW);
        GlStateManager._enableVertexAttribArray(0);
        GlStateManager._vertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0L);
        GlStateManager._glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GlStateManager._glBindVertexArray(0);
    }
}
