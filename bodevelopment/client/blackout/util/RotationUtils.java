package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {
   public static double getYaw(Entity entity) {
      return getYaw(BlackOut.mc.field_1724.method_33571(), entity.method_19538(), 0.0D);
   }

   public static double getPitch(Entity entity) {
      return getPitch(entity.method_19538());
   }

   public static double getYaw(BlockPos pos) {
      return getYaw(pos.method_46558());
   }

   public static double getPitch(BlockPos pos) {
      return getPitch(pos.method_46558());
   }

   public static double getYaw(Vec3d vec) {
      return getYaw(BlackOut.mc.field_1724.method_33571(), vec, 0.0D);
   }

   public static double getPitch(Vec3d vec) {
      return getPitch(BlackOut.mc.field_1724.method_33571(), vec);
   }

   public static float nextYaw(double current, double target, double step) {
      double i = yawAngle(current, target);
      return step >= Math.abs(i) ? (float)(current + i) : (float)(current + (double)(i < 0.0D ? -1 : 1) * step);
   }

   public static double yawAngle(double current, double target) {
      double c = MathHelper.method_15338(current) + 180.0D;
      double t = MathHelper.method_15338(target) + 180.0D;
      if (c > t) {
         return t + 360.0D - c < Math.abs(c - t) ? 360.0D - c + t : t - c;
      } else {
         return 360.0D - t + c < Math.abs(c - t) ? -(360.0D - t + c) : t - c;
      }
   }

   public static double pitchAngle(double current, double target) {
      return target - current;
   }

   public static float nextPitch(double current, double target, double step) {
      double i = pitchAngle(current, target);
      return step >= Math.abs(i) ? (float)target : (float)(i >= 0.0D ? current + step : current - step);
   }

   public static double radAngle(Vec3d vec1, Vec3d vec2) {
      return Math.acos(Math.min(1.0D, Math.max(vec1.method_1026(vec2) / (vec1.method_1033() * vec2.method_1033()), -1.0D)));
   }

   public static double getYaw(Vec3d start, Vec3d target, double yaw) {
      return yaw + MathHelper.method_15338(Math.toDegrees(Math.atan2(start.method_10216() - target.method_10216(), target.method_10215() - start.method_10215())) - yaw);
   }

   public static double getPitch(Vec3d start, Vec3d target) {
      double dx = target.method_10216() - start.method_10216();
      double dz = target.method_10215() - start.method_10215();
      return MathHelper.method_15338(-Math.toDegrees(Math.atan2(target.method_10214() - start.method_10214(), Math.sqrt(dx * dx + dz * dz))));
   }

   public static Vec3d rotationVec(double yaw, double pitch, Vec3d from, double distance) {
      return from.method_1019(rotationVec(yaw, pitch, distance));
   }

   public static Vec3d rotationVec(double yaw, double pitch, double range) {
      double rp = Math.toRadians(pitch);
      double ry = -Math.toRadians(yaw);
      double c = Math.cos(rp);
      return new Vec3d(range * Math.sin(ry) * c, range * -Math.sin(rp), range * Math.cos(ry) * c);
   }
}
