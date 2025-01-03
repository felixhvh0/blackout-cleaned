package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.module.modules.misc.AntiPose;
import bodevelopment.client.blackout.module.modules.movement.SafeWalk;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({PlayerEntity.class})
public abstract class MixinPlayerEntity {
   @Shadow
   protected abstract boolean method_52558(EntityPose var1);

   @Redirect(
      method = {"adjustMovementForSneaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/player/PlayerEntity;clipAtLedge()Z"
)
   )
   private boolean sneakingThing(PlayerEntity instance) {
      return instance.method_5715() || this == BlackOut.mc.field_1724 && SafeWalk.shouldSafeWalk();
   }

   @Redirect(
      method = {"updatePose"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/player/PlayerEntity;canChangeIntoPose(Lnet/minecraft/entity/EntityPose;)Z",
   ordinal = 1
)
   )
   private boolean canEnterPose(PlayerEntity instance, EntityPose pose) {
      return instance == BlackOut.mc.field_1724 && AntiPose.getInstance().enabled || this.method_52558(pose);
   }
}
