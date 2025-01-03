package bodevelopment.client.blackout.randomstuff;

import bodevelopment.client.blackout.enums.HoleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Hole {
   public static final BlockPos[] POSITIONS = new BlockPos[0];
   public final BlockPos pos;
   public final HoleType type;
   public final BlockPos[] positions;
   public final Vec3d middle;

   public Hole(BlockPos pos, HoleType type) {
      this.pos = pos;
      this.type = type;
      switch(type) {
      case Single:
         this.positions = new BlockPos[]{pos};
         this.middle = new Vec3d((double)pos.method_10263() + 0.5D, (double)pos.method_10264(), (double)pos.method_10260() + 0.5D);
         break;
      case DoubleX:
         this.positions = new BlockPos[]{pos, pos.method_10069(1, 0, 0)};
         this.middle = new Vec3d((double)(pos.method_10263() + 1), (double)pos.method_10264(), (double)pos.method_10260() + 0.5D);
         break;
      case DoubleZ:
         this.positions = new BlockPos[]{pos, pos.method_10069(0, 0, 1)};
         this.middle = new Vec3d((double)pos.method_10263() + 0.5D, (double)pos.method_10264(), (double)(pos.method_10260() + 1));
         break;
      case Quad:
         this.positions = new BlockPos[]{pos, pos.method_10069(1, 0, 0), pos.method_10069(0, 0, 1), pos.method_10069(1, 0, 1)};
         this.middle = new Vec3d((double)(pos.method_10263() + 1), (double)pos.method_10264(), (double)(pos.method_10260() + 1));
         break;
      default:
         this.positions = POSITIONS;
         this.middle = new Vec3d((double)pos.method_10263() + 0.5D, (double)pos.method_10264(), (double)pos.method_10260() + 0.5D);
      }

   }
}
