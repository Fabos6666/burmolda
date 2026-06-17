package burmalda.visuals.api.render.msdf;

public final class MsdfGlyph {
    public final int code;
    public final float advance;
    public final float minU;
    public final float maxU;
    public final float minV;
    public final float maxV;
    public final float left;
    public final float right;
    public final float top;
    public final float bottom;
    public final boolean hasAtlasBounds;
    public final boolean hasPlaneBounds;

    public MsdfGlyph(FontData.GlyphData data, float atlasWidth, float atlasHeight) {
        this.code = data.unicode;
        this.advance = data.advance;

        if (data.atlasBounds != null) {
            this.minU = data.atlasBounds.left / atlasWidth;
            this.maxU = data.atlasBounds.right / atlasWidth;
            this.minV = 1.0F - data.atlasBounds.top / atlasHeight;
            this.maxV = 1.0F - data.atlasBounds.bottom / atlasHeight;
            this.hasAtlasBounds = true;
        } else {
            this.minU = 0.0F;
            this.maxU = 0.0F;
            this.minV = 0.0F;
            this.maxV = 0.0F;
            this.hasAtlasBounds = false;
        }

        if (data.planeBounds != null) {
            this.left = data.planeBounds.left;
            this.right = data.planeBounds.right;
            this.top = data.planeBounds.top;
            this.bottom = data.planeBounds.bottom;
            this.hasPlaneBounds = true;
        } else {
            this.left = 0.0F;
            this.right = 0.0F;
            this.top = 0.0F;
            this.bottom = 0.0F;
            this.hasPlaneBounds = false;
        }
    }
}
