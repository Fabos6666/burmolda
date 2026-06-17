package burmalda.visuals.api.module.render;

import com.google.common.eventbus.Subscribe;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import burmalda.visuals.api.event.impl.Event2DRender;
import burmalda.visuals.api.module.Module;
import burmalda.visuals.api.module.ModuleCategory;
import burmalda.visuals.api.module.ModuleInfo;
import burmalda.visuals.api.render.ClientRenderer;
import burmalda.visuals.api.render.Render2D;
import burmalda.visuals.api.render.msdf.Fonts;
import net.minecraft.util.Identifier;

import java.awt.Color;

@ModuleInfo(name = "Interface", description = "2D renderer demo preview", category = ModuleCategory.RENDER)
public final class Interface extends Module {
    private static final Identifier AVATAR = Identifier.of("burmalda", "images/burmalda.png");

    public Interface() {
        setKey(GLFW.GLFW_KEY_G);
    }

    @Subscribe
    public void onRender2D(Event2DRender event) {
        if (!isEnabled()) {
            return;
        }

        ClientRenderer renderer = Render2D.get();
        Vector4f round = new Vector4f(10.0F, 10.0F, 10.0F, 10.0F);
        float fontSize = 10.0F;

        renderer.rect(10, 10, 60, 40, round, 1.0F, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);

        float x = 10;
        float y = 60;
        float panelWidth = 200;
        float panelHeight = 100;

        renderer.blur(x, y, panelWidth, panelHeight, round, 7.0F, 1.0F);
        renderer.rect(x, y, panelWidth, panelHeight, round, 1.0F,
                new Color(11, 18, 17, 180),
                new Color(11, 18, 17, 180),
                new Color(11, 18, 17, 140),
                new Color(11, 18, 17, 140));
        renderer.border(x, y, panelWidth, panelHeight, 1.0F, round, 1.0F,
                new Color(255, 255, 255, 13));

        float textW = renderer.textWidth(Fonts.SF_MEDIUM, "secret render $$$", fontSize);
        float textX = x + (panelWidth - textW) * 0.5F;
        float textY = y + panelHeight * 0.35F;
        renderer.text(Fonts.SF_MEDIUM, "secret render $$$", textX, textY, fontSize, Color.WHITE);

        float imgSize = 64.0F;
        float imgX = textX - imgSize - 8.0F;
        float imgY = y + (panelHeight - imgSize) * 0.5F;
        renderer.image(AVATAR, imgX, imgY, imgSize, imgSize, round, 1.0F, Color.WHITE);
    }
}