package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.enums.RotationType;
import bodevelopment.client.blackout.enums.SwitchMode;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.functional.DoublePredicate;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.ObsidianModule;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.FindResult;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.InvUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.RotationUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;

public class Auto32K extends Module {
   private static Auto32K INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgRender = this.addGroup("Render");
   private final Setting<Auto32K.Mode> mode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> currentSlot;
   private final Setting<Integer> swordSlot;
   private final Setting<Boolean> yCheck;
   private final Setting<Boolean> serverDir;
   private final Setting<ObsidianModule.RotationMode> rotationMode;
   private final Setting<Boolean> silent;
   private final Setting<RenderShape> renderShapeHopper;
   private final Setting<BlackOutColor> lineColorHopper;
   private final Setting<BlackOutColor> sideColorHopper;
   private final Setting<RenderShape> renderShapeShulker;
   private final Setting<BlackOutColor> lineColorShulker;
   private final Setting<BlackOutColor> sideColorShulker;
   private final Setting<RenderShape> renderShapeDispenser;
   private final Setting<BlackOutColor> lineColorDispenser;
   private final Setting<BlackOutColor> sideColorDispenser;
   private final Setting<RenderShape> renderShapeRedstone;
   private final Setting<BlackOutColor> lineColorRedstone;
   private final Setting<BlackOutColor> sideColorRedstone;
   private Direction dispenserDir;
   private BlockPos hopperPos;
   private BlockPos supportPos;
   private BlockPos dispenserPos;
   private BlockPos redstonePos;
   private boolean valid;
   private BlockPos boxInside;
   private BlockPos openedBox;
   private BlockPos openedHopper;
   private boolean placed;
   private boolean found;
   private BlockPos calcMiddle;
   private int progress;
   private Direction calcDispenserDir;
   private BlockPos calcHopperPos;
   private BlockPos calcSupportPos;
   private BlockPos calcDispenserPos;
   private BlockPos calcRedstonePos;
   private boolean calcValid;
   private double calcValue;
   private int calcR;

   public Auto32K() {
      super("Auto 32K", ".", SubCategory.OFFENSIVE, true);
      this.mode = this.sgGeneral.e("Mode", Auto32K.Mode.Hopper, ".");
      this.switchMode = this.sgGeneral.e("Switch Mode", SwitchMode.Silent, ".");
      this.currentSlot = this.sgGeneral.b("Current Slot", true, ".");
      this.swordSlot = this.sgGeneral.i("Slot", 1, 1, 9, 1, ".", () -> {
         return !(Boolean)this.currentSlot.get();
      });
      this.yCheck = this.sgGeneral.b("Y Check", true, ".", () -> {
         return this.mode.get() == Auto32K.Mode.Dispenser;
      });
      this.serverDir = this.sgGeneral.b("Server Dir", true, ".", () -> {
         return this.mode.get() == Auto32K.Mode.Dispenser;
      });
      this.rotationMode = this.sgGeneral.e("Rotation Mode", ObsidianModule.RotationMode.Instant, ".", () -> {
         return this.mode.get() == Auto32K.Mode.Dispenser;
      });
      this.silent = this.sgGeneral.b("Silent", true, ".");
      this.renderShapeHopper = this.sgRender.e("Hopper Render Shape", RenderShape.Full, "Which parts of render should be rendered.");
      this.lineColorHopper = this.sgRender.c("Hopper Line Color", new BlackOutColor(255, 255, 255, 255), "Line color of rendered boxes.");
      this.sideColorHopper = this.sgRender.c("Hopper Side Color", new BlackOutColor(255, 255, 255, 50), "Side color of rendered boxes.");
      this.renderShapeShulker = this.sgRender.e("Shulker Render Shape", RenderShape.Full, "Which parts of render should be rendered.");
      this.lineColorShulker = this.sgRender.c("Shulker Line Color", new BlackOutColor(255, 0, 0, 255), "Line color of rendered boxes.");
      this.sideColorShulker = this.sgRender.c("Shulker Side Color", new BlackOutColor(255, 0, 0, 50), "Side color of rendered boxes.");
      this.renderShapeDispenser = this.sgRender.e("Dispenser Render Shape", RenderShape.Full, "Which parts of render should be rendered.");
      this.lineColorDispenser = this.sgRender.c("Dispenser Line Color", new BlackOutColor(255, 255, 255, 255), "Line color of rendered boxes.");
      this.sideColorDispenser = this.sgRender.c("Dispenser Side Color", new BlackOutColor(255, 255, 255, 50), "Side color of rendered boxes.");
      this.renderShapeRedstone = this.sgRender.e("Redstone Render Shape", RenderShape.Full, "Which parts of render should be rendered.");
      this.lineColorRedstone = this.sgRender.c("Redstone Line Color", new BlackOutColor(255, 0, 0, 255), "Line color of rendered boxes.");
      this.sideColorRedstone = this.sgRender.c("Redstone Side Color", new BlackOutColor(255, 0, 0, 50), "Side color of rendered boxes.");
      this.dispenserDir = null;
      this.hopperPos = null;
      this.supportPos = null;
      this.dispenserPos = null;
      this.redstonePos = null;
      this.valid = false;
      this.boxInside = BlockPos.field_10980;
      this.openedBox = BlockPos.field_10980;
      this.openedHopper = BlockPos.field_10980;
      this.placed = false;
      this.found = false;
      this.calcMiddle = BlockPos.field_10980;
      this.progress = 0;
      this.calcDispenserDir = null;
      this.calcHopperPos = null;
      this.calcSupportPos = null;
      this.calcDispenserPos = null;
      this.calcRedstonePos = null;
      this.calcValid = false;
      this.calcValue = 0.0D;
      this.calcR = 0;
      INSTANCE = this;
   }

   public static Auto32K getInstance() {
      return INSTANCE;
   }

   public boolean isSilenting() {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (!(Boolean)this.silent.get()) {
            return false;
         } else {
            return BlackOut.mc.field_1724.field_7512 instanceof Generic3x3ContainerScreenHandler || BlackOut.mc.field_1724.field_7512 instanceof HopperScreenHandler;
         }
      } else {
         return false;
      }
   }

   public void onEnable() {
      this.resetPos();
   }

   @Event
   public void onMove(MoveEvent.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (this.shouldCalc()) {
            this.calc(1.0F);
            this.endCalc();
         }

         if (!SettingUtils.grimPackets()) {
            this.update(false);
         }

      }
   }

   @Event
   public void onTickPre(TickEvent.Pre event) {
      this.placed = false;
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null && SettingUtils.grimPackets()) {
         this.update(false);
      }
   }

   @Event
   public void onTickPost(TickEvent.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         this.update(true);
         if (this.shouldCalc()) {
            this.startCalc();
         }

      }
   }

   @Event
   public void onRender(RenderEvent.World.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (this.shouldCalc()) {
            this.calc(event.tickDelta);
         }

      }
   }

   @Event
   public void onRenderPost(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (this.hopperPos != null) {
            this.renderBox(this.hopperPos, this.renderShapeHopper, this.sideColorHopper, this.lineColorHopper);
         }

         if (this.hopperPos != null) {
            this.renderBox(this.hopperPos.method_10084(), this.renderShapeShulker, this.sideColorShulker, this.lineColorShulker);
         }

         if (this.dispenserPos != null) {
            this.renderBox(this.dispenserPos, this.renderShapeDispenser, this.sideColorDispenser, this.lineColorDispenser);
         }

         if (this.redstonePos != null) {
            this.renderBox(this.redstonePos, this.renderShapeRedstone, this.sideColorRedstone, this.lineColorRedstone);
         }

      }
   }

   private void renderBox(BlockPos pos, Setting<RenderShape> shape, Setting<BlackOutColor> sideColor, Setting<BlackOutColor> lineColor) {
      Render3DUtils.box(BoxUtils.get(pos), (BlackOutColor)sideColor.get(), (BlackOutColor)lineColor.get(), (RenderShape)shape.get());
   }

   private void resetPos() {
      this.dispenserDir = null;
      this.hopperPos = null;
      this.supportPos = null;
      this.dispenserPos = null;
      this.redstonePos = null;
      this.valid = false;
      this.boxInside = BlockPos.field_10980;
      this.openedBox = BlockPos.field_10980;
      this.openedHopper = BlockPos.field_10980;
      this.placed = false;
      this.found = false;
      this.calcMiddle = BlockPos.field_10980;
      this.progress = 0;
      this.calcDispenserDir = null;
      this.calcHopperPos = null;
      this.calcSupportPos = null;
      this.calcDispenserPos = null;
      this.calcRedstonePos = null;
      this.calcValid = false;
      this.calcValue = 0.0D;
      this.calcR = 0;
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
            this.updatePos(pos);
         }

      }
   }

   private boolean shouldCalc() {
      return !this.valid || !this.found;
   }

   private void startCalc() {
      this.calcDispenserPos = null;
      this.calcHopperPos = null;
      this.calcRedstonePos = null;
      this.calcSupportPos = null;
      this.calcDispenserDir = null;
      this.calcValue = -42069.0D;
      this.found = false;
      this.calcValid = false;
      this.progress = 0;
      this.calcR = (int)Math.ceil(SettingUtils.maxPlaceRange());
      this.calcMiddle = BlockPos.method_49638(BlackOut.mc.field_1724.method_33571());
   }

   private void endCalc() {
      this.dispenserDir = this.calcDispenserDir;
      this.hopperPos = this.calcHopperPos;
      this.supportPos = this.calcSupportPos;
      this.dispenserPos = this.calcDispenserPos;
      this.redstonePos = this.calcRedstonePos;
      this.found = this.valid = this.calcValid;
   }

   private void update(boolean place) {
      switch((Auto32K.Mode)this.mode.get()) {
      case Hopper:
         this.moveSword();
         if (!this.valid) {
            return;
         }

         this.place(Blocks.field_10312, this.hopperPos, place);
         if (this.placeShulker(place)) {
            this.hopperUpdate();
         }
         break;
      case Dispenser:
         this.moveSword();
         if (!this.valid) {
            return;
         }

         this.place(Blocks.field_10312, this.hopperPos, place);
         this.place(Blocks.field_10540, this.supportPos, place);
         this.place(Blocks.field_10200, this.dispenserPos, place);
         if (!this.boxUpdate()) {
            return;
         }

         this.place(Blocks.field_10002, this.redstonePos, place);
         this.hopperUpdate();
      }

   }

   private boolean boxUpdate() {
      if (this.dispenserPos == null) {
         return false;
      } else if (this.get(this.dispenserPos).method_26204() != Blocks.field_10200) {
         return false;
      } else {
         Direction dir = SettingUtils.getPlaceOnDirection(this.dispenserPos);
         if (dir == null) {
            return false;
         } else {
            boolean isOpened = this.openedBox.equals(this.dispenserPos);
            boolean isBox = this.boxInside.equals(this.dispenserPos);
            if (!isOpened) {
               this.openedBox = this.dispenserPos;
               this.interactBlock(Hand.field_5808, this.dispenserPos.method_46558(), dir, this.dispenserPos);
            }

            if (!isBox) {
               this.putBox();
            }

            return isBox;
         }
      }
   }

   private boolean hopperUpdate() {
      if (this.get(this.hopperPos).method_26204() != Blocks.field_10312) {
         return false;
      } else {
         Direction dir = SettingUtils.getPlaceOnDirection(this.hopperPos);
         if (dir == null) {
            return false;
         } else {
            if (!this.openedHopper.equals(this.hopperPos)) {
               this.openedHopper = this.hopperPos;
               this.interactBlock(Hand.field_5808, this.hopperPos.method_46558(), dir, this.hopperPos);
            }

            return this.openedHopper.equals(this.hopperPos);
         }
      }
   }

   private int getSlot() {
      return (Boolean)this.currentSlot.get() ? BlackOut.mc.field_1724.method_31548().field_7545 : (Integer)this.swordSlot.get() - 1;
   }

   private void putBox() {
      ScreenHandler handler = BlackOut.mc.field_1724.field_7512;
      if (handler instanceof Generic3x3ContainerScreenHandler) {
         Iterator var2 = handler.field_7761.iterator();

         Slot slot;
         do {
            if (!var2.hasNext()) {
               return;
            }

            slot = (Slot)var2.next();
         } while(!OLEPOSSUtils.isShulker(slot.method_7677()));

         this.boxInside = this.dispenserPos;
         BlackOut.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, SlotActionType.field_7794, BlackOut.mc.field_1724);
         BlackOut.mc.field_1724.method_3137();
      }
   }

   private boolean moveSword() {
      ScreenHandler handler = BlackOut.mc.field_1724.field_7512;
      if (!(handler instanceof HopperScreenHandler)) {
         return false;
      } else {
         Iterator var2 = handler.field_7761.iterator();

         while(var2.hasNext()) {
            Slot slot = (Slot)var2.next();
            ItemStack stack = slot.method_7677();
            if (stack.method_7909() instanceof SwordItem) {
               Map<Enchantment, Integer> enchants = EnchantmentHelper.method_8222(stack);
               if (enchants.containsKey(Enchantments.field_9118) && (Integer)enchants.get(Enchantments.field_9118) >= 10) {
                  int s = this.getSlot();
                  if (s != BlackOut.mc.field_1724.method_31548().field_7545) {
                     InvUtils.swap(s);
                  }

                  BlackOut.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, s, SlotActionType.field_7791, BlackOut.mc.field_1724);
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean placeShulker(boolean place) {
      BlockPos pos = this.hopperPos.method_10084();
      if (BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof ShulkerBoxBlock) {
         return true;
      } else {
         Hand hand = OLEPOSSUtils.getHand(OLEPOSSUtils::isShulker);
         FindResult findResult = null;
         if (hand == null && !(findResult = ((SwitchMode)this.switchMode.get()).find(OLEPOSSUtils::isShulker)).wasFound()) {
            return false;
         } else {
            PlaceData data = SettingUtils.getPlaceData(pos, false);
            if (!data.valid()) {
               return false;
            } else if (!SettingUtils.inInteractRange(data.pos())) {
               return false;
            } else if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !this.rotateBlock(data, RotationType.BlockPlace, "placing")) {
               return false;
            } else if (place && !this.placed) {
               if (hand == null && !((SwitchMode)this.switchMode.get()).swap(findResult.slot())) {
                  return false;
               } else {
                  this.placeBlock(hand, data);
                  Item var8 = (hand != null ? Managers.PACKET.handStack(hand) : findResult.stack()).method_7909();
                  Block var10000;
                  if (var8 instanceof BlockItem) {
                     BlockItem blockitem = (BlockItem)var8;
                     var10000 = blockitem.method_7711();
                  } else {
                     var10000 = Blocks.field_10603;
                  }

                  BlockState state = var10000.method_9564();
                  BlackOut.mc.field_1687.method_8501(pos, state);
                  this.placed = true;
                  if (hand == null) {
                     ((SwitchMode)this.switchMode.get()).swapBack();
                  }

                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   private void place(Block block, BlockPos pos, boolean place) {
      if (pos != null) {
         Item var5 = block.method_8389();
         if (var5 instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)var5;
            if (BlackOut.mc.field_1687.method_8320(pos).method_26204() != block) {
               Hand hand = OLEPOSSUtils.getHand((Item)blockItem);
               FindResult findResult = null;
               if (hand != null || (findResult = ((SwitchMode)this.switchMode.get()).find((Item)blockItem)).wasFound()) {
                  PlaceData data = SettingUtils.getPlaceData(pos, false);
                  if (data.valid()) {
                     if (SettingUtils.inPlaceRange(data.pos())) {
                        if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || this.rotateBlock(data, RotationType.BlockPlace, "placing")) {
                           if (place && !this.placed) {
                              if (block == Blocks.field_10200) {
                                 switch((ObsidianModule.RotationMode)this.rotationMode.get()) {
                                 case Instant:
                                    if (!this.rotate(this.dispenserDir.method_10144(), 0.0F, 0.0D, 45.0D, RotationType.InstantOther, "facing")) {
                                       return;
                                    }
                                    break;
                                 case Normal:
                                    if (!this.rotate(this.dispenserDir.method_10144(), 0.0F, 0.0D, 45.0D, RotationType.Other, "facing")) {
                                       return;
                                    }
                                 }
                              }

                              if (hand != null || ((SwitchMode)this.switchMode.get()).swap(findResult.slot())) {
                                 if (this.rotationMode.get() == ObsidianModule.RotationMode.Packet) {
                                    this.sendPacket(new LookAndOnGround(this.dispenserDir.method_10144(), 0.0F, Managers.PACKET.isOnGround()));
                                 }

                                 this.placeBlock(hand, data);
                                 BlockState state = block.method_9564();
                                 if (block == Blocks.field_10200) {
                                    state = (BlockState)state.method_11657(DispenserBlock.field_10918, Direction.method_10150((double)Managers.ROTATION.prevYaw).method_10153());
                                 }

                                 BlackOut.mc.field_1687.method_8501(pos, state);
                                 this.placed = true;
                                 if (hand == null) {
                                    ((SwitchMode)this.switchMode.get()).swapBack();
                                 }

                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void updatePos(BlockPos hopper) {
      switch((Auto32K.Mode)this.mode.get()) {
      case Hopper:
         this.updateHopper(hopper);
         break;
      case Dispenser:
         this.updateDispenser(hopper);
      }

   }

   private void updateHopper(BlockPos hopper) {
      double value = this.getValue(hopper);
      if (!(value < this.calcValue)) {
         BlockState state = this.get(hopper);
         if (state.method_26204() != Blocks.field_10312) {
            if (!OLEPOSSUtils.replaceable(hopper)) {
               return;
            }

            PlaceData data = SettingUtils.getPlaceData(hopper);
            if (!data.valid()) {
               return;
            }

            if (!SettingUtils.inPlaceRange(data.pos())) {
               return;
            }
         }

         if (SettingUtils.inInteractRange(hopper) && SettingUtils.getPlaceOnDirection(hopper) != null) {
            if (OLEPOSSUtils.replaceable(hopper.method_10084()) || this.get(hopper.method_10084()).method_26204() instanceof ShulkerBoxBlock) {
               this.calcHopperPos = hopper;
               this.calcValid = true;
               this.calcValue = value;
            }
         }
      }
   }

   private void updateDispenser(BlockPos hopper) {
      double value = this.getValue(hopper);
      if (!(value < this.calcValue)) {
         BlockState state = this.get(hopper);
         if (state.method_26204() != Blocks.field_10312) {
            if (!OLEPOSSUtils.replaceable(hopper)) {
               return;
            }

            PlaceData data = SettingUtils.getPlaceData(hopper);
            if (!data.valid()) {
               return;
            }

            if (!SettingUtils.inPlaceRange(data.pos())) {
               return;
            }
         }

         if (SettingUtils.inInteractRange(hopper) && SettingUtils.getPlaceOnDirection(hopper) != null) {
            if (OLEPOSSUtils.replaceable(hopper.method_10084()) || this.get(hopper.method_10084()).method_26204() instanceof ShulkerBoxBlock) {
               Iterator var14 = Type.field_11062.iterator();

               while(true) {
                  Direction dir;
                  BlockPos pos;
                  do {
                     if (!var14.hasNext()) {
                        return;
                     }

                     dir = (Direction)var14.next();
                     pos = hopper.method_10093(dir).method_10084();
                  } while(!this.validDispenser(pos, dir));

                  Direction[] var8 = Direction.values();
                  int var9 = var8.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Direction direction = var8[var10];
                     BlockPos pos2 = pos.method_10093(direction);
                     if (this.get(pos2).method_26204() == Blocks.field_10002 && this.get(pos).method_26204() != Blocks.field_10200) {
                        break;
                     }

                     if (OLEPOSSUtils.replaceable(pos2) && direction != Direction.field_11033 && direction != dir.method_10153()) {
                        PlaceData data = SettingUtils.getPlaceData(pos2, false);
                        if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
                           this.calcDispenserDir = dir;
                           this.calcHopperPos = hopper;
                           this.calcSupportPos = hopper.method_10093(dir);
                           this.calcDispenserPos = this.calcSupportPos.method_10084();
                           this.calcRedstonePos = this.calcDispenserPos.method_10093(direction);
                           this.calcValid = true;
                           this.calcValue = value;
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean directionCheck(BlockPos pos, Direction direction) {
      Vec3d center = pos.method_46558();
      double pitch;
      if ((Boolean)this.serverDir.get()) {
         pitch = RotationUtils.getYaw(center);
         if (Math.abs(RotationUtils.yawAngle((double)direction.method_10144(), pitch)) > 40.0D) {
            return false;
         }
      }

      if ((Boolean)this.yCheck.get()) {
         pitch = RotationUtils.getPitch(center);
         return Math.abs(pitch) < 45.0D;
      } else {
         return true;
      }
   }

   private double getValue(BlockPos pos) {
      double value = 0.0D;
      Iterator var4 = BlackOut.mc.field_1687.method_18456().iterator();

      while(var4.hasNext()) {
         PlayerEntity player = (PlayerEntity)var4.next();
         double distance = player.method_5707(pos.method_46558());
         if (distance < 100.0D) {
            value += distance;
         }
      }

      return value;
   }

   private boolean validDispenser(BlockPos pos, Direction direction) {
      BlockState state = BlackOut.mc.field_1687.method_8320(pos);
      if (SettingUtils.getPlaceOnDirection(pos) == null) {
         return false;
      } else if (state.method_26204() == Blocks.field_10200) {
         return state.method_11654(DispenserBlock.field_10918) == direction.method_10153();
      } else if (!OLEPOSSUtils.replaceable(pos)) {
         return false;
      } else if (!this.directionCheck(pos, direction)) {
         return false;
      } else {
         PlaceData data = SettingUtils.getPlaceData(pos, (p, d) -> {
            return d == Direction.field_11033;
         }, (DoublePredicate)null);
         return data.valid() && SettingUtils.inPlaceRange(data.pos());
      }
   }

   private BlockState get(BlockPos pos) {
      return BlackOut.mc.field_1687.method_8320(pos);
   }

   public static enum Mode {
      Hopper,
      Dispenser;

      // $FF: synthetic method
      private static Auto32K.Mode[] $values() {
         return new Auto32K.Mode[]{Hopper, Dispenser};
      }
   }
}
