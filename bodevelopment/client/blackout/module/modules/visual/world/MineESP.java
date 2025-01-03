package bodevelopment.client.blackout.module.modules.visual.world;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.module.setting.multisettings.BoxMultiSetting;
import bodevelopment.client.blackout.randomstuff.timers.RenderList;
import bodevelopment.client.blackout.util.BlockUtils;
import java.util.Objects;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public class MineESP extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgRender = this.addGroup("Render");
   private final Setting<Double> range;
   private final Setting<Boolean> accurateTime;
   private final Setting<ToolMaterials> pickaxeMaterial;
   private final Setting<Integer> pickaxeEfficiency;
   private final Setting<Double> fadeIn;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeOut;
   private final BoxMultiSetting box;
   private final RenderList<Triple<BlockPos, Integer, Double>> renders;

   public MineESP() {
      super("Mine ESP", "Renders a box at blocks being mined by other players.", SubCategory.WORLD, true);
      this.range = this.sgGeneral.d("Range", 10.0D, 0.0D, 50.0D, 0.5D, "Only renders inside this range.");
      this.accurateTime = this.sgGeneral.b("Accurate Time", false, ".");
      SettingGroup var10001 = this.sgGeneral;
      ToolMaterials var10003 = ToolMaterials.field_22033;
      Setting var10005 = this.accurateTime;
      Objects.requireNonNull(var10005);
      this.pickaxeMaterial = var10001.e("Pickaxe Material", var10003, ".", var10005::get);
      var10001 = this.sgGeneral;
      Setting var10008 = this.accurateTime;
      Objects.requireNonNull(var10008);
      this.pickaxeEfficiency = var10001.i("Pickaxe Efficiency", 5, 0, 5, 1, ".", var10008::get);
      this.fadeIn = this.sgGeneral.d("Fade In Time", 2.0D, 0.0D, 20.0D, 0.1D, ".", () -> {
         return !(Boolean)this.accurateTime.get();
      });
      this.renderTime = this.sgGeneral.d("Render Time", 4.0D, 0.0D, 20.0D, 0.1D, ".");
      this.fadeOut = this.sgGeneral.d("Fade Out Time", 2.0D, 0.0D, 20.0D, 0.1D, ".");
      this.box = BoxMultiSetting.of(this.sgRender);
      this.renders = RenderList.getList(false);
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         this.renders.update((triple, time, delta) -> {
            double distanceDelta = MathHelper.method_15350(MathHelper.method_15370(BlackOut.mc.field_1724.method_33571().method_1022(((BlockPos)triple.getLeft()).method_46558()), (Double)this.range.get() + 2.0D, (Double)this.range.get()), 0.0D, 1.0D);
            double fadeIn = (Double)triple.getRight();
            double scaleDelta;
            double fadeDelta;
            if (time <= fadeIn) {
               scaleDelta = fadeDelta = time / fadeIn;
            } else if (time >= fadeIn + (Double)this.renderTime.get()) {
               scaleDelta = 1.0D;
               fadeDelta = 1.0D - (time - fadeIn - (Double)this.renderTime.get()) / (Double)this.fadeOut.get();
            } else {
               scaleDelta = 1.0D;
               fadeDelta = 1.0D;
            }

            double colorDelta = MathHelper.method_15350(fadeDelta * distanceDelta, 0.0D, 1.0D);
            this.box.render(this.getBox((BlockPos)triple.getLeft(), scaleDelta));
         });
      }
   }

   @Event
   public void onReceive(PacketEvent.Receive.Pre event) {
      if (BlackOut.mc.field_1687 != null) {
         Packet var3 = event.packet;
         if (var3 instanceof BlockBreakingProgressS2CPacket) {
            BlockBreakingProgressS2CPacket packet = (BlockBreakingProgressS2CPacket)var3;
            if (!this.contains(packet) && BlockUtils.mineable(packet.method_11277())) {
               this.renders.remove((timer) -> {
                  return (Integer)((Triple)timer.value).getMiddle() == packet.method_11280();
               });
               double fadeIn = this.getFadeIn(packet.method_11277());
               this.renders.add(new ImmutableTriple(packet.method_11277(), packet.method_11280(), fadeIn), fadeIn + (Double)this.renderTime.get() + (Double)this.fadeOut.get());
            }
         }
      }

   }

   private double getFadeIn(BlockPos pos) {
      return !(Boolean)this.accurateTime.get() ? (Double)this.fadeIn.get() : 1.0D / BlockUtils.getBlockBreakingDelta(pos, this.getPickaxeStack(), false, false, false) / 20.0D;
   }

   private ItemStack getPickaxeStack() {
      ItemStack var10000;
      switch((ToolMaterials)this.pickaxeMaterial.get()) {
      case field_8922:
         var10000 = new ItemStack(Items.field_8647);
         break;
      case field_8927:
         var10000 = new ItemStack(Items.field_8387);
         break;
      case field_8923:
         var10000 = new ItemStack(Items.field_8403);
         break;
      case field_8930:
         var10000 = new ItemStack(Items.field_8377);
         break;
      case field_8929:
         var10000 = new ItemStack(Items.field_8335);
         break;
      case field_22033:
         var10000 = new ItemStack(Items.field_22024);
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      ItemStack stack = var10000;
      if ((Integer)this.pickaxeEfficiency.get() > 0) {
         stack.method_7978(Enchantments.field_9131, (Integer)this.pickaxeEfficiency.get());
      }

      return stack;
   }

   private boolean contains(BlockBreakingProgressS2CPacket packet) {
      return this.renders.contains((timer) -> {
         return ((BlockPos)((Triple)timer.value).getLeft()).equals(packet.method_11277()) && (Integer)((Triple)timer.value).getMiddle() == packet.method_11280();
      });
   }

   private Box getBox(BlockPos pos, double progress) {
      return new Box((double)pos.method_10263() + 0.5D - progress / 2.0D, (double)pos.method_10264() + 0.5D - progress / 2.0D, (double)pos.method_10260() + 0.5D - progress / 2.0D, (double)pos.method_10263() + 0.5D + progress / 2.0D, (double)pos.method_10264() + 0.5D + progress / 2.0D, (double)pos.method_10260() + 0.5D + progress / 2.0D);
   }
}
