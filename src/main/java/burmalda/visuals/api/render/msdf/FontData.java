package burmalda.visuals.api.render.msdf;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class FontData {
    public AtlasData atlas;
    public MetricsData metrics;
    public List<GlyphData> glyphs;
    @SerializedName("kerning")
    public List<KerningData> kerning;
    @SerializedName("kernings")
    public List<KerningData> kernings;

    public List<KerningData> getKernings() {
        if (kerning != null) return kerning;
        return kernings;
    }

    public static final class AtlasData {
        @SerializedName("distanceRange")
        public float distanceRange;
        public float width;
        public float height;
    }

    public static final class MetricsData {
        public float lineHeight;
        public float ascender;
        public float descender;

        public float baselineHeight() {
            return this.lineHeight + this.descender;
        }
    }

    public static final class GlyphData {
        public int unicode;
        public float advance;
        public BoundsData planeBounds;
        public BoundsData atlasBounds;
    }

    public static final class BoundsData {
        public float left;
        public float top;
        public float right;
        public float bottom;
    }

    public static final class KerningData {
        @SerializedName("unicode1")
        public int leftChar;
        @SerializedName("unicode2")
        public int rightChar;
        public float advance;
    }
}
