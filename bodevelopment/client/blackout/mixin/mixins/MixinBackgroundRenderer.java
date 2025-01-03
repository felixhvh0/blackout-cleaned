package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.module.modules.visual.world.Ambience;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BackgroundRenderer.class})
public class MixinBackgroundRenderer {
   @Shadow
   private static float field_4034;
   @Shadow
   private static float field_4033;
   @Shadow
   private static float field_4032;

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"
)
   )
   private static void redirectColor(float r, float g, float b, float a) {
      Ambience ambience = Ambience.getInstance();
      if (ambience.enabled && (Boolean)ambience.modifyFog.get() && (Boolean)ambience.thickFog.get() && !(Boolean)ambience.removeFog.get()) {
         BlackOutColor color = (BlackOutColor)ambience.color.get();
         field_4034 = (float)color.red / 255.0F;
         field_4033 = (float)color.green / 255.0F;
         field_4032 = (float)color.blue / 255.0F;
      }

      RenderSystem.clearColor(field_4034, field_4033, field_4032, 0.0F);
   }

   @Inject(
      method = {"applyFog"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
      Ambience ambience = Ambience.getInstance();
      if (ambience != null && ambience.enabled && ambience.modifyFog(fogType == FogType.field_20946)) {
         info.cancel();
      }

   }
}
