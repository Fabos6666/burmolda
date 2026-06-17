package burmalda.visuals.api.render.world;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import burmalda.visuals.api.mixin.accessor.CameraAccessor;
import burmalda.visuals.util.minecraft.IMinecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class TargetEspRenderer implements IMinecraft {
    private static final List<VertexCollection> ADDITIVE_QUADS = new ArrayList<>();
    private static final List<VertexCollection> ADDITIVE_NO_DEPTH_QUADS = new ArrayList<>();
    private static final List<Texture> BLOOM_DEPTH = new ArrayList<>();
    private static final List<Texture> BLOOM_NO_DEPTH = new ArrayList<>();
    private static boolean rendering;

    private static final Vector3f[] CRYSTAL_VERTICES = new Vector3f[]{
            new Vector3f(0.0F, 1.5F, 0.0F),
            new Vector3f(0.0F, -1.5F, 0.0F),
            new Vector3f(1.0F, 0.0F, 0.0F),
            new Vector3f(-1.0F, 0.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, 1.0F),
            new Vector3f(0.0F, 0.0F, -1.0F)
    };

    private static final int[][] CRYSTAL_FACES = new int[][]{
            {0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2},
            {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}
    };

    private static final float[] CRYSTAL_FACE_BRIGHTNESS = new float[]{
            1.0F, 0.8F, 0.6F, 0.9F, 0.7F, 0.5F, 0.4F, 0.6F
    };

    private static final RenderPipeline BLOOM_DEPTH_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
                    .withLocation(Identifier.of("burmalda", "pipeline/bloom_depth"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.LIGHTNING)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .build()
    );

    private static final RenderPipeline BLOOM_NO_DEPTH_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
                    .withLocation(Identifier.of("burmalda", "pipeline/bloom_no_depth"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.LIGHTNING)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .build()
    );

    private static final Map<Identifier, RenderLayer> BLOOM_DEPTH_LAYERS = new HashMap<>();
    private static final Map<Identifier, RenderLayer> BLOOM_NO_DEPTH_LAYERS = new HashMap<>();

    private TargetEspRenderer() {
    }

    public static void prepare() {
        ADDITIVE_QUADS.clear();
        ADDITIVE_NO_DEPTH_QUADS.clear();
        BLOOM_DEPTH.clear();
        BLOOM_NO_DEPTH.clear();
        rendering = true;
    }

    public static void finish() {
        rendering = false;
        ADDITIVE_QUADS.clear();
        ADDITIVE_NO_DEPTH_QUADS.clear();
        BLOOM_DEPTH.clear();
        BLOOM_NO_DEPTH.clear();
    }

    public static boolean isRendering() {
        return rendering;
    }

    public static void drawQueued() {
        if (!rendering || (ADDITIVE_QUADS.isEmpty() && ADDITIVE_NO_DEPTH_QUADS.isEmpty() && BLOOM_DEPTH.isEmpty() && BLOOM_NO_DEPTH.isEmpty())) {
            return;
        }

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        drawTextures(immediate, BLOOM_DEPTH, true);
        drawTextures(immediate, BLOOM_NO_DEPTH, false);
        drawCollections(immediate, ADDITIVE_QUADS, RenderLayers.debugQuads(), false, true);
        drawCollections(immediate, ADDITIVE_NO_DEPTH_QUADS, RenderLayers.debugQuads(), true, true);
    }

    public static void drawCrystals(LivingEntity target, float appear, Color color) {
        if (!rendering || target == null || color == null || mc.world == null || mc.player == null || mc.gameRenderer == null) {
            return;
        }

        float alpha = MathHelper.clamp(appear, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null || !camera.isReady()) {
            return;
        }

        float tickDelta = mc.getRenderTickCounter().getTickProgress(true);
        Vec3d cameraPos = ((CameraAccessor) camera).burmalda$getPos();
        Vec3d targetPos = target.getLerpedPos(tickDelta);
        float width = target.getWidth() * 1.5F;
        float height = target.getHeight();

        HitResult hitResult = mc.world.raycast(new RaycastContext(
                cameraPos,
                target.getEyePos(),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));
        boolean depth = hitResult.getType() == HitResult.Type.MISS;

        MatrixStack matrix = new MatrixStack();
        matrix.push();
        matrix.translate(targetPos.x - cameraPos.x, targetPos.y - cameraPos.y, targetPos.z - cameraPos.z);

        Identifier bloom = Identifier.of("burmalda", "images/esp/bloom.png");
        float timeSec = System.currentTimeMillis() * 0.001F;
        float crystalRotate = (System.currentTimeMillis() % 360000L) / 7.25F;
        float rotationRad = (float) Math.toRadians(crystalRotate);
        float sinRot = MathHelper.sin(rotationRad);
        float cosRot = MathHelper.cos(rotationRad);
        float radiusAnimFactor = 1.25F - 0.5F * alpha;

        List<Vector3f> points = buildCrystalPoints(target, width, height);
        List<Vector3f> finalPositions = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            Vector3f point = points.get(i);
            float rotX = point.x * cosRot - point.z * sinRot;
            float rotZ = point.x * sinRot + point.z * cosRot;
            float bob = 0.05F * MathHelper.sin(timeSec * 2.0F + i * 1337.0F);
            finalPositions.add(new Vector3f(rotX * radiusAnimFactor, point.y + bob, rotZ * radiusAnimFactor));
        }

        List<VertexCollection> crystalTarget = depth ? ADDITIVE_QUADS : ADDITIVE_NO_DEPTH_QUADS;
        List<Texture> bloomTarget = depth ? BLOOM_DEPTH : BLOOM_NO_DEPTH;
        int crystalColor = withAlpha(color.getRGB(), (int) (255.0F * alpha));
        int bloomColor = withAlpha(color.getRGB(), (int) (255.0F * alpha * 0.2F));
        float crystalSize = 0.1F;
        float bloomSize = 1.0F;
        Vec3d targetCenter = targetPos.add(0.0, target.getHeight() / 2.0, 0.0);

        for (Vector3f position : finalPositions) {
            matrix.push();
            matrix.translate(position.x, position.y, position.z);

            Vec3d crystalPos = targetPos.add(position.x, position.y, position.z);
            Vector3f directionToTarget = new Vector3f(
                    (float) (targetCenter.x - crystalPos.x),
                    (float) (targetCenter.y - crystalPos.y),
                    (float) (targetCenter.z - crystalPos.z)
            );
            if (directionToTarget.lengthSquared() <= 1.0E-6F) {
                directionToTarget.set(0.0F, 1.0F, 0.0F);
            } else {
                directionToTarget.normalize();
            }

            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), directionToTarget);
            matrix.multiply(rotation);
            renderCrystal(resolveMatrix(matrix), crystalTarget, crystalSize, crystalColor);
            matrix.pop();

            matrix.push();
            matrix.translate(position.x, position.y, position.z);
            matrix.multiply(camera.getRotation());
            bloomTarget.add(new Texture(
                    matrix.peek().copy(),
                    bloom,
                    -bloomSize * 0.5F,
                    -bloomSize * 0.5F,
                    bloomSize,
                    bloomSize,
                    bloomColor,
                    bloomColor,
                    bloomColor,
                    bloomColor
            ));
            matrix.pop();
        }

        matrix.pop();
    }

    private static void drawTextures(VertexConsumerProvider.Immediate immediate, List<Texture> textures, boolean depthAware) {
        if (textures.isEmpty()) {
            return;
        }

        Set<Identifier> ids = new LinkedHashSet<>();
        for (Texture texture : textures) {
            ids.add(texture.id());
        }

        for (Identifier id : ids) {
            RenderLayer layer = getBloomLayer(id, depthAware);
            VertexConsumer consumer = immediate.getBuffer(layer);

            for (Texture texture : textures) {
                if (!texture.id().equals(id)) {
                    continue;
                }

                MatrixStack.Entry entry = texture.entry();
                float x = texture.x();
                float y = texture.y();
                float textureWidth = texture.width();
                float textureHeight = texture.height();

                textureVertex(entry, consumer, x, y, 0.0F, 0.0F, 1.0F, texture.color3());
                textureVertex(entry, consumer, x + textureWidth, y, 0.0F, 1.0F, 1.0F, texture.color4());
                textureVertex(entry, consumer, x + textureWidth, y + textureHeight, 0.0F, 1.0F, 0.0F, texture.color2());
                textureVertex(entry, consumer, x, y + textureHeight, 0.0F, 0.0F, 0.0F, texture.color1());
            }

            immediate.draw(layer);
        }
    }

    private static RenderLayer getBloomLayer(Identifier id, boolean depthAware) {
        Map<Identifier, RenderLayer> cache = depthAware ? BLOOM_DEPTH_LAYERS : BLOOM_NO_DEPTH_LAYERS;
        return cache.computeIfAbsent(id, texture -> {
            RenderSetup setup = RenderSetup.builder(depthAware ? BLOOM_DEPTH_PIPELINE : BLOOM_NO_DEPTH_PIPELINE)
                    .texture("Sampler0", texture)
                    .expectedBufferSize(2048)
                    .build();

            return RenderLayer.of((depthAware ? "bloom_depth_" : "bloom_no_depth_") + Integer.toHexString(texture.hashCode()), setup);
        });
    }

    private static void drawCollections(VertexConsumerProvider.Immediate immediate, List<VertexCollection> collections, RenderLayer layer, boolean noDepth, boolean additive) {
        if (collections.isEmpty()) {
            return;
        }

        if (noDepth || additive) {
            WorldOverrideUtils.set(noDepth, additive);
        }

        try {
            VertexConsumer consumer = immediate.getBuffer(layer);
            for (VertexCollection collection : collections) {
                collection.vertex(consumer);
            }
            immediate.draw(layer);
        } finally {
            if (noDepth || additive) {
                WorldOverrideUtils.clear();
            }
        }
    }

    private static void textureVertex(MatrixStack.Entry entry, VertexConsumer consumer, float x, float y, float z, float u, float v, int color) {
        consumer.vertex(entry, x, y, z).color(color).texture(u, v);
    }

    private static List<Vector3f> buildCrystalPoints(LivingEntity target, float width, float height) {
        int count = Math.max(8, (int) (width + height * 12.0F));
        int candidatesPerPoint = 15;
        long seed = (long) target.getId() * 133769420L;
        Random random = new Random(seed);
        List<Vector3f> points = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Vector3f bestCandidate = null;
            float bestDistSq = -1.0F;

            for (int attempt = 0; attempt < candidatesPerPoint; attempt++) {
                float angle = random.nextFloat() * 360.0F;
                float pointHeight = random.nextFloat() * height;
                float rad = (float) Math.toRadians(angle);
                float px = MathHelper.sin(rad) * width;
                float pz = MathHelper.cos(rad) * width;

                if (points.isEmpty()) {
                    bestCandidate = new Vector3f(px, pointHeight, pz);
                    break;
                }

                float nearestDistSq = Float.MAX_VALUE;
                for (Vector3f existing : points) {
                    float dx = existing.x - px;
                    float dy = existing.y - pointHeight;
                    float dz = existing.z - pz;
                    float distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                    }
                }

                if (nearestDistSq > bestDistSq) {
                    bestDistSq = nearestDistSq;
                    bestCandidate = new Vector3f(px, pointHeight, pz);
                }
            }

            if (bestCandidate != null) {
                points.add(bestCandidate);
            }
        }

        return points;
    }

    private static void renderCrystal(Matrix4f matrix, List<VertexCollection> target, float size, int color) {
        for (int i = 0; i < CRYSTAL_FACES.length; i++) {
            int[] face = CRYSTAL_FACES[i];
            int shadedColor = applyBrightness(color, CRYSTAL_FACE_BRIGHTNESS[i]);
            Vector3f v1 = CRYSTAL_VERTICES[face[0]];
            Vector3f v2 = CRYSTAL_VERTICES[face[1]];
            Vector3f v3 = CRYSTAL_VERTICES[face[2]];
            addTriangle(target, matrix,
                    v1.x * size, v1.y * size, v1.z * size,
                    v2.x * size, v2.y * size, v2.z * size,
                    v3.x * size, v3.y * size, v3.z * size,
                    shadedColor);
        }
    }

    private static void addTriangle(List<VertexCollection> target, Matrix4f matrix,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    double x3, double y3, double z3,
                                    int color) {
        addQuad(target, matrix, x1, y1, z1, color, x2, y2, z2, color, x3, y3, z3, color, x3, y3, z3, color);
    }

    private static void addQuad(List<VertexCollection> target, Matrix4f matrix,
                                double x1, double y1, double z1, int color1,
                                double x2, double y2, double z2, int color2,
                                double x3, double y3, double z3, int color3,
                                double x4, double y4, double z4, int color4) {
        target.add(new VertexCollection(
                vertex(matrix, x1, y1, z1, color1),
                vertex(matrix, x2, y2, z2, color2),
                vertex(matrix, x3, y3, z3, color3),
                vertex(matrix, x4, y4, z4, color4)
        ));
    }

    private static Vertex vertex(Matrix4f matrix, double x, double y, double z, int color) {
        return new Vertex(matrix, (float) x, (float) y, (float) z, color, 0.0F, 1.0F, 0.0F);
    }

    private static Matrix4f resolveMatrix(MatrixStack matrices) {
        return new Matrix4f(matrices.peek().getPositionMatrix());
    }

    private static int withAlpha(int argb, int alpha) {
        alpha = MathHelper.clamp(alpha, 0, 255);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    private static int applyBrightness(int color, float brightness) {
        int alpha = color >> 24 & 255;
        int red = MathHelper.clamp((int) ((color >> 16 & 255) * brightness), 0, 255);
        int green = MathHelper.clamp((int) ((color >> 8 & 255) * brightness), 0, 255);
        int blue = MathHelper.clamp((int) ((color & 255) * brightness), 0, 255);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public record VertexCollection(Vertex... vertices) {
        public void vertex(VertexConsumer consumer) {
            for (Vertex vertex : vertices) {
                consumer.vertex(vertex.matrix(), vertex.x(), vertex.y(), vertex.z()).color(vertex.color()).normal(vertex.nx(), vertex.ny(), vertex.nz());
            }
        }
    }

    public record Vertex(Matrix4f matrix, float x, float y, float z, int color, float nx, float ny, float nz) {
    }

    private record Texture(MatrixStack.Entry entry, Identifier id, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
    }
}
