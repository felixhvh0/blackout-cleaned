package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.EntityAddEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.mixin.accessors.AccessorInteractEntityC2SPacket;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EntityManager extends Manager {
   private final TimerList<Integer> renderDead = new TimerList(true);
   private final TimerList<BlockPos> spawningItems = new TimerList(true);
   private final TimerList<Integer> semiDead = new TimerList(true);
   private final TimerList<BlockPos> waitingToRemoveItem = new TimerList(true);
   private final TimerList<Integer> attacked = new TimerList(true);

   public void setDead(int id, boolean full) {
      if (full) {
         BlackOut.mc.field_1687.method_2945(id, RemovalReason.field_26998);
      } else {
         this.renderDead.add(id, 1.0D);
      }

   }

   public void setSemiDead(int i) {
      this.semiDead.add(i, 0.3D);
   }

   public void addSpawning(BlockPos pos) {
      this.spawningItems.add(pos, 2.0D);
   }

   public void removeSpawning(BlockPos pos) {
      this.spawningItems.remove((timer) -> {
         return ((BlockPos)timer.value).equals(pos);
      });
   }

   public boolean containsItem(BlockPos pos) {
      return this.spawningItems.contains((Object)pos);
   }

   public void removeItems(BlockPos pos) {
      this.spawningItems.remove((p) -> {
         if (((BlockPos)p.value).equals(pos)) {
            this.waitingToRemoveItem.add(pos, 0.5D);
            return true;
         } else {
            return false;
         }
      });
   }

   public boolean isDead(int id) {
      return this.semiDead.contains((Object)id);
   }

   public boolean shouldRender(int id) {
      return !this.renderDead.contains((Object)id);
   }

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return false;
      });
   }

   @Event
   public void onEntity(EntityAddEvent.Post event) {
      if (event.entity.method_5864() == EntityType.field_6052) {
         BlockPos pos = event.entity.method_24515();
         if (this.spawningItems.contains((Object)pos)) {
            this.removeSpawning(pos);
         }

         if (this.waitingToRemoveItem.contains((Object)pos)) {
            this.waitingToRemoveItem.remove((timer) -> {
               return ((BlockPos)timer.value).equals(pos);
            });
            this.setSemiDead(event.id);
         }
      }

   }

   @Event
   public void packetSendEvent(PacketEvent.Sent event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerInteractEntityC2SPacket) {
         PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)var3;
         AccessorInteractEntityC2SPacket packetAccessor = (AccessorInteractEntityC2SPacket)packet;
         if (packetAccessor.getType().method_34211() == InteractType.field_29172) {
            int id = packetAccessor.getId();
            Entity entity = BlackOut.mc.field_1687.method_8469(id);
            if (entity instanceof EndCrystalEntity && !this.attacked.contains((Object)id)) {
               BlockPos center = entity.method_24515();
               Direction[] var7 = Direction.values();
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Direction dir = var7[var9];
                  this.removeItems(center.method_10093(dir));
               }
            }

            this.attacked.replace(id, 0.25D);
         }

      }
   }
}
