package burmalda.visuals.util.other;

import burmalda.visuals.util.minecraft.IMinecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public enum Keyboard implements IMinecraft {
    KEY_SPACE("SPACE", GLFW.GLFW_KEY_SPACE),
    KEY_APOSTROPHE("APOSTROPHE", GLFW.GLFW_KEY_APOSTROPHE),
    KEY_COMMA("COMMA", GLFW.GLFW_KEY_COMMA),
    KEY_MINUS("MINUS", GLFW.GLFW_KEY_MINUS),
    KEY_PERIOD("PERIOD", GLFW.GLFW_KEY_PERIOD),
    KEY_SLASH("SLASH", GLFW.GLFW_KEY_SLASH),
    KEY_0("0", GLFW.GLFW_KEY_0),
    KEY_1("1", GLFW.GLFW_KEY_1),
    KEY_2("2", GLFW.GLFW_KEY_2),
    KEY_3("3", GLFW.GLFW_KEY_3),
    KEY_4("4", GLFW.GLFW_KEY_4),
    KEY_5("5", GLFW.GLFW_KEY_5),
    KEY_6("6", GLFW.GLFW_KEY_6),
    KEY_7("7", GLFW.GLFW_KEY_7),
    KEY_8("8", GLFW.GLFW_KEY_8),
    KEY_9("9", GLFW.GLFW_KEY_9),
    KEY_A("A", GLFW.GLFW_KEY_A),
    KEY_B("B", GLFW.GLFW_KEY_B),
    KEY_C("C", GLFW.GLFW_KEY_C),
    KEY_D("D", GLFW.GLFW_KEY_D),
    KEY_E("E", GLFW.GLFW_KEY_E),
    KEY_F("F", GLFW.GLFW_KEY_F),
    KEY_G("G", GLFW.GLFW_KEY_G),
    KEY_H("H", GLFW.GLFW_KEY_H),
    KEY_I("I", GLFW.GLFW_KEY_I),
    KEY_J("J", GLFW.GLFW_KEY_J),
    KEY_K("K", GLFW.GLFW_KEY_K),
    KEY_L("L", GLFW.GLFW_KEY_L),
    KEY_M("M", GLFW.GLFW_KEY_M),
    KEY_N("N", GLFW.GLFW_KEY_N),
    KEY_O("O", GLFW.GLFW_KEY_O),
    KEY_P("P", GLFW.GLFW_KEY_P),
    KEY_Q("Q", GLFW.GLFW_KEY_Q),
    KEY_R("R", GLFW.GLFW_KEY_R),
    KEY_S("S", GLFW.GLFW_KEY_S),
    KEY_T("T", GLFW.GLFW_KEY_T),
    KEY_U("U", GLFW.GLFW_KEY_U),
    KEY_V("V", GLFW.GLFW_KEY_V),
    KEY_W("W", GLFW.GLFW_KEY_W),
    KEY_X("X", GLFW.GLFW_KEY_X),
    KEY_Y("Y", GLFW.GLFW_KEY_Y),
    KEY_Z("Z", GLFW.GLFW_KEY_Z),
    KEY_ESCAPE("ESCAPE", GLFW.GLFW_KEY_ESCAPE),
    KEY_NONE("", GLFW.GLFW_KEY_UNKNOWN);

    public final String name;
    public final int keyCode;

    private static final Map<Integer, Keyboard> BY_CODE = new HashMap<>();

    static {
        for (Keyboard key : values()) {
            BY_CODE.put(key.keyCode, key);
        }
    }

    Keyboard(String name, int keyCode) {
        this.name = name;
        this.keyCode = keyCode;
    }

    public static String getKeyName(int keyCode) {
        return BY_CODE.getOrDefault(keyCode, KEY_NONE).name;
    }
}
