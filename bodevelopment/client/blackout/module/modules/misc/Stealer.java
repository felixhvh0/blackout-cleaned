package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RotationType;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.GameJoinEvent;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.client.Notifications;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.module.setting.multisettings.BoxMultiSetting;
import bodevelopment.client.blackout.randomstuff.timers.RenderList;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.ItemUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Stealer extends Module {
   private static Stealer INSTANCE;
   public final SettingGroup sgGeneral = this.addGroup("General");
   public final SettingGroup sgBest = this.addGroup("Best");
   public final SettingGroup sgRender = this.addGroup("Render");
   private final Setting<Boolean> instant;
   private final Setting<Double> speed;
   private final Setting<Double> openFor;
   private final Setting<Boolean> close;
   private final Setting<Double> closeDelay;
   private final Setting<Boolean> autoOpen;
   private final Setting<Double> openCooldown;
   private final Setting<Double> retryTime;
   private final Setting<Double> reopenCooldown;
   private final Setting<Boolean> silent;
   private final Setting<Boolean> stopRotations;
   private final Setting<Boolean> tpDisable;
   private final Setting<Boolean> bestWeapon;
   private final Setting<Boolean> swords;
   private final Setting<Boolean> axes;
   private final Setting<Boolean> bestPickaxe;
   private final Setting<Boolean> tools;
   private final Setting<Boolean> chestCheck;
   private final Setting<Stealer.ArmorMode> armor;
   private final Setting<List<Item>> items;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final BoxMultiSetting renderSetting;
   private final List<Stealer.Slot> movable;
   private final List<Stealer.Slot> container;
   private final List<Stealer.Slot> inventory;
   private long prevOpen;
   private boolean wasOpen;
   private boolean isOpen;
   private long openStateChange;
   private int calcR;
   private BlockPos calcMiddle;
   private BlockPos bestChest;
   private Direction bestDir;
   private int progress;
   private double bestDist;
   private BlockPos prevOpened;
   private final TimerList<BlockPos> opened;
   private final TimerList<BlockPos> retryTimers;
   private final RenderList<BlockPos> renderList;
   private double movesLeft;
   private final EquipmentSlot[] equipmentSlots;
   private final Predicate<ItemStack> weaponPredicate;

   public Stealer() {
      super("Stealer", "Transfers items from containers to inventory", SubCategory.MISC, true);
      this.instant = this.sgGeneral.b("Instant", false, ".");
      this.speed = this.sgGeneral.d("Speed", 5.0D, 0.0D, 20.0D, 0.1D, ".", () -> {
         return !(Boolean)this.instant.get();
      });
      this.openFor = this.sgGeneral.d("Open For", 0.2D, 0.0D, 20.0D, 0.1D, ".");
      this.close = this.sgGeneral.b("Close", true, "");
      this.closeDelay = this.sgGeneral.d("Close Delay", 0.2D, 0.0D, 20.0D, 0.1D, ".");
      this.autoOpen = this.sgGeneral.b("Auto Open", false, "");
      this.openCooldown = this.sgGeneral.d("Open Cooldown", 0.0D, 0.0D, 10.0D, 0.1D, "");
      this.retryTime = this.sgGeneral.d("Retry Time", 0.5D, 0.0D, 10.0D, 0.1D, "");
      this.reopenCooldown = this.sgGeneral.d("Reopen Cooldown", 30.0D, 0.0D, 10.0D, 0.1D, "");
      this.silent = this.sgGeneral.b("Silent", false, ".");
      this.stopRotations = this.sgGeneral.b("Stop Rotations", true, ".");
      this.tpDisable = this.sgGeneral.b("Disable on TP", false, "Should we disable when teleporting to another world");
      this.bestWeapon = this.sgBest.b("Best Weapon", true, ".");
      this.swords = this.sgBest.b("Swords", true, ".");
      this.axes = this.sgBest.b("Axes", true, ".");
      this.bestPickaxe = this.sgBest.b("Best Pickaxe", true, ".");
      this.tools = this.sgBest.b("Tools", false, ".");
      this.chestCheck = this.sgBest.b("Chest Check", true, ".");
      this.armor = this.sgBest.e("Tools", Stealer.ArmorMode.Best, ".");
      this.items = this.sgBest.il("Items", ".", Items.field_8463, Items.field_8367, Items.field_20391, Items.field_8118, Items.field_8543, Items.field_8803);
      this.renderTime = this.sgRender.d("Render Time", 5.0D, 0.0D, 10.0D, 0.1D, "How long the box should remain in full alpha value.");
      this.fadeTime = this.sgRender.d("Fade Time", 3.0D, 0.0D, 10.0D, 0.1D, "How long the fading should take.");
      this.renderSetting = BoxMultiSetting.of(this.sgRender);
      this.movable = new ArrayList();
      this.container = new ArrayList();
      this.inventory = new ArrayList();
      this.prevOpen = 0L;
      this.wasOpen = false;
      this.isOpen = false;
      this.openStateChange = 0L;
      this.prevOpened = null;
      this.opened = new TimerList(true);
      this.retryTimers = new TimerList(true);
      this.renderList = RenderList.getList(false);
      this.movesLeft = 0.0D;
      this.equipmentSlots = new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166};
      this.weaponPredicate = (stack) -> {
         return stack != null && ((Boolean)this.swords.get() && stack.method_7909() instanceof SwordItem || (Boolean)this.axes.get() && stack.method_7909() instanceof AxeItem);
      };
      INSTANCE = this;
   }

   public static Stealer getInstance() {
      return INSTANCE;
   }

   public boolean shouldNoRotate() {
      if (!this.enabled) {
         return false;
      } else if (!(Boolean)this.stopRotations.get()) {
         return false;
      } else {
         Screen var2 = BlackOut.mc.field_1755;
         if (var2 instanceof GenericContainerScreen) {
            GenericContainerScreen screen = (GenericContainerScreen)var2;
            return (Boolean)this.chestCheck.get() && !screen.method_25440().getString().toLowerCase().contains("chest") ? false : BlackOut.mc.field_1724.field_7512 instanceof GenericContainerScreenHandler;
         } else {
            return false;
         }
      }
   }

   public boolean isSilenting() {
      if (!this.enabled) {
         return false;
      } else if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (!(Boolean)this.silent.get()) {
            return false;
         } else {
            Screen var2 = BlackOut.mc.field_1755;
            if (var2 instanceof GenericContainerScreen) {
               GenericContainerScreen screen = (GenericContainerScreen)var2;
               return (Boolean)this.chestCheck.get() && !screen.method_25440().getString().toLowerCase().contains("chest") ? false : BlackOut.mc.field_1724.field_7512 instanceof GenericContainerScreenHandler;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Event
   public void onGameJoin(GameJoinEvent event) {
      if ((Boolean)this.tpDisable.get()) {
         this.disable(this.getDisplayName() + " was disabled due to server change/teleport", 5, Notifications.Type.Info);
      }

   }

   @Event
   public void onRender(RenderEvent.World.Pre event) {
      if ((Boolean)this.autoOpen.get()) {
         this.calc(BlackOut.mc.method_1488());
      }

   }

   @Event
   public void onRenderPost(RenderEvent.World.Post event) {
      this.renderList.update((pos, time, d) -> {
         this.renderSetting.render(BoxUtils.get(pos), (float)(1.0D - Math.max(time - (Double)this.renderTime.get(), 0.0D) / (Double)this.fadeTime.get()), 1.0F);
      });
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         ScreenHandler var4 = BlackOut.mc.field_1724.field_7512;
         if (var4 instanceof GenericContainerScreenHandler) {
            GenericContainerScreenHandler handler = (GenericContainerScreenHandler)var4;
            Screen var5 = BlackOut.mc.field_1755;
            if (var5 instanceof GenericContainerScreen) {
               GenericContainerScreen screen = (GenericContainerScreen)var5;
               if ((Boolean)this.chestCheck.get() && !screen.method_25440().getString().toLowerCase().contains("chest")) {
                  this.setOpen(false);
                  return;
               }

               if (this.prevOpened != null && (Boolean)this.autoOpen.get()) {
                  if (!this.wasOpen) {
                     this.opened.add(this.prevOpened, (Double)this.reopenCooldown.get());
                     this.renderList.add(this.prevOpened, (Double)this.renderTime.get() + (Double)this.fadeTime.get());
                  }

                  if ((Boolean)this.close.get() && !SettingUtils.inInteractRange(this.prevOpened)) {
                     BlackOut.mc.field_1724.method_7346();
                  }
               }

               this.setOpen(true);
               if ((double)(System.currentTimeMillis() - this.openStateChange) < (Double)this.openFor.get() * 1000.0D) {
                  return;
               }

               for(int i = 0; i < ((Boolean)this.instant.get() ? 5 : 1); ++i) {
                  this.update(handler);
                  this.updateClose(handler);
                  this.updateMoving(handler);
               }

               return;
            }
         }

         this.setOpen(false);
         if ((Boolean)this.autoOpen.get()) {
            this.calc(1.0F);
            this.openUpdate();
            this.startCalc();
         }

      }
   }

   private void setOpen(boolean state) {
      this.wasOpen = this.isOpen;
      this.isOpen = state;
      if (this.wasOpen != state) {
         this.openStateChange = System.currentTimeMillis();
      }

   }

   private void calc(float tickDelta) {
      if (this.calcMiddle != null) {
         int d = this.calcR * 2 + 1;
         int target = d * d * d;

         for(int i = this.progress; (float)i < (float)target * tickDelta; ++i) {
            this.progress = i;
            int x = i % d - this.calcR;
            int y = i / d % d - this.calcR;
            int z = i / d / d % d - this.calcR;
            BlockPos pos = this.calcMiddle.method_10069(x, y, z);
            this.calcPos(pos);
         }

      }
   }

   private void calcPos(BlockPos pos) {
      BlockState state = BlackOut.mc.field_1687.method_8320(pos);
      if (state.method_26204() == Blocks.field_10034) {
         if (!this.opened.contains((Object)pos)) {
            if (!this.retryTimers.contains((Object)pos)) {
               if (SettingUtils.inInteractRange(pos)) {
                  Direction dir = SettingUtils.getPlaceOnDirection(pos);
                  if (dir != null) {
                     double dist = BlackOut.mc.field_1724.method_33571().method_1025(pos.method_46558());
                     if (!(dist > this.bestDist)) {
                        this.bestDist = dist;
                        this.bestChest = pos;
                        this.bestDir = dir;
                     }
                  }
               }
            }
         }
      }
   }

   private void openUpdate() {
      if (!((double)(System.currentTimeMillis() - this.prevOpen) < (Double)this.openCooldown.get() * 1000.0D)) {
         if (!((double)(System.currentTimeMillis() - this.openStateChange) < (Double)this.closeDelay.get() * 1000.0D)) {
            if (this.bestChest != null) {
               if (!SettingUtils.shouldRotate(RotationType.Interact) || this.rotateBlock(this.bestChest, this.bestDir, RotationType.Interact, "open")) {
                  this.interactBlock(Hand.field_5808, this.bestChest.method_46558(), this.bestDir, this.bestChest);
                  this.retryTimers.add(this.bestChest, (Double)this.retryTime.get());
                  this.prevOpened = this.bestChest;
                  this.prevOpen = System.currentTimeMillis();
               }
            }
         }
      }
   }

   private void startCalc() {
      this.calcMiddle = BlockPos.method_49638(BlackOut.mc.field_1724.method_33571());
      this.calcR = (int)Math.ceil(SettingUtils.maxInteractRange());
      this.progress = 0;
      this.bestDist = Double.MAX_VALUE;
      this.bestChest = null;
      this.bestDir = null;
   }

   private void updateClose(GenericContainerScreenHandler handler) {
      if ((Boolean)this.close.get() && this.movable.isEmpty()) {
         BlackOut.mc.field_1724.method_7346();
      }

   }

   private void updateMoving(GenericContainerScreenHandler handler) {
      this.movesLeft += (Double)this.speed.get() / 20.0D;

      while((this.movesLeft > 0.0D || (Boolean)this.instant.get()) && !this.movable.isEmpty()) {
         this.move(handler, (Stealer.Slot)this.movable.remove(0));
      }

      this.movesLeft = Math.min(this.movesLeft, 1.0D);
   }

   private void move(GenericContainerScreenHandler handler, Stealer.Slot slot) {
      BlackOut.mc.field_1761.method_2906(handler.field_7763, slot.index, 0, SlotActionType.field_7794, BlackOut.mc.field_1724);
      --this.movesLeft;
   }

   private void update(GenericContainerScreenHandler handler) {
      this.movable.clear();
      this.getSlots(this.container, handler, true, 0, handler.method_17388() * 9);
      this.getSlots(this.inventory, handler, false, handler.method_17388() * 9, handler.method_7602().size());
      Stealer.Slot bestPickaxe;
      if ((Boolean)this.bestWeapon.get()) {
         bestPickaxe = this.getBestWeapon();
         if (bestPickaxe != null) {
            if (bestPickaxe.container) {
               this.movable.add(bestPickaxe);
            }

            this.dump((slot) -> {
               return slot != bestPickaxe && this.weaponPredicate.test(slot.stack);
            });
         }
      }

      if ((Boolean)this.bestPickaxe.get()) {
         bestPickaxe = this.getBestPickaxe();
         if (bestPickaxe != null) {
            if (bestPickaxe.container) {
               this.movable.add(bestPickaxe);
            }

            this.dump((slot) -> {
               return slot != bestPickaxe && slot.stack.method_7909() instanceof PickaxeItem;
            });
         }
      }

      if (!(Boolean)this.tools.get()) {
         this.dump((slot) -> {
            return slot.stack.method_7909() instanceof ToolItem && !this.weaponPredicate.test(slot.stack) && !(slot.stack.method_7909() instanceof PickaxeItem);
         });
      } else {
         this.steal((slot) -> {
            return slot.stack.method_7909() instanceof ToolItem && !this.weaponPredicate.test(slot.stack) && !(slot.stack.method_7909() instanceof PickaxeItem);
         });
      }

      switch((Stealer.ArmorMode)this.armor.get()) {
      case Never:
         this.dump((slot) -> {
            return slot.stack.method_7909() instanceof ArmorItem;
         });
         break;
      case All:
         this.steal((slot) -> {
            return slot.stack.method_7909() instanceof ArmorItem;
         });
         break;
      case Best:
         List<Stealer.Slot> list = new ArrayList();
         list.addAll(this.inventory);
         list.addAll(this.container);
         EquipmentSlot[] var3 = this.equipmentSlots;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            EquipmentSlot equipmentSlot = var3[var5];
            Stealer.Slot best = this.getBestArmor(equipmentSlot, list);
            if (best != null) {
               if (best.container) {
                  this.movable.add(best);
               }

               this.dump((slot) -> {
                  boolean var10000;
                  if (slot != best) {
                     Item patt13584$temp = slot.stack.method_7909();
                     if (patt13584$temp instanceof ArmorItem) {
                        ArmorItem armorItem = (ArmorItem)patt13584$temp;
                        if (armorItem.method_7685() == equipmentSlot) {
                           var10000 = true;
                           return var10000;
                        }
                     }
                  }

                  var10000 = false;
                  return var10000;
               });
            }
         }
      }

      this.steal((slot) -> {
         return ((List)this.items.get()).contains(slot.stack.method_7909());
      });
      this.dump((slot) -> {
         if (slot.stack().method_7960()) {
            return false;
         } else if (!(Boolean)this.bestPickaxe.get() && slot.stack.method_7909() instanceof MiningToolItem) {
            return false;
         } else if (slot.stack.method_7909() instanceof ArmorItem) {
            return false;
         } else if ((Boolean)this.bestWeapon.get() && this.weaponPredicate.test(slot.stack)) {
            return false;
         } else {
            return !((List)this.items.get()).contains(slot.stack.method_7909());
         }
      });
   }

   private Stealer.Slot getBestArmor(EquipmentSlot equipmentSlot, List<Stealer.Slot> list) {
      Stealer.Slot bestSlot = new Stealer.Slot(false, -1, BlackOut.mc.field_1724.method_31548().method_7372(equipmentSlot.method_5927()));
      double bestValue = ItemUtils.getArmorValue(bestSlot.stack);
      Iterator var6 = list.stream().filter((slotx) -> {
         Item patt14550$temp = slotx.stack.method_7909();
         boolean var10000;
         if (patt14550$temp instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem)patt14550$temp;
            if (armor.method_7685() == equipmentSlot) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }).toList().iterator();

      while(var6.hasNext()) {
         Stealer.Slot slot = (Stealer.Slot)var6.next();
         double value = ItemUtils.getArmorValue(slot.stack);
         if (!(bestValue >= value)) {
            bestSlot = slot;
            bestValue = value;
         }
      }

      return bestSlot;
   }

   private void steal(Predicate<Stealer.Slot> predicate) {
      Stream var10000 = this.container.stream().filter(predicate);
      List var10001 = this.movable;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::add);
   }

   private void dump(Predicate<Stealer.Slot> predicate) {
      Stream var10000 = this.inventory.stream().filter(predicate);
      List var10001 = this.movable;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::add);
   }

   private Stealer.Slot getBestWeapon() {
      Stealer.Slot bestSlot = null;
      double bestValue = 0.0D;
      List<Stealer.Slot> list = new ArrayList();
      list.addAll(this.inventory);
      list.addAll(this.container);
      Iterator var5 = list.stream().filter((slotx) -> {
         return this.weaponPredicate.test(slotx.stack);
      }).toList().iterator();

      while(var5.hasNext()) {
         Stealer.Slot slot = (Stealer.Slot)var5.next();
         double value = ItemUtils.getWeaponValue(slot.stack);
         if (!(bestValue >= value)) {
            bestSlot = slot;
            bestValue = value;
         }
      }

      return bestSlot;
   }

   private Stealer.Slot getBestPickaxe() {
      Stealer.Slot bestSlot = null;
      double bestValue = 0.0D;
      List<Stealer.Slot> list = new ArrayList();
      list.addAll(this.inventory);
      list.addAll(this.container);
      Iterator var5 = list.stream().filter((slotx) -> {
         return slotx.stack.method_7909() instanceof PickaxeItem;
      }).toList().iterator();

      while(var5.hasNext()) {
         Stealer.Slot slot = (Stealer.Slot)var5.next();
         double value = ItemUtils.getPickaxeValue(slot.stack);
         if (!(bestValue >= value)) {
            bestSlot = slot;
            bestValue = value;
         }
      }

      return bestSlot;
   }

   private void getSlots(List<Stealer.Slot> slots, GenericContainerScreenHandler handler, boolean container, int start, int end) {
      slots.clear();
      Iterator var6 = handler.method_7602().subList(start, end).iterator();

      while(var6.hasNext()) {
         ItemStack stack = (ItemStack)var6.next();
         slots.add(new Stealer.Slot(container, start++, stack));
      }

   }

   public static enum ArmorMode {
      Never,
      All,
      Best;

      // $FF: synthetic method
      private static Stealer.ArmorMode[] $values() {
         return new Stealer.ArmorMode[]{Never, All, Best};
      }
   }

   static record Slot(boolean container, int index, ItemStack stack) {
      Slot(boolean container, int index, ItemStack stack) {
         this.container = container;
         this.index = index;
         this.stack = stack;
      }

      public boolean container() {
         return this.container;
      }

      public int index() {
         return this.index;
      }

      public ItemStack stack() {
         return this.stack;
      }
   }
}
