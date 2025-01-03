package bodevelopment.client.blackout.module.modules.movement;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.mixin.IEntityVelocityUpdateS2CPacket;
import bodevelopment.client.blackout.interfaces.mixin.IExplosionS2CPacket;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.FakePlayerEntity;
import bodevelopment.client.blackout.randomstuff.timers.TickTimerList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {
   private static Velocity INSTANCE;
   private final SettingGroup sgKnockback = this.addGroup("Knockback");
   private final SettingGroup sgPush = this.addGroup("Push");
   public final Setting<Velocity.Mode> mode;
   public final Setting<Double> horizontal;
   public final Setting<Double> vertical;
   public final Setting<Double> hChance;
   public final Setting<Double> vChance;
   public final Setting<Double> chance;
   private final Setting<Boolean> single;
   private final Setting<Integer> minDelay;
   private final Setting<Integer> maxDelay;
   private final Setting<Boolean> delayExplosion;
   private final Setting<Boolean> explosions;
   public final Setting<Boolean> fishingHook;
   public final Setting<Velocity.PushMode> entityPush;
   public final Setting<Double> acceleration;
   public final Setting<Boolean> blockPush;
   public boolean grim;
   private final TickTimerList<Pair<Vec3d, Boolean>> delayed;

   public Velocity() {
      super("Velocity", "Modifies knockback taken.", SubCategory.MOVEMENT, true);
      this.mode = this.sgKnockback.e("Mode", Velocity.Mode.Simple, ".");
      this.horizontal = this.sgKnockback.d("Horizontal", 0.0D, 0.0D, 1.0D, 0.01D, "Multiplier for horizontal knockback.", () -> {
         return this.mode.get() == Velocity.Mode.Simple;
      });
      this.vertical = this.sgKnockback.d("Vertical", 0.0D, 0.0D, 1.0D, 0.01D, "Multiplier for vertical knockback.", () -> {
         return this.mode.get() == Velocity.Mode.Simple;
      });
      this.hChance = this.sgKnockback.d("Horizontal Chance", 1.0D, 0.0D, 1.0D, 0.01D, "Chance for horizontal knockback.", () -> {
         return this.mode.get() == Velocity.Mode.Simple;
      });
      this.vChance = this.sgKnockback.d("Vertical Chance", 1.0D, 0.0D, 1.0D, 0.01D, "Chance for vertical knockback.", () -> {
         return this.mode.get() == Velocity.Mode.Simple;
      });
      this.chance = this.sgKnockback.d("Chance", 1.0D, 0.0D, 1.0D, 0.01D, "Chance for knockback.", () -> {
         return this.mode.get() == Velocity.Mode.Grim;
      });
      this.single = this.sgKnockback.b("Single", true, ".", () -> {
         return this.mode.get() == Velocity.Mode.Grim;
      });
      this.minDelay = this.sgKnockback.i("Min Delay", 0, 0, 20, 1, ".", () -> {
         return this.mode.get() == Velocity.Mode.Delayed;
      });
      this.maxDelay = this.sgKnockback.i("Max Delay", 10, 0, 20, 1, ".", () -> {
         return this.mode.get() == Velocity.Mode.Delayed;
      });
      this.delayExplosion = this.sgKnockback.b("Delay Explosion", false, ".", () -> {
         return this.mode.get() == Velocity.Mode.Delayed;
      });
      this.explosions = this.sgKnockback.b("Explosions", true, ".");
      this.fishingHook = this.sgKnockback.b("Fishing Hook", true, ".");
      this.entityPush = this.sgPush.e("Entity Push", Velocity.PushMode.Ignore, "Prevents you from being pushed by entities.");
      this.acceleration = this.sgPush.d("Acceleration", 1.0D, 0.0D, 2.0D, 0.02D, ".", () -> {
         return this.entityPush.get() == Velocity.PushMode.Accelerate;
      });
      this.blockPush = this.sgPush.b("Block Push", true, "Prevents you from being pushed by blocks.");
      this.grim = false;
      this.delayed = new TickTimerList(false);
      INSTANCE = this;
   }

   public static Velocity getInstance() {
      return INSTANCE;
   }

   public String getInfo() {
      return ((Velocity.Mode)this.mode.get()).name();
   }

   @Event
   public void onTickPre(MoveEvent.Pre event) {
      if (this.enabled) {
         if (this.grim && (Boolean)this.single.get()) {
            this.sendGrimPackets();
            this.grim = false;
         }

         this.delayed.timers.removeIf((item) -> {
            if (item.ticks-- <= 0) {
               if ((Boolean)((Pair)item.value).method_15441()) {
                  BlackOut.mc.field_1724.method_45319((Vec3d)((Pair)item.value).method_15442());
               } else {
                  BlackOut.mc.field_1724.method_5750(((Vec3d)((Pair)item.value).method_15442()).field_1352, ((Vec3d)((Pair)item.value).method_15442()).field_1351, ((Vec3d)((Pair)item.value).method_15442()).field_1350);
               }

               return true;
            } else {
               return false;
            }
         });
      }
   }

   @Event
   public void onVelocity(PacketEvent.Receive.Post event) {
      Packet var3 = event.packet;
      if (var3 instanceof EntityVelocityUpdateS2CPacket) {
         EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket)var3;
         if (BlackOut.mc.field_1724 == null || BlackOut.mc.field_1724.method_5628() != packet.method_11818()) {
            return;
         }

         switch((Velocity.Mode)this.mode.get()) {
         case Simple:
            int x = (int)((double)packet.method_11815() - BlackOut.mc.field_1724.method_18798().field_1352 * 8000.0D);
            int y = (int)((double)packet.method_11816() - BlackOut.mc.field_1724.method_18798().field_1351 * 8000.0D);
            int z = (int)((double)packet.method_11819() - BlackOut.mc.field_1724.method_18798().field_1350 * 8000.0D);
            double random = Math.random();
            if ((Double)this.hChance.get() >= random) {
               ((IEntityVelocityUpdateS2CPacket)packet).blackout_Client$setX((int)((double)x * (Double)this.horizontal.get() + BlackOut.mc.field_1724.method_18798().field_1352 * 8000.0D));
               ((IEntityVelocityUpdateS2CPacket)packet).blackout_Client$setZ((int)((double)z * (Double)this.horizontal.get() + BlackOut.mc.field_1724.method_18798().field_1350 * 8000.0D));
            }

            if ((Double)this.vChance.get() >= random) {
               ((IEntityVelocityUpdateS2CPacket)packet).blackout_Client$setY((int)((double)y * (Double)this.vertical.get() + BlackOut.mc.field_1724.method_18798().field_1351 * 8000.0D));
            }
            break;
         case Delayed:
            this.delayed.add(new Pair(new Vec3d((double)packet.method_11815() / 8000.0D, (double)packet.method_11816() / 8000.0D, (double)packet.method_11819() / 8000.0D), false), this.getDelay());
            event.setCancelled(true);
            break;
         case Grim:
            if ((Double)this.chance.get() >= Math.random()) {
               this.grimCancel(event, false);
            }
         }
      }

      var3 = event.packet;
      if (var3 instanceof ExplosionS2CPacket) {
         ExplosionS2CPacket packet = (ExplosionS2CPacket)var3;
         if ((Boolean)this.explosions.get()) {
            switch((Velocity.Mode)this.mode.get()) {
            case Simple:
               if ((Double)this.hChance.get() >= Math.random()) {
                  ((IExplosionS2CPacket)packet).blackout_Client$multiplyXZ(((Double)this.horizontal.get()).floatValue());
               }

               if ((Double)this.vChance.get() >= Math.random()) {
                  ((IExplosionS2CPacket)packet).blackout_Client$multiplyY(((Double)this.vertical.get()).floatValue());
               }
               break;
            case Delayed:
               if ((Boolean)this.delayExplosion.get()) {
                  this.delayed.add(new Pair(new Vec3d((double)packet.method_11472(), (double)packet.method_11473(), (double)packet.method_11474()), true), this.getDelay());
                  event.setCancelled(true);
               }
               break;
            case Grim:
               if ((Double)this.chance.get() >= Math.random()) {
                  this.grimCancel(event, true);
               }
            }
         }
      }

   }

   @Event
   public void onTickPre(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (this.entityPush.get() == Velocity.PushMode.Accelerate) {
            if (Managers.ROTATION.move) {
               BlackOut.mc.field_1687.method_8333(BlackOut.mc.field_1724, BlackOut.mc.field_1724.method_5829().method_1014(1.0D), (entity) -> {
                  return this.validForCollisions(entity) && !BlackOut.mc.field_1724.method_5794(entity);
               }).forEach((entity) -> {
                  double distX = entity.method_23317() - BlackOut.mc.field_1724.method_23317();
                  double distZ = entity.method_23321() - BlackOut.mc.field_1724.method_23321();
                  double maxDist = MathHelper.method_15391(distX, distZ);
                  if (!(maxDist < 0.009999999776482582D)) {
                     maxDist = Math.sqrt(maxDist);
                     distX /= maxDist;
                     distZ /= maxDist;
                     double d = Math.min(1.0D / maxDist, 1.0D);
                     distX *= d;
                     distZ *= d;
                     double speed = Math.sqrt(distX * distX + distZ * distZ) * (Double)this.acceleration.get() * 0.05D;
                     double yaw = Math.toRadians((double)(Managers.ROTATION.moveYaw + 90.0F));
                     BlackOut.mc.field_1724.method_5762(Math.cos(yaw) * speed, 0.0D, Math.sin(yaw) * speed);
                  }
               });
            }
         }
      }
   }

   private void sendGrimPackets() {
      Vec3d vec = Managers.PACKET.pos;
      Managers.PACKET.sendInstantly(new Full(vec.method_10216(), vec.method_10214(), vec.method_10215(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
      BlockPos pos = new BlockPos((int)Math.floor(vec.field_1352), (int)Math.floor(vec.field_1351) + 1, (int)Math.floor(vec.field_1350));
      Managers.PACKET.sendInstantly(new PlayerActionC2SPacket(Action.field_12973, pos, Direction.field_11033, 0));
   }

   private void grimCancel(PacketEvent.Receive.Post event, boolean explosion) {
      if (!(Boolean)this.single.get()) {
         this.sendGrimPackets();
         event.setCancelled(true);
      } else if (!this.grim) {
         if (!explosion) {
            this.grim = true;
         }

         event.setCancelled(true);
      }

   }

   private boolean validForCollisions(Entity entity) {
      if (entity instanceof FakePlayerEntity) {
         return false;
      } else if (entity instanceof BoatEntity) {
         return true;
      } else if (entity instanceof MinecartEntity) {
         return true;
      } else if (!(entity instanceof LivingEntity)) {
         return false;
      } else {
         return !(entity instanceof ArmorStandEntity);
      }
   }

   private int getDelay() {
      return MathHelper.method_48781((float)Math.random(), (Integer)this.minDelay.get(), (Integer)this.maxDelay.get());
   }

   public static enum Mode {
      Simple,
      Delayed,
      Grim;

      // $FF: synthetic method
      private static Velocity.Mode[] $values() {
         return new Velocity.Mode[]{Simple, Delayed, Grim};
      }
   }

   public static enum PushMode {
      Accelerate,
      Ignore,
      Disabled;

      // $FF: synthetic method
      private static Velocity.PushMode[] $values() {
         return new Velocity.PushMode[]{Accelerate, Ignore, Disabled};
      }
   }
}
