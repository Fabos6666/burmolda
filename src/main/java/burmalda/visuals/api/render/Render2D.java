package burmalda.visuals.api.render;

public final class Render2D {
    private static final ClientRenderer RENDERER = new ClientRenderer();

    private Render2D() {
    }

    public static ClientRenderer get() {
        return RENDERER;
    }
}
