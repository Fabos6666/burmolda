package burmalda.visuals.api.mixin.game;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import burmalda.visuals.Burmalda;
import burmalda.visuals.api.event.impl.EventTick;
import burmalda.visuals.util.minecraft.IMinecraft;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements IMinecraft {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (Burmalda.getEventBus() != null) {
            Burmalda.getEventBus().post(new EventTick());
        }
    }

    @Inject(method = "getWindowTitle", at = @At("TAIL"), cancellable = true)
    private void swapTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Burmalda Visuals - " + SharedConstants.getGameVersion().name());
    }
}
