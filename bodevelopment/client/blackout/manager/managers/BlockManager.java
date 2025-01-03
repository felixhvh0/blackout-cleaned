package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.BlockStateEvent;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.module.modules.misc.Simulation;
import bodevelopment.client.blackout.randomstuff.timers.TimerMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockManager extends Manager {
   private final TimerMap<BlockPos, BlockManager.SpoofedBlock> timers = new TimerMap(true);

   public void set(BlockPos pos, Block type, boolean damage, boolean placing) {
      this.timers.add(pos, new BlockManager.SpoofedBlock(type, damage, placing), 1.0D);
   }

   public void reset(BlockPos pos) {
      if (this.timers.containsKey(pos)) {
         this.timers.removeKey(pos);
      }

   }

   public BlockState damageState(BlockPos pos) {
      if (Simulation.getInstance().blocks() && this.timers.containsKey(pos)) {
         BlockManager.SpoofedBlock block = (BlockManager.SpoofedBlock)this.timers.get(pos);
         if (block != null && block.damage()) {
            return block.type().method_9564();
         }
      }

      return BlackOut.mc.field_1687.method_8320(pos);
   }

   public BlockState blockState(BlockPos pos) {
      if (Simulation.getInstance().blocks() && this.timers.containsKey(pos)) {
         BlockManager.SpoofedBlock block = (BlockManager.SpoofedBlock)this.timers.get(pos);
         if (block != null && block.placing()) {
            return block.type().method_9564();
         }
      }

      return BlackOut.mc.field_1687.method_8320(pos);
   }

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return false;
      });
   }

   @Event
   public void onBlock(BlockStateEvent event) {
      this.reset(event.pos);
   }

   private static record SpoofedBlock(Block type, boolean damage, boolean placing) {
      private SpoofedBlock(Block type, boolean damage, boolean placing) {
         this.type = type;
         this.damage = damage;
         this.placing = placing;
      }

      public Block type() {
         return this.type;
      }

      public boolean damage() {
         return this.damage;
      }

      public boolean placing() {
         return this.placing;
      }
   }
}
