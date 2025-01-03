package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.module.modules.misc.Streamer;
import bodevelopment.client.blackout.util.Capes;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({CapeFeatureRenderer.class})
public class MixinCapeFeatureRenderer {
   @Unique
   private Identifier cape;

   @Redirect(
      method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isPartVisible(Lnet/minecraft/client/render/entity/PlayerModelPart;)Z"
)
   )
   private boolean redirectIsCapeVisible(AbstractClientPlayerEntity instance, PlayerModelPart playerModelPart) {
      this.cape = Capes.getCape(instance);
      if (instance == BlackOut.mc.field_1724) {
         Streamer streamer = Streamer.getInstance();
         if (streamer.enabled && (Boolean)streamer.skin.get()) {
            return false;
         }
      }

      return this.cape != null || instance.method_7348(playerModelPart);
   }

   @Redirect(
      method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/SkinTextures;capeTexture()Lnet/minecraft/util/Identifier;"
)
   )
   private Identifier redirectCapeTexture(SkinTextures instance) {
      return this.cape != null ? this.cape : instance.comp_1627();
   }
}
