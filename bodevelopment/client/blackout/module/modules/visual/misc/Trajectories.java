package bodevelopment.client.blackout.module.modules.visual.misc;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.interfaces.functional.DoubleConsumer;
import bodevelopment.client.blackout.interfaces.functional.DoubleFunction;
import bodevelopment.client.blackout.interfaces.mixin.IRaycastContext;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.combat.offensive.BowSpam;
import bodevelopment.client.blackout.module.modules.visual.entities.Trails;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.util.ColorUtils;
import bodevelopment.client.blackout.util.DamageUtils;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import bodevelopment.client.blackout.util.RotationUtils;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Trajectories extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgColor = this.addGroup("Color");
   private final Setting<Integer> maxTicks;
   private final Setting<Double> fadeLength;
   private final Setting<Boolean> playerVelocity;
   public final Setting<Trails.ColorMode> colorMode;
   private final Setting<Double> saturation;
   private final Setting<BlackOutColor> clr;
   private final Setting<BlackOutColor> clr1;
   private final Map<Item, Trajectories.SimulationData> dataMap;

   public Trajectories() {
      super("Trajectories", "Draws a trajectory when holding throwable items or a bow.", SubCategory.MISC_VISUAL, true);
      this.maxTicks = this.sgGeneral.i("Max Ticks", 500, 0, 500, 5, ".");
      this.fadeLength = this.sgColor.d("Fade Length", 1.0D, 0.0D, 10.0D, 0.1D, ".");
      this.playerVelocity = this.sgGeneral.b("Player Velocity", true, ".");
      this.colorMode = this.sgColor.e("Color Mode", Trails.ColorMode.Custom, "What color to use");
      this.saturation = this.sgColor.d("Rainbow Saturation", 0.8D, 0.0D, 1.0D, 0.1D, ".", () -> {
         return this.colorMode.get() == Trails.ColorMode.Rainbow;
      });
      this.clr = this.sgColor.c("Line Color", new BlackOutColor(255, 255, 255, 255), ".", () -> {
         return this.colorMode.get() != Trails.ColorMode.Rainbow;
      });
      this.clr1 = this.sgColor.c("Wave Color", new BlackOutColor(175, 175, 175, 255), ".", () -> {
         return this.colorMode.get() != Trails.ColorMode.Rainbow;
      });
      this.dataMap = new HashMap();
      this.initMap();
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         ItemStack itemStack = BlackOut.mc.field_1724.method_6047();
         Item item = itemStack.method_7909();
         if (this.dataMap.containsKey(item)) {
            Trajectories.SimulationData data = (Trajectories.SimulationData)this.dataMap.get(item);
            MatrixStack stack = Render3DUtils.matrices;
            stack.method_22903();
            Render3DUtils.setRotation(stack);
            Render3DUtils.start();
            float yaw = Managers.ROTATION.getNextYaw();
            this.draw(data, this.getVelocity((double[])data.speed.apply(itemStack), yaw, 0.0D), itemStack, event.tickDelta, stack);
            if (this.hasMulti(itemStack)) {
               this.draw(data, this.getVelocity((double[])data.speed.apply(itemStack), yaw, -10.0D), itemStack, event.tickDelta, stack);
               this.draw(data, this.getVelocity((double[])data.speed.apply(itemStack), yaw, 10.0D), itemStack, event.tickDelta, stack);
            }

            Render3DUtils.end();
            stack.method_22909();
         }
      }
   }

   private void rotateVelocity(double[] velocity, Vec3d opposite, double yaw) {
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(yaw * 0.01745329238474369D, opposite.field_1352, opposite.field_1351, opposite.field_1350);
      Vec3d velocityVec = new Vec3d(velocity[0], velocity[1], velocity[2]);
      Vector3f vector3f = velocityVec.method_46409().rotate(quaternionf);
      velocity[0] = (double)vector3f.x;
      velocity[1] = (double)vector3f.y;
      velocity[2] = (double)vector3f.z;
   }

   private boolean hasMulti(ItemStack itemStack) {
      if (!(itemStack.method_7909() instanceof CrossbowItem)) {
         return false;
      } else {
         return EnchantmentHelper.method_8225(Enchantments.field_9108, itemStack) > 0;
      }
   }

   private void draw(Trajectories.SimulationData data, double[] velocity, ItemStack itemStack, float tickDelta, MatrixStack stack) {
      HitResult hitResult = this.drawLine(data, velocity, itemStack, tickDelta, stack);
      if (hitResult != null) {
         Matrix4f matrix4f = stack.method_23760().method_23761();
         Vec3d camPos = BlackOut.mc.field_1773.method_19418().method_19326();
         RenderSystem.setShader(GameRenderer::method_34540);
         Tessellator tessellator = Tessellator.method_1348();
         BufferBuilder bufferBuilder = tessellator.method_1349();
         Color color = this.getColor();
         float r = (float)color.getRed() / 255.0F;
         float g = (float)color.getGreen() / 255.0F;
         float b = (float)color.getBlue() / 255.0F;
         float a = (float)color.getAlpha() / 255.0F;
         if (hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);
            Vec3d pos = blockHitResult.method_17784().method_1020(camPos);
            double width = 0.25D;
            switch(blockHitResult.method_17780()) {
            case field_11033:
            case field_11036:
               this.renderCircle(bufferBuilder, matrix4f, (rad) -> {
                  return (float)(pos.field_1352 + Math.cos(rad) * width);
               }, (rad) -> {
                  return (float)pos.field_1351;
               }, (rad) -> {
                  return (float)(pos.field_1350 + Math.sin(rad) * width);
               }, r, g, b, a);
               break;
            case field_11043:
            case field_11035:
               this.renderCircle(bufferBuilder, matrix4f, (rad) -> {
                  return (float)(pos.field_1352 + Math.cos(rad) * width);
               }, (rad) -> {
                  return (float)(pos.field_1351 + Math.sin(rad) * width);
               }, (rad) -> {
                  return (float)pos.field_1350;
               }, r, g, b, a);
               break;
            case field_11039:
            case field_11034:
               this.renderCircle(bufferBuilder, matrix4f, (rad) -> {
                  return (float)pos.field_1352;
               }, (rad) -> {
                  return (float)(pos.field_1351 + Math.cos(rad) * width);
               }, (rad) -> {
                  return (float)(pos.field_1350 + Math.sin(rad) * width);
               }, r, g, b, a);
            }
         } else if (hitResult instanceof EntityHitResult) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            bufferBuilder.method_1328(DrawMode.field_29344, VertexFormats.field_1576);
            Box box = OLEPOSSUtils.getLerpedBox(entityHitResult.method_17782(), (double)tickDelta).method_989(-camPos.field_1352, -camPos.field_1351, -camPos.field_1350);
            Render3DUtils.drawOutlines(stack, bufferBuilder, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324, r, g, b, a);
         }

         tessellator.method_1350();
      }
   }

   private void renderCircle(BufferBuilder bufferBuilder, Matrix4f matrix4f, Function<Double, Float> x, Function<Double, Float> y, Function<Double, Float> z, float r, float g, float b, float a) {
      for(double ar = 0.0D; ar <= 360.0D; ar += 9.0D) {
         double rad = Math.toRadians(ar);
         bufferBuilder.method_22918(matrix4f, (Float)x.apply(rad), (Float)y.apply(rad), (Float)z.apply(rad)).method_22915(r, g, b, a).method_1344();
      }

   }

   private HitResult drawLine(Trajectories.SimulationData data, double[] velocity, ItemStack itemStack, float tickDelta, MatrixStack stack) {
      Vec3d pos = (Vec3d)data.startPos.apply(itemStack, tickDelta);
      Matrix4f matrix4f = stack.method_23760().method_23761();
      RenderSystem.setShader(GameRenderer::method_34540);
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder bufferBuilder = tessellator.method_1349();
      bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);
      MutableDouble dist = new MutableDouble(0.0D);
      this.vertex(bufferBuilder, matrix4f, pos, pos, dist);
      Box box = this.getBox(pos, data);

      for(int i = 0; i < (Integer)this.maxTicks.get(); ++i) {
         Vec3d prevPos = pos;
         pos = pos.method_1031(velocity[0], velocity[1], velocity[2]);
         ((IRaycastContext)DamageUtils.raycastContext).blackout_Client$set(prevPos, pos, ShapeType.field_17558, FluidHandling.field_1348, BlackOut.mc.field_1724);
         HitResult blockHitResult = DamageUtils.raycast(DamageUtils.raycastContext, false);
         EntityHitResult entityHitResult = ProjectileUtil.method_37226(BlackOut.mc.field_1687, BlackOut.mc.field_1724, prevPos, pos, box.method_1012(velocity[0], velocity[1], velocity[2]).method_1014(1.0D), (entity) -> {
            return entity != BlackOut.mc.field_1724 && this.canHit(entity);
         }, 0.3F);
         boolean blockValid = blockHitResult.method_17783() != Type.field_1333;
         boolean entityValid = entityHitResult != null && entityHitResult.method_17783() == Type.field_1331;
         Object hitResult;
         if (blockValid && entityValid) {
            if (prevPos.method_1022(entityHitResult.method_17784()) < prevPos.method_1022(blockHitResult.method_17784())) {
               hitResult = entityHitResult;
            } else {
               hitResult = blockHitResult;
            }
         } else if (blockValid) {
            hitResult = blockHitResult;
         } else if (entityValid) {
            hitResult = entityHitResult;
         } else {
            hitResult = null;
         }

         if (hitResult != null) {
            this.vertex(bufferBuilder, matrix4f, ((HitResult)hitResult).method_17784(), prevPos, dist);
            tessellator.method_1350();
            return (HitResult)hitResult;
         }

         data.physics.accept(box, velocity);
         box = this.getBox(pos, data);
         this.vertex(bufferBuilder, matrix4f, pos, prevPos, dist);
      }

      tessellator.method_1350();
      return null;
   }

   private void vertex(BufferBuilder bufferBuilder, Matrix4f matrix4f, Vec3d pos, Vec3d prevPos, MutableDouble dist) {
      DoubleConsumer<Vec3d, Double> consumer = (vec, d) -> {
         Color color = this.withAlpha(this.getColor(), this.getAlpha(d));
         Vec3d camPos = BlackOut.mc.field_1773.method_19418().method_19326();
         bufferBuilder.method_22918(matrix4f, (float)(vec.field_1352 - camPos.field_1352), (float)(vec.field_1351 - camPos.field_1351), (float)(vec.field_1350 - camPos.field_1350)).method_22915((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F).method_1344();
      };
      double totalDist = prevPos.method_1022(pos);
      if (dist.getValue() <= (Double)this.fadeLength.get()) {
         for(double i = 1.0D; i < 30.0D; ++i) {
            double delta = i / 30.0D;
            consumer.accept(prevPos.method_35590(pos, delta), dist.getValue() + i / 30.0D * totalDist);
         }
      } else {
         consumer.accept(pos, dist.getValue());
      }

      dist.add(totalDist);
   }

   private Color withAlpha(Color color, float alpha) {
      return new Color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F * alpha);
   }

   private float getAlpha(double dist) {
      return (float)Math.min(dist / (Double)this.fadeLength.get(), 1.0D);
   }

   private Box getBox(Vec3d pos, Trajectories.SimulationData data) {
      return new Box(pos.field_1352 - data.width / 2.0D, pos.field_1351, pos.field_1350 - data.width / 2.0D, pos.field_1352 + data.width / 2.0D, pos.field_1351 + data.height, pos.field_1350 + data.width / 2.0D);
   }

   private boolean canHit(Entity entity) {
      if (!entity.method_49108()) {
         return false;
      } else {
         return !BlackOut.mc.field_1724.method_5794(entity);
      }
   }

   private Color getColor() {
      Color color = Color.WHITE;
      switch((Trails.ColorMode)this.colorMode.get()) {
      case Custom:
         color = ((BlackOutColor)this.clr.get()).getColor();
         break;
      case Rainbow:
         int rainbowColor = ColorUtils.getRainbow(4.0F, ((Double)this.saturation.get()).floatValue(), 1.0F, 150L);
         color = new Color(rainbowColor >> 16 & 255, rainbowColor >> 8 & 255, rainbowColor & 255, ((BlackOutColor)this.clr.get()).alpha);
         break;
      case Wave:
         color = ColorUtils.getWave(((BlackOutColor)this.clr.get()).getColor(), ((BlackOutColor)this.clr1.get()).getColor(), 1.0D, 1.0D, 1);
      }

      return color;
   }

   private double[] getVelocity(double[] d, float yaw, double simulation) {
      double[] velocity = new double[]{(double)(-MathHelper.method_15374(yaw * 0.017453292F) * MathHelper.method_15362(Managers.ROTATION.getNextPitch() * 0.017453292F)), (double)(-MathHelper.method_15374((Managers.ROTATION.getNextPitch() + (float)d[1]) * 0.017453292F)), (double)(MathHelper.method_15362(yaw * 0.017453292F) * MathHelper.method_15362(Managers.ROTATION.getNextPitch() * 0.017453292F))};
      if (simulation != 0.0D) {
         this.rotateVelocity(velocity, RotationUtils.rotationVec((double)yaw, (double)(Managers.ROTATION.getNextPitch() - 90.0F), 1.0D), simulation);
      }

      velocity[0] *= d[0];
      velocity[1] *= d[0];
      velocity[2] *= d[0];
      if ((Boolean)this.playerVelocity.get()) {
         velocity[0] += BlackOut.mc.field_1724.method_18798().field_1352;
         if (!BlackOut.mc.field_1724.method_24828()) {
            velocity[1] += BlackOut.mc.field_1724.method_18798().field_1351;
         }

         velocity[2] += BlackOut.mc.field_1724.method_18798().field_1350;
      }

      return velocity;
   }

   private void initMap() {
      double[] snowball = new double[]{1.5D, 0.0D};
      double[] exp = new double[]{0.7D, -20.0D};
      this.put(0.25D, 0.25D, (stack, tickDelta) -> {
         return OLEPOSSUtils.getLerpedPos(BlackOut.mc.field_1724, (double)tickDelta).method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()) - 0.1D, 0.0D);
      }, (stack) -> {
         return snowball;
      }, (box, vel) -> {
         double f = OLEPOSSUtils.inWater(box) ? 0.8D : 0.99D;
         vel[0] *= f;
         vel[1] *= f;
         vel[2] *= f;
         vel[1] -= 0.03D;
      }, Items.field_8543, Items.field_8803);
      this.put(0.5D, 0.5D, (stack, tickDelta) -> {
         return OLEPOSSUtils.getLerpedPos(BlackOut.mc.field_1724, (double)tickDelta).method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()) - 0.1D, 0.0D);
      }, (stack) -> {
         BowSpam bowSpam = BowSpam.getInstance();
         int i;
         if (bowSpam.enabled && BlackOut.mc.field_1690.field_1904.method_1434()) {
            i = (Integer)bowSpam.charge.get();
         } else {
            i = stack.method_7935() - BlackOut.mc.field_1724.method_6014();
         }

         float f = Math.max(BowItem.method_7722(i), 0.1F);
         return new double[]{(double)f * 3.0D, 0.0D};
      }, (box, vel) -> {
         double f = OLEPOSSUtils.inWater(box) ? 0.6D : 0.99D;
         vel[0] *= f;
         vel[1] *= f;
         vel[2] *= f;
         vel[1] -= 0.05D;
      }, Items.field_8102);
      this.put(0.5D, 0.5D, (stack, tickDelta) -> {
         return OLEPOSSUtils.getLerpedPos(BlackOut.mc.field_1724, (double)tickDelta).method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()) - (CrossbowItem.method_7772(stack, Items.field_8639) ? 0.15D : 0.1D), 0.0D);
      }, (stack) -> {
         return new double[]{CrossbowItem.method_7772(stack, Items.field_8639) ? 1.6D : 3.15D, 0.0D};
      }, (box, vel) -> {
         if (!CrossbowItem.method_7772(BlackOut.mc.field_1724.method_6047(), Items.field_8639)) {
            double f = OLEPOSSUtils.inWater(box) ? 0.6D : 0.99D;
            vel[0] *= f;
            vel[1] *= f;
            vel[2] *= f;
            vel[1] -= 0.05D;
         }
      }, Items.field_8399);
      this.put(0.25D, 0.25D, (stack, tickDelta) -> {
         return OLEPOSSUtils.getLerpedPos(BlackOut.mc.field_1724, (double)tickDelta).method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()) - 0.1D, 0.0D);
      }, (stack) -> {
         return exp;
      }, (box, vel) -> {
         double f = OLEPOSSUtils.inWater(box) ? 0.8D : 0.99D;
         vel[0] *= f;
         vel[1] *= f;
         vel[2] *= f;
         vel[1] -= 0.07D;
      }, Items.field_8287);
      this.put(0.25D, 0.25D, (stack, tickDelta) -> {
         return OLEPOSSUtils.getLerpedPos(BlackOut.mc.field_1724, (double)tickDelta).method_1031(0.0D, (double)BlackOut.mc.field_1724.method_18381(BlackOut.mc.field_1724.method_18376()) - 0.1D, 0.0D);
      }, (stack) -> {
         return snowball;
      }, (box, vel) -> {
         double f = OLEPOSSUtils.inWater(box) ? 0.8D : 0.99D;
         vel[0] *= f;
         vel[1] *= f;
         vel[2] *= f;
         vel[1] -= 0.03D;
      }, Items.field_8634);
   }

   private void put(double width, double height, DoubleFunction<ItemStack, Float, Vec3d> startPos, Function<ItemStack, double[]> speed, DoubleConsumer<Box, double[]> physics, Item... items) {
      Item[] var9 = items;
      int var10 = items.length;

      for(int var11 = 0; var11 < var10; ++var11) {
         Item item = var9[var11];
         this.dataMap.put(item, new Trajectories.SimulationData(width, height, startPos, speed, physics));
      }

   }

   private static record SimulationData(double width, double height, DoubleFunction<ItemStack, Float, Vec3d> startPos, Function<ItemStack, double[]> speed, DoubleConsumer<Box, double[]> physics) {
      private SimulationData(double width, double height, DoubleFunction<ItemStack, Float, Vec3d> startPos, Function<ItemStack, double[]> speed, DoubleConsumer<Box, double[]> physics) {
         this.width = width;
         this.height = height;
         this.startPos = startPos;
         this.speed = speed;
         this.physics = physics;
      }

      public double width() {
         return this.width;
      }

      public double height() {
         return this.height;
      }

      public DoubleFunction<ItemStack, Float, Vec3d> startPos() {
         return this.startPos;
      }

      public Function<ItemStack, double[]> speed() {
         return this.speed;
      }

      public DoubleConsumer<Box, double[]> physics() {
         return this.physics;
      }
   }
}
