package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.movement.Blink;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import java.util.Objects;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class FastProjectile extends Module {
   private static FastProjectile INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Boolean> posRot;
   private final Setting<Boolean> blink;
   private final Setting<Double> timer;
   private final Setting<Integer> charge;
   private double throwYaw;
   private boolean down;
   public int ticksLeft;
   private boolean enabledBlink;
   private int toSend;
   private double x;
   private double y;
   private double z;
   private boolean ignore;
   private boolean throwIgnore;
   private Packet<?> throwPacket;

   public FastProjectile() {
      super("Fast Projectile", ".", SubCategory.MISC, true);
      this.posRot = this.sgGeneral.b("Pos Rot", true, ".");
      this.blink = this.sgGeneral.b("Blink", false, ".");
      SettingGroup var10001 = this.sgGeneral;
      Setting var10008 = this.blink;
      Objects.requireNonNull(var10008);
      this.timer = var10001.d("Timer", 1.0D, 1.0D, 10.0D, 0.1D, ".", var10008::get);
      this.charge = this.sgGeneral.i("Charge", 10, 0, 100, 1, ".");
      this.throwYaw = 0.0D;
      this.down = false;
      this.ticksLeft = 0;
      this.enabledBlink = false;
      this.toSend = 0;
      this.ignore = false;
      this.throwIgnore = false;
      this.throwPacket = null;
      INSTANCE = this;
   }

   public static FastProjectile getInstance() {
      return INSTANCE;
   }

   @Event
   public void onSend(PacketEvent.Send event) {
      if (event.packet instanceof PlayerMoveC2SPacket && !this.ignore && this.ticksLeft > 0) {
         event.setCancelled(true);
      }

      Packet var3 = event.packet;
      if (var3 instanceof PlayerInteractItemC2SPacket) {
         PlayerInteractItemC2SPacket packet = (PlayerInteractItemC2SPacket)var3;
         if (this.throwIgnore || !OLEPOSSUtils.getItem(packet.method_12551()).method_31574(Items.field_8634)) {
            return;
         }

         this.move(event);
      }

      var3 = event.packet;
      if (var3 instanceof PlayerActionC2SPacket) {
         PlayerActionC2SPacket packet = (PlayerActionC2SPacket)var3;
         if (packet.method_12363() == Action.field_12974) {
            if (this.throwIgnore || !(BlackOut.mc.field_1724.method_6030().method_7909() instanceof BowItem)) {
               return;
            }

            this.move(event);
         }
      }

   }

   @Event
   public void onMove(MoveEvent.Pre event) {
      if (--this.ticksLeft <= 0) {
         if (this.enabledBlink) {
            Blink.getInstance().disable();
            BlackOut.mc.method_1562().method_48296().method_52915();
            this.enabledBlink = false;
            this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12985));
            this.throwIgnore = true;
            if (this.throwPacket != null) {
               this.sendPacket(this.throwPacket);
            }

            this.throwIgnore = false;
         }

      } else {
         this.toSend = (int)((double)this.toSend + (Double)this.timer.get());
         event.set(this, 0.0D, 0.0D, 0.0D);

         while(this.toSend > 0) {
            this.chargeBlink();
            --this.toSend;
         }

      }
   }

   private void chargeBlink() {
      if (this.down = !this.down) {
         this.x += Math.cos(this.throwYaw) * 1.0E-5D;
         this.z += Math.sin(this.throwYaw) * 1.0E-5D;
         this.send(this.x, this.y + 1.0E-13D, this.z, true);
      } else {
         this.send(this.x, this.y + 2.0E-13D, this.z, false);
         --this.ticksLeft;
      }

   }

   private void move(PacketEvent.Send event) {
      double yaw = Math.toRadians((double)(Managers.ROTATION.prevYaw + 90.0F));
      this.x = BlackOut.mc.field_1724.method_23317();
      this.y = BlackOut.mc.field_1724.method_23318();
      this.z = BlackOut.mc.field_1724.method_23321();
      if ((Boolean)this.blink.get()) {
         this.down = false;
         this.enabledBlink = true;
         this.throwYaw = yaw;
         this.ticksLeft = (Integer)this.charge.get();
         this.throwPacket = event.packet;
         Blink.getInstance().enable();
         event.setCancelled(true);
      }

      this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12981));
      if (!(Boolean)this.blink.get()) {
         for(int i = 0; i < (Integer)this.charge.get(); ++i) {
            this.x += Math.cos(yaw) * 1.0E-5D;
            this.z += Math.sin(yaw) * 1.0E-5D;
            this.send(this.x, this.y + 1.0E-13D, this.z, true);
            this.send(this.x, this.y + 2.0E-13D, this.z, false);
         }

         this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12985));
      }
   }

   private void send(double x, double y, double z, boolean og) {
      this.ignore = true;
      if ((Boolean)this.posRot.get()) {
         this.sendPacket(new Full(x, y, z, Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, og));
      } else {
         this.sendPacket(new PositionAndOnGround(x, y, z, og));
      }

      this.ignore = false;
   }
}
