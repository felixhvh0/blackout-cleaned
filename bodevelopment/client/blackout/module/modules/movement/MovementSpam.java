package bodevelopment.client.blackout.module.modules.movement;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.util.math.Vec3d;

public class MovementSpam extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Integer> packets;
   private int packetsSent;

   public MovementSpam() {
      super("Movement Spam", "Sends movement packets at prev pos to do funny stuff.", SubCategory.MOVEMENT, true);
      this.packets = this.sgGeneral.i("Packets", 1, 1, 10, 1, ".");
   }

   public boolean shouldSkipListeners() {
      return false;
   }

   public String getInfo() {
      return String.valueOf(this.packetsSent);
   }

   @Event
   public void onTickPost(TickEvent.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null && this.enabled) {
         Vec3d pos = Managers.PACKET.pos;

         for(int i = 0; i < (Integer)this.packets.get(); ++i) {
            this.sendPacket(new Full(pos.field_1352, pos.field_1351, pos.field_1350, Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
         }

      }
   }

   @Event
   public void onSend(PacketEvent.Sent event) {
      if (event.packet instanceof PlayerMoveC2SPacket) {
         ++this.packetsSent;
      }

   }
}
