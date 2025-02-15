package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Clear extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Integer> minX;
   private final Setting<Integer> maxX;
   private final Setting<Integer> minY;
   private final Setting<Integer> maxY;
   private final Setting<Integer> minZ;
   private final Setting<Integer> maxZ;
   private final Setting<Double> timer;
   private final Setting<Integer> movement;
   private final Setting<Integer> maxMovements;
   private final Setting<Double> range;
   private boolean setTimer;
   private int x;
   private int y;
   private int z;
   private int sizeX;
   private int sizeY;
   private int sizeZ;
   private boolean directionX;
   private boolean directionZ;

   public Clear() {
      super("Clear", "Clears the spawn of a creative server.", SubCategory.MISC, true);
      this.minX = this.sgGeneral.i("Min X", -75, -100, 100, 1, ".");
      this.maxX = this.sgGeneral.i("Max X", 75, -100, 100, 1, ".");
      this.minY = this.sgGeneral.i("Min Y", 0, -65, 350, 1, ".");
      this.maxY = this.sgGeneral.i("Max Y", 100, -100, 100, 1, ".");
      this.minZ = this.sgGeneral.i("Min Z", -75, -100, 100, 1, ".");
      this.maxZ = this.sgGeneral.i("Max Z", 75, -100, 100, 1, ".");
      this.timer = this.sgGeneral.d("Timer", 5.0D, 1.0D, 10.0D, 0.1D, ".");
      this.movement = this.sgGeneral.i("Movement", 1, 1, 10, 1, ".");
      this.maxMovements = this.sgGeneral.i("Max Movements", 3, 1, 10, 1, ".");
      this.range = this.sgGeneral.d("Range", 6.0D, 1.0D, 10.0D, 0.1D, ".");
      this.setTimer = true;
      this.x = 0;
      this.y = 0;
      this.z = 0;
      this.directionX = false;
      this.directionZ = false;
   }

   public void onEnable() {
      this.x = 0;
      this.y = 0;
      this.z = 0;
      this.directionX = false;
      this.directionZ = false;
   }

   public void onDisable() {
      if (this.setTimer) {
         this.setTimer = false;
         Timer.reset();
      }

   }

   @Event
   public void onMove(MoveEvent.Pre event) {
      this.tick();
      event.set(this, 0.0D, 0.0D, 0.0D);
   }

   @Event
   public void onTickPre(TickEvent.Pre event) {
      this.setTimer = true;
      Timer.set(((Double)this.timer.get()).floatValue());
   }

   private void tick() {
      this.updateScale();

      for(int i = 0; i < (Integer)this.maxMovements.get(); ++i) {
         if (this.move()) {
            this.disable("done");
            return;
         }

         List<BlockPos> list = new ArrayList();
         this.find(list);
         if (!list.isEmpty()) {
            this.updatePos();
            this.mine(list);
            return;
         }
      }

      this.updatePos();
   }

   private void mine(List<BlockPos> list) {
      list.forEach(this::clickBlock);
   }

   private boolean move() {
      if (this.tickX()) {
         this.directionX = !this.directionX;
         this.x = this.directionX ? this.sizeX : 0;
         if (this.tickZ()) {
            this.directionZ = !this.directionZ;
            this.z = this.directionZ ? this.sizeZ : 0;
            return this.y++ >= this.sizeY;
         }
      }

      return false;
   }

   private boolean tickX() {
      if (this.directionX) {
         return --this.x < 0;
      } else {
         return ++this.x > this.sizeX;
      }
   }

   private boolean tickZ() {
      if (this.directionZ) {
         return --this.z < 0;
      } else {
         return ++this.z > this.sizeZ;
      }
   }

   private void updatePos() {
      Vec3d pos = this.getPos();
      BlackOut.mc.field_1724.method_33574(pos);
      this.sendPacket(new PositionAndOnGround(pos.field_1352, pos.field_1351, pos.field_1350, BlackOut.mc.field_1724.method_24828()));
   }

   private Vec3d getPos() {
      return new Vec3d((double)MathHelper.method_48781((float)this.x / (float)this.sizeX, (Integer)this.minX.get(), (Integer)this.maxX.get()), (double)MathHelper.method_48781((float)this.y / (float)this.sizeY, (Integer)this.minY.get(), (Integer)this.maxY.get()), (double)MathHelper.method_48781((float)this.z / (float)this.sizeZ, (Integer)this.minZ.get(), (Integer)this.maxZ.get()));
   }

   private void updateScale() {
      this.sizeX = (int)Math.ceil((double)((float)((Integer)this.maxX.get() - (Integer)this.minX.get()) / ((Integer)this.movement.get()).floatValue()));
      this.sizeY = (int)Math.ceil((double)((float)((Integer)this.maxY.get() - (Integer)this.minY.get()) / ((Integer)this.movement.get()).floatValue()));
      this.sizeZ = (int)Math.ceil((double)((float)((Integer)this.maxZ.get() - (Integer)this.minZ.get()) / ((Integer)this.movement.get()).floatValue()));
   }

   private void find(List<BlockPos> list) {
      Vec3d eyePos = this.getPos().method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()), 0.0D);
      BlockPos center = BlockPos.method_49638(eyePos);
      int r = (int)Math.ceil((Double)this.range.get());

      for(int x = -r; x <= r; ++x) {
         for(int y = -r; y <= r; ++y) {
            for(int z = -r; z <= r; ++z) {
               BlockPos pos = center.method_10069(x, y, z);
               if (!(eyePos.method_1025(pos.method_46558()) > (Double)this.range.get() * (Double)this.range.get()) && !(BlackOut.mc.field_1687.method_8320(pos).method_26204() instanceof AirBlock)) {
                  list.add(pos);
               }
            }
         }
      }

   }

   private void clickBlock(BlockPos pos) {
      this.sendSequenced((sequence) -> {
         return new PlayerActionC2SPacket(Action.field_12968, pos, Direction.field_11033, sequence);
      });
      BlackOut.mc.field_1687.method_8501(pos, Blocks.field_10124.method_9564());
   }
}
