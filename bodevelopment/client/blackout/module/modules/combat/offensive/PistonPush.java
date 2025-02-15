package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.enums.RotationType;
import bodevelopment.client.blackout.enums.SwingHand;
import bodevelopment.client.blackout.enums.SwitchMode;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.functional.DoublePredicate;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.FindResult;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.EntityUtils;
import bodevelopment.client.blackout.util.HoleUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;

public class PistonPush extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgDelay = this.addGroup("Delay");
   private final SettingGroup sgSwing = this.addGroup("Swing");
   private final SettingGroup sgRender = this.addGroup("Render");
   private final Setting<Boolean> pauseEat;
   public final Setting<PistonPush.Redstone> redstone;
   private final Setting<Boolean> onlyHole;
   private final Setting<Boolean> toggleMove;
   public final Setting<SwitchMode> pistonSwitch;
   public final Setting<SwitchMode> redstoneSwitch;
   private final Setting<Double> prDelay;
   private final Setting<Double> rmDelay;
   private final Setting<Double> mpDelay;
   private final Setting<Boolean> pistonSwing;
   private final Setting<SwingHand> pistonHand;
   private final Setting<Boolean> redstoneSwing;
   private final Setting<SwingHand> redstoneHand;
   private final Setting<RenderShape> pistonShape;
   private final Setting<BlackOutColor> psColor;
   private final Setting<BlackOutColor> plColor;
   private final Setting<RenderShape> redstoneShape;
   private final Setting<BlackOutColor> rsColor;
   private final Setting<BlackOutColor> rlColor;
   private long pistonTime;
   private long redstoneTime;
   private long mineTime;
   private boolean minedThisTick;
   private boolean pistonPlaced;
   private boolean redstonePlaced;
   private boolean mined;
   private BlockPos pistonPos;
   private BlockPos redstonePos;
   private Direction pistonDir;
   private PlaceData pistonData;
   private PlaceData redstoneData;
   private BlockPos lastPiston;
   private BlockPos lastRedstone;
   private Direction lastDirection;
   private BlockPos startPos;
   private BlockPos currentPos;

   public PistonPush() {
      super("Piston Push", "Pushes people out of their safe holes.", SubCategory.OFFENSIVE, true);
      this.pauseEat = this.sgGeneral.b("PauseEat", false, "Do we stop while eating");
      this.redstone = this.sgGeneral.e("Redstone", PistonPush.Redstone.Block, "What kind of redstone to use", () -> {
         return true;
      });
      this.onlyHole = this.sgGeneral.b("Only Hole", false, "Toggles when enemy gets out of the hole.");
      this.toggleMove = this.sgGeneral.b("Toggle Move", false, "Toggles when enemy moves.");
      this.pistonSwitch = this.sgGeneral.e("Piston modeMode", SwitchMode.Silent, "Method of switching. Silent is the most reliable.", () -> {
         return true;
      });
      this.redstoneSwitch = this.sgGeneral.e("Redstone Switch", SwitchMode.Silent, "Method of switching. Silent is the most reliable.", () -> {
         return true;
      });
      this.prDelay = this.sgDelay.d("Piston > Redstone", 0.0D, 0.0D, 20.0D, 0.1D, "How many seconds to wait between placing piston and redstone.");
      this.rmDelay = this.sgDelay.d("Redstone > Mine", 0.0D, 0.0D, 20.0D, 0.1D, "How many seconds to wait between placing redstone and starting to mine it.");
      this.mpDelay = this.sgDelay.d("Mine > Piston", 0.0D, 0.0D, 20.0D, 0.1D, "How many seconds to wait after mining the redstone before starting a new cycle.");
      this.pistonSwing = this.sgSwing.b("Piston Swing", true, "Renders swing animation when placing a piston.");
      SettingGroup var10001 = this.sgSwing;
      SwingHand var10003 = SwingHand.RealHand;
      Setting var10005 = this.pistonSwing;
      Objects.requireNonNull(var10005);
      this.pistonHand = var10001.e("Piston Hand", var10003, "Which hand should be swung.", var10005::get);
      this.redstoneSwing = this.sgSwing.b("Redstone Swing", true, "Renders swing animation when placing redstone.");
      var10001 = this.sgSwing;
      var10003 = SwingHand.RealHand;
      var10005 = this.redstoneSwing;
      Objects.requireNonNull(var10005);
      this.redstoneHand = var10001.e("Redstone Hand", var10003, "Which hand should be swung.", var10005::get);
      this.pistonShape = this.sgRender.e("Piston Render Shape", RenderShape.Full, "Which parts should be rendered.");
      this.psColor = this.sgRender.c("Piston Side Color", new BlackOutColor(255, 255, 255, 50), "Color of rendered sides.");
      this.plColor = this.sgRender.c("Piston Line Color", new BlackOutColor(255, 255, 255, 255), "Color of rendered lines.");
      this.redstoneShape = this.sgRender.e("Redstone Render Shape", RenderShape.Full, "Which parts should be rendered.");
      this.rsColor = this.sgRender.c("Redstone Side Color", new BlackOutColor(255, 0, 0, 50), "Color of rendered sides.");
      this.rlColor = this.sgRender.c("Redstone Line Color", new BlackOutColor(255, 0, 0, 255), "Color of rendered lines.");
      this.pistonTime = 0L;
      this.redstoneTime = 0L;
      this.mineTime = 0L;
      this.minedThisTick = false;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.pistonPos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.redstoneData = null;
      this.lastPiston = null;
      this.lastRedstone = null;
      this.lastDirection = null;
      this.startPos = null;
      this.currentPos = null;
   }

   public void onEnable() {
      this.lastPiston = null;
      this.lastRedstone = null;
      this.lastDirection = null;
      this.startPos = null;
      this.redstonePlaced = false;
      this.pistonPlaced = false;
      this.mined = false;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      this.minedThisTick = false;
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (this.startPos != null && (Boolean)this.toggleMove.get() && !this.startPos.equals(this.currentPos)) {
            this.disable("moved");
         } else {
            this.update();
            if (this.pistonPos == null) {
               this.lastPiston = null;
               this.lastRedstone = this.redstonePos;
               this.lastDirection = this.pistonDir;
            } else {
               Render3DUtils.box(this.getBox(this.pistonPos), (BlackOutColor)this.psColor.get(), (BlackOutColor)this.plColor.get(), (RenderShape)this.pistonShape.get());
               Render3DUtils.box(this.getBox(this.redstonePos), (BlackOutColor)this.rsColor.get(), (BlackOutColor)this.rlColor.get(), (RenderShape)this.redstoneShape.get());
               if ((double)(System.currentTimeMillis() - this.mineTime) > (Double)this.mpDelay.get() * 1000.0D && this.redstonePlaced && this.pistonPlaced && this.mined || !this.pistonPos.equals(this.lastPiston) || !this.redstonePos.equals(this.lastRedstone) || !this.pistonDir.equals(this.lastDirection)) {
                  this.redstonePlaced = false;
                  this.pistonPlaced = false;
                  this.mined = false;
               }

               this.lastPiston = this.pistonPos;
               this.lastRedstone = this.redstonePos;
               this.lastDirection = this.pistonDir;
               if (!(Boolean)this.pauseEat.get() || !BlackOut.mc.field_1724.method_6115()) {
                  this.placePiston();
                  this.placeRedstone();
                  this.mineUpdate();
               }
            }
         }
      }
   }

   private void placePiston() {
      if (!this.pistonPlaced) {
         Hand hand = OLEPOSSUtils.getHand(Items.field_8249);
         FindResult result = ((SwitchMode)this.pistonSwitch.get()).find(Items.field_8249);
         if (hand != null || result.wasFound()) {
            if (BlackOut.mc.field_1724.method_24828()) {
               if (!EntityUtils.intersects(BoxUtils.get(this.pistonPos), (entity) -> {
                  return !entity.method_7325() && !(entity instanceof ItemEntity);
               })) {
                  if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || this.rotateBlock(this.pistonData, RotationType.BlockPlace, "piston")) {
                     this.sendPacket(new LookAndOnGround(this.pistonDir.method_10144(), Managers.ROTATION.nextPitch, Managers.PACKET.isOnGround()));
                     boolean switched = false;
                     if (hand == null) {
                        switched = ((SwitchMode)this.pistonSwitch.get()).swap(result.slot());
                     }

                     if (hand != null || switched) {
                        hand = hand == null ? Hand.field_5808 : hand;
                        this.placeBlock(hand, this.pistonData.pos().method_46558(), this.pistonData.dir(), this.pistonData.pos());
                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                           this.end("piston");
                        }

                        this.pistonTime = System.currentTimeMillis();
                        this.pistonPlaced = true;
                        if ((Boolean)this.pistonSwing.get()) {
                           this.clientSwing((SwingHand)this.pistonHand.get(), hand);
                        }

                        if (switched) {
                           ((SwitchMode)this.pistonSwitch.get()).swapBack();
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void placeRedstone() {
      if (this.pistonPlaced && !this.redstonePlaced) {
         if (!((double)(System.currentTimeMillis() - this.pistonTime) < (Double)this.prDelay.get() * 1000.0D)) {
            Hand hand = OLEPOSSUtils.getHand(((PistonPush.Redstone)this.redstone.get()).i);
            FindResult result = ((SwitchMode)this.redstoneSwitch.get()).find(((PistonPush.Redstone)this.redstone.get()).i);
            boolean available = hand != null;
            if (!available) {
               available = result.wasFound();
            }

            if (available) {
               if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || this.rotateBlock(this.redstoneData, RotationType.BlockPlace, "redstone")) {
                  boolean switched = false;
                  if (hand == null) {
                     switched = ((SwitchMode)this.redstoneSwitch.get()).swap(result.slot());
                  }

                  if (hand != null || switched) {
                     hand = hand == null ? Hand.field_5808 : hand;
                     this.placeBlock(hand, this.redstoneData.pos().method_46558(), this.redstoneData.dir(), this.redstoneData.pos());
                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        this.end("redstone");
                     }

                     this.redstonePlaced = true;
                     this.redstoneTime = System.currentTimeMillis();
                     if ((Boolean)this.redstoneSwing.get()) {
                        this.clientSwing((SwingHand)this.redstoneHand.get(), hand);
                     }

                     if (switched) {
                        ((SwitchMode)this.redstoneSwitch.get()).swapBack();
                     }

                  }
               }
            }
         }
      }
   }

   private Box getBox(BlockPos pos) {
      return new Box((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.method_10260() + 1));
   }

   private void mineUpdate() {
      if (this.pistonPlaced && this.redstonePlaced) {
         if (!this.minedThisTick) {
            if (!((double)(System.currentTimeMillis() - this.redstoneTime) < (Double)this.rmDelay.get() * 1000.0D)) {
               if (this.redstonePos != null) {
                  if (this.redstone.get() != PistonPush.Redstone.Torch || BlackOut.mc.field_1687.method_8320(this.redstonePos).method_26204() instanceof RedstoneTorchBlock) {
                     if (this.redstone.get() != PistonPush.Redstone.Block || BlackOut.mc.field_1687.method_8320(this.redstonePos).method_26204() == Blocks.field_10002) {
                        AutoMine autoMine = AutoMine.getInstance();
                        if (!autoMine.enabled || !this.redstonePos.equals(autoMine.minePos)) {
                           if (autoMine.enabled) {
                              if (this.redstonePos.equals(autoMine.minePos)) {
                                 return;
                              }

                              autoMine.onStart(this.redstonePos);
                           } else {
                              Direction mineDir = SettingUtils.getPlaceOnDirection(this.redstonePos);
                              if (mineDir != null) {
                                 this.sendPacket(new PlayerActionC2SPacket(Action.field_12968, this.redstonePos, mineDir));
                                 this.sendPacket(new PlayerActionC2SPacket(Action.field_12973, this.redstonePos, mineDir));
                              }
                           }

                           if (!this.mined) {
                              this.mineTime = System.currentTimeMillis();
                           }

                           this.mined = true;
                           this.minedThisTick = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void update() {
      this.pistonPos = null;
      Iterator var1 = BlackOut.mc.field_1687.method_18456().iterator();

      do {
         AbstractClientPlayerEntity player;
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           if (!var1.hasNext()) {
                              return;
                           }

                           player = (AbstractClientPlayerEntity)var1.next();
                        } while(Managers.FRIENDS.isFriend(player));
                     } while(player == BlackOut.mc.field_1724);
                  } while(BlackOut.mc.field_1724.method_5739(player) > 10.0F);
               } while(player.method_6032() <= 0.0F);
            } while(player.method_7325());
         } while(!OLEPOSSUtils.solid2(player.method_24515()) && (Boolean)this.onlyHole.get() && !HoleUtils.inHole(player.method_24515()));

         this.updatePos(player);
      } while(this.pistonPos == null);

   }

   private void updatePos(PlayerEntity player) {
      BlockPos eyePos = BlockPos.method_49638(player.method_33571());
      if (!OLEPOSSUtils.solid2(eyePos.method_10084())) {
         Iterator var3 = Type.field_11062.method_29716().sorted(Comparator.comparingDouble((d) -> {
            return eyePos.method_10093(d).method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
         })).toList().iterator();

         while(true) {
            Direction dir;
            BlockPos pos;
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  dir = (Direction)var3.next();
                  this.resetPos();
                  pos = eyePos.method_10093(dir);
               } while(!this.upCheck(pos));
            } while(!OLEPOSSUtils.replaceable(pos) && !(BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof PistonBlock) && BlackOut.mc.field_1687.method_8320(pos).method_26204() != Blocks.field_10008);

            if (!OLEPOSSUtils.solid2(eyePos.method_10093(dir.method_10153())) && !OLEPOSSUtils.solid2(eyePos.method_10093(dir.method_10153()).method_10084()) && OLEPOSSUtils.solid2(eyePos.method_10093(dir.method_10153()).method_10074())) {
               PlaceData data = SettingUtils.getPlaceData(pos);
               if (data != null && data.valid()) {
                  this.pistonData = data;
                  this.pistonDir = dir;
                  this.updateRedstone(pos);
                  if (this.redstonePos != null) {
                     if (this.startPos == null) {
                        this.startPos = player.method_24515();
                     }

                     this.currentPos = player.method_24515();
                     this.pistonPos = pos;
                     return;
                  }
               }
            }
         }
      }
   }

   private void updateRedstone(BlockPos pos) {
      Iterator var2;
      Direction direction;
      BlockPos position;
      if (this.redstone.get() == PistonPush.Redstone.Torch) {
         var2 = Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble((i) -> {
            return pos.method_10093(i).method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
         })).toList().iterator();

         do {
            do {
               do {
                  do {
                     do {
                        if (!var2.hasNext()) {
                           this.redstonePos = null;
                           return;
                        }

                        direction = (Direction)var2.next();
                     } while(direction == this.pistonDir.method_10153());
                  } while(direction == Direction.field_11033);
               } while(direction == Direction.field_11036);

               position = pos.method_10093(direction);
            } while(!OLEPOSSUtils.replaceable(position) && !(BlackOut.mc.field_1687.method_8320(position).method_26204() instanceof RedstoneTorchBlock));

            this.redstoneData = SettingUtils.getPlaceData(position, (DoublePredicate)null, (p, d) -> {
               if (d == Direction.field_11036 && !OLEPOSSUtils.solid(position.method_10074())) {
                  return false;
               } else if (direction == d.method_10153()) {
                  return false;
               } else if (pos.equals(p)) {
                  return false;
               } else if (BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof TorchBlock) {
                  return false;
               } else {
                  return !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof PistonBlock) && !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof PistonHeadBlock);
               }
            });
         } while(!this.redstoneData.valid() || !SettingUtils.inPlaceRange(this.redstoneData.pos()) || !SettingUtils.inMineRange(position));

         this.redstonePos = position;
      } else {
         var2 = Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble((i) -> {
            return pos.method_10093(i).method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
         })).toList().iterator();

         while(true) {
            do {
               do {
                  do {
                     if (!var2.hasNext()) {
                        this.redstonePos = null;
                        return;
                     }

                     direction = (Direction)var2.next();
                  } while(direction == this.pistonDir.method_10153());
               } while(direction == Direction.field_11033);

               position = pos.method_10093(direction);
            } while(!OLEPOSSUtils.replaceable(position) && BlackOut.mc.field_1687.method_8320(position).method_26204() != Blocks.field_10002);

            if (!EntityUtils.intersects(BoxUtils.get(position), (entity) -> {
               return !entity.method_7325() && entity instanceof PlayerEntity;
            })) {
               this.redstoneData = SettingUtils.getPlaceData(position, (p, d) -> {
                  return pos.equals(p);
               }, (DoublePredicate)null);
               if (this.redstoneData.valid()) {
                  this.redstonePos = position;
                  return;
               }
            }
         }
      }
   }

   private boolean upCheck(BlockPos pos) {
      double dx = BlackOut.mc.field_1724.method_33571().field_1352 - (double)pos.method_10263() - 0.5D;
      double dz = BlackOut.mc.field_1724.method_33571().field_1350 - (double)pos.method_10260() - 0.5D;
      return Math.sqrt(dx * dx + dz * dz) > Math.abs(BlackOut.mc.field_1724.method_33571().field_1351 - (double)pos.method_10264() - 0.5D);
   }

   private void resetPos() {
      this.pistonPos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.redstoneData = null;
   }

   public static enum Redstone {
      Torch(Items.field_8530, Blocks.field_10523),
      Block(Items.field_8793, Blocks.field_10002);

      public final Item i;
      public final Block b;

      private Redstone(Item i, Block b) {
         this.i = i;
         this.b = b;
      }

      // $FF: synthetic method
      private static PistonPush.Redstone[] $values() {
         return new PistonPush.Redstone[]{Torch, Block};
      }
   }
}
