package bodevelopment.client.blackout.randomstuff;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record PlaceData(BlockPos pos, Direction dir, boolean valid, boolean sneak) {
   public PlaceData(BlockPos pos, Direction dir, boolean valid, boolean sneak) {
      this.pos = pos;
      this.dir = dir;
      this.valid = valid;
      this.sneak = sneak;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public Direction dir() {
      return this.dir;
   }

   public boolean valid() {
      return this.valid;
   }

   public boolean sneak() {
      return this.sneak;
   }
}
