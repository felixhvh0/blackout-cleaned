package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.enums.RotationType;
import bodevelopment.client.blackout.enums.SwingHand;
import bodevelopment.client.blackout.enums.SwingState;
import bodevelopment.client.blackout.enums.SwingType;
import bodevelopment.client.blackout.enums.SwitchMode;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.functional.DoublePredicate;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.client.Notifications;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.FindResult;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.EntityUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;

public class PistonCrystal extends Module {
   private static PistonCrystal INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgDelay = this.addGroup("Delay");
   private final SettingGroup sgSwitch = this.addGroup("Switch");
   private final SettingGroup sgToggle = this.addGroup("Toggle");
   private final SettingGroup sgSwing = this.addGroup("Swing");
   private final SettingGroup sgRender = this.addGroup("Render");
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> fire;
   private final Setting<PistonCrystal.Redstone> redstone;
   private final Setting<Boolean> alwaysAttack;
   private final Setting<Double> attackSpeed;
   private final Setting<Boolean> pauseOffGround;
   private final Setting<Double> pcDelay;
   private final Setting<Double> cfDelay;
   private final Setting<Double> crDelay;
   private final Setting<Double> rmDelay;
   private final Setting<Double> raDelay;
   private final Setting<Double> mpDelay;
   private final Setting<SwitchMode> crystalSwitch;
   private final Setting<SwitchMode> pistonSwitch;
   private final Setting<SwitchMode> redstoneSwitch;
   private final Setting<SwitchMode> fireSwitch;
   private final Setting<Boolean> toggleMove;
   private final Setting<Boolean> toggleEnemyMove;
   private final Setting<Boolean> crystalSwing;
   private final Setting<SwingHand> crystalHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<Boolean> pistonSwing;
   private final Setting<SwingHand> pistonHand;
   private final Setting<Boolean> redstoneSwing;
   private final Setting<SwingHand> redstoneHand;
   private final Setting<Boolean> fireSwing;
   private final Setting<SwingHand> fireHand;
   private final Setting<Double> crystalHeight;
   private final Setting<RenderShape> crystalShape;
   private final Setting<BlackOutColor> crystalLineColor;
   private final Setting<BlackOutColor> crystalSideColor;
   private final Setting<Double> pistonHeight;
   private final Setting<RenderShape> pistonShape;
   private final Setting<BlackOutColor> pistonLineColor;
   private final Setting<BlackOutColor> pistonSideColor;
   private final Setting<Double> redstoneHeight;
   private final Setting<RenderShape> redstoneShape;
   private final Setting<BlackOutColor> redstoneLineColor;
   private final Setting<BlackOutColor> redstoneSideColor;
   private long lastAttack;
   private final TimerList<Entity> attacked;
   public BlockPos crystalPos;
   private BlockPos pistonPos;
   private BlockPos firePos;
   private BlockPos redstonePos;
   private BlockPos lastCrystalPos;
   private BlockPos lastPistonPos;
   private BlockPos lastRedstonePos;
   private Entity prevTarget;
   private Direction pistonDir;
   private PlaceData pistonData;
   private Direction crystalPlaceDir;
   private Direction crystalDir;
   private PlaceData redstoneData;
   private PlaceData fireData;
   private Entity target;
   private BlockPos closestCrystalPos;
   private BlockPos closestPistonPos;
   private BlockPos closestRedstonePos;
   private Direction closestPistonDir;
   private PlaceData closestPistonData;
   private Direction closestCrystalPlaceDir;
   private Direction closestCrystalDir;
   private PlaceData closestRedstoneData;
   private BlockPos closestFirePos;
   private PlaceData closestFireData;
   private long pistonTime;
   private long redstoneTime;
   private long mineTime;
   private long crystalTime;
   private boolean minedThisTick;
   private boolean pistonPlaced;
   private boolean redstonePlaced;
   private boolean mined;
   private boolean crystalPlaced;
   private boolean firePlaced;
   private boolean startedMining;
   private boolean prevBlocking;
   private boolean redstoneBlocking;
   private boolean entityBlocking;
   private boolean pistonBlocking;
   private double cd;
   private double d;
   private BlockPos prevPos;
   private BlockPos prevEnemyPos;
   private long prevNotification;
   public static AbstractClientPlayerEntity targetedPlayer = null;

   public PistonCrystal() {
      super("Piston Crystal", "Pushes crystals into your enemies to deal massive damage.", SubCategory.OFFENSIVE, true);
      this.pauseEat = this.sgGeneral.b("Pause Eat", false, "Pauses when eating.");
      this.fire = this.sgGeneral.b("Fire", false, "Uses fire to blow up the crystal.");
      this.redstone = this.sgGeneral.e("Redstone", PistonCrystal.Redstone.Torch, "What kind of redstone to use.");
      this.alwaysAttack = this.sgGeneral.b("Always Attack", false, "Attacks all crystals blocking crystal placing.");
      this.attackSpeed = this.sgGeneral.d("Attack Speed", 4.0D, 0.0D, 20.0D, 0.1D, "Attacks all crystals blocking crystal placing.");
      this.pauseOffGround = this.sgGeneral.b("Pause Off Ground", true, ".");
      this.pcDelay = this.sgDelay.d("Piston > Crystal", 0.0D, 0.0D, 1.0D, 0.01D, "How many seconds to wait between placing piston and crystal.");
      this.cfDelay = this.sgDelay.d("Crystal > Fire", 0.25D, 0.0D, 1.0D, 0.01D, "How many seconds to wait between placing a crystal and placing fire.");
      this.crDelay = this.sgDelay.d("Crystal > Redstone", 0.25D, 0.0D, 1.0D, 0.01D, "How many seconds to wait between placing a crystal and placing redstone.");
      this.rmDelay = this.sgDelay.d("Redstone > Mine", 0.25D, 0.0D, 1.0D, 0.01D, "How many seconds to wait between placing redstone and starting to mine.");
      this.raDelay = this.sgDelay.d("Redstone > Attack", 0.1D, 0.0D, 1.0D, 0.01D, "How many seconds to wait between placing redstone and attacking a crystal.");
      this.mpDelay = this.sgDelay.d("Mine > Piston", 0.25D, 0.0D, 1.0D, 0.01D, "How many seconds to wait after mining the redstone before starting a new cycle.");
      this.crystalSwitch = this.sgSwitch.e("Crystal Switch", SwitchMode.Normal, "Method of switching. Silent is the most reliable.");
      this.pistonSwitch = this.sgSwitch.e("Piston Switch", SwitchMode.Normal, "Method of switching. Silent is the most reliable.");
      this.redstoneSwitch = this.sgSwitch.e("Redstone Switch", SwitchMode.Normal, "Method of switching. Silent is the most reliable.");
      this.fireSwitch = this.sgSwitch.e("Fire Switch", SwitchMode.Normal, "Method of switching. Silent is the most reliable.");
      this.toggleMove = this.sgToggle.b("Toggle Move", false, "Disables when moved.");
      this.toggleEnemyMove = this.sgToggle.b("Toggle Enemy Move", false, "Disables when enemy moved.");
      this.crystalSwing = this.sgSwing.b("Crystal Swing", false, "Renders swing animation when placing a crystal.");
      SettingGroup var10001 = this.sgSwing;
      SwingHand var10003 = SwingHand.RealHand;
      Setting var10005 = this.crystalSwing;
      Objects.requireNonNull(var10005);
      this.crystalHand = var10001.e("Crystal Hand", var10003, "Which hand should be swung.", var10005::get);
      this.attackSwing = this.sgSwing.b("Attack Swing", false, "Renders swing animation when attacking a crystal.");
      var10001 = this.sgSwing;
      var10003 = SwingHand.RealHand;
      var10005 = this.attackSwing;
      Objects.requireNonNull(var10005);
      this.attackHand = var10001.e("Attack Hand", var10003, "Which hand should be swung.", var10005::get);
      this.pistonSwing = this.sgSwing.b("Piston Swing", false, "Renders swing animation when placing a piston.");
      var10001 = this.sgSwing;
      var10003 = SwingHand.RealHand;
      var10005 = this.pistonSwing;
      Objects.requireNonNull(var10005);
      this.pistonHand = var10001.e("Piston Hand", var10003, "Which hand should be swung.", var10005::get);
      this.redstoneSwing = this.sgSwing.b("Redstone Swing", false, "Renders swing animation when placing redstone.");
      var10001 = this.sgSwing;
      var10003 = SwingHand.RealHand;
      var10005 = this.redstoneSwing;
      Objects.requireNonNull(var10005);
      this.redstoneHand = var10001.e("Redstone Hand", var10003, "Which hand should be swung.", var10005::get);
      this.fireSwing = this.sgSwing.b("Fire Swing", false, "Renders swing animation when placing fire.");
      var10001 = this.sgSwing;
      var10003 = SwingHand.RealHand;
      var10005 = this.fireSwing;
      Objects.requireNonNull(var10005);
      this.fireHand = var10001.e("Fire Hand", var10003, "Which hand should be swung.", var10005::get);
      this.crystalHeight = this.sgRender.d("Crystal Height", 0.25D, -1.0D, 1.0D, 0.05D, "Height of crystal render.");
      this.crystalShape = this.sgRender.e("Crystal Shape", RenderShape.Full, "Which parts should be rendered.");
      this.crystalLineColor = this.sgRender.c("Crystal Line Color", new BlackOutColor(255, 0, 0, 255), ".");
      this.crystalSideColor = this.sgRender.c("Crystal Side Color", new BlackOutColor(255, 0, 0, 50), ".");
      this.pistonHeight = this.sgRender.d("Piston Height", 1.0D, -1.0D, 1.0D, 0.05D, "Height of crystal render.");
      this.pistonShape = this.sgRender.e("Pistonl Shape", RenderShape.Full, "Which parts should be rendered.");
      this.pistonLineColor = this.sgRender.c("Piston Line Color", new BlackOutColor(255, 255, 255, 255), ".");
      this.pistonSideColor = this.sgRender.c("Piston Side Color", new BlackOutColor(255, 255, 255, 50), ".");
      this.redstoneHeight = this.sgRender.d("Redstone Height", 1.0D, -1.0D, 1.0D, 0.05D, "Height of crystal render.");
      this.redstoneShape = this.sgRender.e("Redstone Shape", RenderShape.Full, "Which parts should be rendered.");
      this.redstoneLineColor = this.sgRender.c("Redstone Line Color", new BlackOutColor(255, 0, 0, 255), ".");
      this.redstoneSideColor = this.sgRender.c("Redstone Side Color", new BlackOutColor(255, 0, 0, 50), ".");
      this.lastAttack = 0L;
      this.attacked = new TimerList(true);
      this.crystalPos = null;
      this.pistonPos = null;
      this.firePos = null;
      this.redstonePos = null;
      this.lastCrystalPos = null;
      this.lastPistonPos = null;
      this.lastRedstonePos = null;
      this.prevTarget = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.crystalPlaceDir = null;
      this.crystalDir = null;
      this.redstoneData = null;
      this.fireData = null;
      this.target = null;
      this.closestCrystalPos = null;
      this.closestPistonPos = null;
      this.closestRedstonePos = null;
      this.closestPistonDir = null;
      this.closestPistonData = null;
      this.closestCrystalPlaceDir = null;
      this.closestCrystalDir = null;
      this.closestRedstoneData = null;
      this.closestFirePos = null;
      this.closestFireData = null;
      this.pistonTime = 0L;
      this.redstoneTime = 0L;
      this.mineTime = 0L;
      this.crystalTime = 0L;
      this.minedThisTick = false;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.crystalPlaced = false;
      this.firePlaced = false;
      this.startedMining = false;
      this.prevBlocking = false;
      this.redstoneBlocking = false;
      this.entityBlocking = false;
      this.pistonBlocking = false;
      this.prevPos = null;
      this.prevEnemyPos = null;
      this.prevNotification = 0L;
      INSTANCE = this;
   }

   public static PistonCrystal getInstance() {
      return INSTANCE;
   }

   public void onEnable() {
      this.resetPos();
      this.lastCrystalPos = null;
      this.lastPistonPos = null;
      this.lastRedstonePos = null;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.crystalPlaced = false;
      this.firePlaced = false;
      this.startedMining = false;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      this.minedThisTick = false;
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         String string = this.checkToggle();
         targetedPlayer = null;
         if (string != null) {
            this.disable(string);
         } else {
            this.updatePos();
            if (this.crystalPos != null) {
               Render3DUtils.box(this.getBox(this.crystalPos, (Double)this.crystalHeight.get()), (BlackOutColor)this.crystalSideColor.get(), (BlackOutColor)this.crystalLineColor.get(), (RenderShape)this.crystalShape.get());
               Render3DUtils.box(this.getBox(this.pistonPos, (Double)this.pistonHeight.get()), (BlackOutColor)this.pistonSideColor.get(), (BlackOutColor)this.pistonLineColor.get(), (RenderShape)this.pistonShape.get());
               Render3DUtils.box(this.getBox(this.redstonePos, (Double)this.redstoneHeight.get()), (BlackOutColor)this.redstoneSideColor.get(), (BlackOutColor)this.redstoneLineColor.get(), (RenderShape)this.redstoneShape.get());
            }

            if (this.crystalPos != null) {
               if ((double)(System.currentTimeMillis() - this.mineTime) > (Double)this.mpDelay.get() * 1000.0D && this.crystalPlaced && this.redstonePlaced && this.pistonPlaced && this.mined && (this.firePlaced || !this.canFire(false))) {
                  this.resetProgress();
               }

               if (!(Boolean)this.pauseEat.get() || !BlackOut.mc.field_1724.method_6115()) {
                  if (!(Boolean)this.pauseOffGround.get() || BlackOut.mc.field_1724.method_24828()) {
                     Entity var4 = this.target;
                     if (var4 instanceof AbstractClientPlayerEntity) {
                        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)var4;
                        targetedPlayer = player;
                     }

                     if (this.isBlocked()) {
                        this.updateAttack(true);
                        if (this.redstoneBlocking) {
                           this.mineUpdate(true);
                        }

                        if (this.pistonBlocking) {
                           this.updateCrystal(true);
                        }

                        this.prevBlocking = true;
                     } else {
                        if (this.prevBlocking) {
                           this.resetProgress();
                           this.prevBlocking = false;
                        }

                        this.updateAttack(false);
                        this.updatePiston();
                        this.updateFire();
                        this.updateCrystal(false);
                        this.updateRedstone();
                        this.mineUpdate(false);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean canFire(boolean calc) {
      return (Boolean)this.fire.get() && ((SwitchMode)this.fireSwitch.get()).find(Items.field_8884).wasFound() && (calc || this.firePos != null);
   }

   private void resetProgress() {
      this.redstonePlaced = false;
      this.pistonPlaced = false;
      this.mined = false;
      this.firePlaced = false;
      this.crystalPlaced = false;
      this.pistonTime = 0L;
      this.redstoneTime = 0L;
      this.mineTime = 0L;
      this.crystalTime = 0L;
      this.lastAttack = 0L;
   }

   private String checkToggle() {
      BlockPos currentPos = BlackOut.mc.field_1724.method_24515();
      BlockPos enemyPos = this.target == null ? null : this.target.method_24515();
      if ((Boolean)this.toggleMove.get() && !currentPos.equals(this.prevPos)) {
         return "moved";
      } else if ((Boolean)this.toggleEnemyMove.get() && enemyPos != null && !enemyPos.equals(this.prevEnemyPos)) {
         return "enemy moved";
      } else {
         this.prevPos = currentPos;
         this.prevTarget = this.target;
         this.prevEnemyPos = enemyPos;
         return null;
      }
   }

   private Box getBox(BlockPos pos, double height) {
      return new Box((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)pos.method_10264() + height, (double)(pos.method_10260() + 1));
   }

   private boolean isBlocked() {
      BlockState pistonState = BlackOut.mc.field_1687.method_8320(this.pistonPos);
      this.redstoneBlocking = false;
      this.entityBlocking = false;
      this.pistonBlocking = false;
      if (pistonState.method_26204() != Blocks.field_10560) {
         this.redstoneBlocking = BlackOut.mc.field_1687.method_8320(this.redstonePos).method_26204() == ((PistonCrystal.Redstone)this.redstone.get()).b;
         this.entityBlocking = EntityUtils.intersects(BoxUtils.get(this.pistonPos), this::validForPistonIntersect);
      } else if (pistonState.method_11654(FacingBlock.field_10927) != this.pistonDir) {
         this.pistonBlocking = true;
      }

      if (EntityUtils.intersects(BoxUtils.get(this.crystalPos), this::validForCrystalIntersect)) {
         this.entityBlocking = true;
      }

      return this.redstoneBlocking || this.entityBlocking || this.pistonBlocking;
   }

   private boolean validForPistonIntersect(Entity entity) {
      if (entity.method_7325()) {
         return false;
      } else if (entity instanceof ItemEntity) {
         return false;
      } else if (entity instanceof EndCrystalEntity) {
         return !this.attacked.contains((Object)entity);
      } else {
         return true;
      }
   }

   private boolean validForCrystalIntersect(Entity entity) {
      if (entity.method_7325()) {
         return false;
      } else if (entity.field_6012 < 10) {
         return false;
      } else if (entity instanceof EndCrystalEntity) {
         if (entity.method_24515().equals(this.crystalPos)) {
            return false;
         } else {
            return !this.attacked.contains((Object)entity);
         }
      } else {
         return true;
      }
   }

   private void mineUpdate(boolean blocked) {
      if (!blocked) {
         if ((double)(System.currentTimeMillis() - this.redstoneTime) < (Double)this.rmDelay.get() * 1000.0D) {
            return;
         }

         if (!this.redstonePlaced || this.mined) {
            return;
         }
      }

      if (!this.minedThisTick) {
         AutoMine autoMine = AutoMine.getInstance();
         if (this.redstone.get() == PistonCrystal.Redstone.Torch) {
            Direction mineDir = SettingUtils.getPlaceOnDirection(this.redstonePos);
            if (mineDir != null) {
               this.sendPacket(new PlayerActionC2SPacket(Action.field_12968, this.redstonePos, mineDir));
               this.sendPacket(new PlayerActionC2SPacket(Action.field_12973, this.redstonePos, mineDir));
            }
         } else {
            if (!autoMine.enabled) {
               if (System.currentTimeMillis() - this.prevNotification > 500L) {
                  Managers.NOTIFICATIONS.addNotification("Automine required for redstone block mode.", this.getDisplayName(), 1.0D, Notifications.Type.Info);
                  this.prevNotification = System.currentTimeMillis();
               }

               return;
            }

            if (BlackOut.mc.field_1687.method_8320(this.redstonePos).method_26204() != Blocks.field_10002 && this.mined) {
               return;
            }

            if (this.redstonePos.equals(autoMine.minePos)) {
               return;
            }

            autoMine.onStart(this.redstonePos);
         }

         if (!this.mined) {
            this.mineTime = System.currentTimeMillis();
         }

         this.mined = true;
         this.minedThisTick = true;
      }
   }

   private void updateAttack(boolean blocked) {
      if (!blocked) {
         if (!this.redstonePlaced) {
            return;
         }

         if ((double)(System.currentTimeMillis() - this.redstoneTime) < (Double)this.raDelay.get() * 1000.0D) {
            return;
         }
      }

      EndCrystalEntity crystal = null;
      double cd = 10000.0D;
      Iterator var5 = BlackOut.mc.field_1687.method_18112().iterator();

      while(true) {
         EndCrystalEntity c;
         do {
            do {
               do {
                  Entity entity;
                  do {
                     if (!var5.hasNext()) {
                        if (crystal == null) {
                           return;
                        }

                        if (SettingUtils.shouldRotate(RotationType.Attacking) && !this.attackRotate(crystal.method_5829(), 0.1D, "attacking")) {
                           return;
                        }

                        if ((double)(System.currentTimeMillis() - this.lastAttack) < 1000.0D / (Double)this.attackSpeed.get()) {
                           return;
                        }

                        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.field_5808);
                        this.sendPacket(PlayerInteractEntityC2SPacket.method_34206(crystal, BlackOut.mc.field_1724.method_5715()));
                        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.field_5808);
                        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                           this.end("attacking");
                        }

                        if ((Boolean)this.attackSwing.get()) {
                           this.clientSwing((SwingHand)this.attackHand.get(), Hand.field_5808);
                        }

                        this.lastAttack = System.currentTimeMillis();
                        this.attacked.add(crystal, 0.25D);
                        return;
                     }

                     entity = (Entity)var5.next();
                  } while(!(entity instanceof EndCrystalEntity));

                  c = (EndCrystalEntity)entity;
               } while(!blocked && c.method_23317() == (double)this.crystalPos.method_10263() + 0.5D && c.method_23321() == (double)this.crystalPos.method_10260() + 0.5D);
            } while(!(Boolean)this.alwaysAttack.get() && !blocked && c.method_23317() - (double)c.method_31477() == 0.5D && c.method_23321() - (double)c.method_31479() == 0.5D);
         } while(!c.method_5829().method_994(BoxUtils.crystalSpawnBox(this.crystalPos)) && (!blocked || !c.method_5829().method_994(BoxUtils.get(this.pistonPos))));

         double d = BlackOut.mc.field_1724.method_33571().method_1022(c.method_19538());
         if (d < cd) {
            cd = d;
            crystal = c;
         }
      }
   }

   private void updatePiston() {
      if (!this.pistonPlaced) {
         if (this.pistonData != null) {
            Hand hand = OLEPOSSUtils.getHand(Items.field_8249);
            boolean available = hand != null;
            FindResult result = ((SwitchMode)this.pistonSwitch.get()).find(Items.field_8249);
            if (!available) {
               available = result.wasFound();
            }

            if (available) {
               if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || this.rotateBlock(this.pistonData, RotationType.BlockPlace, "piston")) {
                  boolean switched = false;
                  if (hand != null || (switched = ((SwitchMode)this.pistonSwitch.get()).swap(result.slot()))) {
                     this.sendPacket(new LookAndOnGround(this.pistonDir.method_10153().method_10144(), Managers.ROTATION.nextPitch, Managers.PACKET.isOnGround()));
                     this.placeBlock(hand, this.pistonData.pos().method_46558(), this.pistonData.dir(), this.pistonData.pos());
                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        this.end("piston");
                     }

                     if ((Boolean)this.pistonSwing.get()) {
                        this.clientSwing((SwingHand)this.pistonHand.get(), hand);
                     }

                     this.pistonTime = System.currentTimeMillis();
                     this.pistonPlaced = true;
                     if (switched) {
                        ((SwitchMode)this.pistonSwitch.get()).swapBack();
                     }

                  }
               }
            }
         }
      }
   }

   private void updateCrystal(boolean blocked) {
      if (blocked || this.pistonPlaced && !this.crystalPlaced) {
         if (!((double)(System.currentTimeMillis() - this.pistonTime) < (Double)this.pcDelay.get() * 1000.0D)) {
            if (this.crystalPlaceDir != null) {
               if (!EntityUtils.intersects(BoxUtils.get(this.crystalPos), (entity) -> {
                  if (entity.method_7325()) {
                     return false;
                  } else if (entity instanceof EndCrystalEntity) {
                     if (entity.method_24515().equals(this.crystalPos)) {
                        return false;
                     } else {
                        return !this.attacked.contains((Object)entity);
                     }
                  } else {
                     return true;
                  }
               })) {
                  Hand hand = OLEPOSSUtils.getHand(Items.field_8301);
                  boolean available = hand != null;
                  FindResult result = ((SwitchMode)this.crystalSwitch.get()).find(Items.field_8301);
                  if (!available) {
                     available = result.wasFound();
                  }

                  if (available) {
                     if (!SettingUtils.shouldRotate(RotationType.Interact) || this.rotateBlock(this.crystalPos.method_10074(), this.crystalPlaceDir, RotationType.Interact, "crystal")) {
                        boolean switched = false;
                        if (hand != null || (switched = ((SwitchMode)this.crystalSwitch.get()).swap(result.slot()))) {
                           hand = hand == null ? Hand.field_5808 : hand;
                           this.interactBlock(hand, this.crystalPos.method_10074().method_46558(), this.crystalPlaceDir, this.crystalPos.method_10074());
                           if (SettingUtils.shouldRotate(RotationType.Interact)) {
                              this.end("crystal");
                           }

                           if ((Boolean)this.crystalSwing.get()) {
                              this.clientSwing((SwingHand)this.crystalHand.get(), hand);
                           }

                           this.crystalTime = System.currentTimeMillis();
                           this.crystalPlaced = true;
                           if (switched) {
                              ((SwitchMode)this.crystalSwitch.get()).swapBack();
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void updateRedstone() {
      if (this.crystalPlaced && !this.redstonePlaced) {
         if (!((double)(System.currentTimeMillis() - this.crystalTime) < (Double)this.crDelay.get() * 1000.0D)) {
            if (this.redstoneData != null) {
               Hand hand = OLEPOSSUtils.getHand(((PistonCrystal.Redstone)this.redstone.get()).i);
               boolean available = hand != null;
               FindResult result = ((SwitchMode)this.redstoneSwitch.get()).find(((PistonCrystal.Redstone)this.redstone.get()).i);
               if (!available) {
                  available = result.wasFound();
               }

               if (available) {
                  if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || this.rotateBlock(this.redstoneData, RotationType.BlockPlace, "redstone")) {
                     boolean switched = false;
                     if (hand != null || (switched = ((SwitchMode)this.redstoneSwitch.get()).swap(result.slot()))) {
                        this.placeBlock(hand, this.redstoneData.pos().method_46558(), this.redstoneData.dir(), this.redstoneData.pos());
                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                           this.end("redstone");
                        }

                        if ((Boolean)this.redstoneSwing.get()) {
                           this.clientSwing((SwingHand)this.redstoneHand.get(), hand);
                        }

                        this.redstoneTime = System.currentTimeMillis();
                        this.redstonePlaced = true;
                        if (switched) {
                           ((SwitchMode)this.redstoneSwitch.get()).swapBack();
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void updateFire() {
      if (this.canFire(true)) {
         if (this.crystalPlaced && !this.firePlaced) {
            if (!((double)(System.currentTimeMillis() - this.crystalTime) < (Double)this.cfDelay.get() * 1000.0D)) {
               if (this.firePos == null) {
                  this.firePlaced = true;
               } else {
                  Hand hand = OLEPOSSUtils.getHand(Items.field_8884);
                  FindResult result = ((SwitchMode)this.fireSwitch.get()).find(Items.field_8884);
                  if (hand != null || result.wasFound()) {
                     if (!SettingUtils.shouldRotate(RotationType.Interact) || this.rotateBlock(this.fireData, RotationType.Interact, "fire")) {
                        boolean switched = false;
                        if (hand != null || (switched = ((SwitchMode)this.fireSwitch.get()).swap(result.slot()))) {
                           this.interactBlock(hand, this.fireData.pos().method_46558(), this.fireData.dir(), this.fireData.pos());
                           if (SettingUtils.shouldRotate(RotationType.Interact)) {
                              this.end("fire");
                           }

                           if ((Boolean)this.fireSwing.get()) {
                              this.clientSwing((SwingHand)this.fireHand.get(), hand);
                           }

                           this.firePlaced = true;
                           if (switched) {
                              ((SwitchMode)this.fireSwitch.get()).swapBack();
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean getFirePos(BlockPos posC, BlockPos posP, BlockPos posR, Direction dirC, Direction dirP) {
      BlockPos bestPos = null;
      PlaceData bestData = null;
      double closestDistance = 0.0D;

      for(int x = dirC.method_10153().method_10148() == 0 ? -1 : Math.min(0, dirC.method_10148()); x <= (dirC.method_10153().method_10148() == 0 ? 1 : Math.max(0, dirC.method_10153().method_10148())); ++x) {
         for(int y = 0; y <= 1; ++y) {
            for(int z = dirC.method_10153().method_10165() == 0 ? -1 : Math.min(0, dirC.method_10165()); z <= (dirC.method_10153().method_10165() == 0 ? 1 : Math.max(0, dirC.method_10153().method_10165())); ++z) {
               BlockPos pos = posC.method_10093(dirC.method_10153()).method_10069(x, y, z);
               if (!pos.equals(posC) && !pos.equals(posP) && !pos.equals(posR) && !pos.equals(posP.method_10093(dirP.method_10153()))) {
                  if (BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof FireBlock) {
                     PlaceData data = SettingUtils.getPlaceData(pos);
                     if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
                        this.fireData = SettingUtils.getPlaceData(pos);
                        this.firePos = pos;
                        return true;
                     }
                  }

                  double d = pos.method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
                  if ((bestPos == null || !(d > closestDistance)) && OLEPOSSUtils.solid(pos.method_10074()) && BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof AirBlock) {
                     PlaceData da = SettingUtils.getPlaceData(pos);
                     if (da.valid() && SettingUtils.inPlaceRange(da.pos())) {
                        closestDistance = d;
                        bestPos = pos;
                        bestData = da;
                     }
                  }
               }
            }
         }
      }

      this.firePos = bestPos;
      this.fireData = bestData;
      return this.firePos != null;
   }

   private void updatePos() {
      this.lastCrystalPos = this.crystalPos;
      this.lastPistonPos = this.pistonPos;
      this.lastRedstonePos = this.redstonePos;
      this.closestCrystalPos = null;
      this.closestPistonPos = null;
      this.closestRedstonePos = null;
      this.closestPistonDir = null;
      this.closestPistonData = null;
      this.closestCrystalPlaceDir = null;
      this.closestCrystalDir = null;
      this.closestRedstoneData = null;
      this.resetPos();
      BlackOut.mc.field_1687.method_18456().stream().filter((player) -> {
         return player != BlackOut.mc.field_1724 && player.method_19538().method_1022(BlackOut.mc.field_1724.method_19538()) < 10.0D && player.method_6032() > 0.0F && !Managers.FRIENDS.isFriend(player) && !player.method_7325();
      }).sorted(Comparator.comparingDouble((i) -> {
         return i.method_19538().method_1022(BlackOut.mc.field_1724.method_19538());
      })).forEach((player) -> {
         if (this.crystalPos == null) {
            this.update(player, true);
            if (this.crystalPos != null) {
               return;
            }

            this.update(player, false);
         }

      });
   }

   private void update(PlayerEntity player, boolean top) {
      this.cd = 10000.0D;
      Iterator var3 = Type.field_11062.iterator();

      while(var3.hasNext()) {
         Direction dir = (Direction)var3.next();
         this.resetPos();
         BlockPos cPos = top ? BlockPos.method_49638(player.method_33571()).method_10093(dir).method_10084() : BlockPos.method_49638(player.method_33571()).method_10093(dir);
         this.d = cPos.method_46558().method_1022(BlackOut.mc.field_1724.method_19538());
         if (cPos.equals(this.lastCrystalPos) || !(this.d > this.cd)) {
            Block b = BlackOut.mc.field_1687.method_8320(cPos).method_26204();
            if (b instanceof AirBlock || b == Blocks.field_10379 || b == Blocks.field_10008) {
               b = BlackOut.mc.field_1687.method_8320(cPos.method_10084()).method_26204();
               if ((!SettingUtils.oldCrystals() || b instanceof AirBlock || b == Blocks.field_10379 || b == Blocks.field_10008) && (BlackOut.mc.field_1687.method_8320(cPos.method_10074()).method_26204() == Blocks.field_10540 || BlackOut.mc.field_1687.method_8320(cPos.method_10074()).method_26204() == Blocks.field_9987) && !EntityUtils.intersects(BoxUtils.crystalSpawnBox(cPos), (entity) -> {
                  return !entity.method_7325() && entity instanceof PlayerEntity;
               }) && SettingUtils.inInteractRange(cPos)) {
                  Direction cDir = SettingUtils.getPlaceOnDirection(cPos);
                  if (cDir != null) {
                     this.getPistonPos(cPos, dir);
                     if (this.pistonPos != null && (!this.canFire(true) || this.getFirePos(cPos, this.pistonPos, this.redstonePos, dir, this.pistonDir))) {
                        this.cd = this.d;
                        this.crystalPos = cPos;
                        this.crystalPlaceDir = cDir;
                        this.crystalDir = dir;
                        this.closestCrystalPos = this.crystalPos;
                        this.closestPistonPos = this.pistonPos;
                        this.closestRedstonePos = this.redstonePos;
                        this.closestPistonDir = this.pistonDir;
                        this.closestPistonData = this.pistonData;
                        this.closestCrystalPlaceDir = this.crystalPlaceDir;
                        this.closestCrystalDir = this.crystalDir;
                        this.closestRedstoneData = this.redstoneData;
                        this.closestFirePos = this.firePos;
                        this.closestFireData = this.fireData;
                        if (this.crystalPos.equals(this.lastCrystalPos)) {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      this.crystalPos = this.closestCrystalPos;
      this.pistonPos = this.closestPistonPos;
      this.redstonePos = this.closestRedstonePos;
      this.pistonDir = this.closestPistonDir;
      this.pistonData = this.closestPistonData;
      this.crystalPlaceDir = this.closestCrystalPlaceDir;
      this.crystalDir = this.closestCrystalDir;
      this.redstoneData = this.closestRedstoneData;
      this.firePos = this.closestFirePos;
      this.fireData = this.closestFireData;
      this.target = player;
   }

   private void getPistonPos(BlockPos pos, Direction dir) {
      List<BlockPos> pistonBlocks = this.pistonBlocks(pos, dir);
      this.cd = 10000.0D;
      BlockPos cPos = null;
      PlaceData cData = null;
      Direction cDir = null;
      BlockPos cRedstonePos = null;
      PlaceData cRedstoneData = null;
      Iterator var9 = pistonBlocks.iterator();

      while(var9.hasNext()) {
         BlockPos position = (BlockPos)var9.next();
         this.d = BlackOut.mc.field_1724.method_33571().method_1022(position.method_46558());
         if (position.equals(this.lastPistonPos) || !(this.cd < this.d)) {
            PlaceData placeData = SettingUtils.getPlaceData(position, (DoublePredicate)null, (p, d) -> {
               return !this.isRedstone(p) && !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof PistonBlock) && !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof PistonHeadBlock) && !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof PistonExtensionBlock) && BlackOut.mc.field_1687.method_8320(p).method_26204() != Blocks.field_10008 && !(BlackOut.mc.field_1687.method_8320(p).method_26204() instanceof FireBlock);
            });
            if (placeData.valid() && SettingUtils.inPlaceRange(placeData.pos())) {
               this.redstonePos(position, dir.method_10153(), pos);
               if (this.redstonePos != null) {
                  this.cd = this.d;
                  cRedstonePos = this.redstonePos;
                  cRedstoneData = this.redstoneData;
                  cPos = position;
                  cDir = dir.method_10153();
                  cData = placeData;
                  if (position.equals(this.lastPistonPos)) {
                     break;
                  }
               }
            }
         }
      }

      this.pistonPos = cPos;
      this.pistonDir = cDir;
      this.pistonData = cData;
      this.redstonePos = cRedstonePos;
      this.redstoneData = cRedstoneData;
   }

   private List<BlockPos> pistonBlocks(BlockPos pos, Direction dir) {
      List<BlockPos> blocks = new ArrayList();

      for(int x = dir.method_10148() == 0 ? -1 : dir.method_10148(); x <= (dir.method_10148() == 0 ? 1 : dir.method_10148()); ++x) {
         for(int z = dir.method_10165() == 0 ? -1 : dir.method_10165(); z <= (dir.method_10165() == 0 ? 1 : dir.method_10165()); ++z) {
            for(int y = 0; y <= 1; ++y) {
               if ((x != 0 || y != 0 || z != 0) && (!SettingUtils.oldCrystals() || x != 0 || y != 1 || z != 0) && this.upCheck(pos.method_10069(x, y, z))) {
                  blocks.add(pos.method_10069(x, y, z));
               }
            }
         }
      }

      return blocks.stream().filter((b) -> {
         if (this.blocked(b.method_10093(dir.method_10153()))) {
            return false;
         } else if (EntityUtils.intersects(BoxUtils.get(b), (entity) -> {
            return !entity.method_7325() && entity instanceof PlayerEntity;
         })) {
            return false;
         } else {
            return !(BlackOut.mc.field_1687.method_8320(b).method_26204() instanceof PistonBlock) && BlackOut.mc.field_1687.method_8320(b).method_26204() != Blocks.field_10008 && !(BlackOut.mc.field_1687.method_8320(b).method_26204() instanceof FireBlock) ? OLEPOSSUtils.replaceable(b) : true;
         }
      }).toList();
   }

   private void redstonePos(BlockPos pos, Direction pDir, BlockPos cPos) {
      this.cd = 10000.0D;
      this.redstonePos = null;
      BlockPos cRedstonePos = null;
      PlaceData cRedstoneData = null;
      Direction[] var6;
      int var7;
      int var8;
      Direction direction;
      BlockPos position;
      if (this.redstone.get() == PistonCrystal.Redstone.Torch) {
         var6 = Direction.values();
         var7 = var6.length;

         for(var8 = 0; var8 < var7; ++var8) {
            direction = var6[var8];
            if (direction != pDir && direction != Direction.field_11033) {
               position = pos.method_10093(direction);
               this.d = position.method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
               if ((position.equals(this.lastPistonPos) || !(this.cd < this.d)) && !position.equals(cPos) && (!SettingUtils.oldCrystals() || !position.equals(cPos.method_10084())) && (OLEPOSSUtils.replaceable(position) || BlackOut.mc.field_1687.method_8320(position).method_26204() instanceof RedstoneTorchBlock || BlackOut.mc.field_1687.method_8320(position).method_26204() instanceof FireBlock)) {
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
                  if (this.redstoneData.valid() && SettingUtils.inPlaceRange(this.redstoneData.pos()) && SettingUtils.inMineRange(position)) {
                     this.cd = this.d;
                     cRedstonePos = position;
                     cRedstoneData = this.redstoneData;
                     if (position.equals(this.lastRedstonePos)) {
                        break;
                     }
                  }
               }
            }
         }

         this.redstonePos = cRedstonePos;
         this.redstoneData = cRedstoneData;
      } else {
         var6 = Direction.values();
         var7 = var6.length;

         for(var8 = 0; var8 < var7; ++var8) {
            direction = var6[var8];
            if (direction != pDir) {
               position = pos.method_10093(direction);
               this.d = position.method_46558().method_1022(BlackOut.mc.field_1724.method_33571());
               if ((position.equals(this.lastPistonPos) || !(this.cd < this.d)) && !position.equals(cPos) && (OLEPOSSUtils.replaceable(position) || BlackOut.mc.field_1687.method_8320(position).method_26204() == Blocks.field_10002) && !BoxUtils.get(position).method_994(OLEPOSSUtils.getCrystalBox(cPos)) && !EntityUtils.intersects(BoxUtils.get(position), (entity) -> {
                  return !entity.method_7325() && entity instanceof PlayerEntity;
               })) {
                  this.redstoneData = SettingUtils.getPlaceData(position, (p, d) -> {
                     return pos.equals(p);
                  }, (DoublePredicate)null);
                  if (this.redstoneData.valid()) {
                     this.cd = this.d;
                     cRedstonePos = position;
                     cRedstoneData = this.redstoneData;
                     if (position.equals(this.lastRedstonePos)) {
                        break;
                     }
                  }
               }
            }
         }

         this.redstonePos = cRedstonePos;
         this.redstoneData = cRedstoneData;
      }
   }

   private boolean upCheck(BlockPos pos) {
      double dx = BlackOut.mc.field_1724.method_33571().field_1352 - (double)pos.method_10263() - 0.5D;
      double dz = BlackOut.mc.field_1724.method_33571().field_1350 - (double)pos.method_10260() - 0.5D;
      return Math.sqrt(dx * dx + dz * dz) > Math.abs(BlackOut.mc.field_1724.method_33571().field_1351 - (double)pos.method_10264() - 0.5D);
   }

   private boolean isRedstone(BlockPos pos) {
      return BlackOut.mc.field_1687.method_8320(pos).method_26219();
   }

   private boolean blocked(BlockPos pos) {
      Block b = BlackOut.mc.field_1687.method_8320(pos).method_26204();
      if (b == Blocks.field_10008) {
         return false;
      } else if (b == Blocks.field_10379) {
         return false;
      } else if (b == Blocks.field_10523) {
         return false;
      } else if (b instanceof FireBlock) {
         return false;
      } else {
         return !(BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof AirBlock);
      }
   }

   private void resetPos() {
      this.crystalPos = null;
      this.pistonPos = null;
      this.firePos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.crystalPlaceDir = null;
      this.crystalDir = null;
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
      private static PistonCrystal.Redstone[] $values() {
         return new PistonCrystal.Redstone[]{Torch, Block};
      }
   }
}
