package bodevelopment.client.blackout.module.modules.client.settings;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.SettingsModule;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.ExtrapolationMap;
import bodevelopment.client.blackout.util.BoxUtils;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ExtrapolationSettings extends SettingsModule {
   private static ExtrapolationSettings INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgLag = this.addGroup("Lag");
   private final SettingGroup sgRender = this.addGroup("Render");
   public final Setting<Boolean> stepPredict;
   public final Setting<Double> minStep;
   public final Setting<Integer> stepTicks;
   public final Setting<Boolean> reverseStepPredict;
   public final Setting<Double> minReverseStep;
   public final Setting<Integer> reverseStepTicks;
   public final Setting<Boolean> jumpPredict;
   public final Setting<Integer> maxLag;
   public final Setting<Boolean> extraExtrapolation;
   private final Setting<Boolean> renderExtrapolation;
   private final Setting<Boolean> dashedLine;
   private final Setting<BlackOutColor> lineColor;
   private final ExtrapolationMap extrapolationMap;
   private final MatrixStack stack;

   public ExtrapolationSettings() {
      super("Extrapolation", false, true);
      this.stepPredict = this.sgGeneral.b("Step Predict", true, ".");
      this.minStep = this.sgGeneral.d("Min Step", 0.6D, 0.6D, 3.0D, 0.1D, ".");
      this.stepTicks = this.sgGeneral.i("Step Ticks", 40, 10, 100, 1, ".");
      this.reverseStepPredict = this.sgGeneral.b("Reverse Step Predict", true, ".");
      this.minReverseStep = this.sgGeneral.d("Min Reverse Step", 0.6D, 0.6D, 3.0D, 0.1D, ".");
      this.reverseStepTicks = this.sgGeneral.i("Reverse Step Ticks", 20, 10, 100, 1, ".");
      this.jumpPredict = this.sgGeneral.b("Jump Predict", true, ".");
      this.maxLag = this.sgLag.i("Max Lag", 5, 0, 10, 1, ".");
      this.extraExtrapolation = this.sgLag.b("Extra Extrapolation", true, ".");
      this.renderExtrapolation = this.sgRender.b("Render Extrapolation", false, "");
      this.dashedLine = this.sgRender.b("Dashed Line", false, "");
      this.lineColor = this.sgRender.c("Line Color", new BlackOutColor(255, 255, 255, 255), "");
      this.extrapolationMap = new ExtrapolationMap();
      this.stack = new MatrixStack();
      INSTANCE = this;
   }

   public static ExtrapolationSettings getInstance() {
      return INSTANCE;
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724 != null && (Boolean)this.renderExtrapolation.get()) {
         Map<Entity, Box> map = this.extrapolationMap.getMap();
         Map<Entity, List<Vec3d>> feet = new HashMap();
         map.clear();
         Managers.EXTRAPOLATION.getDataMap().forEach((player, data) -> {
            if (player.method_5805()) {
               List<Vec3d> list = new ArrayList();
               Box box = data.extrapolate(player, 20, (b) -> {
                  list.add(BoxUtils.feet(b));
               });
               feet.put(player, list);
               map.put(player, box);
            }
         });
         this.stack.method_22903();
         Render3DUtils.setRotation(this.stack);
         Render3DUtils.start();
         feet.values().forEach(this::renderList);
         Render3DUtils.end();
         this.stack.method_22909();
      }
   }

   private void renderList(List<Vec3d> list) {
      RenderSystem.setShader(GameRenderer::method_34540);
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder bufferBuilder = tessellator.method_1349();
      bufferBuilder.method_1328((Boolean)this.dashedLine.get() ? DrawMode.field_29344 : DrawMode.field_29345, VertexFormats.field_1576);
      Matrix4f matrix4f = this.stack.method_23760().method_23761();
      Vec3d camPos = BlackOut.mc.field_1773.method_19418().method_19326();
      float red = (float)((BlackOutColor)this.lineColor.get()).red / 255.0F;
      float green = (float)((BlackOutColor)this.lineColor.get()).green / 255.0F;
      float blue = (float)((BlackOutColor)this.lineColor.get()).blue / 255.0F;
      float alpha = (float)((BlackOutColor)this.lineColor.get()).alpha / 255.0F;
      list.forEach((vec) -> {
         bufferBuilder.method_22918(matrix4f, (float)(vec.field_1352 - camPos.field_1352), (float)(vec.field_1351 - camPos.field_1351), (float)(vec.field_1350 - camPos.field_1350)).method_22915(red, green, blue, alpha).method_1344();
      });
      tessellator.method_1350();
   }
}
