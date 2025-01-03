package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IRaycastContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({RaycastContext.class})
public class MixinRaycastContext implements IRaycastContext {
   @Mutable
   @Shadow
   @Final
   private ShapeType field_17555;
   @Mutable
   @Shadow
   @Final
   private FluidHandling field_17556;
   @Mutable
   @Shadow
   @Final
   private ShapeContext field_17557;
   @Mutable
   @Shadow
   @Final
   private Vec3d field_17553;
   @Mutable
   @Shadow
   @Final
   private Vec3d field_17554;

   public void blackout_Client$set(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, Entity entity) {
      this.field_17555 = shapeType;
      this.field_17556 = fluidHandling;
      this.field_17557 = ShapeContext.method_16195(entity);
      this.field_17553 = start;
      this.field_17554 = end;
   }

   public void blackout_Client$set(Vec3d start, Vec3d end) {
      this.field_17553 = start;
      this.field_17554 = end;
   }

   public void blackout_Client$set(ShapeType shapeType, FluidHandling fluidHandling, Entity entity) {
      this.field_17555 = shapeType;
      this.field_17556 = fluidHandling;
      this.field_17557 = ShapeContext.method_16195(entity);
   }

   public void blackout_Client$setStart(Vec3d start) {
      this.field_17553 = start;
   }

   public void blackout_Client$setEnd(Vec3d end) {
      this.field_17554 = end;
   }
}
