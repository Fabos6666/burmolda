package burmalda.visuals.api.event.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import burmalda.visuals.api.event.Event;

public final class EventAttackEntity extends Event {
    private final PlayerEntity player;
    private final Entity target;

    public EventAttackEntity(PlayerEntity player, Entity target) {
        this.player = player;
        this.target = target;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public Entity getTarget() {
        return target;
    }
}
