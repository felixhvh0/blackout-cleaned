package bodevelopment.client.blackout.module.modules.legit;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.SwingHand;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

public class TriggerBot extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<List<EntityType<?>>> entityTypes;
   private final Setting<Boolean> smartDelay;
   private final Setting<Integer> minDelay;
   private final Setting<Integer> attackDelay;
   private final Setting<Boolean> onlyWeapon;
   private final Setting<Boolean> critSync;
   private final Setting<Double> critSyncTime;
   private long critTime;

   public TriggerBot() {
      super("Trigger Bot", "Automatically attacks entities when you look at them.", SubCategory.LEGIT, true);
      this.entityTypes = this.sgGeneral.el("Entities", ".", EntityType.field_6097);
      this.smartDelay = this.sgGeneral.b("Smart Delay", true, "Charges weapon when hitting living entities.");
      SettingGroup var10001 = this.sgGeneral;
      Setting var10008 = this.smartDelay;
      Objects.requireNonNull(var10008);
      this.minDelay = var10001.i("Min Delay", 2, 0, 20, 1, "Ticks between attacks.", var10008::get);
      this.attackDelay = this.sgGeneral.i("Attack Delay", 2, 0, 20, 1, "Ticks between attacks.");
      this.onlyWeapon = this.sgGeneral.b("Only Weapon", true, "Only attacks with weapons");
      this.critSync = this.sgGeneral.b("Crit Sync", false, ".");
      var10001 = this.sgGeneral;
      var10008 = this.critSync;
      Objects.requireNonNull(var10008);
      this.critSyncTime = var10001.d("Crit Sync Time", 0.3D, 0.0D, 1.0D, 0.01D, ".", var10008::get);
      this.critTime = 0L;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (!this.shouldWait()) {
            this.critTime = System.currentTimeMillis();
            HitResult result = BlackOut.mc.field_1765;
            if (result != null && result.method_17783() == Type.field_1331) {
               Entity entity = ((EntityHitResult)result).method_17782();
               if (entity != null && ((List)this.entityTypes.get()).contains(entity.method_5864())) {
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingEntity = (LivingEntity)entity;
                     if (livingEntity.method_29504()) {
                        return;
                     }
                  }

                  if (!(Boolean)this.onlyWeapon.get() || BlackOut.mc.field_1724.method_6047().method_7909() instanceof ToolItem) {
                     int tickDelay = this.getTickDelay(entity);
                     if (BlackOut.mc.field_1724.field_6273 >= tickDelay) {
                        this.critTime = System.currentTimeMillis();
                        BlackOut.mc.field_1761.method_2918(BlackOut.mc.field_1724, entity);
                        this.sendPacket(new HandSwingC2SPacket(Hand.field_5808));
                        this.clientSwing(SwingHand.MainHand, Hand.field_5808);
                        if (entity instanceof EndCrystalEntity && CrystalOptimizer.getInstance().enabled) {
                           BlackOut.mc.field_1687.method_2945(entity.method_5628(), RemovalReason.field_26998);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldWait() {
      if (!(Boolean)this.critSync.get()) {
         return false;
      } else if (BlackOut.mc.field_1724.method_24828() && !BlackOut.mc.field_1690.field_1903.method_1434()) {
         return false;
      } else if (BlackOut.mc.field_1724.field_6017 > 0.0F) {
         return false;
      } else {
         return (double)(System.currentTimeMillis() - this.critTime) <= (Double)this.critSyncTime.get() * 1000.0D;
      }
   }

   private int getTickDelay(Entity entity) {
      return (Boolean)this.smartDelay.get() && entity instanceof LivingEntity ? Math.max((int)Math.ceil(1.0D / BlackOut.mc.field_1724.method_26825(EntityAttributes.field_23723) * 20.0D), (Integer)this.minDelay.get()) : (Integer)this.attackDelay.get();
   }
}
