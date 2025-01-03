package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BlockUtils {
   public static boolean mineable(BlockPos pos) {
      BlockState state = BlackOut.mc.field_1687.method_8320(pos);
      return state.method_26204() == Blocks.field_9987 ? false : state.method_51367();
   }

   public static double getBlockBreakingDelta(BlockPos pos, ItemStack stack) {
      return getBlockBreakingDelta(pos, stack, true, true, true);
   }

   public static double getBlockBreakingDelta(BlockPos pos, ItemStack stack, boolean effects, boolean water, boolean onGround) {
      return getBlockBreakingDelta(stack, BlackOut.mc.field_1687.method_8320(pos), effects, water, onGround);
   }

   public static double getBlockBreakingDelta(ItemStack stack, BlockState state, boolean effects, boolean water, boolean onGround) {
      float f = state.method_26214((BlockView)null, (BlockPos)null);
      if (f == -1.0F) {
         return 0.0D;
      } else {
         int i = state.method_29291() && !stack.method_7951(state) ? 100 : 30;
         return getBlockBreakingSpeed(state, stack, effects, water, onGround) / (double)f / (double)i;
      }
   }

   public static double getBlockBreakingSpeed(BlockState state, ItemStack stack, boolean effects, boolean water, boolean onGround) {
      float f = stack.method_7924(state);
      if (f > 1.0F && !stack.method_7960() && EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9131)) {
         int i = (Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9131);
         if (i > 0 && !stack.method_7960()) {
            f += (float)(i * i + 1);
         }
      }

      if (effects && BlackOut.mc.field_1724.method_6059(StatusEffects.field_5917)) {
         f *= 1.0F + (float)(BlackOut.mc.field_1724.method_6112(StatusEffects.field_5917).method_5578() + 1) * 0.2F;
      }

      if (effects && BlackOut.mc.field_1724.method_6059(StatusEffects.field_5901)) {
         float var10001;
         switch(BlackOut.mc.field_1724.method_6112(StatusEffects.field_5901).method_5578()) {
         case 0:
            var10001 = 0.3F;
            break;
         case 1:
            var10001 = 0.09F;
            break;
         case 2:
            var10001 = 0.0027F;
            break;
         default:
            var10001 = 8.1E-4F;
         }

         f *= var10001;
      }

      if (water && BlackOut.mc.field_1724.method_5777(FluidTags.field_15517) && !EnchantmentHelper.method_8200(BlackOut.mc.field_1724)) {
         f /= 5.0F;
      }

      if (onGround && !Managers.PACKET.isOnGround()) {
         f /= 5.0F;
      }

      return (double)f;
   }
}
