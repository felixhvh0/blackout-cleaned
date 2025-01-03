package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.InteractBlockEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.interfaces.mixin.IMinecraftClient;
import bodevelopment.client.blackout.interfaces.mixin.IRenderTickCounter;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.combat.misc.FastEat;
import bodevelopment.client.blackout.module.modules.combat.misc.MultiTask;
import bodevelopment.client.blackout.module.modules.combat.misc.Quiver;
import bodevelopment.client.blackout.module.modules.legit.HitCrystal;
import bodevelopment.client.blackout.module.modules.misc.FastUse;
import bodevelopment.client.blackout.module.modules.misc.NoInteract;
import bodevelopment.client.blackout.module.modules.misc.Timer;
import bodevelopment.client.blackout.module.modules.movement.FastRiptide;
import bodevelopment.client.blackout.module.modules.visual.misc.CameraModifier;
import bodevelopment.client.blackout.module.modules.visual.misc.CustomChat;
import bodevelopment.client.blackout.randomstuff.CustomChatScreen;
import bodevelopment.client.blackout.randomstuff.timers.TickTimerList;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.SharedFeatures;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient implements IMinecraftClient {
   @Shadow
   @Final
   @Mutable
   private Session field_1726;
   @Shadow
   @Nullable
   public ClientPlayerEntity field_1724;
   @Shadow
   @Final
   private RenderTickCounter field_1728;
   @Shadow
   @Final
   public GameOptions field_1690;
   @Shadow
   private static MinecraftClient field_1700;

   @Shadow
   protected abstract void method_1523(boolean var1);

   @Shadow
   public abstract void method_24288();

   @Shadow
   protected abstract void method_1583();

   @Redirect(
      method = {"openChatScreen"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
)
   )
   private void redirectChat(MinecraftClient instance, Screen screen) {
      instance.method_1507((Screen)(CustomChat.getInstance().enabled ? new CustomChatScreen() : screen));
   }

   @Redirect(
      method = {"handleInputEvents"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/option/Perspective;next()Lnet/minecraft/client/option/Perspective;"
)
   )
   private Perspective setPerspective(Perspective instance) {
      CameraModifier modifier = CameraModifier.getInstance();
      if (modifier != null && modifier.enabled && (Boolean)modifier.noInverse.get()) {
         return instance == Perspective.field_26664 ? Perspective.field_26665 : Perspective.field_26664;
      } else {
         return instance.method_31036();
      }
   }

   @Redirect(
      method = {"tick"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
   ordinal = 6
)
   )
   private Screen redirectCurrentScreen(MinecraftClient instance) {
      return SharedFeatures.shouldSilentScreen() ? null : instance.field_1755;
   }

   @Redirect(
      method = {"tick"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;"
)
   )
   private Overlay redirectOverlay(MinecraftClient instance) {
      return SharedFeatures.shouldSilentScreen() ? null : instance.method_18506();
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void preTick(CallbackInfo ci) {
      TickTimerList.updating.forEach(TickTimerList::update);
      BlackOut.EVENT_BUS.post(TickEvent.Pre.get());
      if (!SettingUtils.grimPackets()) {
         HitCrystal.getInstance().onTick();
      }

   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void postTick(CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(TickEvent.Post.get());
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void onRun(CallbackInfo ci) {
      this.method_24288();
      Timer timer = Timer.getInstance();
      ((IRenderTickCounter)this.field_1728).blackout_Client$set(timer.getTickTime());
      if (BlackOut.mc.field_1687 != null) {
         BlackOut.mc.field_1687.method_54719().method_54671(timer.getTPS());
      }

   }

   @Redirect(
      method = {"handleBlockBreaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
)
   )
   private boolean isUsing(ClientPlayerEntity instance) {
      return !MultiTask.getInstance().enabled && instance.method_6115();
   }

   public void blackout_Client$setSession(String username, UUID uuid, String accessToken, Optional<String> xuid, Optional<String> clientId, AccountType accountType) {
      this.field_1726 = new Session(username, uuid, accessToken, xuid, clientId, accountType);
   }

   public void blackout_Client$setSession(Session session) {
      this.field_1726 = session;
   }

   public void blackout_Client$useItem() {
      this.method_1583();
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
)
   )
   private ActionResult onInteractBlock(ClientPlayerInteractionManager instance, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
      if (!((InteractBlockEvent)BlackOut.EVENT_BUS.post(InteractBlockEvent.get(hitResult, hand))).isCancelled()) {
         NoInteract noInteract = NoInteract.getInstance();
         return noInteract.enabled ? noInteract.handleBlock(hand, hitResult.method_17777(), () -> {
            return instance.method_2896(player, hand, hitResult);
         }) : instance.method_2896(player, hand, hitResult);
      } else {
         return ActionResult.field_5814;
      }
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
)
   )
   private ActionResult onEntityInteractAt(ClientPlayerInteractionManager instance, PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand) {
      NoInteract noInteract = NoInteract.getInstance();
      return noInteract.enabled ? noInteract.handleEntity(hand, entity, () -> {
         return instance.method_2917(player, entity, hitResult, hand);
      }) : instance.method_2917(player, entity, hitResult, hand);
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
)
   )
   private ActionResult onEntityInteract(ClientPlayerInteractionManager instance, PlayerEntity player, Entity entity, Hand hand) {
      NoInteract noInteract = NoInteract.getInstance();
      return noInteract.enabled ? noInteract.handleEntity(hand, entity, () -> {
         return instance.method_2905(player, entity, hand);
      }) : instance.method_2905(player, entity, hand);
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
)
   )
   private ActionResult onItemInteract(ClientPlayerInteractionManager instance, PlayerEntity player, Hand hand) {
      NoInteract noInteract = NoInteract.getInstance();
      return noInteract.enabled ? noInteract.handleUse(hand, () -> {
         return instance.method_2919(player, hand);
      }) : instance.method_2919(player, hand);
   }

   @Redirect(
      method = {"handleInputEvents"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V"
)
   )
   private void onReleaseUsing(ClientPlayerInteractionManager instance, PlayerEntity player) {
      if (!Quiver.charging && !FastEat.eating()) {
         instance.method_2897(player);
      }

   }

   @Redirect(
      method = {"handleInputEvents"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"
)
   )
   private boolean shouldKeepUsing(KeyBinding instance) {
      FastRiptide fastRiptide = FastRiptide.getInstance();
      if (fastRiptide.enabled && BlackOut.mc.field_1724.method_6030() != null && BlackOut.mc.field_1724.method_6030().method_7909() instanceof TridentItem) {
         return (double)(System.currentTimeMillis() - fastRiptide.prevRiptide) < (Double)fastRiptide.cooldown.get() * 1000.0D;
      } else {
         return instance.method_1434();
      }
   }

   @Inject(
      method = {"onResolutionChanged"},
      at = {@At("TAIL")}
   )
   private void onResize(CallbackInfo ci) {
      Managers.FRAME_BUFFER.onResize();
   }

   @Redirect(
      method = {"<init>"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/MinecraftClient;getWindowTitle()Ljava/lang/String;"
)
   )
   private String windowTitle(MinecraftClient instance) {
      return this.getBOTitle();
   }

   @Redirect(
      method = {"updateWindowTitle"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/MinecraftClient;getWindowTitle()Ljava/lang/String;"
)
   )
   private String updateTitle(MinecraftClient instance) {
      return this.getBOTitle();
   }

   @ModifyConstant(
      method = {"doItemUse"},
      constant = {@Constant(
   intValue = 4
)}
   )
   private int itemUseCooldown(int constant) {
      FastUse fastUse = FastUse.getInstance();
      if (fastUse.enabled && fastUse.timing.get() == FastUse.Timing.Tick) {
         ItemStack stack = fastUse.getStack();
         return fastUse.isValid(stack) ? (Integer)fastUse.delayTicks.get() : 4;
      } else {
         return 4;
      }
   }

   @Redirect(
      method = {"doItemUse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"
)
   )
   private boolean multiTaskThingy(ClientPlayerInteractionManager instance) {
      MultiTask multiTask = MultiTask.getInstance();
      FastUse fastUse = FastUse.getInstance();
      if (fastUse.enabled) {
         ItemStack stack = fastUse.getStack();
         if (fastUse.isValid(stack) && fastUse.rotateIfNeeded(stack)) {
            return false;
         }
      }

      return multiTask.enabled ? false : instance.method_2923();
   }

   @Unique
   private String getBOTitle() {
      return "BlackMen clementine 2.0.0";
   }
}
