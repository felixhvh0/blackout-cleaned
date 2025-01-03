package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IVec3d;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({Vec3d.class})
public class MixinVec3d implements IVec3d {
   @Mutable
   @Shadow
   @Final
   public double field_1352;
   @Mutable
   @Shadow
   @Final
   public double field_1351;
   @Mutable
   @Shadow
   @Final
   public double field_1350;

   public void blackout_Client$set(double x, double y, double z) {
      this.field_1352 = x;
      this.field_1351 = y;
      this.field_1350 = z;
   }

   public void blackout_Client$setXZ(double x, double z) {
      this.field_1352 = x;
      this.field_1350 = z;
   }

   public void blackout_Client$setX(double x) {
      this.field_1352 = x;
   }

   public void blackout_Client$setY(double y) {
      this.field_1351 = y;
   }

   public void blackout_Client$setZ(double z) {
      this.field_1350 = z;
   }
}
