package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.EntityAddEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.PopEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.util.HoleUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class StatsManager extends Manager {
   private final Map<UUID, StatsManager.TrackerMap> dataMap = new Object2ObjectOpenHashMap();

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return BlackOut.mc.field_1724 == null || BlackOut.mc.field_1687 == null;
      });
   }

   @Event
   public void onTickPost(TickEvent.Post event) {
      BlackOut.mc.field_1687.method_18456().forEach((player) -> {
         UUID uuid = player.method_7334().getId();
         if (!this.dataMap.containsKey(uuid)) {
            this.dataMap.put(uuid, new StatsManager.TrackerMap());
         }

         StatsManager.TrackerMap map = (StatsManager.TrackerMap)this.dataMap.get(uuid);
         if (!map.is(player)) {
            map.set(player);
         }

      });
      this.dataMap.forEach((uuid, map) -> {
         map.tick();
      });
   }

   @Event
   public void onSpawn(EntityAddEvent.Pre event) {
      Entity var3 = event.entity;
      if (var3 instanceof ExperienceBottleEntity) {
         ExperienceBottleEntity bottle = (ExperienceBottleEntity)var3;
         AbstractClientPlayerEntity player = this.getOwner(bottle);
         StatsManager.TrackerData data = this.getStats(player);
         if (data != null) {
            ++data.bottles;
         }

      }
   }

   @Event
   public void onReceive(PacketEvent.Receive.Pre event) {
      Packet var4 = event.packet;
      AbstractClientPlayerEntity player;
      Entity var8;
      StatsManager.TrackerData data;
      if (var4 instanceof EntityStatusS2CPacket) {
         EntityStatusS2CPacket packet = (EntityStatusS2CPacket)var4;
         if (packet.method_11470() == 35) {
            var8 = packet.method_11469(BlackOut.mc.field_1687);
            if (var8 instanceof AbstractClientPlayerEntity) {
               player = (AbstractClientPlayerEntity)var8;
               data = this.getStats(player);
               if (data != null) {
                  data.onPop(player);
               }
            }
         }
      }

      Packet var7 = event.packet;
      if (var7 instanceof PlaySoundS2CPacket) {
         PlaySoundS2CPacket packet = (PlaySoundS2CPacket)var7;
         if (SoundEvents.field_19149.equals(packet.method_11894().comp_349())) {
            player = this.getClosest((playerx) -> {
               return playerx.method_19538().method_1023(packet.method_11890(), packet.method_11889(), packet.method_11893()).method_1027();
            });
            data = this.getStats(player);
            if (data != null) {
               ++data.eaten;
            }
         }
      }

      var4 = event.packet;
      if (var4 instanceof EntityEquipmentUpdateS2CPacket) {
         EntityEquipmentUpdateS2CPacket packet = (EntityEquipmentUpdateS2CPacket)var4;
         var8 = BlackOut.mc.field_1687.method_8469(packet.method_11820());
         if (var8 instanceof AbstractClientPlayerEntity) {
            player = (AbstractClientPlayerEntity)var8;
            packet.method_30145().forEach((pair) -> {
               EquipmentSlot slot = (EquipmentSlot)pair.getFirst();
               ItemStack oldStack = BlackOut.mc.field_1724.method_31548().method_7372(3 - slot.method_5927());
               ItemStack newStack = (ItemStack)pair.getSecond();
               if (oldStack.method_31574(newStack.method_7909())) {
                  if (Objects.equals(oldStack.method_7969(), newStack.method_7969())) {
                     if (newStack.method_7919() <= oldStack.method_7919()) {
                        int diff = oldStack.method_7919() - newStack.method_7919();
                        if (diff > 0) {
                           StatsManager.TrackerData data = this.getStats(player);
                           if (data != null) {
                              data.armorDamage += (double)diff;
                           }

                        }
                     }
                  }
               }
            });
         }
      }

   }

   private AbstractClientPlayerEntity getOwner(ExperienceBottleEntity bottle) {
      Entity var3 = bottle.method_24921();
      if (var3 instanceof AbstractClientPlayerEntity) {
         AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)var3;
         return player;
      } else {
         return this.getClosest((playerx) -> {
            return playerx.method_33571().method_1022(bottle.method_19538());
         });
      }
   }

   private AbstractClientPlayerEntity getClosest(ToDoubleFunction<AbstractClientPlayerEntity> function) {
      return (AbstractClientPlayerEntity)BlackOut.mc.field_1687.method_18456().stream().min(Comparator.comparingDouble(function)).orElse((Object)null);
   }

   public void reset() {
      this.dataMap.clear();
   }

   public StatsManager.TrackerData getStats(AbstractClientPlayerEntity player) {
      UUID uuid = player.method_7334().getId();
      if (!this.dataMap.containsKey(uuid)) {
         return null;
      } else {
         StatsManager.TrackerMap map = (StatsManager.TrackerMap)this.dataMap.get(uuid);
         return !map.is(player) ? null : map.get().data();
      }
   }

   public static class TrackerData {
      public long lastUpdate = System.currentTimeMillis();
      public int trackedFor = 0;
      public int inHoleFor = 0;
      public int phasedFor = 0;
      public int pops = 0;
      public int eaten = 0;
      public int blocksMoved = 0;
      public double damage = 0.0D;
      public double armorDamage = 0.0D;
      public int bottles = 0;
      private BlockPos prevPos;
      private float prevHealth;

      public TrackerData() {
         this.prevPos = BlockPos.field_10980;
         this.prevHealth = 0.0F;
      }

      private void tick(AbstractClientPlayerEntity player) {
         this.lastUpdate = System.currentTimeMillis();
         ++this.trackedFor;
         float health = player.method_6032() + player.method_6067();
         if (health < this.prevHealth) {
            this.damage += (double)(this.prevHealth - health);
         }

         BlockPos pos = new BlockPos(player.method_31477(), (int)Math.round(player.method_23318()), player.method_31479());
         if (pos.method_10263() != this.prevPos.method_10263() || pos.method_10260() != this.prevPos.method_10260()) {
            ++this.blocksMoved;
         }

         if (HoleUtils.inHole(pos)) {
            ++this.inHoleFor;
         }

         if (OLEPOSSUtils.inside(player, player.method_5829().method_35580(0.04D, 0.06D, 0.04D))) {
            ++this.phasedFor;
         }

         this.prevHealth = health;
         this.prevPos = pos;
      }

      private void onPop(AbstractClientPlayerEntity player) {
         ++this.pops;
         BlackOut.EVENT_BUS.post(PopEvent.get(player, this.pops));
      }
   }

   private static class TrackerMap {
      private StatsManager.TrackerMap.Tracker current;

      private void set(AbstractClientPlayerEntity player) {
         if (this.entityChanged(player)) {
            this.current = new StatsManager.TrackerMap.Tracker(player, new StatsManager.TrackerData());
         } else {
            this.current = new StatsManager.TrackerMap.Tracker(player, this.current.data());
         }

      }

      private boolean entityChanged(AbstractClientPlayerEntity newPlayer) {
         if (this.current == null) {
            return true;
         } else {
            long sinceUpdate = System.currentTimeMillis() - this.current.data.lastUpdate;
            if (sinceUpdate < 500L && this.current.data.prevPos.method_46558().method_1025(newPlayer.method_19538()) > 1000.0D) {
               return true;
            } else if (sinceUpdate > 60000L) {
               return true;
            } else {
               return this.current.player.method_35049() == RemovalReason.field_26998;
            }
         }
      }

      private boolean is(AbstractClientPlayerEntity player) {
         return this.current != null && this.current.player() == player;
      }

      private void tick() {
         if (!this.current.player().method_31481()) {
            this.current.data().tick(this.current.player());
         }

      }

      private StatsManager.TrackerMap.Tracker get() {
         return this.current;
      }

      private static record Tracker(AbstractClientPlayerEntity player, StatsManager.TrackerData data) {
         private Tracker(AbstractClientPlayerEntity player, StatsManager.TrackerData data) {
            this.player = player;
            this.data = data;
         }

         public AbstractClientPlayerEntity player() {
            return this.player;
         }

         public StatsManager.TrackerData data() {
            return this.data;
         }
      }
   }
}
