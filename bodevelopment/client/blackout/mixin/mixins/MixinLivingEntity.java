package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.movement.ElytraFly;
import bodevelopment.client.blackout.module.modules.movement.NoJumpDelay;
import bodevelopment.client.blackout.module.modules.movement.Speed;
import bodevelopment.client.blackout.module.modules.visual.entities.PlayerModifier;
import bodevelopment.client.blackout.util.SettingUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class MixinLivingEntity {
   @Shadow
   public abstract void method_5650(RemovalReason var1);

   @Shadow
   protected abstract void method_6043();

   @Inject(
      method = {"getLeaningPitch"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void injectLeaning(float tickDelta, CallbackInfoReturnable<Float> cir) {
      if (this instanceof PlayerEntity) {
         PlayerEntity player = (PlayerEntity)this;
         PlayerModifier playerModifier = PlayerModifier.getInstance();
         if (playerModifier.enabled && (Boolean)playerModifier.setLeaning.get()) {
            cir.setReturnValue(playerModifier.getLeaning(player));
         }
      }
   }

   @ModifyConstant(
      method = {"tickMovement"},
      constant = {@Constant(
   intValue = 10
)}
   )
   private int modifyJumpDelay(int constant) {
      return NoJumpDelay.getInstance().enabled ? 0 : constant;
   }

   @Redirect(
      method = {"jump"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
)
   )
   private float sprintJumpYaw(LivingEntity instance) {
      return instance == BlackOut.mc.field_1724 && SettingUtils.grimMovement() ? Managers.ROTATION.moveLookYaw : instance.method_36454();
   }

   @Redirect(
      method = {"tick"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getYaw()F",
   ordinal = 0
)
   )
   private float yaw1(LivingEntity instance) {
      return this != BlackOut.mc.field_1724 ? instance.method_36454() : this.getModifiedYaw(instance);
   }

   @Redirect(
      method = {"tick"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getYaw()F",
   ordinal = 1
)
   )
   private float yaw2(LivingEntity instance) {
      return this != BlackOut.mc.field_1724 ? instance.method_36454() : this.getModifiedYaw(instance);
   }

   @Redirect(
      method = {"turnHead"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
)
   )
   private float yaw(LivingEntity instance) {
      return this != BlackOut.mc.field_1724 ? instance.method_36454() : this.getModifiedYaw(instance);
   }

   @Unique
   private float getModifiedYaw(LivingEntity livingEntity) {
      return livingEntity == BlackOut.mc.field_1724 && Managers.ROTATION.yawActive() ? Managers.ROTATION.renderYaw : livingEntity.method_36454();
   }

   @Redirect(
      method = {"getMovementSpeed(F)F"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getMovementSpeed()F"
)
   )
   private float vanillaSpeed(LivingEntity instance) {
      if (this != BlackOut.mc.field_1724) {
         return instance.method_6029();
      } else {
         Speed speed = Speed.getInstance();
         return speed.enabled && speed.mode.get() == Speed.SpeedMode.Vanilla ? ((Double)speed.vanillaSpeed.get()).floatValue() * instance.method_6029() : instance.method_6029();
      }
   }

   @Redirect(
      method = {"travel"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"
)
   )
   private float redirectElytraPitch(LivingEntity instance) {
      if (this == BlackOut.mc.field_1724) {
         if (SettingUtils.grimMovement()) {
            return Managers.ROTATION.nextPitch;
         }

         ElytraFly elytraFly = ElytraFly.getInstance();
         if (elytraFly.enabled && elytraFly.isBouncing()) {
            return elytraFly.getPitch();
         }
      }

      return instance.method_36455();
   }

   @Redirect(
      method = {"travel"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"
)
   )
   private Vec3d redirectRotationVec(LivingEntity instance) {
      return this == BlackOut.mc.field_1724 && SettingUtils.grimMovement() ? instance.method_5631(Managers.ROTATION.nextPitch, Managers.ROTATION.nextYaw) : instance.method_5720();
   }

   @Redirect(
      method = {"tickMovement"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;jump()V"
)
   )
   private void onJump(LivingEntity instance) {
      if (this == BlackOut.mc.field_1724) {
         ElytraFly elytraFly = ElytraFly.getInstance();
         if (!elytraFly.enabled || !elytraFly.isBouncing()) {
            this.method_6043();
         }

      }
   }
}
