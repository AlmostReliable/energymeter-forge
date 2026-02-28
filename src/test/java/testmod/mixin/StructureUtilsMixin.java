package testmod.mixin;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureUtils.class)
public abstract class StructureUtilsMixin {

    @Inject(method = "encaseStructure", at = @At("HEAD"), cancellable = true)
    private static void cancelBarrierEncasing(AABB bounds, ServerLevel level, boolean placeBarriers, CallbackInfo ci) {
        ci.cancel();
    }
}
