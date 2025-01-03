package bodevelopment.client.blackout.module.modules.legit;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RandomMode;
import bodevelopment.client.blackout.enums.SwingHand;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;

public class Clicker extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private long prev = 0L;
   private final Setting<RandomMode> randomise;
   private final Setting<Double> cps;
   private final Setting<Double> minCps;

   public Clicker() {
      super("Clicker", "Automatically clicks", SubCategory.LEGIT, true);
      this.randomise = this.sgGeneral.e("Randomise", RandomMode.Random, "Randomises CPS.");
      this.cps = this.sgGeneral.d("CPS", 14.0D, 0.0D, 20.0D, 0.1D, ".");
      this.minCps = this.sgGeneral.d("Min CPS", 10.0D, 0.0D, 20.0D, 0.1D, ".", () -> {
         return this.randomise.get() != RandomMode.Disabled;
      });
   }

   @Event
   public void onRender(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null) {
         if (BlackOut.mc.field_1690.field_1886.method_1434()) {
            if (this.delayCheck()) {
               this.sendPacket(new HandSwingC2SPacket(Hand.field_5808));
               this.clientSwing(SwingHand.MainHand, Hand.field_5808);
               HitResult result = BlackOut.mc.field_1765;
               if (result == null || result.method_17783() != Type.field_1331) {
                  return;
               }

               Entity entity = ((EntityHitResult)result).method_17782();
               if (entity instanceof LivingEntity) {
                  LivingEntity livingEntity = (LivingEntity)entity;
                  if (livingEntity.method_29504()) {
                     return;
                  }
               }

               BlackOut.mc.field_1761.method_2918(BlackOut.mc.field_1724, entity);
               this.prev = System.currentTimeMillis();
            }

         }
      }
   }

   private boolean delayCheck() {
      double d = this.randomise.get() == RandomMode.Disabled ? (Double)this.cps.get() : MathHelper.method_16436(((RandomMode)this.randomise.get()).get(), (Double)this.cps.get(), (Double)this.minCps.get());
      return (double)(System.currentTimeMillis() - this.prev) > 1000.0D / d;
   }
}
