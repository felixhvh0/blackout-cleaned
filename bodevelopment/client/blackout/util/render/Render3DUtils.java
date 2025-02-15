package bodevelopment.client.blackout.util.render;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class Render3DUtils {
   public static MatrixStack matrices = new MatrixStack();

   public static void box(Box box, BlackOutColor sideColor, BlackOutColor lineColor, RenderShape shape) {
      box(box, sideColor == null ? 0 : sideColor.getRGB(), lineColor == null ? 0 : lineColor.getRGB(), shape);
   }

   public static void box(Box box, int sideColor, int lineColor, RenderShape shape) {
      matrices.method_22903();
      setRotation(matrices);
      Vec3d camPos = BlackOut.mc.field_1773.method_19418().method_19326();
      box(matrices, box.method_989(-camPos.field_1352, -camPos.field_1351, -camPos.field_1350), sideColor, lineColor, shape);
      matrices.method_22909();
   }

   public static void box(MatrixStack stack, Box box, BlackOutColor sideColor, BlackOutColor lineColor, RenderShape shape) {
      box(stack, box, sideColor.getRGB(), lineColor.getRGB(), shape);
   }

   public static void box(MatrixStack stack, Box box, int sideColor, int lineColor, RenderShape shape) {
      start();
      if (shape.sides) {
         renderSides(stack, box, sideColor);
      }

      if (shape.outlines) {
         renderOutlines(stack, box, lineColor);
      }

      end();
   }

   public static void renderOutlines(MatrixStack stack, Box box, int color) {
      RenderSystem.setShader(GameRenderer::method_34535);
      RenderSystem.lineWidth(1.5F);
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder bufferBuilder = tessellator.method_1349();
      bufferBuilder.method_1328(DrawMode.field_27377, VertexFormats.field_29337);
      drawOutlines(stack, bufferBuilder, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324, (float)Argb.method_27765(color) / 255.0F, (float)Argb.method_27766(color) / 255.0F, (float)Argb.method_27767(color) / 255.0F, (float)Argb.method_27762(color) / 255.0F);
      tessellator.method_1350();
   }

   public static void drawOutlines(MatrixStack stack, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      Matrix3f matrix3f = stack.method_23760().method_23762();
      line(matrix4f, matrix3f, vertexConsumer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, minX, minY, minZ, minX, minY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, minX, maxY, minZ, minX, maxY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
      line(matrix4f, matrix3f, vertexConsumer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
   }

   private static void line(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float sx, float sy, float sz, float ex, float ey, float ez, float r, float g, float b, float a) {
      float dx = ex - sx;
      float dy = ey - sy;
      float dz = ez - sz;
      float length = (float)Math.sqrt((double)(dx * dx + dy * dy + dz * dz));
      float nx = dx / length;
      float ny = dy / length;
      float nz = dz / length;
      vertexConsumer.method_22918(matrix4f, sx, sy, sz).method_22915(r, g, b, a).method_23763(matrix3f, nx, ny, nz).method_1344();
      vertexConsumer.method_22918(matrix4f, ex, ey, ez).method_22915(r, g, b, a).method_23763(matrix3f, nx, ny, nz).method_1344();
   }

   public static void renderSides(MatrixStack stack, Box box, int color) {
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1576);
      drawSides(stack, bufferBuilder, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324, (float)Argb.method_27765(color) / 255.0F, (float)Argb.method_27766(color) / 255.0F, (float)Argb.method_27767(color) / 255.0F, (float)Argb.method_27762(color) / 255.0F);
      BufferRenderer.method_43433(bufferBuilder.method_1326());
   }

   public static void drawSides(MatrixStack stack, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      vertexConsumer.method_22918(matrix4f, minX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, minZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, minX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, maxY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, maxX, minY, maxZ).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
   }

   public static void drawPlane(Matrix4f matrix4f, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a) {
      vertexConsumer.method_22918(matrix4f, x1, y1, z1).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, x2, y2, z2).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, x3, y3, z3).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      vertexConsumer.method_22918(matrix4f, x4, y4, z4).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
   }

   public static void start() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
   }

   public static void end() {
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
   }

   public static void setRotation(MatrixStack stack) {
      stack.method_22907(RotationAxis.field_40714.rotationDegrees(BlackOut.mc.field_1773.method_19418().method_19329()));
      stack.method_22907(RotationAxis.field_40716.rotationDegrees(BlackOut.mc.field_1773.method_19418().method_19330() + 180.0F));
   }

   public static void text(String string, Vec3d pos, int color, float scale) {
      GlStateManager._disableDepthTest();
      GlStateManager._enableBlend();
      GlStateManager._disableCull();
      Vec3d cameraPos = BlackOut.mc.field_1773.method_19418().method_19326();
      matrices.method_22903();
      setRotation(matrices);
      Vec3d vec = pos.method_1020(cameraPos);
      matrices.method_22904(vec.field_1352, vec.field_1351, vec.field_1350);
      scale *= -0.025F;
      scale *= 0.2F;
      matrices.method_22905(scale, scale, scale);
      matrices.method_22907(BlackOut.mc.field_1773.method_19418().method_23767());
      BlackOut.FONT.string(string, matrices, -BlackOut.FONT.getWidth(string) * 4.0F, -BlackOut.FONT.getHeight() * 4.0F, color);
      GlStateManager._enableCull();
      matrices.method_22909();
   }
}
