package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.visual.entities.PlayerModifier;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> {
   @Unique
   private boolean itsami = false;

   @Shadow
   protected abstract float method_4044(T var1, float var2);

   @Redirect(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LimbAnimator;getSpeed(F)F"
)
   )
   private float redirectSpeed(LimbAnimator instance, float tickDelta) {
      float normal = instance.method_48570(tickDelta);
      if (!(this instanceof PlayerEntity)) {
         return normal;
      } else {
         PlayerModifier playerModifier = PlayerModifier.getInstance();
         return !playerModifier.enabled ? normal : 100.0F;
      }
   }

   @Redirect(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getHandSwingProgress(Lnet/minecraft/entity/LivingEntity;F)F"
)
   )
   private float swingProgress(LivingEntityRenderer instance, T entity, float tickDelta) {
      PlayerModifier playerModifier = PlayerModifier.getInstance();
      return playerModifier.enabled && (Boolean)playerModifier.noSwing.get() ? 0.0F : this.method_4044(entity, tickDelta);
   }

   @ModifyArgs(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V"
)
   )
   private void iHateNamingMixinStuff(Args args) {
      PlayerModifier modifier = PlayerModifier.getInstance();
      if (modifier.enabled && (Boolean)modifier.noAnimations.get()) {
         args.set(1, 0.0F);
         args.set(2, 0.0F);
         args.set(3, 0.0F);
      }
   }

   @Inject(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")}
   )
   private void inject(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      this.itsami = livingEntity == BlackOut.mc.field_1724;
   }

   @ModifyArgs(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
   ordinal = 1
)
   )
   public void changeHeadYaw(Args args) {
      if (this.itsami) {
         this.setYaw(args);
      }

   }

   @ModifyArgs(
      method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"
)
   )
   public void changePitch(Args args) {
      if (this.itsami && Managers.ROTATION.pitchActive()) {
         args.set(1, Managers.ROTATION.prevRenderPitch);
         args.set(2, Managers.ROTATION.renderPitch);
      }

   }

   @Unique
   private void setYaw(Args args) {
      if (Managers.ROTATION.yawActive()) {
         args.set(1, Managers.ROTATION.prevRenderYaw);
         args.set(2, Managers.ROTATION.renderYaw);
      }

   }
}
