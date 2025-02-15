package bodevelopment.client.blackout.module.modules.combat.defensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.ObsidianModule;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.combat.misc.AutoTrap;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.util.SettingUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SelfTrap extends ObsidianModule {
   private final SettingGroup sgToggle = this.addGroup("Toggle");
   private final Setting<AutoTrap.TrapMode> trapMode;
   private final Setting<Boolean> toggleMove;
   private final Setting<Surround.VerticalToggleMode> toggleVertical;
   private final Direction[] directions;
   private BlockPos prevPos;

   public SelfTrap() {
      super("Self Trap", "Covers you in blocks.", SubCategory.DEFENSIVE);
      this.trapMode = this.sgGeneral.e("Trap Mode", AutoTrap.TrapMode.Both, "");
      this.toggleMove = this.sgToggle.b("Toggle Move", false, "Toggles if you move horizontally.");
      this.toggleVertical = this.sgToggle.e("Toggle Vertical", Surround.VerticalToggleMode.Up, "Toggles the module if you move vertically.");
      this.directions = new Direction[]{Direction.field_11043, Direction.field_11035, Direction.field_11034, Direction.field_11039, Direction.field_11036};
      this.prevPos = BlockPos.field_10980;
   }

   public void onTick(TickEvent.Pre event) {
      super.onTick(event);
      BlockPos currentPos = this.getPos();
      this.checkToggle(currentPos);
      this.prevPos = currentPos;
   }

   private void checkToggle(BlockPos currentPos) {
      if (this.prevPos != null) {
         if ((Boolean)this.toggleMove.get() && (currentPos.method_10263() != this.prevPos.method_10263() || currentPos.method_10260() != this.prevPos.method_10260())) {
            this.disable("moved horizontally");
         }

         if ((this.toggleVertical.get() == Surround.VerticalToggleMode.Up || this.toggleVertical.get() == Surround.VerticalToggleMode.Any) && currentPos.method_10264() > this.prevPos.method_10264()) {
            this.disable("moved up");
         }

         if ((this.toggleVertical.get() == Surround.VerticalToggleMode.Down || this.toggleVertical.get() == Surround.VerticalToggleMode.Any) && currentPos.method_10264() < this.prevPos.method_10264()) {
            this.disable("moved down");
         }

      }
   }

   protected void addPlacements() {
      this.insideBlocks.forEach((pos) -> {
         Direction[] var2 = this.directions;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Direction dir = var2[var4];
            if (((AutoTrap.TrapMode)this.trapMode.get()).allowed(dir) && !this.blockPlacements.contains(pos.method_10093(dir)) && !this.insideBlocks.contains(pos.method_10093(dir))) {
               this.blockPlacements.add(pos.method_10093(dir));
            }
         }

      });
   }

   protected void addInsideBlocks() {
      this.addBlocks(BlackOut.mc.field_1724, this.getSize(BlackOut.mc.field_1724));
   }

   protected void addBlocks(Entity entity, int[] size) {
      int eyeY = (int)Math.ceil(entity.method_5829().field_1325);

      for(int x = size[0]; x <= size[1]; ++x) {
         for(int z = size[2]; z <= size[3]; ++z) {
            BlockPos p = entity.method_24515().method_10069(x, 0, z).method_33096(eyeY - 1);
            if (!(BlackOut.mc.field_1687.method_8320(p).method_26204().method_9520() > 600.0F) && SettingUtils.inPlaceRange(p)) {
               this.insideBlocks.add(p);
            }
         }
      }

   }
}
