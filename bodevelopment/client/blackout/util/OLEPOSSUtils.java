package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class OLEPOSSUtils {
   public static final EquipmentSlot[] equipmentSlots;

   public static BlockPos roundedPos() {
      return roundedPos(BlackOut.mc.field_1724);
   }

   public static BlockPos roundedPos(Entity entity) {
      return new BlockPos(entity.method_31477(), (int)Math.round(entity.method_23318()), entity.method_31479());
   }

   public static long testTime(Runnable runnable) {
      long start = System.currentTimeMillis();
      runnable.run();
      long end = System.currentTimeMillis();
      return end - start;
   }

   public static double similarity(String string1, String string2) {
      String shorter;
      String longer;
      if (string1.length() > string2.length()) {
         shorter = string2.toLowerCase();
         longer = string1.toLowerCase();
      } else {
         shorter = string1.toLowerCase();
         longer = string2.toLowerCase();
      }

      int i = 0;
      int a = 0;

      for(int c = 0; c < longer.length(); ++c) {
         char charLonger = longer.charAt(c);

         for(int ci = i; ci < shorter.length(); ++ci) {
            char charShorter = shorter.charAt(ci);
            if (charLonger == charShorter) {
               ++a;
               i = ci;
               break;
            }
         }
      }

      return (double)((float)a / (float)longer.length());
   }

   public static boolean inWater(Box box) {
      return inFluid(box, FluidTags.field_15517);
   }

   public static boolean inLava(Box box) {
      return inFluid(box, FluidTags.field_15518);
   }

   public static boolean inFluid(Box box, TagKey<Fluid> tag) {
      int minX = MathHelper.method_15357(box.field_1323 + 0.001D);
      int maxX = MathHelper.method_15384(box.field_1320 - 0.001D);
      int minY = MathHelper.method_15357(box.field_1322 + 0.001D);
      int maxY = MathHelper.method_15384(box.field_1325 - 0.001D);
      int minZ = MathHelper.method_15357(box.field_1321 + 0.001D);
      int maxZ = MathHelper.method_15384(box.field_1324 - 0.001D);

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            for(int z = minZ; z <= maxZ; ++z) {
               FluidState fluidState = BlackOut.mc.field_1687.method_8316(new BlockPos(x, y, z));
               if (fluidState.method_15767(tag) && (float)y + fluidState.method_20785() > (float)minY) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static double fluidHeight(Box box, TagKey<Fluid> tag) {
      int minX = MathHelper.method_15357(box.field_1323 + 0.001D);
      int maxX = MathHelper.method_15384(box.field_1320 - 0.001D);
      int minY = MathHelper.method_15357(box.field_1322 + 0.001D);
      int maxY = MathHelper.method_15384(box.field_1325 - 0.001D);
      int minZ = MathHelper.method_15357(box.field_1321 + 0.001D);
      int maxZ = MathHelper.method_15384(box.field_1324 - 0.001D);
      double maxHeight = 0.0D;

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            for(int z = minZ; z <= maxZ; ++z) {
               FluidState fluidState = BlackOut.mc.field_1687.method_8316(new BlockPos(x, y, z));
               if (fluidState.method_15767(tag)) {
                  maxHeight = Math.max(maxHeight, (double)((float)y + fluidState.method_20785()) - box.field_1322);
               }
            }
         }
      }

      return maxHeight;
   }

   public static double approach(double from, double to, double delta) {
      return to > from ? Math.min(from + delta, to) : Math.max(from - delta, to);
   }

   public static Vec3d getLerpedPos(Entity entity, double tickDelta) {
      double x = MathHelper.method_16436(tickDelta, entity.field_6014, entity.method_23317());
      double y = MathHelper.method_16436(tickDelta, entity.field_6036, entity.method_23318());
      double z = MathHelper.method_16436(tickDelta, entity.field_5969, entity.method_23321());
      return new Vec3d(x, y, z);
   }

   public static Box getLerpedBox(Entity entity, double tickDelta) {
      double x = MathHelper.method_16436(tickDelta, entity.field_6014, entity.method_23317());
      double y = MathHelper.method_16436(tickDelta, entity.field_6036, entity.method_23318());
      double z = MathHelper.method_16436(tickDelta, entity.field_5969, entity.method_23321());
      double halfX = entity.method_5829().method_17939() / 2.0D;
      double halfZ = entity.method_5829().method_17941() / 2.0D;
      return new Box(x - halfX, y, z - halfZ, x + halfX, y + entity.method_5829().method_17940(), z + halfZ);
   }

   public static int secondsSince(LocalDateTime dateTime) {
      LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
      int days;
      int i;
      if (currentTime.getYear() != dateTime.getYear()) {
         days = 0;

         for(i = dateTime.getYear(); i <= currentTime.getYear(); ++i) {
            days += daysInYear(i);
         }

         days -= currentTime.getDayOfYear();
         days -= daysInYear(dateTime.getYear()) - dateTime.getDayOfYear();
      } else {
         days = currentTime.getDayOfYear() - dateTime.getDayOfYear();
      }

      i = currentTime.getHour() - dateTime.getHour();
      int minutes = currentTime.getMinute() - dateTime.getMinute();
      int seconds = currentTime.getSecond() - dateTime.getSecond();
      i += days * 24;
      minutes += i * 60;
      seconds += minutes * 60;
      return seconds;
   }

   private static int daysInYear(int year) {
      return Year.of(year).length();
   }

   public static String getDateTimeString(int seconds) {
      int minutes = (int)((float)seconds / 60.0F);
      seconds -= minutes * 60;
      int hours = (int)((float)minutes / 60.0F);
      minutes -= hours * 60;
      int days = (int)((float)hours / 24.0F);
      hours -= days * 24;
      int months = (int)((float)days / 30.0F);
      days -= months * 30;
      int years = (int)((float)months / 12.0F);
      months -= years * 12;
      if (years > 0) {
         return years + "y " + months + "mo";
      } else if (months > 0) {
         return months + "mo " + days + "d";
      } else if (days > 0) {
         return days + "d " + hours + "h";
      } else if (hours > 0) {
         return hours + "h " + minutes + "m";
      } else {
         return minutes > 0 ? minutes + "m " + seconds + "s" : seconds + " s";
      }
   }

   public static String getTimeString(long ms) {
      int hours = (int)((double)ms / 3600000.0D % 60.0D);
      int minutes = (int)((double)ms / 60000.0D % 60.0D);
      int seconds = (int)((double)ms / 1000.0D % 60.0D);
      StringBuilder result = new StringBuilder();
      if (hours > 0) {
         result.append(hours).append("h ");
      }

      if (minutes > 0) {
         result.append(minutes).append("m ");
      }

      if (seconds > 0 || hours == 0 && minutes == 0) {
         result.append(seconds).append("s");
      }

      return result.toString();
   }

   public static double safeDivide(double v1, double v2) {
      double v3 = v1 / v2;
      return Double.isNaN(v3) ? 1.0D : v3;
   }

   public static <T> List<T> reverse(List<T> list) {
      List<T> l = new ArrayList();
      list.forEach((t) -> {
         l.add(0, t);
      });
      return l;
   }

   public static <T> void limitList(List<T> list, int cap) {
      if (list.size() > cap) {
         list.subList(cap, list.size()).clear();
      }

   }

   public static <T> boolean contains(T[] array, T object) {
      Object[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         T t = var2[var4];
         if (t.equals(object)) {
            return true;
         }
      }

      return false;
   }

   public static Hand getHand(Item item) {
      return getHand((stack) -> {
         return stack.method_7909() == item;
      });
   }

   public static Hand getHand(Predicate<ItemStack> predicate) {
      if (predicate.test(Managers.PACKET.getStack())) {
         return Hand.field_5808;
      } else {
         return predicate.test(BlackOut.mc.field_1724.method_6079()) ? Hand.field_5810 : null;
      }
   }

   public static ItemStack getItem(Hand hand) {
      return hand == Hand.field_5808 ? Managers.PACKET.getStack() : (hand == Hand.field_5810 ? BlackOut.mc.field_1724.method_6079() : null);
   }

   public static Vec3d getMiddle(Box box) {
      return new Vec3d((box.field_1323 + box.field_1320) / 2.0D, (box.field_1322 + box.field_1325) / 2.0D, (box.field_1321 + box.field_1324) / 2.0D);
   }

   public static boolean inside(Entity en, Box bb) {
      return BlackOut.mc.field_1687.method_20812(en, bb).iterator().hasNext();
   }

   public static int closerToZero(int x) {
      return (int)((float)x - Math.signum((float)x));
   }

   public static Vec3d getClosest(Vec3d playerPos, Vec3d feet, double width, double height) {
      double halfWidth = width / 2.0D;
      return getClosest(playerPos, feet.method_10216() - halfWidth, feet.method_10216() + halfWidth, feet.method_10214(), feet.method_10214() + height, feet.method_10215() - halfWidth, feet.method_10215() + halfWidth);
   }

   public static Vec3d getClosest(Vec3d playerPos, Box box) {
      return getClosest(playerPos, box.field_1323, box.field_1320, box.field_1322, box.field_1325, box.field_1321, box.field_1324);
   }

   public static Vec3d getClosest(Vec3d playerPos, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
      return new Vec3d(MathHelper.method_15350(playerPos.method_10216(), minX, maxX), MathHelper.method_15350(playerPos.method_10214(), minY, maxY), MathHelper.method_15350(playerPos.method_10215(), minZ, maxZ));
   }

   public static boolean strictDir(BlockPos pos, Direction dir, boolean ncp) {
      boolean var10000;
      switch(dir) {
      case field_11033:
         var10000 = BlackOut.mc.field_1724.method_33571().field_1351 <= (double)pos.method_10264() + (ncp ? 0.5D : 0.0D);
         break;
      case field_11036:
         var10000 = BlackOut.mc.field_1724.method_33571().field_1351 >= (double)pos.method_10264() + (ncp ? 0.5D : 1.0D);
         break;
      case field_11043:
         var10000 = BlackOut.mc.field_1724.method_23321() < (double)pos.method_10260();
         break;
      case field_11035:
         var10000 = BlackOut.mc.field_1724.method_23321() >= (double)(pos.method_10260() + 1);
         break;
      case field_11039:
         var10000 = BlackOut.mc.field_1724.method_23317() < (double)pos.method_10263();
         break;
      case field_11034:
         var10000 = BlackOut.mc.field_1724.method_23317() >= (double)(pos.method_10263() + 1);
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static Box getCrystalBox(BlockPos pos) {
      return new Box((double)pos.method_10263() - 0.5D, (double)pos.method_10264(), (double)pos.method_10260() - 0.5D, (double)pos.method_10263() + 1.5D, (double)(pos.method_10264() + 2), (double)pos.method_10260() + 1.5D);
   }

   public static Box getCrystalBox(Vec3d pos) {
      return new Box(pos.method_10216() - 1.0D, pos.method_10214(), pos.method_10215() - 1.0D, pos.method_10216() + 1.0D, pos.method_10214() + 2.0D, pos.method_10215() + 1.0D);
   }

   public static boolean replaceable(BlockPos block) {
      return replaceable(BlackOut.mc.field_1687.method_8320(block));
   }

   public static boolean replaceable(BlockState state) {
      return replaceable(state.method_26204());
   }

   public static boolean replaceable(Block block) {
      return block.field_23155.field_44630;
   }

   public static boolean solid2(BlockPos block) {
      return BlackOut.mc.field_1687.method_8320(block).method_51367();
   }

   public static boolean solid(BlockPos block) {
      Block b = BlackOut.mc.field_1687.method_8320(block).method_26204();
      return !(b instanceof AbstractFireBlock) && !(b instanceof FluidBlock) && !(b instanceof AirBlock);
   }

   public static boolean isGapple(Item item) {
      return item == Items.field_8463 || item == Items.field_8367;
   }

   public static boolean isShulker(ItemStack stack) {
      return isShulker(stack.method_7909());
   }

   public static boolean isShulker(Item item) {
      boolean var10000;
      if (item instanceof BlockItem) {
         BlockItem blockItem = (BlockItem)item;
         if (blockItem.method_7711() instanceof ShulkerBoxBlock) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public static boolean isGapple(ItemStack stack) {
      return isGapple(stack.method_7909());
   }

   public static boolean isBed(Item item) {
      return item instanceof BedItem;
   }

   public static boolean isBed(ItemStack stack) {
      return isBed(stack.method_7909());
   }

   public static boolean collidable(BlockPos block) {
      return BlackOut.mc.field_1687.method_8320(block).method_26204().field_23155.field_10664;
   }

   static {
      equipmentSlots = new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166};
   }
}
