package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IEndCrystalEntity;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.visual.entities.CrystalChams;
import java.util.Random;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({EndCrystalEntityRenderer.class})
public abstract class MixinEndCrystalEntityRenderer {
   @Shadow
   @Final
   private ModelPart field_21005;
   @Unique
   private long spawnTime = 0L;
   @Unique
   private final Random random = new Random();
   @Unique
   private EndCrystalEntity entity = null;
   @Unique
   private float tickDelta = 0.0F;
   @Unique
   private int id;
   @Unique
   private long seed = 0L;

   @Inject(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preRender(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (!Managers.ENTITY.shouldRender(endCrystalEntity.method_5628())) {
         ci.cancel();
      }

      if (CrystalChams.getInstance().enabled) {
         this.id = 3;
         this.tickDelta = g;
         this.entity = endCrystalEntity;
         this.spawnTime = ((IEndCrystalEntity)endCrystalEntity).blackout_Client$getSpawnTime();
         this.seed = (long)(endCrystalEntity.method_19538().field_1352 * 1000.0D + endCrystalEntity.method_19538().field_1351 * 1000.0D + endCrystalEntity.method_19538().field_1350 * 1000.0D);
      }
   }

   @ModifyArgs(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V",
   ordinal = 0
)
   )
   private void setSize(Args args) {
      CrystalChams crystalChams = CrystalChams.getInstance();
      if (crystalChams.enabled) {
         float scale;
         if ((Boolean)crystalChams.spawnAnimation.get()) {
            float animTime = ((Double)crystalChams.animationTime.get()).floatValue() * 1000.0F;
            scale = MathHelper.method_37166(0.0F, ((Double)crystalChams.scale.get()).floatValue() * 2.0F, Math.min((float)(System.currentTimeMillis() - this.spawnTime), animTime) / animTime);
         } else {
            scale = ((Double)crystalChams.scale.get()).floatValue() * 2.0F;
         }

         this.setSeed();
         args.set(0, scale);
         args.set(1, scale);
         args.set(2, scale);
      }
   }

   @Redirect(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
   ordinal = 0
)
   )
   private void yOffset(MatrixStack instance, float x, float y, float z) {
      if (!CrystalChams.getInstance().enabled) {
         instance.method_46416(x, y, z);
      }

   }

   @ModifyArg(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
   ordinal = 1
),
      index = 1
   )
   private float getBounce(float x) {
      return CrystalChams.getInstance().enabled ? this.getBounce(this.entity, this.tickDelta) : x;
   }

   @Redirect(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"
)
   )
   private void renderPart(ModelPart instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
      CrystalChams crystalChams = CrystalChams.getInstance();
      if (crystalChams.enabled && instance != this.field_21005) {
         crystalChams.renderBox(matrices, --this.id);
      } else {
         instance.method_22699(matrices, vertices, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   @Unique
   private float getBounce(EndCrystalEntity crystal, float tickDelta) {
      this.setSeed();
      CrystalChams crystalChams = CrystalChams.getInstance();
      float r = crystalChams.enabled && (Boolean)crystalChams.bounceSync.get() ? (float)((double)(this.random.nextFloat() * 2.0F) * 3.141592653589793D) : 0.0F;
      float f = (float)(crystalChams.enabled && (Boolean)crystalChams.bounceSync.get() ? crystalChams.age : crystal.field_7034) + tickDelta;
      float g = MathHelper.method_15374(f * 0.2F * ((Double)crystalChams.bounceSpeed.get()).floatValue() + r) / 2.0F + 0.5F;
      g = (g * g + g) * 0.4F;
      return (float)((Double)crystalChams.y.get() + 0.5D + (double)g * (Double)crystalChams.bounce.get()) / 2.0F;
   }

   @Redirect(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/entity/decoration/EndCrystalEntity;endCrystalAge:I",
   ordinal = 0,
   opcode = 180
)
   )
   private int getAge(EndCrystalEntity instance) {
      CrystalChams crystalChams = CrystalChams.getInstance();
      if (crystalChams.enabled && (Boolean)crystalChams.rotationSync.get()) {
         this.setSeed();
         return crystalChams.age + this.random.nextInt(100);
      } else {
         return instance.field_7034;
      }
   }

   @Redirect(
      method = {"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"
)
   )
   private Quaternionf rotationSpeed(RotationAxis instance, float deg) {
      CrystalChams crystalChams = CrystalChams.getInstance();
      return instance.rotationDegrees(deg * (crystalChams.enabled ? ((Double)crystalChams.rotationSpeed.get()).floatValue() : 1.0F));
   }

   @Unique
   private void setSeed() {
      this.random.setSeed(this.seed);
   }
}
