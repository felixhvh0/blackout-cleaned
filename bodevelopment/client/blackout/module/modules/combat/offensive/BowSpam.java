package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.util.InvUtils;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class BowSpam extends Module {
   private static BowSpam INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   public final Setting<Integer> charge;
   public final Setting<Boolean> fast;

   public BowSpam() {
      super("Bow Spam", "Automatically releases arrows", SubCategory.OFFENSIVE, true);
      this.charge = this.sgGeneral.i("Charge", 3, 3, 20, 1, "How long to charge until releasing");
      this.fast = this.sgGeneral.b("Instant", false, "Instantly restarts using after stopping. Might not lose charge.");
      INSTANCE = this;
   }

   public static BowSpam getInstance() {
      return INSTANCE;
   }

   public String getInfo() {
      return String.valueOf(InvUtils.count(true, true, (stack) -> {
         return stack.method_7909() instanceof ArrowItem;
      }));
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         if (BlackOut.mc.field_1724.method_6047().method_31574(Items.field_8102) && BlackOut.mc.field_1724.method_6048() >= (Integer)this.charge.get() && BlackOut.mc.field_1690.field_1904.method_1434()) {
            BlackOut.mc.field_1761.method_2897(BlackOut.mc.field_1724);
            if ((Boolean)this.fast.get()) {
               BlackOut.mc.field_1761.method_2919(BlackOut.mc.field_1724, Hand.field_5808);
            }
         }

      }
   }
}
