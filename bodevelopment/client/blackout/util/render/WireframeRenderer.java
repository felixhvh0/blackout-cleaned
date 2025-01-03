package bodevelopment.client.blackout.util.render;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WireframeRenderer {
   public static final MatrixStack matrixStack = new MatrixStack();
   public static final ModelVertexConsumerProvider provider = new ModelVertexConsumerProvider();
   public static boolean hidden = false;

   public static void renderModel(AbstractClientPlayerEntity player, BlackOutColor lineColor, BlackOutColor sideColor, RenderShape shape, float tickDelta) {
      matrixStack.method_22903();
      Render3DUtils.setRotation(matrixStack);
      Render3DUtils.start();
      provider.consumer.start();
      drawEntity(player, tickDelta, provider);
      List<Vec3d[]> positions = provider.consumer.positions;
      if (shape.sides) {
         drawQuads(positions, (float)sideColor.red / 255.0F, (float)sideColor.green / 255.0F, (float)sideColor.blue / 255.0F, (float)sideColor.alpha / 255.0F);
      }

      if (shape.outlines) {
         drawLines(positions, (float)lineColor.red / 255.0F, (float)lineColor.green / 255.0F, (float)lineColor.blue / 255.0F, (float)lineColor.alpha / 255.0F);
      }

      matrixStack.method_22909();
   }

   public static void drawLines(List<Vec3d[]> positions, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::method_34535);
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder builder = tessellator.method_1349();
      builder.method_1328(DrawMode.field_27377, VertexFormats.field_29337);
      List<Vec3d[]> rendered = new ArrayList();
      positions.forEach((arr) -> {
         for(int i = 0; i < 4; ++i) {
            Vec3d[] line = new Vec3d[]{arr[i], arr[(i + 1) % 4]};
            if (!contains(rendered, line)) {
               Vec3d normal = line[1].method_1020(line[0]).method_1029();
               vertex(builder, line[0], normal, red, green, blue, alpha);
               vertex(builder, line[1], normal, red, green, blue, alpha);
               rendered.add(line);
            }
         }

      });
      tessellator.method_1350();
   }

   private static boolean contains(List<Vec3d[]> lines, Vec3d[] line) {
      Iterator var2 = lines.iterator();

      Vec3d[] arr;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         arr = (Vec3d[])var2.next();
         if (line[0].equals(arr[0]) && line[1].equals(arr[1])) {
            return true;
         }
      } while(!line[0].equals(arr[1]) || !line[1].equals(arr[0]));

      return true;
   }

   public static void vertex(BufferBuilder builder, Vec3d pos, Vec3d normal, float red, float green, float blue, float alpha) {
      builder.method_22912((double)((float)pos.field_1352), (double)((float)pos.field_1351), (double)((float)pos.field_1350)).method_22915(red, green, blue, alpha).method_22914((float)normal.field_1352, (float)normal.field_1351, (float)normal.field_1350).method_1344();
   }

   public static void drawQuads(List<Vec3d[]> positions, float red, float green, float blue, float alpha) {
      RenderSystem.setShader(GameRenderer::method_34540);
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder builder = tessellator.method_1349();
      builder.method_1328(DrawMode.field_27382, VertexFormats.field_1576);
      positions.forEach((arr) -> {
         Vec3d[] var6 = arr;
         int var7 = arr.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Vec3d pos = var6[var8];
            builder.method_22912((double)((float)pos.field_1352), (double)((float)pos.field_1351), (double)((float)pos.field_1350)).method_22915(red, green, blue, alpha).method_1344();
         }

      });
      tessellator.method_1350();
   }

   public static void drawEntity(AbstractClientPlayerEntity player, float tickDelta, VertexConsumerProvider vertexConsumerProvider) {
      double d = MathHelper.method_16436((double)tickDelta, player.field_6038, player.method_23317());
      double e = MathHelper.method_16436((double)tickDelta, player.field_5971, player.method_23318());
      double f = MathHelper.method_16436((double)tickDelta, player.field_5989, player.method_23321());
      float yaw = MathHelper.method_16439(tickDelta, player.field_5982, player.method_36454());
      EntityRenderer<? super AbstractClientPlayerEntity> entityRenderer = BlackOut.mc.field_1769.field_4109.method_3953(player);
      Vec3d cameraPos = BlackOut.mc.field_1773.method_19418().method_19326();
      double x = d - cameraPos.field_1352;
      double y = e - cameraPos.field_1351;
      double z = f - cameraPos.field_1350;
      Vec3d vec3d = entityRenderer.method_23169(player, tickDelta);
      double d2 = x + vec3d.method_10216();
      double e2 = y + vec3d.method_10214();
      double f2 = z + vec3d.method_10215();
      matrixStack.method_22903();
      matrixStack.method_22904(d2, e2, f2);
      hidden = true;
      entityRenderer.method_3936(player, yaw, tickDelta, matrixStack, vertexConsumerProvider, 69420);
      hidden = false;
      matrixStack.method_22909();
   }
}
