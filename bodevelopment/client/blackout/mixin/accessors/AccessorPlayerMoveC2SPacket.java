package bodevelopment.client.blackout.mixin.accessors;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PlayerMoveC2SPacket.class})
public interface AccessorPlayerMoveC2SPacket {
   @Accessor("yaw")
   @Mutable
   void setYaw(float var1);

   @Accessor("pitch")
   @Mutable
   void setPitch(float var1);

   @Accessor("onGround")
   @Mutable
   void setOnGround(boolean var1);
}
