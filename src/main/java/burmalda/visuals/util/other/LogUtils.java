package burmalda.visuals.util.other;

public final class LogUtils {
    public static void info(String message) {
        System.out.println("[burmalda] " + message);
    }

    public static void warn(String message) {
        System.err.println("[burmalda] " + message);
    }
}
