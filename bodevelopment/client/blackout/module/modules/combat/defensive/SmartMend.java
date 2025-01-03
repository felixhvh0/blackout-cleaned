package bodevelopment.client.blackout.module.modules.combat.defensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.util.InvUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class SmartMend extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Integer> antiWaste;
   private final Setting<Double> moveSpeed;
   private final Setting<Boolean> closeInv;
   private long prevMove;
   private final List<EquipmentSlot> moveBack;
   private final TimerList<EquipmentSlot> delays;
   private final Map<EquipmentSlot, Long> wornSince;

   public SmartMend() {
      super("Smart Mend", "Moves fully mended items to inventory.", SubCategory.DEFENSIVE, true);
      this.antiWaste = this.sgGeneral.i("Anti Waste", 90, 0, 100, 1, "Doesn't use experience if any armor piece is above this durability.");
      this.moveSpeed = this.sgGeneral.d("Move Speed", 2.0D, 0.0D, 20.0D, 0.2D, ".");
      this.closeInv = this.sgGeneral.b("Close Inventory", true, ".");
      this.prevMove = 0L;
      this.moveBack = new ArrayList();
      this.delays = new TimerList(true);
      this.wornSince = new HashMap();
   }

   @Event
   public void onRender(RenderEvent.World.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         EquipmentSlot[] var2 = OLEPOSSUtils.equipmentSlots;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EquipmentSlot equipmentSlot = var2[var4];
            if (BlackOut.mc.field_1724.method_31548().method_7372(equipmentSlot.method_5927()).method_7960()) {
               this.wornSince.remove(equipmentSlot);
            } else if (!this.wornSince.containsKey(equipmentSlot)) {
               this.wornSince.put(equipmentSlot, System.currentTimeMillis());
            }
         }

         this.updateMoving(this.isMending());
      }
   }

   private void updateMoving(boolean mending) {
      if (mending) {
         this.onMend();
      } else {
         this.onMendStop();
      }

   }

   private void onMend() {
      List<EquipmentSlot> mended = this.getMended();
      if (InvUtils.find(true, true, ItemStack::method_7960).wasFound()) {
         if (!mended.isEmpty()) {
            EquipmentSlot equipmentSlot = (EquipmentSlot)mended.get(0);
            if (!((double)(System.currentTimeMillis() - this.prevMove) < 1000.0D / (Double)this.moveSpeed.get())) {
               if (!this.delays.contains((Object)equipmentSlot)) {
                  int slot = 8 - equipmentSlot.method_5927();
                  if (!this.moveBack.contains(equipmentSlot)) {
                     this.moveBack.add(equipmentSlot);
                  }

                  this.move(equipmentSlot, slot);
                  mended.remove(0);
                  this.prevMove = System.currentTimeMillis();
               }
            }
         }
      }
   }

   private void onMendStop() {
      this.moveBack.removeIf((equipmentSlotx) -> {
         return this.wornSince.containsKey(equipmentSlotx) && System.currentTimeMillis() - (Long)this.wornSince.get(equipmentSlotx) > 500L;
      });
      if (!this.moveBack.isEmpty()) {
         EquipmentSlot equipmentSlot = (EquipmentSlot)this.moveBack.get(0);
         if (!this.wornSince.containsKey(equipmentSlot)) {
            if (!((double)(System.currentTimeMillis() - this.prevMove) < 1000.0D / (Double)this.moveSpeed.get())) {
               if (!this.delays.contains((Object)equipmentSlot)) {
                  int slot = this.getArmorSlot(equipmentSlot);
                  if (slot >= 0) {
                     this.move(equipmentSlot, slot);
                     this.prevMove = System.currentTimeMillis();
                  }
               }
            }
         }
      }
   }

   private void move(EquipmentSlot equipmentSlot, int slot) {
      this.delays.add(equipmentSlot, 0.5D);
      InvUtils.interactHandler(slot, 0, SlotActionType.field_7794);
      if ((Boolean)this.closeInv.get()) {
         this.closeInventory();
      }

   }

   private int getArmorSlot(EquipmentSlot equipmentSlot) {
      Iterator var2 = BlackOut.mc.field_1724.field_7512.field_7761.iterator();

      while(var2.hasNext()) {
         Slot slot = (Slot)var2.next();
         Item var5 = slot.method_7677().method_7909();
         if (var5 instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem)var5;
            if (armorItem.method_7685() == equipmentSlot) {
               return slot.method_34266();
            }
         }
      }

      return -1;
   }

   private List<EquipmentSlot> getMended() {
      List<EquipmentSlot> armor = new ArrayList();
      EquipmentSlot[] var2 = OLEPOSSUtils.equipmentSlots;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EquipmentSlot equipmentSlot = var2[var4];
         ItemStack stack = BlackOut.mc.field_1724.method_31548().method_7372(equipmentSlot.method_5927());
         if (!stack.method_7960() && stack.method_7963()) {
            double dur = (double)(stack.method_7936() - stack.method_7919()) / (double)stack.method_7936() * 100.0D;
            if (dur >= (double)(Integer)this.antiWaste.get()) {
               armor.add(equipmentSlot);
            }
         }
      }

      return armor;
   }

   private boolean isMending() {
      return ExpThrower.getInstance().enabled;
   }
}
