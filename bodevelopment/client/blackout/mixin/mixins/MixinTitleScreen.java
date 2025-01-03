package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.gui.menu.MainMenu;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TitleScreen.class})
public abstract class MixinTitleScreen {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      ci.cancel();
      MainMenu.getInstance().set((TitleScreen)this);
      MainMenu.getInstance().render(mouseX, mouseY, delta);
   }

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      cir.cancel();
   }

   @Inject(
      method = {"initWidgetsNormal"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void initWidgetsNormal(CallbackInfo ci) {
      ci.cancel();
   }

   @Inject(
      method = {"init"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void init(CallbackInfo ci) {
      ci.cancel();
   }
}
