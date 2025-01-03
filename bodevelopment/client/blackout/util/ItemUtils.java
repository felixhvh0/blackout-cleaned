package bodevelopment.client.blackout.util;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUtils {
   public static double getArmorValue(ItemStack stack) {
      Item var2 = stack.method_7909();
      if (var2 instanceof ArmorItem) {
         ArmorItem armor = (ArmorItem)var2;
         double value = (double)armor.method_7687();
         value += (double)(armor.method_26353() / 4.0F);
         value += (double)(armor.method_7686().method_24355() / 3.0F);
         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9111)) {
            value += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9111) + 1);
         }

         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9095)) {
            value += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9095) + 1) * 0.05D;
         }

         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9129)) {
            value += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9129) + 1) * 0.1D;
         }

         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9107)) {
            value += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9107) + 1) * 0.05D;
         }

         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9096)) {
            value += ((double)(Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9096) + 0.1D) * 0.15D;
         }

         return value;
      } else {
         return 0.0D;
      }
   }

   public static double getPickaxeValue(ItemStack stack) {
      float f = stack.method_7924(Blocks.field_10340.method_9564());
      if (f > 1.0F && !stack.method_7960() && EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9131)) {
         int i = (Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9131);
         if (i > 0 && !stack.method_7960()) {
            f += (float)(i * i + 1);
         }
      }

      return (double)f;
   }

   public static double getAxeValue(ItemStack stack) {
      float f = stack.method_7924(Blocks.field_10431.method_9564());
      if (f > 1.0F && !stack.method_7960() && EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9131)) {
         int i = (Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9131);
         if (i > 0 && !stack.method_7960()) {
            f += (float)(i * i + 1);
         }
      }

      return (double)f;
   }

   public static double getWeaponValue(ItemStack stack) {
      double damage = DamageUtils.itemDamage(stack);
      if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9124)) {
         damage += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9124) + 1) * 0.1D;
      }

      if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9121)) {
         damage += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9121) + 1) * 0.05D;
      }

      return damage;
   }

   public static double getBowValue(ItemStack stack) {
      int power = EnchantmentHelper.method_8225(Enchantments.field_9103, stack);
      double damage;
      if (power > 0) {
         damage = 2.5D + (double)power * 0.5D;
      } else {
         damage = 2.0D;
      }

      int punch = EnchantmentHelper.method_8225(Enchantments.field_9116, stack);
      return damage + (double)punch * 0.3D;
   }

   public static double getElytraValue(ItemStack stack) {
      return (double)((EnchantmentHelper.method_8225(Enchantments.field_9101, stack) > 0 ? 100 : 0) + EnchantmentHelper.method_8225(Enchantments.field_9119, stack));
   }
}
