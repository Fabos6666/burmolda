package burmalda.visuals.api.event.impl;

import net.minecraft.client.input.KeyInput;
import burmalda.visuals.api.event.Event;

public final class EventKeyboard extends Event {
    private final long window;
    private final int action;
    private final KeyInput input;

    public EventKeyboard(long window, int action, KeyInput input) {
        this.window = window;
        this.action = action;
        this.input = input;
    }

    public long getWindow() {
        return window;
    }

    public int getAction() {
        return action;
    }

    public KeyInput getInput() {
        return input;
    }
}
