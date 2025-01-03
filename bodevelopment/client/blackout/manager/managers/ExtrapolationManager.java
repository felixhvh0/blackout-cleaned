package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.functional.EpicInterface;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.module.modules.client.settings.ExtrapolationSettings;
import bodevelopment.client.blackout.randomstuff.MotionData;
import bodevelopment.client.blackout.randomstuff.timers.TickTimerList;
import bodevelopment.client.blackout.util.HorizontalExtrapolation;
import bodevelopment.client.blackout.util.MovementPrediction;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.RotationUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.SimulationContext;
import bodevelopment.client.blackout.util.Simulator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

public class ExtrapolationManager extends Manager {
   private Map<PlayerEntity, ExtrapolationManager.ExtrapolationData> dataMap = new ConcurrentHashMap();

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return false;
      });
   }

   @Event
   public void onTick(TickEvent.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         Map<PlayerEntity, ExtrapolationManager.ExtrapolationData> newMap = new ConcurrentHashMap();
         Iterator var3 = BlackOut.mc.field_1687.method_18456().iterator();

         while(var3.hasNext()) {
            PlayerEntity player = (PlayerEntity)var3.next();
            ExtrapolationManager.ExtrapolationData data = this.getFromMap(player);
            if (data != null) {
               data.update();
               newMap.put(player, data);
            } else {
               newMap.put(player, new ExtrapolationManager.ExtrapolationData(player));
            }
         }

         this.dataMap = newMap;
         this.dataMap.forEach((playerx, datax) -> {
            datax.setTicksSince(Math.min(datax.getTicksSince() + 1, 8));
            if (!(playerx instanceof OtherClientPlayerEntity)) {
               datax.handleMotion(playerx.method_19538().method_1023(playerx.field_6014, playerx.field_6036, playerx.field_5969), datax.getEntity());
            }
         });
      }
   }

   @Event
   public void onReceive(PacketEvent.Receive.Pre event) {
      Packet var3 = event.packet;
      if (var3 instanceof EntityAnimationS2CPacket) {
         EntityAnimationS2CPacket packet = (EntityAnimationS2CPacket)var3;
         if (packet.method_11267() == 0 || packet.method_11267() == 3) {
            this.dataMap.forEach((player, data) -> {
               if (packet.method_11269() == player.method_5628()) {
                  data.stopLag();
               }

            });
         }
      }

   }

   private ExtrapolationManager.ExtrapolationData getFromMap(PlayerEntity player) {
      Iterator var2 = this.dataMap.entrySet().iterator();

      Entry data;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         data = (Entry)var2.next();
      } while(data.getKey() != player);

      return (ExtrapolationManager.ExtrapolationData)data.getValue();
   }

   public void tick(PlayerEntity player, Vec3d motion) {
      ExtrapolationManager.ExtrapolationData data = this.getFromMap(player);
      if (data != null) {
         data.handleMotion(motion, data.getEntity());
      }
   }

   public Map<PlayerEntity, ExtrapolationManager.ExtrapolationData> getDataMap() {
      return this.dataMap;
   }

   public void extrapolateMap(Map<Entity, Box> old, EpicInterface<Entity, Integer> extrapolation) {
      old.clear();
      this.dataMap.forEach((player, data) -> {
         Box box = data.extrapolate(player, (Integer)extrapolation.get(player));
         old.put(player, box);
      });
   }

   public Box extrapolate(Entity entity, int ticks) {
      if (entity instanceof PlayerEntity) {
         PlayerEntity player = (PlayerEntity)entity;
         ExtrapolationManager.ExtrapolationData data = this.getFromMap(player);
         return data == null ? entity.method_5829() : data.extrapolate(player, ticks);
      } else {
         return entity.method_5829();
      }
   }

   private static MotionData getMotion(ExtrapolationManager.ExtrapolationData data) {
      return HorizontalExtrapolation.getMotion(data).y(data.motions.isEmpty() ? 0.0D : gravityMod(gravityMod(((Vec3d)data.motions.get(0)).field_1351)));
   }

   private static double gravityMod(double y) {
      return (y - 0.08D) * 0.98D;
   }

   public static class ExtrapolationData {
      public final List<Vec3d> motions = new ArrayList();
      private final List<Boolean> onGrounds = new ArrayList();
      private final TickTimerList<Double> step = new TickTimerList(false);
      private final TickTimerList<Double> reverseStep = new TickTimerList(false);
      private boolean goingToJump = false;
      private double stepHeight = 0.0D;
      private double reverseHeight = 0.0D;
      private double jumpHeight = 0.42D;
      private MotionData motionData = MotionData.of(new Vec3d(0.0D, 0.0D, 0.0D));
      private final Entity entity;
      private Vec3d prevPos;
      private int ticksSince = 0;
      private int stillFor = 0;
      private boolean prevOffGround = false;
      private boolean movedUp = false;
      private boolean movedDown = false;
      private double prevYaw = 0.0D;
      private double prevPitch = 0.0D;

      public ExtrapolationData(Entity entity) {
         this.entity = entity;
         this.motions.add(new Vec3d(0.0D, 0.0D, 0.0D));
      }

      private void update() {
         this.step.update();
         this.reverseStep.update();
         Vec3d currentPos = this.entity.method_19538();
         if (this.rotated() && !this.entity.method_24828()) {
            this.stopLag();
         } else if (this.prevPos != null && this.prevPos.method_1025(currentPos) < 0.01D) {
            ++this.stillFor;
         }

         this.prevPos = currentPos;
         this.prevYaw = (double)this.entity.method_36454();
         this.prevPitch = (double)this.entity.method_36455();
         this.onGrounds.add(0, Simulator.isOnGround(this.getEntity(), this.getEntity().method_5829()));
         OLEPOSSUtils.limitList(this.onGrounds, 3);
         boolean offGround = this.isOffGround();
         if (offGround && !this.prevOffGround) {
            this.movedDown = ((Vec3d)this.motions.get(0)).field_1351 > 0.0D;
            this.movedUp = false;
         }

         this.prevOffGround = offGround;
         this.goingToJump = offGround && this.movedUp && this.movedDown;
         this.stepHeight = 0.0D;
         this.step.forEach((height) -> {
            if (height > this.stepHeight) {
               this.stepHeight = height;
            }

         });
         this.reverseHeight = 0.0D;
         this.reverseStep.forEach((height) -> {
            if (height > this.reverseHeight) {
               this.reverseHeight = height;
            }

         });
         ExtrapolationSettings settings = ExtrapolationSettings.getInstance();
         this.stepHeight = Math.max(this.stepHeight, (Double)settings.minStep.get());
         this.reverseHeight = Math.max(this.reverseHeight, (Double)settings.minReverseStep.get());
         this.motionData = ExtrapolationManager.getMotion(this);
         if (this.motionData.reset) {
            this.motions.clear();
         }

      }

      private void stopLag() {
         this.stillFor = (Integer)ExtrapolationSettings.getInstance().maxLag.get();
      }

      private boolean isOffGround() {
         return this.onGrounds.stream().anyMatch((b) -> {
            return !b;
         });
      }

      private void handleMotion(Vec3d motion, Entity entity) {
         if (motion.field_1351 > 0.0D) {
            this.movedUp = true;
         }

         if (motion.field_1351 < 0.0D && !entity.method_24828()) {
            this.movedDown = true;
         }

         this.motionData = ExtrapolationManager.getMotion(this);
         if (!(motion.method_1027() < 1.0E-4D) || this.stillFor >= (Integer)ExtrapolationSettings.getInstance().maxLag.get() || entity == BlackOut.mc.field_1724) {
            this.stillFor = 0;
            if (this.handleMotion2(motion, entity)) {
               this.setTicksSince(0);
            } else {
               double yVel = MovementPrediction.approximateYVelocity(motion.field_1351, this.getTicksSince(), 1000);

               for(int startTicks = this.getTicksSince(); this.getTicksSince() > 0; yVel = ExtrapolationManager.gravityMod(yVel)) {
                  this.setTicksSince(this.getTicksSince() - 1);
                  this.addMotion(motion.method_38499(Axis.field_11052, yVel).method_18805((double)(1.0F / (float)startTicks), 1.0D, (double)(1.0F / (float)startTicks)));
               }

            }
         }
      }

      private boolean rotated() {
         return Math.abs(RotationUtils.yawAngle((double)this.entity.method_36454(), this.prevYaw)) > 5.0D || Math.abs((double)this.entity.method_36455() - this.prevPitch) > 5.0D;
      }

      private boolean handleMotion2(Vec3d motion, Entity entity) {
         if (motion.method_37268() > 3.0D) {
            this.addMotion(new Vec3d(0.0D, 0.0D, 0.0D));
            return true;
         } else {
            if (motion.field_1351 >= 0.45D && motion.field_1351 <= 4.0D && Simulator.isOnGround(entity, entity.method_5829())) {
               if (SettingUtils.stepPredict()) {
                  this.step.add(motion.field_1351, SettingUtils.stepTicks());
                  this.motions.clear();
                  this.addMotion(new Vec3d(motion.field_1352, 0.0D, motion.field_1350));
                  return true;
               }
            } else if (motion.field_1351 <= -0.45D && motion.field_1351 >= -6.0D && Simulator.isOnGround(entity, entity.method_5829())) {
               if (SettingUtils.reverseStepPredict()) {
                  this.reverseStep.add(-motion.field_1351, SettingUtils.reverseStepTicks());
                  this.motions.clear();
                  this.addMotion(new Vec3d(motion.field_1352, 0.0D, motion.field_1350));
                  return true;
               }
            } else if (motion.field_1351 > 0.35D && motion.field_1351 < 0.45D && !Simulator.isOnGround(entity, entity.method_5829())) {
               this.jumpHeight = motion.field_1351;
            }

            return false;
         }
      }

      public Box extrapolate(Entity entity, int ticks) {
         return this.extrapolate(entity, ticks, (Consumer)null);
      }

      public Box extrapolate(Entity entity, int ticks, Consumer<Box> consumer) {
         if (ticks == 0) {
            return entity.method_5829();
         } else {
            SimulationContext context = new SimulationContext(entity, this.getExtTicks(ticks), this.jumpHeight, this.motionData.motion, consumer, (c, i) -> {
               double prevYaw = this.motionYaw(c.motionX, c.motionZ);
               double motionLength = Math.sqrt(c.motionX * c.motionX + c.motionZ * c.motionZ);
               double yaw = Math.toRadians(prevYaw + this.motionData.yawDiff + 90.0D);
               MotionData var10000 = this.motionData;
               var10000.yawDiff *= 0.8D;
               c.motionX = Math.cos(yaw) * motionLength;
               c.motionZ = Math.sin(yaw) * motionLength;
            });
            context.jump = this.goingToJump;
            context.reverseStep = this.reverseHeight;
            context.step = this.stepHeight;
            return Simulator.extrapolate(context);
         }
      }

      private int getExtTicks(int ticks) {
         if ((Boolean)ExtrapolationSettings.getInstance().extraExtrapolation.get() && this.entity != BlackOut.mc.field_1724) {
            int extra;
            if (this.stillFor < (Integer)ExtrapolationSettings.getInstance().maxLag.get()) {
               extra = this.stillFor;
            } else {
               extra = 0;
            }

            return ticks + extra;
         } else {
            return ticks;
         }
      }

      private double motionYaw(double x, double z) {
         return MathHelper.method_15338(Math.toDegrees(Math.atan2(-x, z)));
      }

      private void addMotion(Vec3d motion) {
         if (motion.method_1033() > 0.0D && this.motions.size() > 1 && (this.collided() || this.entity.field_5976)) {
            motion = (Vec3d)this.motions.get(1);
         }

         this.motions.add(0, motion);
         OLEPOSSUtils.limitList(this.motions, 5);
      }

      private boolean collided() {
         Box box = this.entity.method_5829();
         Box newBox = new Box(this.prevPos.field_1352 - box.method_17939() / 2.0D, this.prevPos.field_1351, this.prevPos.field_1350 - box.method_17941() / 2.0D, this.prevPos.field_1352 + box.method_17939() / 2.0D, this.prevPos.field_1351 + box.method_17940(), this.prevPos.field_1350 + box.method_17941() / 2.0D);
         return !BlackOut.mc.field_1687.method_20743(this.entity, newBox.method_18804((Vec3d)this.motions.get(1))).isEmpty();
      }

      public Entity getEntity() {
         return this.entity;
      }

      public int getTicksSince() {
         return this.ticksSince;
      }

      public void setTicksSince(int ticksSince) {
         this.ticksSince = ticksSince;
      }
   }
}
