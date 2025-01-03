package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.module.modules.client.BlurSettings;
import bodevelopment.client.blackout.module.modules.visual.entities.ShaderESP;
import bodevelopment.client.blackout.module.modules.visual.misc.Crosshair;
import bodevelopment.client.blackout.module.modules.visual.misc.CustomScoreboard;
import bodevelopment.client.blackout.module.modules.visual.misc.HandESP;
import bodevelopment.client.blackout.module.modules.visual.misc.NoRender;
import bodevelopment.client.blackout.rendering.renderer.Renderer;
import bodevelopment.client.blackout.util.render.RenderUtils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class MixinInGameHud {
   @Final
   @Shadow
   private static Identifier field_2019;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void preRender(DrawContext context, float tickDelta, CallbackInfo ci) {
      BlurSettings blur = BlurSettings.getInstance();
      if (Renderer.shouldLoad3DBlur()) {
         RenderUtils.loadBlur("3dblur", blur.get3DBlurStrength());
      }

      HandESP handESP = HandESP.getInstance();
      if (handESP.enabled) {
         handESP.renderHud();
      }

      ShaderESP shaderESP = ShaderESP.getInstance();
      if (shaderESP.enabled) {
         shaderESP.onRenderHud();
      }

      if (Renderer.shouldLoadHUDBlur()) {
         RenderUtils.loadBlur("hudblur", blur.getHUDBlurStrength());
      }

      BlackOut.EVENT_BUS.post(RenderEvent.Hud.Pre.get(context, tickDelta));
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void postRender(DrawContext context, float tickDelta, CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(RenderEvent.Hud.Post.get(context, tickDelta));
   }

   @Inject(
      method = {"renderStatusEffectOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
      if (NoRender.getInstance().enabled && (Boolean)NoRender.getInstance().effectOverlay.get()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderScoreboardSidebar"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
      CustomScoreboard customScoreboard = CustomScoreboard.getInstance();
      if (customScoreboard.enabled) {
         ci.cancel();
         customScoreboard.objectiveName = objective.method_1114().method_27662().getString();
         TextColor clr = objective.method_1114().method_10866().method_10973();
         int rgbValue = 1;
         if (clr != null) {
            rgbValue = clr.method_27716();
         }

         customScoreboard.objectiveColor = new Color(rgbValue >> 16 & 255, rgbValue >> 8 & 255, rgbValue & 255);
      }

   }

   @Inject(
      method = {"renderOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void injectPumpkinBlur(DrawContext context, Identifier texture, float opacity, CallbackInfo callback) {
      if (NoRender.getInstance().enabled && (Boolean)NoRender.getInstance().pumpkin.get() && field_2019.equals(texture)) {
         callback.cancel();
      }

   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void drawCrosshair(DrawContext context, CallbackInfo ci) {
      if (Crosshair.getInstance().enabled) {
         ci.cancel();
      }

   }
}
