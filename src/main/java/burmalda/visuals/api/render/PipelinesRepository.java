package burmalda.visuals.api.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import static net.minecraft.client.gl.RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET;

public class PipelinesRepository {
    public static final RenderPipeline RECTANGLE = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/rect"))
            .withFragmentShader(Identifier.of("burmalda", "core/rect"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_rect")
            .build());

    public static final RenderPipeline BORDER = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/border"))
            .withFragmentShader(Identifier.of("burmalda", "core/border"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_border")
            .build());

    public static final RenderPipeline MSDF = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/msdf"))
            .withFragmentShader(Identifier.of("burmalda", "core/msdf"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withSampler("Sampler0")
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_msdf")
            .build());

    public static final RenderPipeline IMAGE = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/image"))
            .withFragmentShader(Identifier.of("burmalda", "core/image"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withSampler("Sampler0")
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_image")
            .build());

    public static final RenderPipeline KAWASE_DOWNSAMPLE = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/kawase_pass"))
            .withFragmentShader(Identifier.of("burmalda", "core/kawase_downsample"))
            .withoutBlend()
            .withSampler("Sampler0")
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_kawase_downsample")
            .build());

    public static final RenderPipeline KAWASE_UPSAMPLE = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/kawase_pass"))
            .withFragmentShader(Identifier.of("burmalda", "core/kawase_upsample"))
            .withoutBlend()
            .withSampler("Sampler0")
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_kawase_upsample")
            .build());

    public static final RenderPipeline KAWASE_COMPOSITE = RenderPipelines.register(RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader(Identifier.of("burmalda", "core/kawase_composite"))
            .withFragmentShader(Identifier.of("burmalda", "core/kawase_composite"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withSampler("Sampler0")
            .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
            .withLocation("pipeline/client_kawase_composite")
            .build());

	public static final RenderLayer RECT_LAYER = RenderLayer.of(
            "rect_layer", RenderSetup.builder(RECTANGLE).translucent().build()
    );
}
