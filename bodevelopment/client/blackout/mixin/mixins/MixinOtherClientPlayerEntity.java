package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.combat.misc.NoInterpolation;
import bodevelopment.client.blackout.module.modules.combat.offensive.BackTrack;
import bodevelopment.client.blackout.randomstuff.Pair;
import bodevelopment.client.blackout.randomstuff.timers.TickTimerList;
import bodevelopment.client.blackout.util.BoxUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({OtherClientPlayerEntity.class})
public class MixinOtherClientPlayerEntity {
   @Redirect(
      method = {"tickMovement"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;lerpPosAndRotation(IDDDDD)V"
)
   )
   private void updatePos(OtherClientPlayerEntity instance, int steps, double x, double y, double z, double yaw, double pit) {
      BackTrack backTrack = BackTrack.getInstance();
      Vec3d pos = instance.method_19538();
      double[] realPos = this.realPos(instance, steps, x, y, z, yaw, pit);
      backTrack.realPositions.removeKey(instance);
      if (backTrack.enabled) {
         TickTimerList.TickTimer<Pair<OtherClientPlayerEntity, Box>> t = backTrack.spoofed.get((timer) -> {
            return ((OtherClientPlayerEntity)((Pair)timer.value).method_15442()).equals(instance) && timer.ticks > 3;
         });
         if (t != null) {
            backTrack.realPositions.add(instance, new Vec3d(realPos[0], realPos[1], realPos[2]), 1.0D);
            this.setPosition(instance, BoxUtils.feet((Box)((Pair)t.value).method_15441()), pos);
            return;
         }
      }

      this.setPosition(instance, new Vec3d(realPos[0], realPos[1], realPos[2]), pos);
      instance.method_36456((float)realPos[3]);
      instance.method_36457((float)realPos[4]);
   }

   @Unique
   private void setPosition(OtherClientPlayerEntity instance, Vec3d pos, Vec3d prev) {
      instance.method_33574(pos);
      Managers.EXTRAPOLATION.tick(instance, pos.method_1020(prev));
   }

   @Unique
   private double[] realPos(OtherClientPlayerEntity instance, int steps, double x, double y, double z, double yaw, double pit) {
      NoInterpolation noInterpolation = NoInterpolation.getInstance();
      if (!noInterpolation.enabled) {
         double d = 1.0D / (double)steps;
         double e = MathHelper.method_16436(d, instance.method_23317(), x);
         double f = MathHelper.method_16436(d, instance.method_23318(), y);
         double g = MathHelper.method_16436(d, instance.method_23321(), z);
         float h = (float)MathHelper.method_52468(d, (double)instance.method_36454(), yaw);
         float i = (float)MathHelper.method_16436(d, (double)instance.method_36455(), pit);
         return new double[]{e, f, g, (double)h, (double)i};
      } else {
         return new double[]{x, y, z, yaw, pit};
      }
   }
}
