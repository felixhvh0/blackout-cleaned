package bodevelopment.client.blackout.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BoxUtils {
   public static Vec3d clamp(Vec3d vec, Box box) {
      return new Vec3d(MathHelper.method_15350(vec.field_1352, box.field_1323, box.field_1320), MathHelper.method_15350(vec.field_1351, box.field_1322, box.field_1325), MathHelper.method_15350(vec.field_1350, box.field_1321, box.field_1324));
   }

   public static Box get(BlockPos pos) {
      return new Box((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.method_10260() + 1));
   }

   public static Vec3d middle(Box box) {
      return new Vec3d((box.field_1323 + box.field_1320) / 2.0D, (box.field_1322 + box.field_1325) / 2.0D, (box.field_1321 + box.field_1324) / 2.0D);
   }

   public static Vec3d feet(Box box) {
      return new Vec3d((box.field_1323 + box.field_1320) / 2.0D, box.field_1322, (box.field_1321 + box.field_1324) / 2.0D);
   }

   public static Box crystalSpawnBox(BlockPos pos) {
      return new Box((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + (SettingUtils.cc() ? 1 : 2)), (double)(pos.method_10260() + 1));
   }

   public static Box lerp(double delta, Box start, Box end) {
      return new Box(MathHelper.method_16436(delta, start.field_1323, end.field_1323), MathHelper.method_16436(delta, start.field_1322, end.field_1322), MathHelper.method_16436(delta, start.field_1321, end.field_1321), MathHelper.method_16436(delta, start.field_1320, end.field_1320), MathHelper.method_16436(delta, start.field_1325, end.field_1325), MathHelper.method_16436(delta, start.field_1324, end.field_1324));
   }
}
