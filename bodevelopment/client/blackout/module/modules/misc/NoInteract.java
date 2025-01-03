package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.interfaces.functional.SingleOut;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class NoInteract extends Module {
   private static NoInteract INSTANCE;
   private final SettingGroup sgBlocks = this.addGroup("Blocks");
   private final SettingGroup sgItems = this.addGroup("Items");
   private final SettingGroup sgEntity = this.addGroup("Entity");
   private final Setting<NoInteract.NoInteractFilterMode> filterMode;
   private final Setting<List<Item>> whenHolding;
   private final Setting<NoInteract.NoInteractFilterMode> blockFilterMode;
   private final Setting<List<Block>> blocks;
   private final Setting<NoInteract.IgnoreMode> ignoreMode;
   private final Setting<NoInteract.NoInteractFilterMode> itemFilterMode;
   private final Setting<List<Item>> items;
   private final Setting<NoInteract.NoInteractFilterMode> filterModeEntity;
   private final Setting<List<Item>> whenHoldingEntity;
   private final Setting<NoInteract.NoInteractFilterMode> entityFilterMode;
   private final Setting<List<EntityType<?>>> entities;

   public NoInteract() {
      super("No Interact", "Prevents interacting with blocks and entities.", SubCategory.MISC, false);
      this.filterMode = this.sgBlocks.e("Holding Filter Mode (Block)", NoInteract.NoInteractFilterMode.Cancel, ".");
      this.whenHolding = this.sgBlocks.il("When Holding (Block)", ".", Items.field_8367, Items.field_8463);
      this.blockFilterMode = this.sgBlocks.e("Block Filter Mode", NoInteract.NoInteractFilterMode.Cancel, ".");
      this.blocks = this.sgBlocks.bl("Blocks", ".");
      this.ignoreMode = this.sgBlocks.e("Ignore Mode", NoInteract.IgnoreMode.SneakBlocks, ".");
      this.itemFilterMode = this.sgItems.e("Item Filter Mode", NoInteract.NoInteractFilterMode.Cancel, ".");
      this.items = this.sgItems.il("Items", ".");
      this.filterModeEntity = this.sgEntity.e("Holding Filter Mode (Entity)", NoInteract.NoInteractFilterMode.Cancel, ".");
      this.whenHoldingEntity = this.sgEntity.il("When Holding (Entity)", ".", Items.field_8367, Items.field_8463);
      this.entityFilterMode = this.sgEntity.e("Entity Filter Mode", NoInteract.NoInteractFilterMode.Accept, ".");
      this.entities = this.sgEntity.el("Entities", ".");
      INSTANCE = this;
   }

   public static NoInteract getInstance() {
      return INSTANCE;
   }

   public ActionResult handleBlock(Hand hand, BlockPos pos, SingleOut<ActionResult> action) {
      Item item = BlackOut.mc.field_1724.method_5998(hand).method_7909();
      if (((NoInteract.NoInteractFilterMode)this.filterMode.get()).shouldAccept(item, this.whenHolding)) {
         return (ActionResult)action.get();
      } else {
         Block block = BlackOut.mc.field_1687.method_8320(pos).method_26204();
         if (((NoInteract.NoInteractFilterMode)this.blockFilterMode.get()).shouldAccept(block, this.blocks)) {
            return (ActionResult)action.get();
         } else {
            ActionResult actionResult;
            switch((NoInteract.IgnoreMode)this.ignoreMode.get()) {
            case Sneak:
               this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12979));
               actionResult = (ActionResult)action.get();
               this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12984));
               return actionResult;
            case SneakBlocks:
               if (!(item instanceof BlockItem)) {
                  return ActionResult.field_5811;
               }

               this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12979));
               actionResult = (ActionResult)action.get();
               this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12984));
               return actionResult;
            default:
               return ActionResult.field_5811;
            }
         }
      }
   }

   public ActionResult handleEntity(Hand hand, Entity entity, SingleOut<ActionResult> action) {
      Item item = BlackOut.mc.field_1724.method_5998(hand).method_7909();
      if (((NoInteract.NoInteractFilterMode)this.filterModeEntity.get()).shouldAccept(item, this.whenHoldingEntity)) {
         return (ActionResult)action.get();
      } else {
         return ((NoInteract.NoInteractFilterMode)this.entityFilterMode.get()).shouldAccept(entity.method_5864(), this.entities) ? (ActionResult)action.get() : ActionResult.field_5811;
      }
   }

   public ActionResult handleUse(Hand hand, SingleOut<ActionResult> action) {
      return ((NoInteract.NoInteractFilterMode)this.itemFilterMode.get()).shouldAccept(BlackOut.mc.field_1724.method_5998(hand).method_7909(), this.items) ? (ActionResult)action.get() : ActionResult.field_5811;
   }

   public static enum NoInteractFilterMode {
      Cancel,
      Accept;

      private <T> boolean shouldAccept(T item, Setting<List<T>> list) {
         if (this == Cancel) {
            return !((List)list.get()).contains(item);
         } else {
            return ((List)list.get()).contains(item);
         }
      }

      // $FF: synthetic method
      private static NoInteract.NoInteractFilterMode[] $values() {
         return new NoInteract.NoInteractFilterMode[]{Cancel, Accept};
      }
   }

   public static enum IgnoreMode {
      Cancel,
      Sneak,
      SneakBlocks;

      // $FF: synthetic method
      private static NoInteract.IgnoreMode[] $values() {
         return new NoInteract.IgnoreMode[]{Cancel, Sneak, SneakBlocks};
      }
   }
}
