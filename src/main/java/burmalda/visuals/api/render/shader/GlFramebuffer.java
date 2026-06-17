package burmalda.visuals.api.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public final class GlFramebuffer {
    private final String name;
    private final boolean useDepth;

    private int fboId;
    private int colorTextureId;
    private int depthTextureId;
    private int width;
    private int height;

    public GlFramebuffer(String name, int width, int height, boolean useDepth) {
        this.name = name;
        this.useDepth = useDepth;
        resize(width, height);
    }

    public void resize(int width, int height) {
        RenderSystem.assertOnRenderThread();
        if (width <= 0 || height <= 0) {
            return;
        }

        if (this.width == width && this.height == height && this.fboId != 0) {
            return;
        }

        delete();

        this.width = width;
        this.height = height;

        this.fboId = GlStateManager.glGenFramebuffers();
        this.colorTextureId = createColorTexture(width, height);
        this.depthTextureId = useDepth ? createDepthTexture(width, height) : 0;

        GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, fboId);
        GlStateManager._glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, colorTextureId, 0);
        if (useDepth) {
            GlStateManager._glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, GL33.GL_TEXTURE_2D, depthTextureId, 0);
        }

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer '" + name + "' is incomplete");
        }

        GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    public void bindForWrite(boolean clear) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, fboId);
        GlStateManager._viewport(0, 0, width, height);
        if (clear) {
            clear();
        }
    }

    public void clear() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glBindFramebuffer(GL33.GL_FRAMEBUFFER, fboId);
        int mask = GL11.GL_COLOR_BUFFER_BIT;
        if (useDepth) {
            mask |= GL11.GL_DEPTH_BUFFER_BIT;
        }
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager._clear(mask);
    }

    public void delete() {
        RenderSystem.assertOnRenderThread();
        if (depthTextureId != 0) {
            GlStateManager._deleteTexture(depthTextureId);
            depthTextureId = 0;
        }
        if (colorTextureId != 0) {
            GlStateManager._deleteTexture(colorTextureId);
            colorTextureId = 0;
        }
        if (fboId != 0) {
            GlStateManager._glDeleteFramebuffers(fboId);
            fboId = 0;
        }
    }

    public int getColorTextureId() {
        return colorTextureId;
    }

    public int getDepthTextureId() {
        return depthTextureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasDepth() {
        return useDepth;
    }

    private static int createColorTexture(int width, int height) {
        int textureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(textureId);
        GlStateManager._texImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA8, width, height, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GlStateManager._bindTexture(0);
        return textureId;
    }

    private static int createDepthTexture(int width, int height) {
        int textureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(textureId);
        GlStateManager._texImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT24, width, height, 0, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, (ByteBuffer) null);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GlStateManager._bindTexture(0);
        return textureId;
    }
}
