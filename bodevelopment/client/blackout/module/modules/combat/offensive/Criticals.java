package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.mixin.accessors.AccessorInteractEntityC2SPacket;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Vec3d;

public class Criticals extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   public final Setting<Criticals.Mode> mode;
   private final Setting<Integer> spoofTime;
   private boolean shouldSpoof;
   private long prevJump;

   public Criticals() {
      super("Criticals", "Deals critical hits", SubCategory.OFFENSIVE, true);
      this.mode = this.sgGeneral.e("Mode", Criticals.Mode.Packet, "How to crit.");
      this.spoofTime = this.sgGeneral.i("Spoof Time", 500, 0, 2500, 50, ".", () -> {
         return this.mode.get() == Criticals.Mode.Strict;
      });
      this.shouldSpoof = false;
      this.prevJump = 0L;
   }

   public String getInfo() {
      return ((Criticals.Mode)this.mode.get()).name();
   }

   @Event
   public void onSend(PacketEvent.Send event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (BlackOut.mc.field_1724.method_24828() && !BlackOut.mc.field_1724.method_6101()) {
            Packet var3 = event.packet;
            if (var3 instanceof AccessorInteractEntityC2SPacket) {
               AccessorInteractEntityC2SPacket packet = (AccessorInteractEntityC2SPacket)var3;
               if (packet.getType().method_34211() == InteractType.field_29172 && BlackOut.mc.field_1687.method_8469(packet.getId()) instanceof LivingEntity) {
                  switch((Criticals.Mode)this.mode.get()) {
                  case Packet:
                     this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 0.2D, BlackOut.mc.field_1724.method_23321(), false));
                     this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), false));
                     break;
                  case Jump:
                     BlackOut.mc.field_1724.method_6043();
                     break;
                  case NCP:
                     this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 0.11D, BlackOut.mc.field_1724.method_23321(), false));
                     this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 0.01D, BlackOut.mc.field_1724.method_23321(), false));
                     break;
                  case Strict:
                     if (!this.input()) {
                        if (Managers.PACKET.isOnGround()) {
                           this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 1.1E-7D, BlackOut.mc.field_1724.method_23321(), false));
                           this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 1.0E-8D, BlackOut.mc.field_1724.method_23321(), false));
                           this.prevJump = System.currentTimeMillis();
                        }

                        this.shouldSpoof = true;
                     }
                     break;
                  case Grim:
                     Vec3d pos = Managers.PACKET.pos;
                     if (BlackOut.mc.field_1724.method_24828()) {
                        return;
                     }

                     this.sendPacket(new Full(pos.method_10216(), pos.method_10214() - 1.0E-6D, pos.method_10215(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
                     break;
                  case BlocksMC:
                     if (BlackOut.mc.field_1724.field_6012 % 4 == 0) {
                        this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318() + 0.0011D, BlackOut.mc.field_1724.method_23321(), true));
                        this.sendPacket(new PositionAndOnGround(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), false));
                     }
                  }
               }
            }

         }
      }
   }

   @Event
   public void onSent(PacketEvent.Sent event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerMoveC2SPacket) {
         PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket)var3;
         if (packet.method_12273()) {
            this.shouldSpoof = false;
         }
      }

   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (this.input()) {
         this.shouldSpoof = false;
      }

      if (this.shouldSpoof && System.currentTimeMillis() - this.prevJump < (long)(Integer)this.spoofTime.get()) {
         Managers.PACKET.spoofOG(false);
      }

   }

   private boolean input() {
      if (BlackOut.mc.field_1724 == null) {
         return false;
      } else {
         return BlackOut.mc.field_1724.field_3913.method_3128().method_35587() > 0.0F ? true : BlackOut.mc.field_1724.field_3913.field_3904;
      }
   }

   public static enum Mode {
      Packet,
      Jump,
      NCP,
      Strict,
      Grim,
      BlocksMC;

      // $FF: synthetic method
      private static Criticals.Mode[] $values() {
         return new Criticals.Mode[]{Packet, Jump, NCP, Strict, Grim, BlocksMC};
      }
   }
}
