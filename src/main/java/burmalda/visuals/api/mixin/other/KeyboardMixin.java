package burmalda.visuals.api.mixin.other;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import burmalda.visuals.api.event.impl.EventKeyboard;
import burmalda.visuals.Burmalda;
import burmalda.visuals.util.minecraft.IMinecraft;

@Mixin(Keyboard.class)
public class KeyboardMixin implements IMinecraft {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (mc.getWindow().getHandle() == window && Burmalda.getEventBus() != null) {
            Burmalda.getEventBus().post(new EventKeyboard(window, action, input));
        }
    }
}
