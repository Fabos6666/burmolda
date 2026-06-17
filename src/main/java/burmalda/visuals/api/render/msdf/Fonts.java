package burmalda.visuals.api.render.msdf;

import net.minecraft.util.Identifier;
import burmalda.visuals.util.other.LogUtils;

import java.io.IOException;

public final class Fonts {
    public static final FontRef SF_MEDIUM = new FontRef(
            "SF_MEDIUM",
            Identifier.of("burmalda", "fonts/sfmedium.png"),
            Identifier.of("burmalda", "fonts/sfmedium.json")
    );

    public static final FontRef SF_PRO_DISPLAY_MEDIUM = new FontRef(
            "SF_PRO_DISPLAY_MEDIUM",
            Identifier.of("burmalda", "fonts/sfprodisplaymedium.png"),
            Identifier.of("burmalda", "fonts/sfprodisplaymedium.json")
    );

    public static final FontRef ICONS = new FontRef(
            "ICONS",
            Identifier.of("burmalda", "fonts/icons.png"),
            Identifier.of("burmalda", "fonts/icons.json")
    );

    public static final class FontRef {
        private final String name;
        private final Identifier atlasId;
        private final Identifier dataId;

        private MsdfFont cached;
        private boolean attemptedLoad;

        public FontRef(String name, Identifier atlasId, Identifier dataId) {
            this.name = name;
            this.atlasId = atlasId;
            this.dataId = dataId;
        }

        public String name() {
            return this.name;
        }

        public MsdfFont get() {
            if (cached != null) {
                return cached;
            }
            if (attemptedLoad) {
                return null;
            }
            attemptedLoad = true;
            try {
                cached = MsdfFont.load(this.atlasId, this.dataId);
            } catch (IOException exception) {
                LogUtils.warn("Failed to load font " + this.name + ": " + exception.getMessage());
            }
            return cached;
        }
    }
}
