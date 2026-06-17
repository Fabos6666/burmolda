package burmalda.visuals.api.event.impl;

import burmalda.visuals.api.event.Event;

public final class Event2DRender extends Event {
    private final int width;
    private final int height;

    public Event2DRender(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
