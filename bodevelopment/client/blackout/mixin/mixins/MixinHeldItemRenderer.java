package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.module.modules.combat.offensive.Aura;
import bodevelopment.client.blackout.module.modules.visual.misc.SwingModifier;
import bodevelopment.client.blackout.module.modules.visual.misc.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({HeldItemRenderer.class})
public abstract class MixinHeldItemRenderer {
   @Unique
   private MatrixStack matrices;
   @Unique
   private ItemStack item;
   @Unique
   private boolean mainHand;
   @Unique
   private VertexConsumerProvider vertexConsumers;
   @Unique
   private int light;

   @Shadow
   public abstract void method_3233(LivingEntity var1, ItemStack var2, ModelTransformationMode var3, boolean var4, MatrixStack var5, VertexConsumerProvider var6, int var7);

   @ModifyArgs(
      method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
)
   )
   private void setArgs(Args args) {
      this.matrices = (MatrixStack)args.get(7);
      this.item = (ItemStack)args.get(5);
      this.mainHand = args.get(3) == Hand.field_5808;
      this.vertexConsumers = (VertexConsumerProvider)args.get(8);
      this.light = (Integer)args.get(9);
      SwingModifier module = SwingModifier.getInstance();
      if (module.enabled) {
         args.set(6, module.getY((Hand)args.get(3)));
         args.set(4, module.getSwing((Hand)args.get(3)));
      }

   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      ViewModel viewModel = ViewModel.getInstance();
      if (viewModel.enabled) {
         if (viewModel.shouldCancel(hand)) {
            ci.cancel();
         } else {
            viewModel.transform(matrices, hand);
         }

      }
   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("TAIL")}
   )
   private void postRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      ViewModel viewModel = ViewModel.getInstance();
      if (viewModel.enabled) {
         viewModel.post(matrices);
      }

   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
)}
   )
   private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      ViewModel viewModel = ViewModel.getInstance();
      if (viewModel.enabled) {
         viewModel.scaleAndRotate(matrices, hand);
      }

   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
   shift = Shift.AFTER
)}
   )
   private void onRenderItemPost(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      ViewModel viewModel = ViewModel.getInstance();
      if (viewModel.enabled) {
         viewModel.postRender(matrices);
      }

   }

   @Redirect(
      method = {"renderFirstPersonItem"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingSpyglass()Z"
)
   )
   private boolean redirectRiptideTransform(AbstractClientPlayerEntity instance) {
      if (instance.method_31550()) {
         return true;
      } else if (this.mainHand && Aura.getInstance().blockTransform(this.matrices)) {
         this.method_3233(instance, this.item, this.mainHand ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321, !this.mainHand, this.matrices, this.vertexConsumers, this.light);
         this.matrices.method_22909();
         return true;
      } else {
         return false;
      }
   }
}
