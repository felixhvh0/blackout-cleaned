package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.RemoveEvent;
import bodevelopment.client.blackout.interfaces.mixin.IVec3d;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.legit.HitCrystal;
import bodevelopment.client.blackout.module.modules.misc.Timer;
import bodevelopment.client.blackout.module.modules.movement.CollisionShrink;
import bodevelopment.client.blackout.module.modules.movement.Step;
import bodevelopment.client.blackout.module.modules.movement.TargetStrafe;
import bodevelopment.client.blackout.module.modules.movement.Velocity;
import bodevelopment.client.blackout.util.SettingUtils;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class MixinEntity {
   @Shadow
   public abstract Box method_5829();

   @Shadow
   public abstract World method_37908();

   @Shadow
   public abstract boolean method_24828();

   @Shadow
   public abstract float method_49476();

   @Shadow
   public abstract void method_5651(NbtCompound var1);

   @Inject(
      method = {"move"},
      at = {@At("HEAD")}
   )
   private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      if (this == BlackOut.mc.field_1724) {
         BlackOut.EVENT_BUS.post(MoveEvent.Pre.get(movement, movementType));
         TargetStrafe strafe = TargetStrafe.getInstance();
         if (strafe.enabled) {
            strafe.onMove(movement);
         }
      }

   }

   @Inject(
      method = {"move"},
      at = {@At("TAIL")}
   )
   private void onMovePost(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      if (this == BlackOut.mc.field_1724) {
         BlackOut.EVENT_BUS.post(MoveEvent.Post.get());
      }

      if (SettingUtils.grimPackets()) {
         HitCrystal.getInstance().onTick();
      }

   }

   @Redirect(
      method = {"updateVelocity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/Entity;getYaw()F"
)
   )
   private float getMoveYaw(Entity instance) {
      return this == BlackOut.mc.field_1724 && SettingUtils.grimMovement() ? Managers.ROTATION.moveLookYaw : instance.method_36454();
   }

   @Inject(
      method = {"adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void doStepStuff(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
      if (this == BlackOut.mc.field_1724) {
         Step step = Step.getInstance();
         if (step.isActive()) {
            cir.setReturnValue(this.getStep(step, movement));
            cir.cancel();
         }
      }
   }

   @Redirect(
      method = {"adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"
)
   )
   private Box box(Entity instance) {
      return this.getBox();
   }

   @Unique
   private Vec3d getStep(Step step, Vec3d movement) {
      if (step.stepProgress > -1 && (Boolean)step.slow.get() && step.offsets != null) {
         if (movement.method_37268() <= 0.0D) {
            ((IVec3d)movement).blackout_Client$setXZ(step.prevMovement.field_1352, step.prevMovement.field_1350);
         }

         step.prevMovement = movement.method_1021(1.0D);
      }

      Entity entity = (Entity)this;
      Box box = this.getBox();
      List<VoxelShape> list = this.method_37908().method_20743(entity, box.method_18804(movement));
      Vec3d vec3d = movement.method_1027() == 0.0D ? movement : Entity.method_20736(entity, movement, box, this.method_37908(), list);
      boolean collidedX = movement.field_1352 != vec3d.field_1352;
      boolean collidedY = movement.field_1351 != vec3d.field_1351;
      boolean collidedZ = movement.field_1350 != vec3d.field_1350;
      boolean collidedHorizontally = collidedX || collidedZ;
      boolean bl4 = this.method_24828() || collidedY && movement.field_1351 < 0.0D;
      double vanillaHeight = step.stepMode.get() == Step.StepMode.Vanilla ? (Double)step.height.get() : (double)this.method_49476();
      Vec3d stepMovement = Entity.method_20736(entity, new Vec3d(movement.field_1352, vanillaHeight, movement.field_1350), box, this.method_37908(), list);
      Vec3d stepMovementUp = Entity.method_20736(entity, new Vec3d(0.0D, vanillaHeight, 0.0D), box.method_1012(movement.field_1352, 0.0D, movement.field_1350), this.method_37908(), list);
      if (vanillaHeight > 0.0D && bl4 && collidedHorizontally && (!(Boolean)step.slow.get() || step.stepProgress < 0)) {
         if (stepMovementUp.field_1351 < vanillaHeight) {
            Vec3d vec3d4 = Entity.method_20736(entity, new Vec3d(movement.field_1352, 0.0D, movement.field_1350), box.method_997(stepMovementUp), this.method_37908(), list).method_1019(stepMovementUp);
            if (vec3d4.method_37268() > stepMovement.method_37268()) {
               stepMovement = vec3d4;
            }
         }

         if (stepMovement.method_37268() > vec3d.method_37268()) {
            return stepMovement.method_1019(Entity.method_20736(entity, new Vec3d(0.0D, -stepMovement.field_1351 + movement.field_1351, 0.0D), box.method_997(stepMovement), this.method_37908(), list));
         }
      }

      double height = (Double)step.height.get();
      stepMovement = Entity.method_20736(entity, new Vec3d(movement.field_1352, height, movement.field_1350), box, this.method_37908(), list);
      stepMovementUp = Entity.method_20736(entity, new Vec3d(0.0D, height, 0.0D), box.method_1012(movement.field_1352, 0.0D, movement.field_1350), this.method_37908(), list);
      if (height > 0.0D && entity.method_24828() && collidedHorizontally && (!(Boolean)step.slow.get() || step.stepProgress < 0 || step.offsets == null) && step.cooldownCheck()) {
         Vec3d vec3d3;
         if (stepMovementUp.field_1351 < height) {
            vec3d3 = Entity.method_20736(entity, new Vec3d(movement.field_1352, 0.0D, movement.field_1350), box.method_997(stepMovementUp), this.method_37908(), list).method_1019(stepMovementUp);
            if (vec3d3.method_37268() > stepMovement.method_37268()) {
               stepMovement = vec3d3;
            }
         }

         if (stepMovement.method_37268() > vec3d.method_37268()) {
            vec3d3 = stepMovement.method_1019(Entity.method_20736(entity, new Vec3d(0.0D, -stepMovement.field_1351 + movement.field_1351, 0.0D), box.method_997(stepMovement), this.method_37908(), list));
            step.start(vec3d3.field_1351);
            if (step.offsets != null) {
               step.lastStep = System.currentTimeMillis();
               if (!(Boolean)step.slow.get()) {
                  double y = 0.0D;
                  double[] var21 = step.offsets;
                  int var22 = var21.length;

                  for(int var23 = 0; var23 < var22; ++var23) {
                     double offset = var21[var23];
                     y += offset;
                     BlackOut.mc.method_1562().method_52787(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + y, BlackOut.mc.field_1724.method_23321(), false));
                  }

                  return vec3d3;
               }

               step.stepProgress = 0;
            }
         }
      }

      if (step.stepProgress > -1 && (Boolean)step.slow.get() && step.offsets != null) {
         step.sinceStep = 0;
         if ((Boolean)step.useTimer.get()) {
            Timer.set(((Double)step.timer.get()).floatValue());
            step.shouldResetTimer = true;
         }

         double h;
         if (step.stepProgress < step.offsets.length) {
            h = step.offsets[step.stepProgress];
            ++step.stepProgress;
            stepMovement = Entity.method_20736(entity, new Vec3d(movement.field_1352, 0.0D, movement.field_1350), box, this.method_37908(), list);
         } else {
            Vec3d m;
            if (step.stepMode.get() == Step.StepMode.UpdatedNCP) {
               if (step.stepProgress == step.offsets.length) {
                  ++step.stepProgress;
                  h = step.lastSlow;
               } else {
                  h = 0.0D;
                  step.stepProgress = -1;
                  step.offsets = null;
               }

               m = new Vec3d(0.0D, 0.0D, 0.0D);
            } else {
               h = step.lastSlow;
               step.stepProgress = -1;
               step.offsets = null;
               m = movement.method_38499(Axis.field_11052, 0.0D);
            }

            stepMovement = Entity.method_20736(entity, m, box, this.method_37908(), list);
         }

         return stepMovement.method_1019(Entity.method_20736(entity, new Vec3d(0.0D, h, 0.0D), box.method_997(stepMovement), this.method_37908(), list));
      } else {
         if (step.shouldResetTimer) {
            step.stepProgress = -1;
            step.offsets = null;
            Timer.reset();
            step.shouldResetTimer = false;
         }

         return vec3d;
      }
   }

   @Unique
   private Box getBox() {
      CollisionShrink shrink = CollisionShrink.getInstance();
      return shrink.enabled ? shrink.getBox(this.method_5829()) : this.method_5829();
   }

   @Inject(
      method = {"pushAwayFrom"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pushAwayFromEntities(Entity entity, CallbackInfo ci) {
      if (this == BlackOut.mc.field_1724) {
         Velocity velocity = Velocity.getInstance();
         if (velocity.enabled && velocity.entityPush.get() != Velocity.PushMode.Disabled) {
            ci.cancel();
         }

      }
   }

   @Inject(
      method = {"setRemoved"},
      at = {@At("HEAD")}
   )
   private void onRemove(RemovalReason reason, CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(RemoveEvent.get((Entity)this, reason));
   }
}
