package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.randomstuff.FakePlayerEntity;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.DamageUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class FakePlayerManager extends Manager {
   public final List<FakePlayerEntity> fakePlayers = new ArrayList();
   private final List<FakePlayerEntity.PlayerPos> recorded = new ArrayList();
   private boolean recording = false;

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return false;
      });
   }

   @Event
   public void onReceive(PacketEvent.Receive.Pre event) {
      Packet var3 = event.packet;
      if (var3 instanceof ExplosionS2CPacket) {
         ExplosionS2CPacket packet = (ExplosionS2CPacket)var3;
         this.fakePlayers.forEach((entity) -> {
            Vec3d pos = new Vec3d(packet.method_11475(), packet.method_11477(), packet.method_11478());
            Box box = entity.method_5829();
            double q = 12.0D;
            double dist = BoxUtils.feet(box).method_1022(pos) / q;
            if (!(dist > 1.0D)) {
               double aa = DamageUtils.getExposure(pos, box, (BlockPos)null);
               double ab = (1.0D - dist) * aa;
               float damage = (float)((int)((ab * ab + ab) * 3.5D * q + 1.0D));
               entity.method_5643(BlackOut.mc.field_1724.method_48923().method_48819((Entity)null, (Entity)null), damage);
            }
         });
      }

   }

   public void onAttack(FakePlayerEntity player) {
      this.playHitSound(player);
      player.method_5643(BlackOut.mc.field_1724.method_48923().method_48802(BlackOut.mc.field_1724), this.getDamage(player));
   }

   private void playHitSound(FakePlayerEntity target) {
      if (!(this.getDamage(target) <= 0.0F) && !target.method_29504()) {
         boolean bl = BlackOut.mc.field_1724.method_7261(0.5F) > 0.9F;
         boolean sprintHit = BlackOut.mc.field_1724.method_5624() && bl;
         if (sprintHit) {
            BlackOut.mc.field_1687.method_8486(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), SoundEvents.field_14999, BlackOut.mc.field_1724.method_5634(), 1.0F, 1.0F, true);
         }

         boolean critical = bl && BlackOut.mc.field_1724.field_6017 > 0.0F && !BlackOut.mc.field_1724.method_24828() && !BlackOut.mc.field_1724.method_6101() && !BlackOut.mc.field_1724.method_5799() && !BlackOut.mc.field_1724.method_6059(StatusEffects.field_5919) && !BlackOut.mc.field_1724.method_5765() && !BlackOut.mc.field_1724.method_5624();
         double d = (double)(BlackOut.mc.field_1724.field_5973 - BlackOut.mc.field_1724.field_6039);
         boolean bl42 = bl && !critical && !sprintHit && BlackOut.mc.field_1724.method_24828() && d < (double)BlackOut.mc.field_1724.method_6029() && BlackOut.mc.field_1724.method_5998(Hand.field_5808).method_7909() instanceof SwordItem;
         if (bl42) {
            BlackOut.mc.field_1687.method_8486(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), SoundEvents.field_14706, BlackOut.mc.field_1724.method_5634(), 1.0F, 1.0F, true);
         } else if (!critical) {
            SoundEvent soundEvent = bl ? SoundEvents.field_14840 : SoundEvents.field_14625;
            BlackOut.mc.field_1687.method_8486(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), soundEvent, BlackOut.mc.field_1724.method_5634(), 1.0F, 1.0F, true);
         }

         if (critical) {
            BlackOut.mc.field_1687.method_8486(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), SoundEvents.field_15016, BlackOut.mc.field_1724.method_5634(), 1.0F, 1.0F, true);
         }

      } else {
         BlackOut.mc.field_1687.method_8486(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), SoundEvents.field_14914, BlackOut.mc.field_1724.method_5634(), 1.0F, 1.0F, true);
      }
   }

   private float getDamage(Entity target) {
      float damage = (float)DamageUtils.itemDamage(BlackOut.mc.field_1724.method_6047());
      float g;
      if (target instanceof LivingEntity) {
         g = EnchantmentHelper.method_8218(BlackOut.mc.field_1724.method_6047(), ((LivingEntity)target).method_6046());
      } else {
         g = EnchantmentHelper.method_8218(BlackOut.mc.field_1724.method_6047(), EntityGroup.field_6290);
      }

      float h = BlackOut.mc.field_1724.method_7261(0.5F);
      damage *= 0.2F + h * h * 0.8F;
      if ((double)h > 0.9D && BlackOut.mc.field_1724.field_6017 > 0.0F && !BlackOut.mc.field_1724.method_24828() && !BlackOut.mc.field_1724.method_6101() && !BlackOut.mc.field_1724.method_5799() && !BlackOut.mc.field_1724.method_6059(StatusEffects.field_5919) && !BlackOut.mc.field_1724.method_5765() && target instanceof LivingEntity && !BlackOut.mc.field_1724.method_5624()) {
         damage *= 1.5F;
      }

      return damage + g * h;
   }

   @Event
   public void onTick(TickEvent.Post event) {
      if (!this.recording) {
         this.recorded.clear();
      } else {
         FakePlayerEntity.PlayerPos playerPos = new FakePlayerEntity.PlayerPos(BlackOut.mc.field_1724.method_19538(), BlackOut.mc.field_1724.method_18798(), BlackOut.mc.field_1724.method_18376(), BlackOut.mc.field_1724.method_36455(), BlackOut.mc.field_1724.method_36454(), BlackOut.mc.field_1724.method_5791(), BlackOut.mc.field_1724.field_6283);
         this.recorded.add(playerPos);
         if (this.recorded.size() > 1200) {
            this.endRecording();
         }

      }
   }

   public void restart() {
      this.fakePlayers.forEach((player) -> {
         player.progress = 0;
      });
   }

   public void startRecording() {
      this.recording = true;
   }

   public void endRecording() {
      this.recording = false;
   }

   public void add(String name) {
      FakePlayerEntity player = new FakePlayerEntity(name == null ? "KassuK" : name);
      player.set(this.recorded);
      this.recorded.clear();
      this.endRecording();
      this.fakePlayers.add(player);
   }

   public void clear() {
      this.fakePlayers.removeIf((player) -> {
         player.method_5650(RemovalReason.field_26999);
         return true;
      });
   }

   public static FakePlayerEntity.PlayerPos getPlayerPos(PlayerEntity player) {
      return new FakePlayerEntity.PlayerPos(player.method_19538(), player.method_18798(), player.method_18376(), player.method_36455(), player.method_36454(), player.method_5791(), player.method_43078());
   }
}
