package bodevelopment.client.blackout.util.render;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.gui.menu.MainMenu;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.randomstuff.ShaderSetup;
import bodevelopment.client.blackout.rendering.framebuffer.FrameBuffer;
import bodevelopment.client.blackout.rendering.renderer.Renderer;
import bodevelopment.client.blackout.rendering.renderer.ShaderRenderer;
import bodevelopment.client.blackout.rendering.shader.Shader;
import bodevelopment.client.blackout.rendering.shader.Shaders;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class RenderUtils {
   public static final long initTime = System.currentTimeMillis();
   public static final MatrixStack emptyStack = new MatrixStack();
   private static Immediate vertexConsumers = null;
   private static Matrix4f projMat;
   private static Matrix4f modelMat;

   public static boolean insideRounded(double mx, double my, double x, double y, double width, double height, double rad) {
      double offsetX = mx - x;
      double offsetY = my - y;
      double dx = offsetX - MathHelper.method_15350(offsetX, 0.0D, width);
      double dy = offsetY - MathHelper.method_15350(offsetY, 0.0D, height);
      return dx * dx + dy * dy <= rad * rad;
   }

   private static Immediate getVertexConsumers() {
      if (vertexConsumers == null) {
         vertexConsumers = BlackOut.mc.field_1773.field_20948.method_23000();
      }

      return vertexConsumers;
   }

   public static void onRender() {
      projMat = RenderSystem.getProjectionMatrix();
      modelMat = RenderSystem.getModelViewMatrix();
   }

   public static Vec2f getCoords(double x, double y, double z, boolean checkVisible) {
      Matrix4f matrix4f = new Matrix4f();
      Camera camera = BlackOut.mc.field_1773.method_19418();
      matrix4f.rotate(RotationAxis.field_40714.rotationDegrees(camera.method_19329()));
      matrix4f.rotate(RotationAxis.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
      matrix4f.translate((float)(x - camera.method_19326().field_1352), (float)(y - camera.method_19326().field_1351), (float)(z - camera.method_19326().field_1350));
      Vector4f f = matrix4f.transform(new Vector4f(0.0F, 0.0F, 0.0F, 1.0F));
      Vector4f f2 = (new Vector4f(f.x, f.y, f.z, 1.0F)).mul(modelMat).mul(projMat);
      return f2.z < 0.0F && checkVisible ? null : new Vec2f((float)(((double)(f2.x / 2.0F / Math.abs(f2.z)) + 0.5D) * (double)BlackOut.mc.method_22683().method_4480()), (float)(((double)(1.0F - f2.y / 2.0F / Math.abs(f2.z)) - 0.5D) * (double)BlackOut.mc.method_22683().method_4507()));
   }

   public static void renderItem(MatrixStack stack, Item item, float x, float y, float scale) {
      ItemStack itemStack = item.method_7854();
      if (!itemStack.method_7960()) {
         BakedModel bakedModel = BlackOut.mc.method_1480().method_4019(itemStack, BlackOut.mc.field_1687, BlackOut.mc.field_1724, 0);
         stack.method_22903();
         stack.method_46416(x + 8.0F, y + 8.0F, 0.0F);
         stack.method_34425((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
         stack.method_22905(scale, scale, scale);
         boolean bl = !bakedModel.method_24304();
         if (bl) {
            DiffuseLighting.method_24210();
         }

         BlackOut.mc.method_1480().method_23179(itemStack, ModelTransformationMode.field_4317, false, stack, getVertexConsumers(), 15728880, OverlayTexture.field_21444, bakedModel);
         getVertexConsumers().method_22993();
         if (bl) {
            DiffuseLighting.method_24211();
         }

         stack.method_22909();
      }
   }

   public static void scissor(float x, float y, float w, float h) {
      scissor(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
   }

   public static void scissor(int x, int y, int w, int h) {
      GlStateManager._enableScissorTest();
      GlStateManager._scissorBox(x, y, Math.max(w, 0), Math.max(h, 0));
   }

   public static void endScissor() {
      GlStateManager._disableScissorTest();
   }

   public static void blurBufferBW(String name, int strength) {
      loadBlur(name, Managers.FRAME_BUFFER.getBuffer(name).getTexture(), strength, Shaders.bloomblur);
   }

   public static void blurBuffer(String name, int strength) {
      loadBlur(name, Managers.FRAME_BUFFER.getBuffer(name).getTexture(), strength, Shaders.screenblur);
   }

   public static void loadBlur(String name, int strength) {
      loadBlur(name, BlackOut.mc.method_1522().method_30277(), strength, Shaders.screenblur);
   }

   public static void loadBlur(String name, int from, int strength, Shader shader) {
      emptyStack.method_22903();
      unGuiScale(emptyStack);
      float alpha = Renderer.getAlpha();
      Renderer.setAlpha(1.0F);

      for(int dist = 1; dist <= strength; ++dist) {
         FrameBuffer buffer = Managers.FRAME_BUFFER.getBuffer(dist == strength ? name : "screenblur" + dist);
         buffer.clear();
         buffer.bind(true);
         int tex;
         if (dist == 1) {
            tex = from;
         } else {
            tex = Managers.FRAME_BUFFER.getBuffer("screenblur" + (dist - 1)).getTexture();
         }

         drawBlur(tex, dist, shader);
      }

      Renderer.setAlpha(alpha);
      BlackOut.mc.method_1522().method_1235(true);
      emptyStack.method_22909();
   }

   public static void drawLoadedBlur(String name, MatrixStack stack, Consumer<ShaderRenderer> consumer) {
      FrameBuffer buffer = Managers.FRAME_BUFFER.getBuffer(name);
      BlackOut.mc.method_1522().method_1235(true);
      ShaderRenderer renderer = ShaderRenderer.getInstance();
      Renderer.setTexture(buffer.getTexture(), 0);
      renderer.startRender(stack, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27381, VertexFormats.field_1592);
      consumer.accept(renderer);
      renderer.endRender(Shaders.screentex, new ShaderSetup((setup) -> {
         setup.set("uTexture", 0);
         setup.set("alpha", 1.0F);
      }));
   }

   public static void renderBufferWith(String frameBuffer, Shader shader, ShaderSetup setup) {
      renderBufferWith(Managers.FRAME_BUFFER.getBuffer(frameBuffer), shader, setup);
   }

   public static void renderBufferWith(FrameBuffer frameBuffer, Shader shader, ShaderSetup setup) {
      ShaderRenderer renderer = ShaderRenderer.getInstance();
      Renderer.setTexture(frameBuffer.getTexture(), 0);
      emptyStack.method_22903();
      unGuiScale(emptyStack);
      renderer.startRender(emptyStack, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27381, VertexFormats.field_1592);
      renderer.quadShape(0.0F, 0.0F, (float)BlackOut.mc.method_22683().method_4480(), (float)BlackOut.mc.method_22683().method_4507());
      renderer.endRender(shader, setup);
      emptyStack.method_22909();
   }

   public static void renderBufferOverlay(FrameBuffer frameBuffer, int id) {
      BlackOut.mc.method_1522().method_1235(true);
      ShaderRenderer renderer = ShaderRenderer.getInstance();
      Renderer.setTexture(frameBuffer.getTexture(), 0);
      Renderer.setTexture(id, 1);
      emptyStack.method_22903();
      unGuiScale(emptyStack);
      renderer.startRender(emptyStack, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27381, VertexFormats.field_1592);
      renderer.quadShape(0.0F, 0.0F, (float)BlackOut.mc.method_22683().method_4480(), (float)BlackOut.mc.method_22683().method_4507());
      renderer.endRender(Shaders.screentexoverlay, new ShaderSetup((setup) -> {
         setup.set("uTexture0", 0);
         setup.set("uTexture1", 1);
      }));
      emptyStack.method_22909();
   }

   public static void drawWithBlur(MatrixStack stack, int strength, Consumer<ShaderRenderer> consumer) {
      loadBlur("temp", strength);
      drawLoadedBlur("temp", stack, consumer);
   }

   public static void blur(int strength, float alpha) {
      emptyStack.method_22903();
      unGuiScale(emptyStack);
      int prevBuffer = FrameBuffer.getCurrent();
      float prevAlpha = Renderer.getAlpha();
      Renderer.setAlpha(1.0F);

      for(int dist = 1; dist <= strength; ++dist) {
         if (dist == strength) {
            FrameBuffer.bind(prevBuffer);
            Renderer.setAlpha(alpha);
         } else {
            Managers.FRAME_BUFFER.getBuffer("screenblur" + dist).bind(true);
         }

         int tex;
         if (dist == 1) {
            tex = BlackOut.mc.method_1522().method_30277();
         } else {
            tex = Managers.FRAME_BUFFER.getBuffer("screenblur" + (dist - 1)).getTexture();
         }

         drawBlur(tex, dist, Shaders.screenblur);
      }

      Renderer.setAlpha(prevAlpha);
      emptyStack.method_22909();
   }

   private static void drawBlur(int from, int dist, Shader shader) {
      ShaderRenderer renderer = ShaderRenderer.getInstance();
      Renderer.setTexture(from, 0);
      renderer.quad(emptyStack, 0.0F, 0.0F, (float)BlackOut.mc.method_22683().method_4480(), (float)BlackOut.mc.method_22683().method_4507(), shader, new ShaderSetup((setup) -> {
         setup.set("dist", getBlurDist(dist));
         setup.set("uTexture", 0);
      }), VertexFormats.field_1576);
      Renderer.setTexture(from, 0);
   }

   private static float getBlurDist(int i) {
      return 1.0F + Math.max(0.0F, (float)i - 1.5F) * 2.0F;
   }

   public static void roundedRight(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      innerRounded(stack, x - radius, y, w + radius, h, radius, shadowRadius, color, shadowColor, x, maxX, minY, maxY);
   }

   public static void roundedLeft(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      innerRounded(stack, x, y, w + radius, h, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY);
   }

   public static void roundedTop(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h;
      innerRounded(stack, x, y, w, h + radius, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY);
   }

   public static void roundedBottom(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      innerRounded(stack, x, y - radius, w, h + radius, radius, shadowRadius, color, shadowColor, minX, maxX, y, maxY);
   }

   public static void roundedBottomLeft(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius;
      float maxY = y + h + radius + shadowRadius;
      innerRounded(stack, x, y - radius, w + radius, h + radius, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY);
   }

   public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      innerRounded(stack, x, y, w, h, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY);
   }

   private static void innerRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor, float minX, float maxX, float minY, float maxY) {
      --minX;
      ++maxX;
      --minY;
      ++maxY;
      Renderer.setMatrices(stack);
      if (shadowRadius > 0.0F) {
         moreInnerRounded(x, y, w, h, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY, true);
      }

      moreInnerRounded(x, y, w, h, radius, shadowRadius, color, shadowColor, minX, maxX, minY, maxY, false);
   }

   private static void moreInnerRounded(float x, float y, float w, float h, float radius, float shadowRadius, int color, int shadowColor, float minX, float maxX, float minY, float maxY, boolean shadow) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      shaderRenderer.endRender(shadow ? Shaders.roundedshadow : Shaders.rounded, new ShaderSetup((setup) -> {
         setup.set("rad", radius, shadowRadius);
         if (shadow) {
            setup.color("shadowClr", shadowColor);
         } else {
            setup.color("clr", color);
         }

         setup.set("pos", x, y, w, h);
      }));
   }

   public static void fadeRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float frequency, float speed) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      fadeRounded(stack, x, y, w, h, radius, shadowRadius, clr, clr2, frequency, speed, minX, maxX, minY, maxY);
   }

   private static void fadeRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float frequency, float speed, float minX, float maxX, float minY, float maxY) {
      --minX;
      ++maxX;
      --minY;
      ++maxY;
      Renderer.setMatrices(stack);
      if (shadowRadius > 0.0F) {
         moreFadeRounded(x, y, w, h, radius, shadowRadius, clr, clr2, frequency, speed, minX, maxX, minY, maxY, true);
      }

      moreFadeRounded(x, y, w, h, radius, shadowRadius, clr, clr2, frequency, speed, minX, maxX, minY, maxY, false);
   }

   private static void moreFadeRounded(float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float frequency, float speed, float minX, float maxX, float minY, float maxY, boolean shadow) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      shaderRenderer.endRender(shadow ? Shaders.shadowfade : Shaders.roundedfade, new ShaderSetup((setup) -> {
         setup.set("rad", radius, shadowRadius);
         setup.color("clr", clr);
         setup.color("clr2", clr2);
         setup.set("pos", x, y, w, h);
         setup.set("frequency", frequency * 2.0F);
         setup.set("speed", speed);
         setup.time(initTime);
      }));
   }

   public static void rainbowRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, float saturation, float frequency, float speed) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      rainbowRounded(stack, x, y, w, h, radius, shadowRadius, saturation, frequency, speed, minX, maxX, minY, maxY);
   }

   private static void rainbowRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, float saturation, float frequency, float speed, float minX, float maxX, float minY, float maxY) {
      --minX;
      ++maxX;
      --minY;
      ++maxY;
      Renderer.setMatrices(stack);
      if (shadowRadius > 0.0F) {
         moreRainbowRounded(x, y, w, h, radius, shadowRadius, saturation, frequency, speed, minX, maxX, minY, maxY, true);
      }

      moreRainbowRounded(x, y, w, h, radius, shadowRadius, saturation, frequency, speed, minX, maxX, minY, maxY, false);
   }

   private static void moreRainbowRounded(float x, float y, float w, float h, float radius, float shadowRadius, float saturation, float frequency, float speed, float minX, float maxX, float minY, float maxY, boolean shadow) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      shaderRenderer.endRender(shadow ? Shaders.shadowrainbow : Shaders.roundedrainbow, new ShaderSetup((setup) -> {
         setup.set("rad", radius, shadowRadius);
         setup.set("pos", x, y, w, h);
         setup.set("frequency", frequency);
         setup.set("speed", speed);
         setup.set("saturation", saturation);
         setup.time(initTime);
      }));
   }

   public static void tenaRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float speed) {
      float minX = x - radius - shadowRadius;
      float maxX = x + w + radius + shadowRadius;
      float minY = y - radius - shadowRadius;
      float maxY = y + h + radius + shadowRadius;
      tenaRounded(stack, x, y, w, h, radius, shadowRadius, clr, clr2, speed, minX, maxX, minY, maxY);
   }

   private static void tenaRounded(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float speed, float minX, float maxX, float minY, float maxY) {
      --minX;
      ++maxX;
      --minY;
      ++maxY;
      Renderer.setMatrices(stack);
      if (shadowRadius > 0.0F) {
         moreTenaRounded(x, y, w, h, radius, shadowRadius, clr, clr2, speed, minX, maxX, minY, maxY, true);
      }

      moreTenaRounded(x, y, w, h, radius, shadowRadius, clr, clr2, speed, minX, maxX, minY, maxY, false);
   }

   private static void moreTenaRounded(float x, float y, float w, float h, float radius, float shadowRadius, int clr, int clr2, float speed, float minX, float maxX, float minY, float maxY, boolean shadow) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      shaderRenderer.endRender(shadow ? Shaders.tenacityshadow : Shaders.tenacity, new ShaderSetup((setup) -> {
         setup.set("rad", radius, shadowRadius);
         setup.color("color1", clr);
         setup.color("color2", clr2);
         setup.set("pos", x, y, w, h);
         setup.set("speed", speed);
         setup.time(initTime);
      }));
   }

   public static void bloom(MatrixStack stack, float x, float y, float radX, float radY, int color) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      float minX = x - radX;
      float maxX = x + radX;
      float minY = y - radY;
      float maxY = y + radY;
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      Renderer.setMatrices(stack);
      shaderRenderer.endRender(Shaders.bloom, new ShaderSetup((setup) -> {
         setup.color("clr", color);
         setup.set("pos", x, y, radX, radY);
      }));
   }

   public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color, boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27381, VertexFormats.field_1576);
      drawRounded(x, y, w, h, radius, p, r, g, b, a, bufferBuilder, matrix4f, topLeft, topRight, bottomLeft, bottomRight);
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void drawRounded(float x, float y, float w, float h, float radius, int p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f, boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
      if (bottomRight) {
         corner(x + w, y + h, radius, 90, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      } else {
         bufferBuilder.method_22918(matrix4f, x + w + radius, y + h + radius, 0.0F).method_22915(r, g, b, a).method_1344();
      }

      if (topRight) {
         corner(x + w, y, radius, 360, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      } else {
         bufferBuilder.method_22918(matrix4f, x + w + radius, y - radius, 0.0F).method_22915(r, g, b, a).method_1344();
      }

      if (topLeft) {
         corner(x, y, radius, 270, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      } else {
         bufferBuilder.method_22918(matrix4f, x - radius, y - radius, 0.0F).method_22915(r, g, b, a).method_1344();
      }

      if (bottomLeft) {
         corner(x, y + h, radius, 180, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      } else {
         bufferBuilder.method_22918(matrix4f, x - radius, y + h + radius, 0.0F).method_22915(r, g, b, a).method_1344();
      }

   }

   public static void roundedShadow(MatrixStack stack, float x, float y, float w, float h, float radius, float shadowRad, int color) {
      rounded(stack, x, y, w, h, radius, shadowRad, MainMenu.EMPTY_COLOR, color);
   }

   private static void renderCorner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
      bufferBuilder.method_1328(DrawMode.field_27381, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22915(r, g, b, a).method_1344();
      corner(x, y, radius, angle, p, r, g, b, 0.0F, bufferBuilder, matrix4f);
      BufferRenderer.method_43433(bufferBuilder.method_1326());
   }

   public static void corner2(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
      for(float i = (float)angle; i >= (float)(angle - 90); i -= 90.0F / p) {
         bufferBuilder.method_22918(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).method_22915(r, g, b, a).method_1344();
      }

   }

   public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
      for(float i = (float)angle; i >= (float)(angle - 90); i -= 90.0F / p) {
         bufferBuilder.method_22918(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).method_22915(r, g, b, a).method_1344();
      }

   }

   public static void line(MatrixStack stack, float x1, float y1, float x2, float y2, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29344, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x1, y1, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y2, 0.0F).method_22915(r, g, b, a).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void line(MatrixStack stack, float x1, float y1, float x2, float y2, int color, int color2) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      float a2 = (float)Argb.method_27762(color2) / 255.0F;
      float r2 = (float)Argb.method_27765(color2) / 255.0F;
      float g2 = (float)Argb.method_27766(color2) / 255.0F;
      float b2 = (float)Argb.method_27767(color2) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29344, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x1, y1, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y2, 0.0F).method_22915(r2, g2, b2, a2).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void line(MatrixStack stack, float x1, float y1, float x2, float y2, int color, float width) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29344, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x1, y1, 0.0F).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y2, 0.0F).method_22915(r, g, b, a).method_22914(0.0F, 0.0F, 0.0F).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void fadeLine(MatrixStack stack, float x1, float y1, float x2, float y2, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x1, y1, 0.0F).method_22915(r, g, b, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, (float)MathHelper.method_16436(0.4D, (double)x1, (double)x2), (float)MathHelper.method_16436(0.4D, (double)y1, (double)y2), 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, (float)MathHelper.method_16436(0.6D, (double)x1, (double)x2), (float)MathHelper.method_16436(0.6D, (double)y1, (double)y2), 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y2, 0.0F).method_22915(r, g, b, 0.0F).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void circle(MatrixStack stack, float x, float y, float radius, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27381, VertexFormats.field_1576);

      for(int i = 360; i >= 0; --i) {
         bufferBuilder.method_22918(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).method_22915(r, g, b, a).method_1344();
      }

      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void circle2(MatrixStack stack, float x, float y, float radius, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);

      for(int i = 0; i <= 360; ++i) {
         bufferBuilder.method_22918(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).method_22915(r, g, b, a).method_1344();
      }

      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void circle3(MatrixStack stack, float x, float y, float radius, int color, int angle) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);

      for(int i = 0; i <= angle; ++i) {
         bufferBuilder.method_22918(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).method_22915(r, g, b, a).method_1344();
      }

      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void texturedQuad(Identifier identifier, MatrixStack stack, float x, float y, float w, float h, int color) {
      RenderSystem.setShaderTexture(0, identifier);
      GlStateManager._activeTexture(33984);
      BlackOut.mc.method_1531().method_22813(identifier);
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34542);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1585);
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_22913(0.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_22913(1.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22913(1.0F, 0.0F).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void drawTexture(MatrixStack stack, Identifier texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2) {
      int id = BlackOut.mc.method_1531().method_4619(texture).method_4624();
      drawTexturedQuad(stack, id, x1, x2, y1, y2, u1, u2, y1, y2);
   }

   public static void drawTexturedQuad(MatrixStack stack, int texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::method_34542);
      Matrix4f matrix4f = stack.method_23760().method_23761();
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1585);
      bufferBuilder.method_22918(matrix4f, x1, y1, 0.0F).method_22913(u1, v1).method_1344();
      bufferBuilder.method_22918(matrix4f, x1, y2, 0.0F).method_22913(u1, v2).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y2, 0.0F).method_22913(u2, v2).method_1344();
      bufferBuilder.method_22918(matrix4f, x2, y1, 0.0F).method_22913(u2, v1).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
   }

   public static void drawFontQuad(MatrixStack stack, int texture, float x, float y, float w, float h) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      RenderSystem.enableBlend();
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1585);
      RenderSystem.setShaderTexture(0, texture);
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_22913(0.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_22913(1.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22913(1.0F, 0.0F).method_1344();
      Shaders.font.render(bufferBuilder, new ShaderSetup());
      RenderSystem.disableBlend();
   }

   public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_22915(r, g, b, a).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void inQuadShadow(MatrixStack stack, float x, float y, float w, float h, float shadow, int color) {
      bottomFade(stack, x, y, w, shadow, color);
      topFade(stack, x, y + h - shadow, w, shadow, color);
      rightFade(stack, x, y, shadow, h, color);
      leftFade(stack, x + w - shadow, y, shadow, h, color);
   }

   public static void quad2(MatrixStack stack, float x, float y, float w, float h, int color) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_29345, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_22915(r, g, b, a).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22915(r, g, b, a).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void shaderQuad(MatrixStack stack, Shader shader, ShaderSetup setup, float x, float y, float w, float h) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      RenderSystem.enableBlend();
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1592);
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_1344();
      shader.render(bufferBuilder, setup);
      RenderSystem.disableBlend();
   }

   public static void skeet(MatrixStack stack, float x, float y, float w, float h, float saturation, float frequency, float speed) {
      float maxX = x + w;
      float maxY = y + h - 1.0F;
      moreSkeet(stack, x, y, w, h, saturation, frequency, speed, x, maxX, y, maxY);
   }

   private static void moreSkeet(MatrixStack stack, float x, float y, float w, float h, float radius, float frequency, float speed, float minX, float maxX, float minY, float maxY) {
      --minX;
      ++maxX;
      --minY;
      ++maxY;
      Renderer.setMatrices(stack);
      skeetSkeet(x, y, w, h, radius, frequency, speed, minX, maxX, minY, maxY);
   }

   private static void skeetSkeet(float x, float y, float w, float h, float saturation, float frequency, float speed, float minX, float maxX, float minY, float maxY) {
      ShaderRenderer shaderRenderer = ShaderRenderer.getInstance();
      shaderRenderer.startRender((MatrixStack)null, 1.0F, 1.0F, 1.0F, 1.0F, DrawMode.field_27382, VertexFormats.field_1592);
      shaderRenderer.vertex2D(minX, minY);
      shaderRenderer.vertex2D(minX, maxY);
      shaderRenderer.vertex2D(maxX, maxY);
      shaderRenderer.vertex2D(maxX, minY);
      shaderRenderer.endRender(Shaders.skeet, new ShaderSetup((setup) -> {
         setup.set("frequency", frequency);
         setup.set("speed", speed);
         setup.set("saturation", saturation);
         setup.time(initTime);
      }));
   }

   public static void drawSkeetBox(MatrixStack stack, float x, float y, float width, float height, boolean drawLine) {
      int skeetLight = (new Color(30, 30, 30, 255)).getRGB();
      int skeet = (new Color(20, 20, 20, 255)).getRGB();
      int skeetBG = (new Color(10, 10, 10, 255)).getRGB();
      quad(stack, x, y, width, height, skeetLight);
      quad(stack, x + 1.0F, y + 1.0F, width - 2.0F, height - 2.0F, skeet);
      quad(stack, x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, skeetLight);
      quad(stack, x + 3.0F, y + 3.0F, width - 6.0F, height - 6.0F, skeetBG);
      if (drawLine) {
         skeet(stack, x + 3.0F, y + 3.0F, width - 6.0F, 0.1F, 0.6F, 0.7F, 0.1F);
      }

   }

   public static void rightFade(MatrixStack stack, float x, float y, float w, float h, int color) {
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      quad(stack, x, y, w, h, r, g, b, a, r, g, b, a, r, g, b, 0.0F, r, g, b, 0.0F);
   }

   public static void leftFade(MatrixStack stack, float x, float y, float w, float h, int color) {
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      quad(stack, x, y, w, h, r, g, b, 0.0F, r, g, b, 0.0F, r, g, b, a, r, g, b, a);
   }

   public static void topFade(MatrixStack stack, float x, float y, float w, float h, int color) {
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      quad(stack, x, y, w, h, r, g, b, 0.0F, r, g, b, a, r, g, b, 0.0F, r, g, b, a);
   }

   public static void bottomFade(MatrixStack stack, float x, float y, float w, float h, int color) {
      float a = (float)Argb.method_27762(color) / 255.0F;
      float r = (float)Argb.method_27765(color) / 255.0F;
      float g = (float)Argb.method_27766(color) / 255.0F;
      float b = (float)Argb.method_27767(color) / 255.0F;
      quad(stack, x, y, w, h, r, g, b, a, r, g, b, 0.0F, r, g, b, a, r, g, b, 0.0F);
   }

   public static void quad(MatrixStack stack, float x, float y, float w, float h, float tlr, float tlg, float tlb, float tla, float blr, float blg, float blb, float bla, float trr, float trg, float trb, float tra, float brr, float brg, float brb, float bra) {
      Matrix4f matrix4f = stack.method_23760().method_23761();
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::method_34540);
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1576);
      bufferBuilder.method_22918(matrix4f, x + w, y, 0.0F).method_22915(trr, trg, trb, tra).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y, 0.0F).method_22915(tlr, tlg, tlb, tla).method_1344();
      bufferBuilder.method_22918(matrix4f, x, y + h, 0.0F).method_22915(blr, blg, blb, bla).method_1344();
      bufferBuilder.method_22918(matrix4f, x + w, y + h, 0.0F).method_22915(brr, brg, brb, bra).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.disableBlend();
   }

   public static void startClickGui(MatrixStack stack, float unscaled, float scale, float width, float height, float x, float y) {
      stack.method_22903();
      stack.method_22905(scale, scale, 1.0F);
      stack.method_46416(width / -2.0F, height / -2.0F, 0.0F);
      stack.method_22904(((double)BlackOut.mc.method_22683().method_4480() / 2.0D + (double)x) / (double)unscaled, ((double)BlackOut.mc.method_22683().method_4507() / 2.0D + (double)y) / (double)unscaled, 0.0D);
   }

   public static void unGuiScale(MatrixStack stack) {
      int scale = getScale();
      stack.method_22905(1.0F / (float)scale, 1.0F / (float)scale, 1.0F);
   }

   public static int getScale() {
      return BlackOut.mc.method_22683().method_4476((Integer)BlackOut.mc.field_1690.method_42474().method_41753(), false);
   }
}
