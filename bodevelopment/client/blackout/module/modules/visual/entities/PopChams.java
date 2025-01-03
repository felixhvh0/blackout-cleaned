package bodevelopment.client.blackout.module.modules.visual.entities;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PopEvent;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import bodevelopment.client.blackout.util.render.WireframeRenderer;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class PopChams extends Module {
   private static PopChams INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<Double> time;
   private final Setting<Double> y;
   private final Setting<Double> scale;
   private final Setting<Boolean> enemy;
   private final Setting<Boolean> friends;
   private final Setting<Boolean> self;
   private final Setting<RenderShape> renderShape;
   private final Setting<BlackOutColor> lineColor;
   private final Setting<BlackOutColor> sideColor;
   private final TimerList<PopChams.Pop> pops;
   private final MatrixStack matrixStack;

   public PopChams() {
      super("Pop Chams", ".", SubCategory.ENTITIES, true);
      this.time = this.sgGeneral.d("Time", 1.0D, 0.0D, 5.0D, 0.05D, ".");
      this.y = this.sgGeneral.d("Y", 0.0D, -5.0D, 5.0D, 0.1D, ".");
      this.scale = this.sgGeneral.d("Scale", 1.0D, 0.0D, 5.0D, 0.1D, ".");
      this.enemy = this.sgGeneral.b("Enemy", true, ".");
      this.friends = this.sgGeneral.b("Friends", true, ".");
      this.self = this.sgGeneral.b("Self", false, ".");
      this.renderShape = this.sgGeneral.e("Render Shape", RenderShape.Full, "Which parts of boxes should be rendered.");
      this.lineColor = this.sgGeneral.c("Line Color", new BlackOutColor(255, 255, 255, 255), "Fill Color");
      this.sideColor = this.sgGeneral.c("Side Color", new BlackOutColor(255, 255, 255, 50), "Side Color");
      this.pops = new TimerList(true);
      this.matrixStack = new MatrixStack();
      INSTANCE = this;
   }

   public static PopChams getInstance() {
      return INSTANCE;
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         this.matrixStack.method_22903();
         Render3DUtils.setRotation(this.matrixStack);
         this.pops.forEach((timer) -> {
            this.renderPop((PopChams.Pop)timer.value, (float)MathHelper.method_15370((double)System.currentTimeMillis(), (double)timer.startTime, (double)timer.endTime));
         });
         this.matrixStack.method_22909();
      }
   }

   @Event
   public void onPop(PopEvent event) {
      if (this.shouldRender(event.player)) {
         this.pops.add(new PopChams.Pop(event.player), (Double)this.time.get());
      }

   }

   private boolean shouldRender(AbstractClientPlayerEntity player) {
      if (player == BlackOut.mc.field_1724) {
         return (Boolean)this.self.get();
      } else {
         return Managers.FRIENDS.isFriend(player) ? (Boolean)this.friends.get() : (Boolean)this.enemy.get();
      }
   }

   private void renderPop(PopChams.Pop pop, float progress) {
      Render3DUtils.start();
      WireframeRenderer.provider.consumer.start();
      this.drawPlayer(pop.player, pop, (Double)this.y.get() * (double)progress, MathHelper.method_16439(progress, 0.9375F, ((Double)this.scale.get()).floatValue() * 0.9375F));
      List<Vec3d[]> positions = WireframeRenderer.provider.consumer.positions;
      BlackOutColor sides = ((BlackOutColor)this.sideColor.get()).alphaMulti((double)(1.0F - progress));
      BlackOutColor lines = ((BlackOutColor)this.lineColor.get()).alphaMulti((double)(1.0F - progress));
      if (((RenderShape)this.renderShape.get()).sides) {
         WireframeRenderer.drawQuads(positions, (float)sides.red / 255.0F, (float)sides.green / 255.0F, (float)sides.blue / 255.0F, (float)sides.alpha / 255.0F);
      }

      if (((RenderShape)this.renderShape.get()).outlines) {
         WireframeRenderer.drawLines(positions, (float)lines.red / 255.0F, (float)lines.green / 255.0F, (float)lines.blue / 255.0F, (float)lines.alpha / 255.0F);
      }

   }

   private void drawPlayer(AbstractClientPlayerEntity player, PopChams.Pop pop, double extraY, float scale) {
      EntityRenderer<? super AbstractClientPlayerEntity> entityRenderer = BlackOut.mc.field_1769.field_4109.method_3953(player);
      Vec3d cameraPos = BlackOut.mc.field_1773.method_19418().method_19326();
      double x = pop.x - cameraPos.field_1352;
      double y = pop.y - cameraPos.field_1351 + extraY;
      double z = pop.z - cameraPos.field_1350;
      this.matrixStack.method_22903();
      this.matrixStack.method_22904(x, y, z);
      WireframeRenderer.hidden = true;
      this.renderModel((LivingEntityRenderer)entityRenderer, pop, scale);
      WireframeRenderer.hidden = false;
      this.matrixStack.method_22909();
   }

   private void renderModel(LivingEntityRenderer<? super AbstractClientPlayerEntity, EntityModel<AbstractClientPlayerEntity>> renderer, PopChams.Pop pop, float scale) {
      BipedEntityModel<AbstractClientPlayerEntity> model = (BipedEntityModel)renderer.method_4038();
      this.matrixStack.method_22903();
      model.field_3447 = pop.swingProgress;
      model.field_3449 = pop.riding;
      model.field_3448 = false;
      float h = pop.bodyYaw;
      float j = pop.headYaw;
      float k = j - h;
      float l;
      if (pop.hasVehicle) {
         h = pop.vehicleYaw;
         k = j - h;
         l = MathHelper.method_15363(MathHelper.method_15393(k), -85.0F, 85.0F);
         h = j - l;
         if (l * l > 2500.0F) {
            h += l * 0.2F;
         }

         k = j - h;
      }

      float m = pop.pitch;
      if (pop.flip) {
         m *= -1.0F;
         k *= -1.0F;
      }

      float n;
      if (pop.sleeping) {
         Direction direction = pop.sleepDir;
         if (direction != null) {
            n = pop.eyeHeight;
            this.matrixStack.method_46416((float)(-direction.method_10148()) * n, 0.0F, (float)(-direction.method_10165()) * n);
         }
      }

      l = pop.animationProgress;
      this.matrixStack.method_22905(scale, -scale, -scale);
      this.matrixStack.method_46416(0.0F, -1.501F, 0.0F);
      float o = 0.0F;
      if (!pop.hasVehicle) {
         n = Math.min(pop.limbSpeed, 1.0F);
         o = pop.limbPos;
      } else {
         n = 0.0F;
      }

      this.matrixStack.method_22907(RotationAxis.field_40716.rotation((float)Math.toRadians((double)h)));
      model.field_3396 = pop.leaningPitch;
      model.method_17087(pop.player, o, n, l, k, m);
      model.method_2828(this.matrixStack, WireframeRenderer.provider.getBuffer((RenderLayer)null), 69420, 0, 1.0F, 1.0F, 1.0F, 1.0F);
      this.matrixStack.method_22909();
   }

   private static class Pop {
      private final AbstractClientPlayerEntity player;
      private final boolean riding;
      private final double x;
      private final double y;
      private final double z;
      private final boolean flip;
      private final float pitch;
      private final float bodyYaw;
      private final float headYaw;
      private final float swingProgress;
      private final boolean hasVehicle;
      private final float vehicleYaw;
      private final float eyeHeight;
      private final float animationProgress;
      private final float leaningPitch;
      private final float limbSpeed;
      private final float limbPos;
      private final boolean sleeping;
      private final Direction sleepDir;

      public Pop(AbstractClientPlayerEntity player) {
         this.player = player;
         float tickDelta = BlackOut.mc.method_1488();
         this.riding = player.method_5765();
         this.bodyYaw = MathHelper.method_16439(tickDelta, player.field_6220, player.field_6283);
         this.headYaw = MathHelper.method_16439(tickDelta, player.field_6259, player.field_6241);
         Entity veh = player.method_5854();
         if (veh instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)veh;
            this.hasVehicle = true;
            this.vehicleYaw = MathHelper.method_17821(tickDelta, livingEntity.field_6220, livingEntity.field_6283);
         } else {
            this.hasVehicle = false;
            this.vehicleYaw = 0.0F;
         }

         this.flip = LivingEntityRenderer.method_38563(player);
         this.pitch = MathHelper.method_16439(tickDelta, player.field_6004, player.method_36455());
         this.eyeHeight = player.method_18381(EntityPose.field_18076) - 0.1F;
         this.animationProgress = (float)player.field_6012 + tickDelta;
         this.leaningPitch = player.method_6024(tickDelta);
         this.limbSpeed = player.field_42108.method_48570(tickDelta);
         this.limbPos = player.field_42108.method_48572(tickDelta);
         this.swingProgress = player.method_6055(tickDelta);
         this.x = MathHelper.method_16436((double)tickDelta, player.field_6038, player.method_23317());
         this.y = MathHelper.method_16436((double)tickDelta, player.field_5971, player.method_23318());
         this.z = MathHelper.method_16436((double)tickDelta, player.field_5989, player.method_23321());
         this.sleeping = player.method_41328(EntityPose.field_18078);
         this.sleepDir = player.method_18401();
      }
   }
}
