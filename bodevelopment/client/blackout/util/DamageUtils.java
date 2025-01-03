package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.interfaces.mixin.IRaycastContext;
import bodevelopment.client.blackout.interfaces.mixin.IVec3d;
import bodevelopment.client.blackout.manager.Managers;
import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.enchantment.ProtectionEnchantment.Type;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.apache.commons.lang3.mutable.MutableInt;

public class DamageUtils {
   public static RaycastContext raycastContext;

   public static double crystalDamage(LivingEntity entity, Box box, Vec3d pos) {
      return crystalDamage(entity, box, pos, (BlockPos)null);
   }

   public static double crystalDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos) {
      return explosionDamage(entity, box, pos, ignorePos, 6.0D);
   }

   public static double anchorDamage(LivingEntity entity, Box box, Vec3d pos) {
      return explosionDamage(entity, box, pos, (BlockPos)null, 5.0D);
   }

   public static double anchorDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos) {
      return explosionDamage(entity, box, pos, ignorePos, 5.0D);
   }

   public static double creeperDamage(LivingEntity entity, Box box, Vec3d pos) {
      return explosionDamage(entity, box, pos, (BlockPos)null, 3.0D);
   }

   public static double creeperDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos) {
      return explosionDamage(entity, box, pos, ignorePos, 3.0D);
   }

   public static double chargedCreeperDamage(LivingEntity entity, Box box, Vec3d pos) {
      return explosionDamage(entity, box, pos, (BlockPos)null, 6.0D);
   }

   public static double chargedCreeperDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos) {
      return explosionDamage(entity, box, pos, ignorePos, 6.0D);
   }

   private static double explosionDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, double strength) {
      double q = strength * 2.0D;
      double dist = BoxUtils.feet(box).method_1022(pos) / q;
      if (dist > 1.0D) {
         return 0.0D;
      } else {
         double aa = getExposure(pos, box, ignorePos);
         double ab = (1.0D - dist) * aa;
         double damage = (double)((float)((int)((ab * ab + ab) * 3.5D * q + 1.0D)));
         damage = difficultyDamage(damage);
         damage = applyArmor(entity, damage);
         damage = applyResistance(entity, damage);
         damage = applyProtection(entity, damage, true);
         return damage;
      }
   }

   public static int getProtectionAmount(Iterable<ItemStack> equipment, boolean explosion) {
      MutableInt mint = new MutableInt();
      Iterator var3 = equipment.iterator();

      while(true) {
         ItemStack stack;
         do {
            if (!var3.hasNext()) {
               return mint.intValue();
            }

            stack = (ItemStack)var3.next();
         } while(stack.method_7960());

         NbtList nbtList = stack.method_7921();

         for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.method_10602(i);
            Registries.field_41176.method_17966(EnchantmentHelper.method_37427(nbtCompound)).ifPresent((enchantment) -> {
               if (enchantment instanceof ProtectionEnchantment) {
                  ProtectionEnchantment protection = (ProtectionEnchantment)enchantment;
                  if (protection.field_9133 == Type.field_9138) {
                     mint.add(EnchantmentHelper.method_37424(nbtCompound));
                  } else if (explosion && protection.field_9133 == Type.field_9141) {
                     mint.add(EnchantmentHelper.method_37424(nbtCompound) * 2);
                  }
               }

            });
         }
      }
   }

   public static double difficultyDamage(double damage) {
      if (BlackOut.mc.field_1687.method_8407() == Difficulty.field_5805) {
         return Math.min(damage / 2.0D + 1.0D, damage);
      } else {
         return BlackOut.mc.field_1687.method_8407() == Difficulty.field_5802 ? damage : damage * 1.5D;
      }
   }

   public static double applyArmor(LivingEntity entity, double damage) {
      double armor = (double)entity.method_6096();
      double f = 2.0D + entity.method_26825(EntityAttributes.field_23725) / 4.0D;
      return damage * (1.0D - MathHelper.method_15350(armor - damage / f, armor * 0.2D, 20.0D) / 25.0D);
   }

   public static double applyResistance(LivingEntity entity, double damage) {
      int amplifier = entity.method_6059(StatusEffects.field_5907) ? entity.method_6112(StatusEffects.field_5907).method_5578() : 0;
      int j = 25 - (amplifier + 1) * 5;
      return Math.max(damage * (double)j / 25.0D, 0.0D);
   }

   public static double applyProtection(LivingEntity entity, double damage, boolean explosions) {
      int i = getProtectionAmount(entity.method_5661(), explosions);
      if (i > 0) {
         damage *= (double)(1.0F - MathHelper.method_15363((float)i, 0.0F, 20.0F) / 25.0F);
      }

      return damage;
   }

   public static double getExposure(Vec3d source, Box box, BlockPos ignorePos) {
      ((IRaycastContext)raycastContext).blackout_Client$set(ShapeType.field_17558, FluidHandling.field_1348, BlackOut.mc.field_1724);
      ((IRaycastContext)raycastContext).blackout_Client$setStart(source);
      Vec3d vec3d = new Vec3d(0.0D, 0.0D, 0.0D);
      double lx = box.method_17939();
      double ly = box.method_17940();
      double lz = box.method_17941();
      double deltaX = 1.0D / (lx * 2.0D + 1.0D);
      double deltaY = 1.0D / (ly * 2.0D + 1.0D);
      double deltaZ = 1.0D / (lz * 2.0D + 1.0D);
      double offsetX = (1.0D - Math.floor(1.0D / deltaX) * deltaX) / 2.0D;
      double offsetZ = (1.0D - Math.floor(1.0D / deltaZ) * deltaZ) / 2.0D;
      double stepX = deltaX * lx;
      double stepY = deltaY * ly;
      double stepZ = deltaZ * lz;
      if (!(stepX < 0.0D) && !(stepY < 0.0D) && !(stepZ < 0.0D)) {
         float i = 0.0F;
         float j = 0.0F;
         double x = box.field_1323 + offsetX;

         for(double maxX = box.field_1320 + offsetX; x <= maxX; x += stepX) {
            ((IVec3d)vec3d).blackout_Client$setX(x);

            for(double y = box.field_1322; y <= box.field_1325; y += stepY) {
               ((IVec3d)vec3d).blackout_Client$setY(y);
               double z = box.field_1321 + offsetZ;

               for(double maxZ = box.field_1324 + offsetZ; z <= maxZ; z += stepZ) {
                  ((IVec3d)vec3d).blackout_Client$setZ(z);
                  ((IRaycastContext)raycastContext).blackout_Client$setEnd(vec3d);
                  if (raycast(raycastContext, true, ignorePos).method_17783() == net.minecraft.util.hit.HitResult.Type.field_1333) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (double)(i / j);
      } else {
         return 0.0D;
      }
   }

   public static BlockHitResult raycast(RaycastContext context, boolean damage) {
      return raycast(context, damage, (BlockPos)null);
   }

   public static BlockHitResult raycast(RaycastContext context, boolean damage, BlockPos ignorePos) {
      return (BlockHitResult)BlockView.method_17744(context.method_17750(), context.method_17747(), context, (contextx, pos) -> {
         BlockState blockState;
         if (pos.equals(ignorePos)) {
            blockState = Blocks.field_10124.method_9564();
         } else if (damage) {
            if (BlackOut.mc.field_1687.method_8320(pos).method_26204().method_9520() < 200.0F) {
               blockState = Blocks.field_10124.method_9564();
            } else {
               blockState = Managers.BLOCK.damageState(pos);
            }
         } else {
            blockState = BlackOut.mc.field_1687.method_8320(pos);
         }

         Vec3d vec3d = contextx.method_17750();
         Vec3d vec3d2 = contextx.method_17747();
         VoxelShape voxelShape = contextx.method_17748(blockState, BlackOut.mc.field_1687, pos);
         return BlackOut.mc.field_1687.method_17745(vec3d, vec3d2, pos, voxelShape, blockState);
      }, (contextx) -> {
         Vec3d vec3d = contextx.method_17750().method_1020(contextx.method_17747());
         return BlockHitResult.method_17778(contextx.method_17747(), Direction.method_10142(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350), BlockPos.method_49638(contextx.method_17747()));
      });
   }

   public static double itemDamage(ItemStack stack) {
      if (stack.method_7960()) {
         return 1.0D;
      } else {
         double damage = 1.0D;
         Item var6 = stack.method_7909();
         if (var6 instanceof MiningToolItem) {
            MiningToolItem miningTool = (MiningToolItem)var6;
            damage += (double)miningTool.method_26366();
         } else {
            var6 = stack.method_7909();
            if (var6 instanceof SwordItem) {
               SwordItem sword = (SwordItem)var6;
               damage += (double)sword.method_8020();
            } else {
               var6 = stack.method_7909();
               if (var6 instanceof ToolItem) {
                  ToolItem tool = (ToolItem)var6;
                  damage += (double)tool.method_8022().method_8028();
               }
            }
         }

         if (EnchantmentHelper.method_8222(stack).containsKey(Enchantments.field_9118)) {
            damage += (double)((Integer)EnchantmentHelper.method_8222(stack).get(Enchantments.field_9118) + 1) * 0.5D;
         }

         return damage;
      }
   }
}
