package burmalda.visuals.api.render.uniforms;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.gl.DynamicUniformStorage;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

/**
 * @author ultra_lox
 * @since 19.03.2026
 */

public class MsdfUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putVec2()
            .get();

    private static final DynamicUniformStorage<UniformValue> STORAGE =
            new DynamicUniformStorage<>("Font UBO", SIZE, 2);

    public static void clearStorage() {
        STORAGE.clear();
    }

    private final Vector2f params;

    public MsdfUniform(float rangeScale, float smoothness) {
        this.params = new Vector2f(rangeScale, smoothness);
    }

    public record UniformValue(Vector2f params) implements DynamicUniformStorage.Uploadable {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putVec2(params);
        }
    }

    public GpuBufferSlice uniforms() {
        return STORAGE.write(new UniformValue(this.params));
    }
}
