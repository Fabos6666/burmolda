package burmalda.visuals.api.render.uniforms;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.gl.DynamicUniformStorage;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

/**
 * @author ultra_lox
 * @since 19.03.2026
 */

public class BlurCompositeUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putVec2()
            .putVec4()
            .putFloat()
            .putFloat()
            .get();

    private static final DynamicUniformStorage<UniformValue> STORAGE =
            new DynamicUniformStorage<>("Blur Composite UBO", SIZE, 2);

    public static void clearStorage() {
        STORAGE.clear();
    }

    private final Vector2f size;
    private final Vector4f round;
    private final float smoothness;
    private final float alpha;

    public BlurCompositeUniform(Vector2f size, Vector4f round, float smoothness, float alpha) {
        this.size = size;
        this.round = round;
        this.smoothness = smoothness;
        this.alpha = alpha;
    }

    public record UniformValue(Vector2f size, Vector4f round, float smoothness, float alpha)
            implements DynamicUniformStorage.Uploadable {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putVec2(size)
                    .putVec4(round)
                    .putFloat(smoothness)
                    .putFloat(alpha);
        }
    }

    public GpuBufferSlice uniforms() {
        return STORAGE.write(new UniformValue(this.size, this.round, this.smoothness, this.alpha));
    }
}
