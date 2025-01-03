package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IEntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({EntityVelocityUpdateS2CPacket.class})
public class MixinEntityVelocityUpdateS2CPacket implements IEntityVelocityUpdateS2CPacket {
   @Mutable
   @Shadow
   @Final
   private int field_12563;
   @Mutable
   @Shadow
   @Final
   private int field_12562;
   @Mutable
   @Shadow
   @Final
   private int field_12561;

   public void blackout_Client$setX(int x) {
      this.field_12563 = x;
   }

   public void blackout_Client$setY(int y) {
      this.field_12562 = y;
   }

   public void blackout_Client$setZ(int z) {
      this.field_12561 = z;
   }
}
