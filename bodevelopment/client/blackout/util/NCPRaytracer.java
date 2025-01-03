package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class NCPRaytracer {
   public static boolean raytrace(Vec3d from, Vec3d to, Box box) {
      int lx = 0;
      int ly = 0;
      int lz = 0;

      for(double delta = 0.0D; delta < 1.0D; delta += 0.0010000000474974513D) {
         double x = MathHelper.method_16436(from.field_1352, to.field_1352, delta);
         double y = MathHelper.method_16436(from.field_1351, to.field_1351, delta);
         double z = MathHelper.method_16436(from.field_1350, to.field_1350, delta);
         if (box.method_1008(x, y, z)) {
            return true;
         }

         int ix = (int)Math.floor(x);
         int iy = (int)Math.floor(y);
         int iz = (int)Math.floor(z);
         if (lx != ix || ly != iy || lz != iz) {
            BlockPos pos = new BlockPos(ix, iy, iz);
            if (validForCheck(pos, BlackOut.mc.field_1687.method_8320(pos))) {
               return false;
            }
         }

         lx = ix;
         ly = iy;
         lz = iz;
      }

      return false;
   }

   public static boolean validForCheck(BlockPos pos, BlockState state) {
      if (state.method_51367()) {
         return true;
      } else if (state.method_26204() instanceof FluidBlock) {
         return false;
      } else if (state.method_26204() instanceof StairsBlock) {
         return false;
      } else {
         return state.method_31709() ? false : state.method_26234(BlackOut.mc.field_1687, pos);
      }
   }
}
