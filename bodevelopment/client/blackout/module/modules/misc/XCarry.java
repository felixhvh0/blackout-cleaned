package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class XCarry extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Boolean> fill;
   private final Setting<Double> fillDelay;
   private final Setting<List<Item>> fillItems;
   private final Setting<Integer> minStacks;
   private final Setting<Boolean> onlyInventory;
   private long prevMove;

   public XCarry() {
      super("XCarry", "Cancels inventory close packets.", SubCategory.MISC, true);
      this.fill = this.sgGeneral.b("Fill", false, ".");
      SettingGroup var10001 = this.sgGeneral;
      Setting var10008 = this.fill;
      Objects.requireNonNull(var10008);
      this.fillDelay = var10001.d("Fill Delay", 1.0D, 0.0D, 5.0D, 0.05D, ".", var10008::get);
      var10001 = this.sgGeneral;
      Setting var10004 = this.fill;
      Objects.requireNonNull(var10004);
      this.fillItems = var10001.il("Items", ".", var10004::get, Items.field_8301, Items.field_8287);
      var10001 = this.sgGeneral;
      var10008 = this.fill;
      Objects.requireNonNull(var10008);
      this.minStacks = var10001.i("Min Stacks", 1, 0, 10, 1, ".", var10008::get);
      this.onlyInventory = this.sgGeneral.b("Only Inventory", true, ".");
      this.prevMove = 0L;
   }

   @Event
   public void onSend(PacketEvent.Send event) {
      Packet var3 = event.packet;
      if (var3 instanceof CloseHandledScreenC2SPacket) {
         CloseHandledScreenC2SPacket packet = (CloseHandledScreenC2SPacket)var3;
         if (this.shouldCancel(packet)) {
            event.setCancelled(true);
         }
      }

   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724.field_7512 instanceof PlayerScreenHandler && (Boolean)this.fill.get()) {
         Slot returnSlot = this.returnSlot();
         Slot emptySlot = this.emptySlot();
         if (returnSlot != null && emptySlot != null) {
            if (this.delayCheck()) {
               this.clickSlot(returnSlot.field_7874, 0, SlotActionType.field_7794);
               if (!this.anythingPicked()) {
                  this.closeInventory();
               }
            }

         } else {
            Slot craftSlot = this.craftSlot();
            Slot fillSlot = this.fillSlot();
            if (fillSlot != null && craftSlot != null && this.delayCheck()) {
               if (this.isPicked((stack) -> {
                  return stack.method_31574(fillSlot.method_7677().method_7909());
               })) {
                  this.clickSlot(craftSlot.field_7874, 0, SlotActionType.field_7790);
               } else {
                  this.clickSlot(fillSlot.field_7874, 0, SlotActionType.field_7790);
                  this.clickSlot(craftSlot.field_7874, 0, SlotActionType.field_7790);
               }

               if (this.anythingPicked()) {
                  Slot empty = this.emptySlot();
                  if (empty != null) {
                     this.clickSlot(empty.field_7874, 0, SlotActionType.field_7790);
                  }
               }

               this.closeInventory();
            }

         }
      }
   }

   private void clickSlot(int id, int button, SlotActionType actionType) {
      ScreenHandler handler = BlackOut.mc.field_1724.field_7512;
      BlackOut.mc.field_1761.method_2906(handler.field_7763, id, button, actionType, BlackOut.mc.field_1724);
      this.prevMove = System.currentTimeMillis();
   }

   private boolean isPicked(Predicate<ItemStack> predicate) {
      return predicate.test(BlackOut.mc.field_1724.field_7512.method_34255());
   }

   private boolean anythingPicked() {
      return !BlackOut.mc.field_1724.field_7512.method_34255().method_7960();
   }

   private boolean delayCheck() {
      return (double)(System.currentTimeMillis() - this.prevMove) > (Double)this.fillDelay.get() * 1000.0D;
   }

   private Slot emptySlot() {
      for(int i = 9; i < 45; ++i) {
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(i);
         if (slot.method_7677().method_7960()) {
            return slot;
         }
      }

      return null;
   }

   private Slot fillSlot() {
      for(int i = 9; i < 36; ++i) {
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(i);
         ItemStack stack = slot.method_7677();
         if (!stack.method_7960() && ((List)this.fillItems.get()).contains(stack.method_7909()) && this.stacksOf(stack.method_7909()) > (Integer)this.minStacks.get()) {
            return slot;
         }
      }

      return null;
   }

   private Slot craftSlot() {
      for(int i = 1; i < 5; ++i) {
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(i);
         if (slot.method_7677().method_7960()) {
            return slot;
         }
      }

      return null;
   }

   private Slot returnSlot() {
      for(int i = 1; i < 5; ++i) {
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(i);
         ItemStack stack = slot.method_7677();
         if (!stack.method_7960() && ((List)this.fillItems.get()).contains(stack.method_7909()) && this.stacksOf(stack.method_7909()) < (Integer)this.minStacks.get()) {
            return slot;
         }
      }

      return null;
   }

   private int stacksOf(Item item) {
      int stacks = 0;

      for(int i = 9; i < 45; ++i) {
         if (BlackOut.mc.field_1724.field_7512.method_7611(i).method_7677().method_31574(item)) {
            ++stacks;
         }
      }

      return stacks;
   }

   private boolean shouldCancel(CloseHandledScreenC2SPacket packet) {
      return !(Boolean)this.onlyInventory.get() || packet.method_36168() == 0;
   }
}
