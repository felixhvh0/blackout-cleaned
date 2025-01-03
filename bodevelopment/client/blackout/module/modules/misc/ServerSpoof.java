package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.UUID;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class ServerSpoof extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Double> delay;
   private final Status[] statuses;
   private UUID id;
   private long time;
   private int progress;

   public ServerSpoof() {
      super("Server Spoof", ".", SubCategory.MISC, true);
      this.delay = this.sgGeneral.d("Delay", 2.0D, 0.0D, 10.0D, 0.1D, ".");
      this.statuses = new Status[]{Status.field_13016, Status.field_47704, Status.field_13017};
      this.time = -1L;
      this.progress = 0;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724.field_6012 >= 20 && this.time >= 0L) {
         if ((double)System.currentTimeMillis() > (double)this.time + (Double)this.delay.get() * 1000.0D) {
            this.sendPacket(new ResourcePackStatusC2SPacket(this.id, this.statuses[this.progress]));
            if (this.progress > 1) {
               this.time = -1L;
            } else {
               this.time = System.currentTimeMillis();
            }

            ++this.progress;
         }

      }
   }

   @Event
   public void onReceive(PacketEvent.Receive.Post event) {
      Packet var3 = event.packet;
      if (var3 instanceof ResourcePackSendS2CPacket) {
         ResourcePackSendS2CPacket packet = (ResourcePackSendS2CPacket)var3;
         event.setCancelled(true);
         this.id = packet.comp_2158();
         this.time = System.currentTimeMillis();
         this.progress = 0;
      }

   }
}
