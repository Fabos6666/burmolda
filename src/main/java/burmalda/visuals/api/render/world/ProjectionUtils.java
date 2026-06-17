package burmalda.visuals.api.render.world;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public final class ProjectionUtils {
    private ProjectionUtils() {
    }

    public static void withCamera(Camera camera, Runnable action) {
        Quaternionf rotation = camera.getRotation().conjugate(new Quaternionf());
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        try {
            modelView.mul(new Matrix4f().rotation(rotation));
            action.run();
        } finally {
            modelView.popMatrix();
        }
    }
}
