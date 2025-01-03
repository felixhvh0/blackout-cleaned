package bodevelopment.client.blackout.module.modules.legit;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.mixin.accessors.AccessorInteractEntityC2SPacket;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;

public class CrystalOptimizer extends Module {
   private static CrystalOptimizer INSTANCE;

   public CrystalOptimizer() {
      super("Crystal Optimizer", "Stupid name but basically means set dead.", SubCategory.LEGIT, true);
      INSTANCE = this;
   }

   public static CrystalOptimizer getInstance() {
      return INSTANCE;
   }

   @Event
   public void onSent(PacketEvent.Sent event) {
      Packet var4 = event.packet;
      if (var4 instanceof PlayerInteractEntityC2SPacket) {
         PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)var4;
         if (((AccessorInteractEntityC2SPacket)packet).getType().method_34211() == InteractType.field_29172) {
            Entity var5 = BlackOut.mc.field_1687.method_8469(((AccessorInteractEntityC2SPacket)packet).getId());
            if (var5 instanceof EndCrystalEntity) {
               EndCrystalEntity entity = (EndCrystalEntity)var5;
               BlackOut.mc.field_1687.method_2945(entity.method_5628(), RemovalReason.field_26998);
            }
         }
      }

   }
}
