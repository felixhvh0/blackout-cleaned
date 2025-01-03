package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.GameJoinEvent;
import bodevelopment.client.blackout.event.events.KeyEvent;
import bodevelopment.client.blackout.event.events.MouseButtonEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.functional.DoubleConsumer;
import bodevelopment.client.blackout.keys.KeyBind;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.client.Notifications;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.util.ItemUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;

public class Manager extends Module {
   private static Manager INSTANCE;
   public final SettingGroup sgGeneral = this.addGroup("General");
   public final SettingGroup sgAutoArmor = this.addGroup("Auto Armor");
   public final SettingGroup sgHotbar = this.addGroup("Hotbar");
   public final SettingGroup sgReplenish = this.addGroup("Replenish");
   public final SettingGroup sgCleaner = this.addGroup("Cleaner");
   private final Setting<Boolean> onlyInv;
   private final Setting<Double> inventoryOpenTime;
   private final Setting<Boolean> inInventoryInstant;
   private final Setting<Boolean> silentInstant;
   private final Setting<Double> cooldown;
   private final Setting<Boolean> tpDisable;
   private final Setting<Boolean> pauseCombat;
   private final Setting<Boolean> stopRotations;
   private final Setting<Boolean> autoArmor;
   private final Setting<KeyBind> chestSwap;
   private final Setting<Boolean> elytra;
   private final Setting<Integer> weaponSlot;
   private final Setting<Manager.WeaponMode> weaponMode;
   private final Setting<List<Item>> slot1;
   private final Setting<List<Item>> slot2;
   private final Setting<List<Item>> slot3;
   private final Setting<List<Item>> slot4;
   private final Setting<List<Item>> slot5;
   private final Setting<List<Item>> slot6;
   private final Setting<List<Item>> slot7;
   private final Setting<List<Item>> slot8;
   private final Setting<List<Item>> slot9;
   private final Setting<Boolean> replenish;
   private final Setting<Boolean> unstackableReplenish;
   private final Setting<Integer> percetageLeft;
   private final Setting<Double> replenishMemory;
   private final Setting<List<Item>> cleanerItems;
   private final Setting<Boolean> badArmor;
   private final Setting<Boolean> badSwords;
   private final Setting<Boolean> badAxes;
   private final Setting<Manager.AxeCompareMode> axeComparing;
   private final Setting<Boolean> badPickaxes;
   private final Setting<Boolean> badBows;
   private final Setting<List<Item>>[] slotSettings;
   private final Manager.ReplenishSlot[] replenishItems;
   private Manager.Action currentAction;
   private boolean prevOpen;
   private Boolean currentlyElytra;
   private int moveProgress;
   private long openTime;
   private long prevMove;
   private long containerInteractTime;
   private long prevDamage;

   public Manager() {
      super("Manager", ".", SubCategory.MISC, true);
      this.onlyInv = this.sgGeneral.b("Only Inv", false, ".");
      SettingGroup var10001 = this.sgGeneral;
      Setting var10008 = this.onlyInv;
      Objects.requireNonNull(var10008);
      this.inventoryOpenTime = var10001.d("Inventory Open Time", 0.1D, 0.0D, 1.0D, 0.01D, ".", var10008::get);
      this.inInventoryInstant = this.sgGeneral.b("In Inventory Instant", true, ".");
      this.silentInstant = this.sgGeneral.b("Silent Instant", true, ".", () -> {
         return !(Boolean)this.onlyInv.get();
      });
      this.cooldown = this.sgGeneral.d("Cooldown", 0.3D, 0.0D, 1.0D, 0.01D, ".");
      this.tpDisable = this.sgGeneral.b("Disable on TP", false, "Should we disable when teleporting to another world");
      this.pauseCombat = this.sgGeneral.b("Pause Combat", false, ".");
      this.stopRotations = this.sgGeneral.b("Stop Rotations", true, ".");
      this.autoArmor = this.sgAutoArmor.b("Auto Armor", true, ".");
      this.chestSwap = this.sgAutoArmor.k("Chest Swap", ".");
      this.elytra = this.sgAutoArmor.b("Elytra Priority", false, ".", () -> {
         return ((KeyBind)this.chestSwap.get()).value == null || ((KeyBind)this.chestSwap.get()).value.key >= 0;
      });
      this.weaponSlot = this.sgHotbar.i("Weapon Slot", 0, 0, 9, 1, ".");
      this.weaponMode = this.sgHotbar.e("Weapon Mode", Manager.WeaponMode.Sword, ".");
      this.slot1 = this.sgHotbar.il("Slot 1", ".", () -> {
         return (Integer)this.weaponSlot.get() != 1;
      });
      this.slot2 = this.sgHotbar.il("Slot 2", ".", () -> {
         return (Integer)this.weaponSlot.get() != 2;
      });
      this.slot3 = this.sgHotbar.il("Slot 3", ".", () -> {
         return (Integer)this.weaponSlot.get() != 3;
      });
      this.slot4 = this.sgHotbar.il("Slot 4", ".", () -> {
         return (Integer)this.weaponSlot.get() != 4;
      });
      this.slot5 = this.sgHotbar.il("Slot 5", ".", () -> {
         return (Integer)this.weaponSlot.get() != 5;
      });
      this.slot6 = this.sgHotbar.il("Slot 6", ".", () -> {
         return (Integer)this.weaponSlot.get() != 6;
      });
      this.slot7 = this.sgHotbar.il("Slot 7", ".", () -> {
         return (Integer)this.weaponSlot.get() != 7;
      });
      this.slot8 = this.sgHotbar.il("Slot 8", ".", () -> {
         return (Integer)this.weaponSlot.get() != 8;
      });
      this.slot9 = this.sgHotbar.il("Slot 9", ".", () -> {
         return (Integer)this.weaponSlot.get() != 9;
      });
      this.replenish = this.sgReplenish.b("Replenish", false, ".");
      this.unstackableReplenish = this.sgReplenish.b("Unstackable Replenish", true, ".");
      this.percetageLeft = this.sgReplenish.i("Left %", 25, 0, 100, 1, ".");
      this.replenishMemory = this.sgReplenish.d("Replenish Memory", 1.0D, 0.0D, 5.0D, 0.05D, ".");
      this.cleanerItems = this.sgCleaner.il("Cleaner Items", ".");
      this.badArmor = this.sgCleaner.b("Bad Armor", false, ".");
      this.badSwords = this.sgCleaner.b("Bad Swords", false, ".");
      this.badAxes = this.sgCleaner.b("Bad Axes", false, ".");
      var10001 = this.sgCleaner;
      Manager.AxeCompareMode var10003 = Manager.AxeCompareMode.Efficiency;
      Setting var10005 = this.badAxes;
      Objects.requireNonNull(var10005);
      this.axeComparing = var10001.e("Axe Comparing", var10003, ".", var10005::get);
      this.badPickaxes = this.sgCleaner.b("Bad Pickaxes", false, ".");
      this.badBows = this.sgCleaner.b("Bad Bows", false, ".");
      this.slotSettings = new Setting[]{this.slot1, this.slot2, this.slot3, this.slot4, this.slot5, this.slot6, this.slot7, this.slot8, this.slot9};
      this.replenishItems = new Manager.ReplenishSlot[]{new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot(), new Manager.ReplenishSlot()};
      this.currentAction = null;
      this.prevOpen = false;
      this.currentlyElytra = null;
      this.moveProgress = 0;
      this.openTime = 0L;
      this.prevMove = 0L;
      this.containerInteractTime = 0L;
      this.prevDamage = 0L;
      INSTANCE = this;
   }

   public static Manager getInstance() {
      return INSTANCE;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      this.checkChestSwap();
      if (this.canUpdate()) {
         this.update();
      }

   }

   @Event
   public void onGameJoin(GameJoinEvent event) {
      if ((Boolean)this.tpDisable.get()) {
         this.disable(this.getDisplayName() + " was disabled due to server change/teleport", 5, Notifications.Type.Info);
      }

   }

   @Event
   public void onSent(PacketEvent.Sent event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerInteractBlockC2SPacket) {
         PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket)var3;
         BlockHitResult hitResult = packet.method_12543();
         Block block = BlackOut.mc.field_1687.method_8320(hitResult.method_17777()).method_26204();
         if (block instanceof AbstractChestBlock || block instanceof AnvilBlock || block instanceof HopperBlock || block instanceof DispenserBlock) {
            this.containerInteractTime = System.currentTimeMillis();
         }
      }

   }

   @Event
   public void onKey(KeyEvent event) {
      if (event.pressed && ((KeyBind)this.chestSwap.get()).isKey(event.key) && this.currentlyElytra != null) {
         this.doChestSwap();
      }

   }

   @Event
   public void onKey(MouseButtonEvent event) {
      if (event.pressed && ((KeyBind)this.chestSwap.get()).isMouse(event.button) && this.currentlyElytra != null) {
         this.doChestSwap();
      }

   }

   public String getInfo() {
      if (((Boolean)this.elytra.get() || ((KeyBind)this.chestSwap.get()).value != null && ((KeyBind)this.chestSwap.get()).value.key >= 0) && this.currentlyElytra != null) {
         return this.currentlyElytra ? "Elytra" : "Armor";
      } else {
         return null;
      }
   }

   public boolean shouldNoRotate() {
      if (!(Boolean)this.stopRotations.get()) {
         return false;
      } else {
         return System.currentTimeMillis() - this.prevMove < 300L;
      }
   }

   private void doChestSwap() {
      this.currentlyElytra = !this.currentlyElytra;
      String var10001 = this.getDisplayName();
      this.sendNotification(var10001 + " " + Formatting.field_1078 + " changed to " + (this.currentlyElytra ? "Elytra" : "Chestplate"), this.getDisplayName() + "  changed to " + (this.currentlyElytra ? "Elytra" : "Chestplate"), "Chest Swap", Notifications.Type.Info, 2.0D);
   }

   private void checkChestSwap() {
      if (BlackOut.mc.field_1724 != null) {
         ItemStack stack = BlackOut.mc.field_1724.method_31548().method_7372(EquipmentSlot.field_6174.method_5927());
         boolean isElytra = stack.method_31574(Items.field_8833);
         if (this.currentlyElytra == null) {
            this.currentlyElytra = isElytra;
         }

      }
   }

   private boolean canUpdate() {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (BlackOut.mc.field_1724.field_6235 > 0) {
            this.prevDamage = System.currentTimeMillis();
         }

         if (this.inCombat()) {
            return false;
         } else {
            if ((Boolean)this.onlyInv.get()) {
               boolean open = BlackOut.mc.field_1755 instanceof InventoryScreen;
               if (open && !this.prevOpen) {
                  this.openTime = System.currentTimeMillis();
               }

               this.prevOpen = open;
               if (!open) {
                  return false;
               }

               if ((double)(System.currentTimeMillis() - this.openTime) < (Double)this.inventoryOpenTime.get() * 1000.0D) {
                  return false;
               }
            }

            return BlackOut.mc.field_1724.field_7512 instanceof PlayerScreenHandler;
         }
      } else {
         return false;
      }
   }

   private void update() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = BlackOut.mc.field_1724.field_7512.method_7611(36 + i).method_7677();
         Manager.ReplenishSlot slot = this.replenishItems[i];
         if (stack.method_7960()) {
            if ((double)(System.currentTimeMillis() - slot.lastSeen) > (Double)this.replenishMemory.get() * 1000.0D) {
               slot.item = null;
            }
         } else {
            slot.item = stack.method_7909();
            slot.lastSeen = System.currentTimeMillis();
         }
      }

      if (this.shouldUpdateAction()) {
         this.currentAction = this.nextAction();
      }

      if (this.currentAction != null) {
         this.handleAction();
      }

   }

   private boolean shouldUpdateAction() {
      if (this.anythingPicked()) {
         return false;
      } else {
         return this.moveProgress <= 0;
      }
   }

   private void handleAction() {
      Manager.Action var2 = this.currentAction;
      if (var2 instanceof Manager.Drop) {
         Manager.Drop drop = (Manager.Drop)var2;
         if (this.delayCheck()) {
            drop.consumer.accept(BlackOut.mc.field_1761, BlackOut.mc.field_1724.field_7512);
            this.setPrev();
            this.closeIfPossible();
            this.resetAction();
         }
      }

      var2 = this.currentAction;
      if (var2 instanceof Manager.Move) {
         Manager.Move move = (Manager.Move)var2;
         if (this.delayCheck()) {
            if (this.shouldInstant()) {
               this.moveInstantly(move);
            } else if (this.updateSlowMove(move)) {
               this.setPrev();
            }
         }
      }

      var2 = this.currentAction;
      if (var2 instanceof Manager.QuickMove) {
         Manager.QuickMove quickMove = (Manager.QuickMove)var2;
         if (this.delayCheck()) {
            quickMove.consumer.accept(BlackOut.mc.field_1761, BlackOut.mc.field_1724.field_7512);
            this.setPrev();
            this.closeIfPossible();
            this.resetAction();
         }
      }

      var2 = this.currentAction;
      if (var2 instanceof Manager.Swap) {
         Manager.Swap swap = (Manager.Swap)var2;
         if (this.delayCheck()) {
            swap.consumer.accept(BlackOut.mc.field_1761, BlackOut.mc.field_1724.field_7512);
            this.setPrev();
            this.closeIfPossible();
            this.resetAction();
         }
      }

   }

   private void moveInstantly(Manager.Move move) {
      if (this.isPicked(move.predicate)) {
         this.clickSlot(move.to, 0, SlotActionType.field_7790);
      } else {
         this.clickSlot(move.from, 0, SlotActionType.field_7790);
         this.clickSlot(move.to, 0, SlotActionType.field_7790);
      }

      if (this.anythingPicked()) {
         Slot empty = this.findSlot((slot) -> {
            return slot.method_7677().method_7960();
         }, (ToDoubleFunction)null, Manager.FindArea.Both);
         if (empty != null) {
            this.clickSlot(empty.field_7874, 0, SlotActionType.field_7790);
         }
      }

      this.resetAction();
      this.setPrev();
      this.closeInventory();
   }

   private boolean isPicked(Predicate<ItemStack> predicate) {
      return predicate.test(BlackOut.mc.field_1724.field_7512.method_34255());
   }

   private boolean anythingPicked() {
      return !BlackOut.mc.field_1724.field_7512.method_34255().method_7960();
   }

   private boolean shouldInstant() {
      return BlackOut.mc.field_1755 instanceof InventoryScreen ? (Boolean)this.inInventoryInstant.get() : (Boolean)this.silentInstant.get();
   }

   private void closeIfPossible() {
      if (!this.anythingPicked()) {
         this.closeInventory();
      }

   }

   private boolean updateSlowMove(Manager.Move move) {
      switch(this.moveProgress) {
      case 0:
         if (this.delayCheck()) {
            this.clickSlot(move.from, 0, SlotActionType.field_7790);
            ++this.moveProgress;
            return true;
         }
         break;
      case 1:
         if (!move.predicate.test(BlackOut.mc.field_1724.field_7512.method_34255())) {
            this.resetAction();
            this.closeIfPossible();
            return false;
         }

         if (this.delayCheck()) {
            this.clickSlot(move.to, 0, SlotActionType.field_7790);
            ++this.moveProgress;
            return true;
         }
         break;
      case 2:
         if (!move.predicate.test(BlackOut.mc.field_1724.field_7512.method_34255())) {
            this.resetAction();
            this.closeIfPossible();
            return false;
         }

         if (this.delayCheck()) {
            Slot empty = this.findSlot((slot) -> {
               return slot.method_7677().method_7960();
            }, (ToDoubleFunction)null, Manager.FindArea.Inventory);
            if (empty != null) {
               this.clickSlot(empty.field_7874, 0, SlotActionType.field_7790);
            }

            this.closeIfPossible();
            this.resetAction();
            return empty != null;
         }
      }

      return false;
   }

   private void resetAction() {
      this.currentAction = null;
      this.moveProgress = 0;
   }

   private void clickSlot(int id, int button, SlotActionType actionType) {
      ScreenHandler handler = BlackOut.mc.field_1724.field_7512;
      BlackOut.mc.field_1761.method_2906(handler.field_7763, id, button, actionType, BlackOut.mc.field_1724);
   }

   private boolean delayCheck() {
      if ((double)(System.currentTimeMillis() - this.containerInteractTime) < Simulation.getInstance().managerStop() * 1000.0D) {
         return false;
      } else {
         return (double)(System.currentTimeMillis() - this.prevMove) > (Double)this.cooldown.get() * 1000.0D;
      }
   }

   private void setPrev() {
      this.prevMove = System.currentTimeMillis();
   }

   private Manager.Action nextAction() {
      Manager.Action hotbarAction = this.findHotbar();
      if (hotbarAction != null) {
         return hotbarAction;
      } else {
         Slot cleanerSlot = this.findCleaner();
         if (cleanerSlot != null) {
            return new Manager.Drop(cleanerSlot.field_7874);
         } else {
            Manager.Action replenishAction;
            if ((Boolean)this.autoArmor.get()) {
               replenishAction = this.findAutoArmor();
               if (replenishAction != null) {
                  return replenishAction;
               }
            }

            if ((Boolean)this.replenish.get()) {
               replenishAction = this.findReplenish();
               if (replenishAction != null) {
                  return replenishAction;
               }
            }

            return null;
         }
      }
   }

   private Manager.Action findReplenish() {
      for(int i = 1; i <= 9; ++i) {
         int slotId = 35 + i;
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(slotId);
         ItemStack stack = slot.method_7677();
         if (stack.method_7960()) {
            Item item = this.replenishItems[i - 1].item;
            if (item != null && ((Boolean)this.unstackableReplenish.get() || item.method_7882() > 1)) {
               Slot from = this.findSlot((s) -> {
                  return s.method_7677().method_31574(item);
               }, (s) -> {
                  return (double)s.method_7677().method_7947();
               }, Manager.FindArea.Inventory);
               if (from != null) {
                  return new Manager.Swap(from.field_7874, i - 1);
               }
            }
         } else if (!((float)slot.method_7677().method_7947() / (float)slot.method_7677().method_7914() * 100.0F > (float)(Integer)this.percetageLeft.get())) {
            Slot from = this.findSlot((s) -> {
               return ItemStack.method_31577(stack, s.method_7677());
            }, (s) -> {
               return (double)(-s.method_7677().method_7947());
            }, Manager.FindArea.Inventory);
            if (from != null) {
               if (this.shouldQuick(i)) {
                  return new Manager.QuickMove(from.field_7874);
               }

               return new Manager.Move(from.field_7874, slotId);
            }
         }
      }

      return null;
   }

   private boolean shouldQuick(int hotbarSlot) {
      for(int i = 1; i <= 9; ++i) {
         if (i != hotbarSlot && BlackOut.mc.field_1724.field_7512.method_7611(35 + i).method_7677().method_7960()) {
            return false;
         }
      }

      return true;
   }

   private Manager.Action findHotbar() {
      for(int i = 1; i <= 9; ++i) {
         int slotId = 35 + i;
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(slotId);
         Manager.HotbarSearch search = this.getHotbarSearch(i);
         if (!search.predicate().test(slot)) {
            Slot fromSlot = this.findSlot((s) -> {
               return search.predicate().test(s) && !this.alreadyValid(s);
            }, search.function(), Manager.FindArea.Both);
            if (fromSlot != null) {
               return new Manager.Swap(fromSlot.field_7874, i - 1);
            }
         }
      }

      return null;
   }

   private boolean alreadyValid(Slot slot) {
      if (slot.field_7874 < 36) {
         return false;
      } else {
         int slotId = slot.field_7874 - 35;
         Manager.HotbarSearch hotbarSearch = this.getHotbarSearch(slotId);
         return hotbarSearch.predicate().test(slot);
      }
   }

   private Manager.HotbarSearch getHotbarSearch(int slot) {
      return (Integer)this.weaponSlot.get() == slot ? ((Manager.WeaponMode)this.weaponMode.get()).search : new Manager.HotbarSearch((s) -> {
         return ((List)this.slotSettings[slot - 1].get()).contains(s.method_7677().method_7909());
      }, (s) -> {
         return (double)s.method_7677().method_7947();
      });
   }

   private Manager.Action findAutoArmor() {
      EquipmentSlot[] var1 = OLEPOSSUtils.equipmentSlots;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EquipmentSlot equipmentSlot = var1[var3];
         int toSlot = 8 - equipmentSlot.method_5927();
         Slot bestArmor = this.findBestArmor(equipmentSlot);
         if (bestArmor != null && bestArmor.field_7874 != toSlot) {
            return new Manager.Move(bestArmor.field_7874, toSlot);
         }
      }

      return null;
   }

   private Slot findBestArmor(EquipmentSlot equipmentSlot) {
      Slot bestArmor = this.findSlot((slot) -> {
         Item patt18399$temp = slot.method_7677().method_7909();
         boolean var10000;
         if (patt18399$temp instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem)patt18399$temp;
            if (armorItem.method_7685() == equipmentSlot) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }, (slot) -> {
         return ItemUtils.getArmorValue(slot.method_7677());
      }, Manager.FindArea.All);
      if (equipmentSlot != EquipmentSlot.field_6174) {
         return bestArmor;
      } else {
         Slot bestElytra = this.findSlot((slot) -> {
            return slot.method_7677().method_7909() instanceof ElytraItem;
         }, (slot) -> {
            return ItemUtils.getElytraValue(slot.method_7677());
         }, Manager.FindArea.All);
         boolean elytraPriority = this.swapBinded() ? this.currentlyElytra != null && this.currentlyElytra : (Boolean)this.elytra.get();
         Slot higherPriority = elytraPriority ? bestElytra : bestArmor;
         if (higherPriority != null) {
            return higherPriority;
         } else {
            return elytraPriority ? bestArmor : bestElytra;
         }
      }
   }

   private boolean swapBinded() {
      KeyBind keyBind = (KeyBind)this.chestSwap.get();
      return keyBind.value != null && keyBind.value.key >= 0;
   }

   private Slot findCleaner() {
      Slot basicCleaner = this.findSlot((slot) -> {
         return ((List)this.cleanerItems.get()).contains(slot.method_7677().method_7909());
      }, (ToDoubleFunction)null, Manager.FindArea.Both);
      if (basicCleaner != null) {
         return basicCleaner;
      } else {
         if ((Boolean)this.badArmor.get()) {
            EquipmentSlot[] var2 = OLEPOSSUtils.equipmentSlots;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               EquipmentSlot equipmentSlot = var2[var4];
               Slot badArmor = this.findBadItem((slot) -> {
                  Item patt19623$temp = slot.method_7677().method_7909();
                  boolean var10000;
                  if (patt19623$temp instanceof ArmorItem) {
                     ArmorItem armorItem = (ArmorItem)patt19623$temp;
                     if (armorItem.method_7685() == equipmentSlot) {
                        var10000 = true;
                        return var10000;
                     }
                  }

                  var10000 = false;
                  return var10000;
               }, (slot) -> {
                  return ItemUtils.getArmorValue(slot.method_7677());
               }, Manager.FindArea.All);
               if (badArmor != null) {
                  return badArmor;
               }
            }
         }

         Slot badPickaxe;
         if ((Boolean)this.badSwords.get()) {
            badPickaxe = this.findBadItem((slot) -> {
               return slot.method_7677().method_7909() instanceof SwordItem;
            }, (slot) -> {
               return ItemUtils.getWeaponValue(slot.method_7677());
            }, Manager.FindArea.Both);
            if (badPickaxe != null) {
               return badPickaxe;
            }
         }

         if ((Boolean)this.badAxes.get()) {
            badPickaxe = this.findBadItem((slot) -> {
               return slot.method_7677().method_7909() instanceof AxeItem;
            }, ((Manager.AxeCompareMode)this.axeComparing.get()).function, Manager.FindArea.Both);
            if (badPickaxe != null) {
               return badPickaxe;
            }
         }

         if ((Boolean)this.badPickaxes.get()) {
            badPickaxe = this.findBadItem((slot) -> {
               return slot.method_7677().method_7909() instanceof PickaxeItem;
            }, (slot) -> {
               return ItemUtils.getPickaxeValue(slot.method_7677());
            }, Manager.FindArea.Both);
            if (badPickaxe != null) {
               return badPickaxe;
            }
         }

         return (Boolean)this.badBows.get() ? this.findBadItem((slot) -> {
            return slot.method_7677().method_7909() instanceof BowItem;
         }, (slot) -> {
            return ItemUtils.getBowValue(slot.method_7677());
         }, Manager.FindArea.Both) : null;
      }
   }

   private Slot findBadItem(Predicate<Slot> predicate, ToDoubleFunction<Slot> value, Manager.FindArea area) {
      Slot best = this.findSlot(predicate, value, area);
      return best == null ? null : this.findSlot((slot) -> {
         return predicate.test(slot) && slot != best;
      }, (slot) -> {
         return -value.applyAsDouble(slot);
      }, area);
   }

   private Slot findSlot(Predicate<Slot> predicate, ToDoubleFunction<Slot> value, Manager.FindArea area) {
      boolean best = value != null;
      List<Slot> valid = best ? new ArrayList() : null;

      for(int i = area.start; i <= area.end; ++i) {
         Slot slot = BlackOut.mc.field_1724.field_7512.method_7611(i);
         if (predicate.test(slot)) {
            if (!best) {
               return slot;
            }

            valid.add(slot);
         }
      }

      return best ? (Slot)valid.stream().max(Comparator.comparingDouble(value)).orElse((Object)null) : null;
   }

   private boolean inCombat() {
      return (Boolean)this.pauseCombat.get() && System.currentTimeMillis() - this.prevDamage < 1000L;
   }

   public static enum WeaponMode {
      Sword((slot) -> {
         return slot.method_7677().method_7909() instanceof SwordItem;
      }),
      Axe((slot) -> {
         return slot.method_7677().method_7909() instanceof AxeItem;
      }),
      Both((slot) -> {
         Item item = slot.method_7677().method_7909();
         return item instanceof SwordItem || item instanceof AxeItem;
      });

      private final Manager.HotbarSearch search;

      private WeaponMode(Predicate<Slot> predicate) {
         this.search = new Manager.HotbarSearch(predicate, (slot) -> {
            return ItemUtils.getWeaponValue(slot.method_7677());
         });
      }

      // $FF: synthetic method
      private static Manager.WeaponMode[] $values() {
         return new Manager.WeaponMode[]{Sword, Axe, Both};
      }
   }

   public static enum AxeCompareMode {
      Damage((slot) -> {
         return ItemUtils.getWeaponValue(slot.method_7677());
      }),
      Efficiency((slot) -> {
         return ItemUtils.getAxeValue(slot.method_7677());
      });

      private final ToDoubleFunction<Slot> function;

      private AxeCompareMode(ToDoubleFunction<Slot> function) {
         this.function = function;
      }

      // $FF: synthetic method
      private static Manager.AxeCompareMode[] $values() {
         return new Manager.AxeCompareMode[]{Damage, Efficiency};
      }
   }

   private class ReplenishSlot {
      private Item item = null;
      private long lastSeen = 0L;
   }

   private class Action {
   }

   private class Drop extends Manager.Action {
      private final DoubleConsumer<ClientPlayerInteractionManager, ScreenHandler> consumer;

      private Drop(int id) {
         super();
         this.consumer = (manager, handler) -> {
            manager.method_2906(handler.field_7763, id, 1, SlotActionType.field_7795, BlackOut.mc.field_1724);
         };
      }
   }

   private class Move extends Manager.Action {
      private final int from;
      private final int to;
      private final Predicate<ItemStack> predicate;

      private Move(int from, int to) {
         super();
         this.from = from;
         this.to = to;
         ItemStack copy = BlackOut.mc.field_1724.field_7512.method_7611(from).method_7677().method_7972();
         this.predicate = (stack) -> {
            return ItemStack.method_7984(stack, copy);
         };
      }
   }

   private class QuickMove extends Manager.Action {
      private final DoubleConsumer<ClientPlayerInteractionManager, ScreenHandler> consumer;

      private QuickMove(int id) {
         super();
         this.consumer = (manager, handler) -> {
            manager.method_2906(handler.field_7763, id, 0, SlotActionType.field_7794, BlackOut.mc.field_1724);
         };
      }
   }

   private class Swap extends Manager.Action {
      private final DoubleConsumer<ClientPlayerInteractionManager, ScreenHandler> consumer;

      private Swap(int id, int slotId) {
         super();
         this.consumer = (manager, handler) -> {
            manager.method_2906(handler.field_7763, id, slotId, SlotActionType.field_7791, BlackOut.mc.field_1724);
         };
      }
   }

   public static enum FindArea {
      Armor(5, 8),
      Hotbar(36, 44),
      Inventory(9, 35),
      Both(9, 44),
      All(5, 44);

      private final int start;
      private final int end;

      private FindArea(int start, int end) {
         this.start = start;
         this.end = end;
      }

      // $FF: synthetic method
      private static Manager.FindArea[] $values() {
         return new Manager.FindArea[]{Armor, Hotbar, Inventory, Both, All};
      }
   }

   private static record HotbarSearch(Predicate<Slot> predicate, ToDoubleFunction<Slot> function) {
      private HotbarSearch(Predicate<Slot> predicate, ToDoubleFunction<Slot> function) {
         this.predicate = predicate;
         this.function = function;
      }

      public Predicate<Slot> predicate() {
         return this.predicate;
      }

      public ToDoubleFunction<Slot> function() {
         return this.function;
      }
   }
}
