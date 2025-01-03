package bodevelopment.client.blackout.module.modules.visual.entities;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import bodevelopment.client.blackout.util.render.WireframeRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.math.Vec3d;

public class SkeletonESP extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<BlackOutColor> lineColor;
   private final Setting<BlackOutColor> friendColor;

   public SkeletonESP() {
      super("Skeleton ESP", ".", SubCategory.ENTITIES, true);
      this.lineColor = this.sgGeneral.c("Line Color", new BlackOutColor(255, 0, 0, 255), ".");
      this.friendColor = this.sgGeneral.c("Friend Color", new BlackOutColor(0, 255, 255, 255), ".");
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         Iterator var2 = BlackOut.mc.field_1687.method_18456().iterator();

         while(var2.hasNext()) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)var2.next();
            if (player != BlackOut.mc.field_1724) {
               WireframeRenderer.matrixStack.method_22903();
               Render3DUtils.setRotation(WireframeRenderer.matrixStack);
               Render3DUtils.start();
               WireframeRenderer.provider.consumer.start();
               WireframeRenderer.drawEntity(player, event.tickDelta, WireframeRenderer.provider);
               List<Vec3d[]> positions = WireframeRenderer.provider.consumer.positions;
               RenderSystem.setShader(GameRenderer::method_34535);
               Tessellator tessellator = Tessellator.method_1348();
               BufferBuilder builder = tessellator.method_1349();
               builder.method_1328(DrawMode.field_27377, VertexFormats.field_29337);
               RenderSystem.lineWidth(1.5F);
               BlackOutColor color = (BlackOutColor)(Managers.FRIENDS.isFriend(player) ? this.friendColor : this.lineColor).get();
               this.renderBones(positions, builder, (float)color.red / 255.0F, (float)color.green / 255.0F, (float)color.blue / 255.0F, (float)color.alpha / 255.0F);
               tessellator.method_1350();
               WireframeRenderer.matrixStack.method_22909();
            }
         }

      }
   }

   private void renderBones(List<Vec3d[]> positions, BufferBuilder builder, float red, float green, float blue, float alpha) {
      Vec3d chest = Vec3d.field_1353;
      Vec3d ass = Vec3d.field_1353;
      if (positions.size() >= 36) {
         for(int i = 0; i < 6; ++i) {
            Vec3d boxTop = this.average((Vec3d[])positions.get(i * 6));
            Vec3d boxBottom = this.average((Vec3d[])positions.get(i * 6 + 1));
            Vec3d legBottom;
            switch(i) {
            case 0:
               this.line(builder, boxTop.method_35590(boxBottom, 0.25D), boxBottom, red, green, blue, alpha);
               break;
            case 1:
               chest = boxTop.method_35590(boxBottom, 0.05D);
               ass = boxTop.method_35590(boxBottom, 0.95D);
               this.line(builder, boxTop, ass, red, green, blue, alpha);
               break;
            case 2:
            case 3:
               legBottom = boxTop.method_35590(boxBottom, 0.1D);
               Vec3d handBottom = boxTop.method_35590(boxBottom, 0.9D);
               this.line(builder, legBottom, handBottom, red, green, blue, alpha);
               this.line(builder, legBottom, chest, red, green, blue, alpha);
               break;
            case 4:
            case 5:
               legBottom = boxTop.method_35590(boxBottom, 0.9D);
               this.line(builder, boxTop, legBottom, red, green, blue, alpha);
               this.line(builder, boxTop, ass, red, green, blue, alpha);
            }
         }

      }
   }

   private void line(BufferBuilder builder, Vec3d pos, Vec3d pos2, float red, float green, float blue, float alpha) {
      Vec3d normal = pos2.method_1020(pos).method_1029();
      builder.method_22912((double)((float)pos.field_1352), (double)((float)pos.field_1351), (double)((float)pos.field_1350)).method_22915(red, green, blue, alpha).method_22914((float)normal.field_1352, (float)normal.field_1351, (float)normal.field_1350).method_1344();
      builder.method_22912((double)((float)pos2.field_1352), (double)((float)pos2.field_1351), (double)((float)pos2.field_1350)).method_22915(red, green, blue, alpha).method_22914((float)normal.field_1352, (float)normal.field_1351, (float)normal.field_1350).method_1344();
   }

   private Vec3d average(Vec3d... vecs) {
      Vec3d total = vecs[0];

      for(int i = 1; i < vecs.length; ++i) {
         total = total.method_1019(vecs[i]);
      }

      return total.method_1021((double)(1.0F / (float)vecs.length));
   }
}
