package burmalda.visuals.api.render.msdf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MsdfFont {
    private static final Gson GSON = new GsonBuilder().create();

    private final Identifier atlasId;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;

    private MsdfFont(Identifier atlasId, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
        this.atlasId = atlasId;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public static MsdfFont load(Identifier atlasId, Identifier dataId) throws IOException {
        MinecraftClient mc = MinecraftClient.getInstance();
        Resource resource = mc.getResourceManager().getResource(dataId)
                .orElseThrow(() -> new IOException("Missing MSDF data: " + dataId));

        FontData data;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            data = GSON.fromJson(reader, FontData.class);
        }

        if (data == null || data.atlas == null || data.metrics == null || data.glyphs == null) {
            throw new IOException("Invalid MSDF data json: " + dataId);
        }

        ensureAtlasTextureLoaded(mc, atlasId);

        Map<Integer, MsdfGlyph> glyphMap = new HashMap<>();
        for (FontData.GlyphData glyphData : data.glyphs) {
            glyphMap.put(glyphData.unicode, new MsdfGlyph(glyphData, data.atlas.width, data.atlas.height));
        }

        Map<Integer, Map<Integer, Float>> kerningMap = new HashMap<>();
        List<FontData.KerningData> kerningList = data.getKernings();
        if (kerningList != null) {
            for (FontData.KerningData kerningData : kerningList) {
                kerningMap.computeIfAbsent(kerningData.leftChar, ignored -> new HashMap<>())
                        .put(kerningData.rightChar, kerningData.advance);
            }
        }

        return new MsdfFont(atlasId, data.atlas, data.metrics, glyphMap, kerningMap);
    }

    public GpuTextureView getTextureView() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ensureAtlasTextureLoaded(mc, this.atlasId);
        return mc.getTextureManager().getTexture(this.atlasId).getGlTextureView();
    }

    public GpuSampler getSampler() {
        return RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
    }

    private static void ensureAtlasTextureLoaded(MinecraftClient mc, Identifier atlasId) {
        AbstractTexture texture = mc.getTextureManager().getTexture(atlasId);
        try {
            texture.getGlTextureView();
        } catch (IllegalStateException ignored) {
            mc.getTextureManager().registerTexture(atlasId, new ResourceTexture(atlasId));
            texture = mc.getTextureManager().getTexture(atlasId);
            texture.getGlTextureView();
        }

        texture.getGlTextureView();
    }

    public MsdfGlyph getGlyph(int codePoint) {
        return this.glyphs.get(codePoint);
    }

    public float getKerning(int leftCodePoint, int rightCodePoint) {
        Map<Integer, Float> pairs = this.kernings.get(leftCodePoint);
        if (pairs == null) {
            return 0.0F;
        }
        return pairs.getOrDefault(rightCodePoint, 0.0F);
    }

    public float getDistanceRange() {
        return this.atlas.distanceRange;
    }

    public float getLineHeight(float size) {
        return this.metrics.lineHeight * size;
    }

    public float getBaselineHeight(float size) {
        return this.metrics.baselineHeight() * size;
    }

    public float getWidth(String text, float size) {
        float width = 0.0F;
        int previousCodePoint = -1;
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (codePoint == '\n') {
                break;
            }

            MsdfGlyph glyph = this.glyphs.get(codePoint);
            if (glyph == null) {
                continue;
            }

            if (previousCodePoint != -1) {
                width += this.getKerning(previousCodePoint, codePoint) * size;
            }

            width += glyph.advance * size;
            previousCodePoint = codePoint;
        }
        return width;
    }
}
