package bodevelopment.client.blackout.module.modules.client.settings;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.interfaces.functional.DoublePredicate;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.SettingsModule;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import java.util.Objects;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FacingSettings extends SettingsModule {
   private static FacingSettings INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   public final Setting<Boolean> strictDir;
   public final Setting<Boolean> ncpDirection;
   public final Setting<Boolean> unblocked;
   public final Setting<Boolean> airPlace;
   public final Setting<FacingSettings.MaxHeight> maxHeight;

   public FacingSettings() {
      super("Facing", false, true);
      this.strictDir = this.sgGeneral.b("Strict Direction", false, "Doesn't place on faces which aren't in your direction.");
      SettingGroup var10001 = this.sgGeneral;
      Setting var10005 = this.strictDir;
      Objects.requireNonNull(var10005);
      this.ncpDirection = var10001.b("NCP Directions", false, ".", var10005::get);
      this.unblocked = this.sgGeneral.b("Unblocked", false, "Doesn't place on faces that have block on them.");
      this.airPlace = this.sgGeneral.b("Air Place", false, "Allows placing blocks in air.");
      this.maxHeight = this.sgGeneral.e("Max Height", FacingSettings.MaxHeight.New, "Doesn't place on top sides of blocks at max height. Old: 1.12, New: 1.17+");
      INSTANCE = this;
   }

   public static FacingSettings getInstance() {
      return INSTANCE;
   }

   public PlaceData getPlaceData(BlockPos blockPos, DoublePredicate<BlockPos, Direction> predicateOR, DoublePredicate<BlockPos, Direction> predicateAND, boolean ignoreContainers) {
      Direction direction = null;
      boolean closestSneak = false;
      double closestDist = 1000.0D;
      Direction[] var9 = Direction.values();
      int var10 = var9.length;

      for(int var11 = 0; var11 < var10; ++var11) {
         Direction dir = var9[var11];
         BlockPos pos = blockPos.method_10093(dir);
         boolean sneak = this.ignoreBlock(this.state(pos));
         if (!this.outOfBuildHeightCheck(pos) && (!sneak || !ignoreContainers) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos, dir.method_10153(), (Boolean)this.ncpDirection.get())) && (predicateOR != null && predicateOR.test(pos, dir) || this.solid(pos) && (predicateAND == null || predicateAND.test(pos, dir)))) {
            double dist = SettingUtils.placeRangeTo(pos.method_10093(dir));
            if (direction == null || dist < closestDist) {
               closestDist = dist;
               direction = dir;
               closestSneak = sneak;
            }
         }
      }

      if ((Boolean)this.airPlace.get()) {
         return new PlaceData(blockPos, Direction.field_11036, true, false);
      } else if (direction == null) {
         return new PlaceData((BlockPos)null, (Direction)null, false, false);
      } else {
         return new PlaceData(blockPos.method_10093(direction), direction.method_10153(), true, closestSneak);
      }
   }

   private boolean ignoreBlock(BlockState state) {
      if (state.method_31709()) {
         return true;
      } else {
         Block block = state.method_26204();
         return block instanceof AnvilBlock || block instanceof BedBlock || block instanceof CraftingTableBlock;
      }
   }

   public Direction getPlaceOnDirection(BlockPos position) {
      Direction direction = null;
      double closestDist = 1000.0D;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction dir = var5[var7];
         BlockPos pos = position.method_10093(dir);
         if (!this.outOfBuildHeightCheck(pos) && (!(Boolean)this.unblocked.get() || !this.solid(pos)) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(position, dir, (Boolean)this.ncpDirection.get()))) {
            double dist = this.dist(position, dir);
            if (direction == null || dist < closestDist) {
               closestDist = dist;
               direction = dir;
            }
         }
      }

      return direction;
   }

   private boolean solid(BlockPos pos) {
      return this.state(pos).method_51367();
   }

   private BlockState state(BlockPos pos) {
      return Managers.BLOCK.blockState(pos);
   }

   private boolean outOfBuildHeightCheck(BlockPos pos) {
      int var10000 = pos.method_10264();
      short var10001;
      switch((FacingSettings.MaxHeight)this.maxHeight.get()) {
      case Old:
         var10001 = 255;
         break;
      case New:
         var10001 = 319;
         break;
      case Disabled:
         var10001 = 1000;
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      return var10000 > var10001;
   }

   private double dist(BlockPos pos, Direction dir) {
      Vec3d vec = new Vec3d((double)((float)pos.method_10263() + (float)dir.method_10148() / 2.0F), (double)((float)pos.method_10264() + (float)dir.method_10164() / 2.0F), (double)((float)pos.method_10260() + (float)dir.method_10165() / 2.0F));
      Vec3d dist = BlackOut.mc.field_1724.method_33571().method_1020(vec);
      return dist.method_1033();
   }

   public static enum MaxHeight {
      Old,
      New,
      Disabled;

      // $FF: synthetic method
      private static FacingSettings.MaxHeight[] $values() {
         return new FacingSettings.MaxHeight[]{Old, New, Disabled};
      }
   }
}
