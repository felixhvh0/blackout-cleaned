package bodevelopment.client.blackout.interfaces.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public interface IRaycastContext {
   void blackout_Client$set(Vec3d var1, Vec3d var2, ShapeType var3, FluidHandling var4, Entity var5);

   void blackout_Client$set(Vec3d var1, Vec3d var2);

   void blackout_Client$set(ShapeType var1, FluidHandling var2, Entity var3);

   void blackout_Client$setStart(Vec3d var1);

   void blackout_Client$setEnd(Vec3d var1);
}
