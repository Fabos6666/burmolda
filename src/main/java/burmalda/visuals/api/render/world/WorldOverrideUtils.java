package burmalda.visuals.api.render.world;

public final class WorldOverrideUtils {
    private static final ThreadLocal<PassOverride> PASS_OVERRIDE = new ThreadLocal<>();

    private WorldOverrideUtils() {
    }

    public static void set(boolean noDepth, boolean additive) {
        PASS_OVERRIDE.set(new PassOverride(noDepth, additive));
    }

    public static void clear() {
        PASS_OVERRIDE.remove();
    }

    public static PassOverride get() {
        return PASS_OVERRIDE.get();
    }

    public record PassOverride(boolean noDepth, boolean additive) {
    }
}
