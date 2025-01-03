package bodevelopment.client.blackout.module.modules.combat.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.SwitchMode;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.FindResult;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class InstantEat extends Module {
   private static InstantEat INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<InstantEat.PacketMode> packetMode;
   private final Setting<Integer> packets;
   private final Setting<List<Item>> items;
   private final Setting<SwitchMode> switchMode;
   private final Predicate<ItemStack> predicate;
   private int packetsSent;

   public InstantEat() {
      super("Instant Eat", "Instantly eats a food item (for 1.8)", SubCategory.MISC_COMBAT, true);
      this.packetMode = this.sgGeneral.e("Packet Mode", InstantEat.PacketMode.Full, ".");
      this.packets = this.sgGeneral.i("Packets", 32, 0, 50, 1, ".");
      this.items = this.sgGeneral.il("Items", ".", Items.field_8463);
      this.switchMode = this.sgGeneral.e("Switch Mode", SwitchMode.Silent, ".");
      this.predicate = (itemStack) -> {
         return ((List)this.items.get()).contains(itemStack.method_7909());
      };
      this.packetsSent = 0;
      INSTANCE = this;
   }

   public static InstantEat getInstance() {
      return INSTANCE;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      this.disable(this.doStuff());
   }

   private String doStuff() {
      Hand hand = OLEPOSSUtils.getHand(this.predicate);
      if (hand == null) {
         FindResult result = ((SwitchMode)this.switchMode.get()).find(this.predicate);
         if (!result.wasFound() || !((SwitchMode)this.switchMode.get()).swapInstantly(result.slot())) {
            return "No item found";
         }
      }

      if (!BlackOut.mc.field_1724.method_6115()) {
         this.useItemInstantly(hand);
      }

      for(int i = 0; i < (Integer)this.packets.get(); ++i) {
         this.sendInstantly((Packet)((InstantEat.PacketMode)this.packetMode.get()).supplier.get());
      }

      if (hand == null) {
         ((SwitchMode)this.switchMode.get()).swapBackInstantly();
      }

      return null;
   }

   public static enum PacketMode {
      Full(() -> {
         Vec3d pos = Managers.PACKET.pos;
         return new Full(pos.method_10216(), pos.method_10214(), pos.method_10215(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround());
      }),
      FullOffG(() -> {
         Vec3d pos = Managers.PACKET.pos;
         return new Full(pos.method_10216(), pos.method_10214(), pos.method_10215(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, false);
      }),
      Rotation(() -> {
         Vec3d pos = Managers.PACKET.pos;
         return new Full(pos.method_10216(), pos.method_10214(), pos.method_10215(), Managers.ROTATION.prevYaw + ((InstantEat.getInstance().packetsSent & 1) == 0 ? 0.3759F : -0.2143F), Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround());
      }),
      DoubleRotation(() -> {
         return new LookAndOnGround(Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround());
      }),
      Position(() -> {
         Vec3d pos = Managers.PACKET.pos;
         return new PositionAndOnGround(pos.method_10216(), pos.method_10214(), pos.method_10215(), Managers.PACKET.isOnGround());
      }),
      Og(() -> {
         return new OnGroundOnly(Managers.PACKET.isOnGround());
      });

      private final Supplier<PlayerMoveC2SPacket> supplier;

      private PacketMode(Supplier<PlayerMoveC2SPacket> supplier) {
         this.supplier = supplier;
      }

      // $FF: synthetic method
      private static InstantEat.PacketMode[] $values() {
         return new InstantEat.PacketMode[]{Full, FullOffG, Rotation, DoubleRotation, Position, Og};
      }
   }
}
