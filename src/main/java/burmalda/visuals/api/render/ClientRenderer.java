package burmalda.visuals.api.render;

import burmalda.visuals.api.render.msdf.Fonts;
import burmalda.visuals.api.render.msdf.MsdfFont;
import burmalda.visuals.api.render.msdf.MsdfGlyph;
import burmalda.visuals.api.render.uniforms.*;
import burmalda.visuals.util.minecraft.IMinecraft;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class ClientRenderer implements IMinecraft {
    private static final int MAX_KAWASE_LEVELS = 6;

    private final ProjectionMatrix2 guiProjectionMatrix = new ProjectionMatrix2("client renderer", 1000.0F, 11000.0F, true);
    private final ArrayList<QueuedRender> renderQueue = new ArrayList<>();
    private final BufferAllocator allocator = new BufferAllocator(1 << 20);
    private final Supplier<String> bufferName = () -> "client render";

    private final MatrixStack stack = new MatrixStack();
    private final MatrixStack.Entry defaultMatrixEntry;

    private boolean msdfTextureFailed;

    private final ArrayList<SimpleFramebuffer> kawaseLevels = new ArrayList<>();
    private int kawaseLevelsWidth = -1;
    private int kawaseLevelsHeight = -1;
    private int kawaseLevelsCount = -1;

    public ClientRenderer() {
        stack.peek().getPositionMatrix().setTranslation(0.0F, 0.0F, -10000.0F);
        defaultMatrixEntry = stack.peek().copy();
    }

    public MatrixStack getStack() {
        return stack;
    }

    public void rect(float x, float y, float width, float height, Vector4f round, float smooth, Color color1, Color color2, Color color3, Color color4) {
        try (BuiltBuffer builtBuffer = createRectBuffer(x, y, width, height, color1, color2, color3, color4).end()) {
            GpuBufferSlice uniforms = new RectUniform(new Vector2f(width, height), round, smooth).uniforms();
            enqueueDrawCall(PipelinesRepository.RECTANGLE, builtBuffer, uniforms, null, null, 1, stack.peek());
        }
    }

    public void rect(float x, float y, float width, float height, Vector4f round, float smooth, Color color) {
        try (BuiltBuffer builtBuffer = createRectBuffer(x, y, width, height, color, color, color, color).end()) {
            GpuBufferSlice uniforms = new RectUniform(new Vector2f(width, height), round, smooth).uniforms();
            enqueueDrawCall(PipelinesRepository.RECTANGLE, builtBuffer, uniforms, null, null, 1, stack.peek());
        }
    }

    public void border(float x, float y, float width, float height, float thickness, Vector4f round, float smooth, Color color1, Color color2, Color color3, Color color4) {
        try (BuiltBuffer builtBuffer = createRectBuffer(x, y, width, height, color1, color2, color3, color4).end()) {
            GpuBufferSlice uniforms = new BorderUniform(new Vector2f(width, height), round, thickness, smooth).uniforms();
            enqueueDrawCall(PipelinesRepository.BORDER, builtBuffer, uniforms, null, null, 1, stack.peek());
        }
    }

    public void border(float x, float y, float width, float height, float thickness, Vector4f round, float smooth, Color color) {
        try (BuiltBuffer builtBuffer = createRectBuffer(x, y, width, height, color, color, color, color).end()) {
            GpuBufferSlice uniforms = new BorderUniform(new Vector2f(width, height), round, thickness, smooth).uniforms();
            enqueueDrawCall(PipelinesRepository.BORDER, builtBuffer, uniforms, null, null, 1, stack.peek());
        }
    }

    public void image(Identifier textureId, float x, float y, float width, float height, Vector4f round, float smoothness, Color color) {
        image(textureId, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, round, smoothness, color, color, color, color);
    }

    public void image(Identifier textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, Vector4f round, float smoothness, Color color1, Color color2, Color color3, Color color4) {
        TextureBinding textureBinding = getTextureBinding(textureId);
        if (textureBinding == null) {
            return;
        }

        try (BuiltBuffer builtBuffer = createImageBuffer(x, y, width, height, u0, v0, u1, v1, color1, color2, color3, color4).end()) {
            GpuBufferSlice uniforms = new RectUniform(new Vector2f(width, height), round, smoothness).uniforms();
            enqueueDrawCall(PipelinesRepository.IMAGE, builtBuffer, uniforms, "Sampler0", textureBinding.view(), textureBinding.sampler(), 1, stack.peek());
        }
    }

    public void blur(float x, float y, float width, float height, Vector4f round, float blurRadius, float smoothness) {
        int iterations = Math.max(2, Math.min(12, Math.round(blurRadius)));
        float step = Math.max(0.75F, blurRadius * 0.35F);
        blur(x, y, width, height, round, iterations, step, smoothness, 1.0F);
    }

    public void blur(float x, float y, float width, float height, Vector4f round, int iterations, float offset, float smoothness, float alpha) {
        renderQueue.add(new BlurCall(
                x, y, width, height,
                new Vector4f(round),
                Math.max(1, iterations),
                Math.max(0.1F, offset),
                smoothness,
                alpha
        ));
    }

    public void text(Fonts.FontRef fontRef, String text, float x, float y, float size, Color color) {
        text(fontRef, text, x, y, size, 1.0F, color, color, color, color);
    }

    public void text(Fonts.FontRef fontRef, String text, float x, float y, float size, float smoothness,
                     Color color1, Color color2, Color color3, Color color4) {
        if (text == null || text.isEmpty()) {
            return;
        }
        MsdfFont font = fontRef.get();
        if (font == null) {
            return;
        }

        int quads = 0;
        float startX = x;
        float penX = x;
        float baseline = y + font.getBaselineHeight(size);
        int previousCodePoint = -1;

        BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (codePoint == '\n') {
                penX = startX;
                baseline += font.getLineHeight(size);
                previousCodePoint = -1;
                continue;
            }

            MsdfGlyph glyph = font.getGlyph(codePoint);
            if (glyph == null) {
                continue;
            }

            if (previousCodePoint != -1) {
                penX += font.getKerning(previousCodePoint, codePoint) * size;
            }

            if (glyph.hasAtlasBounds && glyph.hasPlaneBounds) {
                float x0 = penX + glyph.left * size;
                float y0 = baseline - glyph.top * size;
                float x1 = penX + glyph.right * size;
                float y1 = baseline - glyph.bottom * size;

                builder.vertex(x0, y0, 0.0F).texture(glyph.minU, glyph.minV).color(color1.getRGB());
                builder.vertex(x0, y1, 0.0F).texture(glyph.minU, glyph.maxV).color(color2.getRGB());
                builder.vertex(x1, y1, 0.0F).texture(glyph.maxU, glyph.maxV).color(color3.getRGB());
                builder.vertex(x1, y0, 0.0F).texture(glyph.maxU, glyph.minV).color(color4.getRGB());
                quads++;
            }

            penX += glyph.advance * size;
            previousCodePoint = codePoint;
        }

        if (quads == 0) {
            return;
        }

        GpuTextureView fontTextureView;
        try {
            fontTextureView = font.getTextureView();
            msdfTextureFailed = false;
        } catch (RuntimeException exception) {
            if (!msdfTextureFailed) {
                System.err.println("[client] Failed to get MSDF texture view: " + exception.getMessage());
                msdfTextureFailed = true;
            }
            return;
        }

        try (BuiltBuffer builtBuffer = builder.end()) {
            float rangeScale = Math.max(1.0F, font.getDistanceRange());
            GpuBufferSlice uniforms = new MsdfUniform(rangeScale, Math.max(0.25F, smoothness)).uniforms();
            enqueueDrawCall(
                    PipelinesRepository.MSDF,
                    builtBuffer,
                    uniforms,
                    "Sampler0",
                    fontTextureView,
                    font.getSampler(),
                    quads,
                    stack.peek()
            );
        }
    }

    public float textWidth(Fonts.FontRef fontRef, String text, float size) {
        MsdfFont font = fontRef.get();
        if (font == null || text == null) {
            return 0.0F;
        }
        return font.getWidth(text, size);
    }

    public float textWidth(String text, float size) {
        return textWidth(Fonts.SF_MEDIUM, text, size);
    }

    public void drawBuffer() {
        if (renderQueue.isEmpty()) {
            return;
        }

        RenderSystem.setProjectionMatrix(
                this.guiProjectionMatrix.set(
                        (float) mc.getWindow().getFramebufferWidth() / (float) mc.getWindow().getScaleFactor(),
                        (float) mc.getWindow().getFramebufferHeight() / (float) mc.getWindow().getScaleFactor()
                ),
                ProjectionType.ORTHOGRAPHIC
        );

        Framebuffer mainFramebuffer = mc.getFramebuffer();

        for (QueuedRender queuedRender : renderQueue) {
            if (queuedRender instanceof DrawCall drawCall) {
                drawToFramebuffer(drawCall, mainFramebuffer);
            } else if (queuedRender instanceof BlurCall blurCall) {
                applyKawaseBlur(mainFramebuffer, blurCall);
            }
        }

        renderQueue.clear();
        RectUniform.clearStorage();
        BorderUniform.clearStorage();
        MsdfUniform.clearStorage();
        KawasePassUniform.clearStorage();
        BlurCompositeUniform.clearStorage();
    }

    private void applyKawaseBlur(Framebuffer mainFramebuffer, BlurCall blurCall) {
        int levels = computeKawaseLevels(mainFramebuffer.textureWidth, mainFramebuffer.textureHeight, blurCall.iterations);
        ensureKawasePyramid(mainFramebuffer.textureWidth, mainFramebuffer.textureHeight, levels);

        int scaledWidth = mc.getWindow().getScaledWidth();
        int scaledHeight = mc.getWindow().getScaledHeight();

        GpuBuffer fullscreen = createPositionTextureBuffer(0.0F, 0.0F, scaledWidth, scaledHeight, 0.0F, 0.0F, 1.0F, 1.0F);

        GpuTextureView source = mainFramebuffer.getColorAttachmentView();
        int sourceWidth = mainFramebuffer.textureWidth;
        int sourceHeight = mainFramebuffer.textureHeight;
        for (int level = 0; level < levels; level++) {
            Framebuffer target = kawaseLevels.get(level);
            float passOffset = blurCall.offset * (1.0F + level * 0.35F);
            runKawasePass(
                    PipelinesRepository.KAWASE_DOWNSAMPLE,
                    source,
                    sourceWidth,
                    sourceHeight,
                    target,
                    passOffset,
                    fullscreen
            );
            source = target.getColorAttachmentView();
            sourceWidth = target.textureWidth;
            sourceHeight = target.textureHeight;
        }


        for (int level = levels - 2; level >= 0; level--) {
            Framebuffer sourceFramebuffer = kawaseLevels.get(level + 1);
            Framebuffer target = kawaseLevels.get(level);
            float passOffset = Math.max(0.5F, blurCall.offset * (0.65F + level * 0.2F));
            runKawasePass(
                    PipelinesRepository.KAWASE_UPSAMPLE,
                    sourceFramebuffer.getColorAttachmentView(),
                    sourceFramebuffer.textureWidth,
                    sourceFramebuffer.textureHeight,
                    target,
                    passOffset,
                    fullscreen
            );
        }
        source = kawaseLevels.get(0).getColorAttachmentView();

        GpuBuffer compositeQuad = createPositionTextureBuffer(
                blurCall.x, blurCall.y, blurCall.width, blurCall.height,
                blurCall.x / scaledWidth,
                blurCall.y / scaledHeight,
                (blurCall.x + blurCall.width) / scaledWidth,
                (blurCall.y + blurCall.height) / scaledHeight
        );

        DrawCall compositeCall = new DrawCall(
                PipelinesRepository.KAWASE_COMPOSITE,
                compositeQuad,
                new BlurCompositeUniform(
                        new Vector2f(blurCall.width, blurCall.height),
                        blurCall.round,
                        blurCall.smoothness,
                        blurCall.alpha
                ).uniforms(),
                1,
                stack.peek(),
                "Sampler0",
                source,
                RenderSystem.getSamplerCache().get(FilterMode.LINEAR),
                true
        );
        drawToFramebuffer(compositeCall, mainFramebuffer);

        fullscreen.close();
    }

    private int computeKawaseLevels(int width, int height, int iterations) {
        int maxByResolution = 1;
        int w = width;
        int h = height;
        while (maxByResolution < MAX_KAWASE_LEVELS && w > 2 && h > 2) {
            maxByResolution++;
            w >>= 1;
            h >>= 1;
        }
        int requested = Math.max(2, 1 + iterations / 2);
        return Math.min(maxByResolution, requested);
    }

    private void runKawasePass(RenderPipeline pipeline, GpuTextureView source,
                               int sourceWidth, int sourceHeight,
                               Framebuffer target, float offset, GpuBuffer fullscreen) {
        GpuBufferSlice uniforms = new KawasePassUniform(
                new Vector2f(1.0F / sourceWidth, 1.0F / sourceHeight),
                new Vector2f(offset, offset)
        ).uniforms();

        DrawCall passCall = new DrawCall(
                pipeline,
                fullscreen,
                uniforms,
                1,
                defaultMatrixEntry,
                "Sampler0",
                source,
                RenderSystem.getSamplerCache().get(FilterMode.LINEAR),
                false
        );
        drawToFramebuffer(passCall, target);
    }

    private void drawToFramebuffer(DrawCall drawCall, Framebuffer framebuffer) {
        GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().write(
                drawCall.matrix.getPositionMatrix(),
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                new Matrix4f()
        );

        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        GpuBuffer indexBuffer = shapeIndexBuffer.getIndexBuffer(drawCall.quadCount);
        VertexFormat.IndexType indexType = shapeIndexBuffer.getIndexType();

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "client renderer pass",
                        framebuffer.getColorAttachmentView(),
                        OptionalInt.empty(),
                        framebuffer.useDepthAttachment ? framebuffer.getDepthAttachmentView() : null,
                        OptionalDouble.empty()
                )) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicUniforms);
            renderPass.setPipeline(drawCall.pipeline);
            if (drawCall.customUniforms != null) {
                renderPass.setUniform("Uniforms", drawCall.customUniforms);
            }
            if (drawCall.samplerName != null && drawCall.samplerView != null) {
                renderPass.bindTexture(drawCall.samplerName, drawCall.samplerView, drawCall.sampler);
            }
            renderPass.setVertexBuffer(0, drawCall.vertexBuffer);
            renderPass.disableScissor();
            renderPass.setIndexBuffer(indexBuffer, indexType);
            renderPass.drawIndexed(0, 0, drawCall.quadCount * 6, 1);
        } finally {
            if (drawCall.closeAfterDraw) {
                drawCall.vertexBuffer.close();
            }
        }
    }

    private void ensureKawasePyramid(int width, int height, int levels) {
        if (width == kawaseLevelsWidth && height == kawaseLevelsHeight && levels == kawaseLevelsCount) {
            return;
        }

        clearKawasePyramid();

        kawaseLevelsWidth = width;
        kawaseLevelsHeight = height;
        kawaseLevelsCount = levels;

        for (int level = 0; level < levels; level++) {
            int levelWidth = Math.max(1, width >> level);
            int levelHeight = Math.max(1, height >> level);
            SimpleFramebuffer framebuffer = new SimpleFramebuffer("client_kawase_level_" + level, levelWidth, levelHeight, false);
            kawaseLevels.add(framebuffer);
        }
    }

    private void clearKawasePyramid() {
        for (SimpleFramebuffer framebuffer : kawaseLevels) {
            framebuffer.delete();
        }
        kawaseLevels.clear();
        kawaseLevelsWidth = -1;
        kawaseLevelsHeight = -1;
        kawaseLevelsCount = -1;
    }

    private void enqueueDrawCall(RenderPipeline pipeline, BuiltBuffer builtBuffer, GpuBufferSlice customUniforms,
                                 String samplerName, GpuTextureView samplerView,
                                 int quadCount, MatrixStack.Entry matrix) {
        enqueueDrawCall(pipeline, builtBuffer, customUniforms, samplerName, samplerView, null, quadCount, matrix);
    }

    private void enqueueDrawCall(RenderPipeline pipeline, BuiltBuffer builtBuffer, GpuBufferSlice customUniforms,
                                 String samplerName, GpuTextureView samplerView, GpuSampler sampler,
                                 int quadCount, MatrixStack.Entry matrix) {
        GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(bufferName, GpuBuffer.USAGE_VERTEX, builtBuffer.getBuffer());
        renderQueue.add(new DrawCall(
                pipeline,
                vertexBuffer,
                customUniforms,
                quadCount,
                matrix.copy(),
                samplerName,
                samplerView,
                sampler,
                true
        ));
    }

    private BufferBuilder createRectBuffer(float x, float y, float width, float height,
                                           Color color1, Color color2, Color color3, Color color4) {
        BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(x, y, 0.0F).color(color1.getRGB());
        builder.vertex(x, y + height, 0.0F).color(color2.getRGB());
        builder.vertex(x + width, y + height, 0.0F).color(color3.getRGB());
        builder.vertex(x + width, y, 0.0F).color(color4.getRGB());
        return builder;
    }

    private BufferBuilder createImageBuffer(float x, float y, float width, float height,
                                            float u0, float v0, float u1, float v1,
                                            Color color1, Color color2, Color color3, Color color4) {
        BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(x, y, 0.0F).texture(u0, v0).color(color1.getRGB());
        builder.vertex(x, y + height, 0.0F).texture(u0, v1).color(color2.getRGB());
        builder.vertex(x + width, y + height, 0.0F).texture(u1, v1).color(color3.getRGB());
        builder.vertex(x + width, y, 0.0F).texture(u1, v0).color(color4.getRGB());
        return builder;
    }

    private TextureBinding getTextureBinding(Identifier textureId) {
        try {
            var texture = mc.getTextureManager().getTexture(textureId);
            return new TextureBinding(texture.getGlTextureView(), texture.getSampler());
        } catch (IllegalStateException ignored) {
            try {
                mc.getTextureManager().registerTexture(textureId, new ResourceTexture(textureId));
                var texture = mc.getTextureManager().getTexture(textureId);
                return new TextureBinding(texture.getGlTextureView(), texture.getSampler());
            } catch (RuntimeException exception) {
                System.err.println("[client] Failed to load texture " + textureId + ": " + exception.getMessage());
                return null;
            }
        }
    }

    private GpuBuffer createPositionTextureBuffer(float x, float y, float width, float height,
                                                  float u0, float v0, float u1, float v1) {
        BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        builder.vertex(x, y, 0.0F).texture(u0, v0);
        builder.vertex(x, y + height, 0.0F).texture(u0, v1);
        builder.vertex(x + width, y + height, 0.0F).texture(u1, v1);
        builder.vertex(x + width, y, 0.0F).texture(u1, v0);

        try (BuiltBuffer builtBuffer = builder.end()) {
            return RenderSystem.getDevice().createBuffer(bufferName, GpuBuffer.USAGE_VERTEX, builtBuffer.getBuffer());
        }
    }

    private sealed interface QueuedRender permits DrawCall, BlurCall {
    }

    private record TextureBinding(GpuTextureView view, GpuSampler sampler) {
    }

    private record DrawCall(
            RenderPipeline pipeline,
            GpuBuffer vertexBuffer,
            GpuBufferSlice customUniforms,
            int quadCount,
            MatrixStack.Entry matrix,
            String samplerName,
            GpuTextureView samplerView,
            GpuSampler sampler,
            boolean closeAfterDraw
    ) implements QueuedRender {
    }

    private record BlurCall(
            float x,
            float y,
            float width,
            float height,
            Vector4f round,
            int iterations,
            float offset,
            float smoothness,
            float alpha
    ) implements QueuedRender {
    }
}
