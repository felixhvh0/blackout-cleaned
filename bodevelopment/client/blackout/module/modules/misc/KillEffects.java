package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.Iterator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class KillEffects extends Module {
   public final SettingGroup sgGeneral = this.addGroup("General");
   public final Setting<Integer> range;
   public final Setting<Integer> tickDelay;
   private int ticks;

   public KillEffects() {
      super("Kill Effects", "Spawns lighting when someone dies", SubCategory.MISC, true);
      this.range = this.sgGeneral.i("Range", 50, 0, 100, 1, ".");
      this.tickDelay = this.sgGeneral.i("Tick Delay", 10, 0, 20, 1, ".");
      this.ticks = 0;
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         ++this.ticks;
         if (this.ticks >= (Integer)this.tickDelay.get()) {
            Iterator var2 = BlackOut.mc.field_1687.method_18456().iterator();

            while(true) {
               PlayerEntity player;
               do {
                  do {
                     do {
                        if (!var2.hasNext()) {
                           return;
                        }

                        player = (PlayerEntity)var2.next();
                     } while(player == BlackOut.mc.field_1724);
                  } while(!(player.method_19538().method_1022(BlackOut.mc.field_1724.method_19538()) <= (double)(Integer)this.range.get()));
               } while(!(player.method_6032() <= 0.0F) && !player.method_29504());

               this.ticks = 0;
               LightningEntity lightning = new LightningEntity(EntityType.field_6112, BlackOut.mc.field_1687);
               lightning.method_5814(player.method_23317(), player.method_23318(), player.method_23321());
               BlackOut.mc.field_1687.method_53875(lightning);
               BlackOut.mc.field_1687.method_8486(player.method_23317(), player.method_23318(), player.method_23321(), SoundEvents.field_14865, SoundCategory.field_15252, 1.0F, 1.0F, true);
            }
         }
      }
   }
}
