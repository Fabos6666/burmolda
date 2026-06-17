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

public class KawasePassUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putVec2()
            .putVec2()
            .get();

    private static final DynamicUniformStorage<UniformValue> STORAGE =
            new DynamicUniformStorage<>("Blur UBO", SIZE, 2);

    public static void clearStorage() {
        STORAGE.clear();
    }

    private final Vector2f texelSize;
    private final Vector2f offset;

    public KawasePassUniform(Vector2f texelSize, Vector2f offset) {
        this.texelSize = texelSize;
        this.offset = offset;
    }

    public record UniformValue(Vector2f texelSize, Vector2f offset) implements DynamicUniformStorage.Uploadable {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putVec2(texelSize)
                    .putVec2(offset);
        }
    }

    public GpuBufferSlice uniforms() {
        return STORAGE.write(new UniformValue(this.texelSize, this.offset));
    }
}
