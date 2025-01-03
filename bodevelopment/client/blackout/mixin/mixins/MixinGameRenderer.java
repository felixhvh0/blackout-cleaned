package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.combat.misc.NoTrace;
import bodevelopment.client.blackout.module.modules.misc.Reach;
import bodevelopment.client.blackout.module.modules.visual.misc.FovModifier;
import bodevelopment.client.blackout.module.modules.visual.misc.Freecam;
import bodevelopment.client.blackout.module.modules.visual.misc.HandESP;
import bodevelopment.client.blackout.module.modules.visual.misc.Highlight;
import bodevelopment.client.blackout.module.modules.visual.misc.ViewModel;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.randomstuff.timers.TimerMap;
import bodevelopment.client.blackout.rendering.shader.Shaders;
import bodevelopment.client.blackout.rendering.texture.BOTextures;
import bodevelopment.client.blackout.util.SharedFeatures;
import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public abstract class MixinGameRenderer {
   @Shadow
   public abstract void method_3203();

   @Shadow
   public abstract void method_3192(float var1, long var2, boolean var4);

   @Shadow
   public abstract boolean method_35765();

   @Shadow
   protected abstract void method_3172(MatrixStack var1, Camera var2, float var3);

   @Redirect(
      method = {"render"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
)
   )
   private Screen redirectCurrentScreen(MinecraftClient instance) {
      return instance.field_1755 instanceof GenericContainerScreen && SharedFeatures.shouldSilentScreen() ? null : instance.field_1755;
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void postRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
      Managers.PING.update();
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V"
)}
   )
   private void onRenderWorldPre(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
      TimerList.updating.forEach(TimerList::update);
      TimerMap.updating.forEach(TimerMap::update);
      BlackOut.EVENT_BUS.post(RenderEvent.World.Pre.get(matrices, tickDelta));
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
   shift = Shift.AFTER
)}
   )
   private void onRenderWorldPost(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(RenderEvent.World.Post.get(matrices, tickDelta));
   }

   @Redirect(
      method = {"renderWorld"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V"
)
   )
   private void renderHeldItems(GameRenderer instance, MatrixStack matrices, Camera camera, float tickDelta) {
      HandESP.getInstance().draw(() -> {
         this.method_3172(matrices, camera, tickDelta);
      });
   }

   @Inject(
      method = {"preloadPrograms"},
      at = {@At("TAIL")}
   )
   private void onShaderLoad(ResourceFactory factory, CallbackInfo ci) {
      Shaders.loadPrograms();
      BlackOut.FONT.loadFont();
      BlackOut.BOLD_FONT.loadFont();
      BOTextures.init();
   }

   @Redirect(
      method = {"updateTargetedEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;getReachDistance()F"
)
   )
   private float getBlockReach(ClientPlayerInteractionManager instance) {
      Reach reach = Reach.getInstance();
      return reach.enabled ? ((Double)reach.blockReach.get()).floatValue() : instance.method_2904();
   }

   @Redirect(
      method = {"updateTargetedEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasExtendedReach()Z"
)
   )
   private boolean getEntityReach(ClientPlayerInteractionManager instance) {
      return Reach.getInstance().enabled || instance.method_2926();
   }

   @Redirect(
      method = {"updateTargetedEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"
)
   )
   private HitResult raycast(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
      Freecam freecam = Freecam.getInstance();
      if (!freecam.enabled) {
         return instance.method_5745(maxDistance, tickDelta, includeFluids);
      } else {
         Vec3d vec3d = freecam.pos;
         Vec3d vec3d2 = instance.method_5828(tickDelta);
         Vec3d vec3d3 = vec3d.method_1031(vec3d2.field_1352 * maxDistance, vec3d2.field_1351 * maxDistance, vec3d2.field_1350 * maxDistance);
         return BlackOut.mc.field_1687.method_17742(new RaycastContext(vec3d, vec3d3, ShapeType.field_17559, includeFluids ? FluidHandling.field_1347 : FluidHandling.field_1348, instance));
      }
   }

   @Redirect(
      method = {"updateTargetedEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/Entity;getCameraPosVec(F)Lnet/minecraft/util/math/Vec3d;"
)
   )
   private Vec3d cameraPos(Entity instance, float tickDelta) {
      Freecam freecam = Freecam.getInstance();
      if (!freecam.enabled) {
         return instance.method_5836(tickDelta);
      } else {
         BlackOut.mc.field_1765 = this.raycast(instance, (double)BlackOut.mc.field_1761.method_2904(), tickDelta, false);
         return freecam.pos;
      }
   }

   @ModifyConstant(
      method = {"updateTargetedEntity"},
      constant = {@Constant(
   doubleValue = 6.0D
)}
   )
   private double extendedRange(double constant) {
      Reach reach = Reach.getInstance();
      return reach.enabled ? (Double)reach.entityReach.get() : constant;
   }

   @Redirect(
      method = {"updateTargetedEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"
)
   )
   private EntityHitResult raycastEntities(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double d) {
      return NoTrace.getInstance().enabled ? null : ProjectileUtil.method_18075(entity, min, max, box, predicate, d);
   }

   @Inject(
      method = {"getFov"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
      FovModifier modifier = FovModifier.getInstance();
      if (modifier.enabled) {
         cir.setReturnValue(this.getFOV(changingFov, modifier));
         cir.cancel();
      }

   }

   @Unique
   private double getFOV(boolean changing, FovModifier fovModifier) {
      if (this.method_35765()) {
         return 90.0D;
      } else if (!changing) {
         ViewModel handView = ViewModel.getInstance();
         return handView.enabled ? (Double)handView.fov.get() : 70.0D;
      } else {
         return fovModifier.getFOV();
      }
   }

   @Inject(
      method = {"shouldRenderBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void outlineRender(CallbackInfoReturnable<Boolean> cir) {
      if (Highlight.getInstance().enabled) {
         cir.setReturnValue(false);
      }

   }
}
