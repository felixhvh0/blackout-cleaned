package bodevelopment.client.blackout.module.modules.visual.entities;

import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.combat.misc.AntiBot;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.ShaderSetup;
import bodevelopment.client.blackout.rendering.framebuffer.FrameBuffer;
import bodevelopment.client.blackout.rendering.renderer.Renderer;
import bodevelopment.client.blackout.rendering.shader.Shaders;
import bodevelopment.client.blackout.util.render.RenderUtils;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class ShaderESP extends Module {
   private static ShaderESP INSTANCE;
   private final BufferBuilderStorage storage = new BufferBuilderStorage(69);
   private final Immediate vertexConsumerProvider;
   private final SettingGroup sgGeneral;
   public final Setting<List<EntityType<?>>> entities;
   public final Setting<Boolean> texture;
   private final Setting<Integer> bloom;
   private final Setting<BlackOutColor> outsideColor;
   private final Setting<BlackOutColor> insideColor;
   public static boolean ignore = false;

   public ShaderESP() {
      super("Shader ESP", ".", SubCategory.ENTITIES, true);
      this.vertexConsumerProvider = this.storage.method_23000();
      this.sgGeneral = this.addGroup("General");
      this.entities = this.sgGeneral.el("Entities", ".");
      this.texture = this.sgGeneral.b("Texture", true, ".");
      this.bloom = this.sgGeneral.i("Bloom", 3, 1, 10, 1, ".");
      this.outsideColor = this.sgGeneral.c("Outside Color", new BlackOutColor(255, 0, 0, 255), ".");
      this.insideColor = this.sgGeneral.c("Inside Color", new BlackOutColor(255, 0, 0, 50), ".");
      INSTANCE = this;
   }

   public static ShaderESP getInstance() {
      return INSTANCE;
   }

   public <T extends Entity> void onRender(EntityRenderer<T> instance, T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if ((Boolean)this.texture.get()) {
         instance.method_3936(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      }

      if (this.shouldRenderLabel(entity)) {
         ignore = true;
         instance.method_3926(entity, entity.method_5476(), matrices, vertexConsumers, light);
         ignore = false;
      }

      FrameBuffer buffer = Managers.FRAME_BUFFER.getBuffer("shaderESP");
      buffer.bind(true);
      instance.method_3936(entity, yaw, tickDelta, matrices, this.vertexConsumerProvider, light);
      this.vertexConsumerProvider.method_22993();
      buffer.unbind();
   }

   private boolean shouldRenderLabel(Entity entity) {
      if (Nametags.shouldCancelLabel(entity)) {
         return false;
      } else if (this.shouldRender(entity)) {
         return false;
      } else {
         return entity.method_5733() && entity.method_16914();
      }
   }

   public void onRenderHud() {
      FrameBuffer buffer = Managers.FRAME_BUFFER.getBuffer("shaderESP");
      FrameBuffer bloomBuffer = Managers.FRAME_BUFFER.getBuffer("shaderESP-bloom");
      buffer.bind(true);
      RenderUtils.renderBufferWith(buffer, Shaders.convert, new ShaderSetup());
      buffer.unbind();
      RenderUtils.renderBufferWith(buffer, Shaders.shaderbloom, new ShaderSetup((setup) -> {
         setup.color("clr", ((BlackOutColor)this.insideColor.get()).getRGB());
      }));
      if ((Integer)this.bloom.get() > 0) {
         bloomBuffer.clear(0.0F, 0.0F, 0.0F, 1.0F);
         bloomBuffer.bind(true);
         RenderUtils.renderBufferWith(buffer, Shaders.screentex, new ShaderSetup((setup) -> {
            setup.set("alpha", 1.0F);
         }));
         bloomBuffer.unbind();
         RenderUtils.blurBufferBW("shaderESP-bloom", (Integer)this.bloom.get() + 1);
         bloomBuffer.bind(true);
         Renderer.setTexture(buffer.getTexture(), 1);
         RenderUtils.renderBufferWith(bloomBuffer, Shaders.subtract, new ShaderSetup());
         bloomBuffer.unbind();
         RenderUtils.renderBufferWith(bloomBuffer, Shaders.shaderbloom, new ShaderSetup((setup) -> {
            setup.color("clr", ((BlackOutColor)this.outsideColor.get()).getRGB());
         }));
         buffer.clear(1.0F, 1.0F, 1.0F, 0.0F);
      }
   }

   public boolean shouldRender(Entity entity) {
      AntiBot antiBot = AntiBot.getInstance();
      if (antiBot.enabled && antiBot.mode.get() == AntiBot.HandlingMode.Ignore && entity instanceof AbstractClientPlayerEntity) {
         AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
         if (antiBot.getBots().contains(player)) {
            return false;
         }
      }

      return ((List)this.entities.get()).contains(entity.method_5864());
   }
}
