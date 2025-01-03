package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.combat.offensive.AutoMine;
import bodevelopment.client.blackout.module.modules.misc.AntiRotationSync;
import bodevelopment.client.blackout.module.modules.misc.HandMine;
import bodevelopment.client.blackout.randomstuff.FakePlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class MixinClientPlayerInteractionManager {
   @Shadow
   @Final
   private MinecraftClient field_3712;
   @Shadow
   private float field_3715;
   @Shadow
   private ItemStack field_3718;
   @Shadow
   public BlockPos field_3714;
   @Shadow
   private boolean field_3717;
   @Shadow
   private float field_3713;
   @Unique
   private BlockPos position = null;
   @Unique
   private Direction dir = null;

   @Shadow
   public abstract ActionResult method_2896(ClientPlayerEntity var1, Hand var2, BlockHitResult var3);

   @Shadow
   protected abstract void method_41931(ClientWorld var1, SequencedPacketCreator var2);

   @Shadow
   public abstract boolean method_2899(BlockPos var1);

   @Shadow
   public abstract int method_51888();

   @Inject(
      method = {"attackBlock"},
      at = {@At("HEAD")}
   )
   private void onAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      this.position = pos;
      this.dir = direction;
   }

   @Redirect(
      method = {"attackBlock"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
   ordinal = 1
)
   )
   private void onStart(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
      AutoMine autoMine = AutoMine.getInstance();
      if (!autoMine.enabled) {
         HandMine handMine = HandMine.getInstance();
         if (!handMine.enabled) {
            this.method_41931(world, packetCreator);
         } else {
            BlockState blockState = world.method_8320(this.position);
            boolean bl = !blockState.method_26215();
            boolean canInstant = bl && handMine.getDelta(this.position, blockState.method_26165(this.field_3712.field_1724, this.field_3712.field_1724.method_37908(), this.position)) >= 1.0F;
            Runnable runnable = () -> {
               this.method_41931(world, (sequence) -> {
                  if (bl && this.field_3715 == 0.0F) {
                     blockState.method_26179(this.field_3712.field_1687, this.position, this.field_3712.field_1724);
                  }

                  if (bl && canInstant) {
                     handMine.onInstant(this.position, () -> {
                        this.method_2899(this.position);
                     });
                  } else {
                     this.field_3717 = true;
                     this.field_3714 = this.position;
                     this.field_3718 = this.field_3712.field_1724.method_6047();
                     this.field_3715 = 0.0F;
                     this.field_3713 = 0.0F;
                     this.field_3712.field_1687.method_8517(this.field_3712.field_1724.method_5628(), this.field_3714, this.method_51888());
                  }

                  return new PlayerActionC2SPacket(Action.field_12968, this.position, this.dir, sequence);
               });
            };
            if (canInstant) {
               handMine.onInstant(this.position, runnable);
            } else {
               runnable.run();
            }

         }
      } else {
         BlockState blockState = world.method_8320(this.position);
         boolean bl = !blockState.method_26215();
         if (bl && this.field_3715 == 0.0F) {
            blockState.method_26179(this.field_3712.field_1687, this.position, this.field_3712.field_1724);
         }

         if (bl && blockState.method_26165(this.field_3712.field_1724, this.field_3712.field_1724.method_37908(), this.position) >= 1.0F) {
            this.method_2899(this.position);
         } else {
            this.field_3717 = true;
            this.field_3714 = this.position;
            this.field_3718 = this.field_3712.field_1724.method_6047();
            this.field_3715 = 0.0F;
            this.field_3713 = 0.0F;
            this.field_3712.field_1687.method_8517(this.field_3712.field_1724.method_5628(), this.field_3714, this.method_51888());
         }

         autoMine.onStart(this.position);
      }
   }

   @Redirect(
      method = {"attackBlock"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 0
)
   )
   private void onAbort(ClientPlayNetworkHandler instance, Packet<?> packet) {
      AutoMine autoMine = AutoMine.getInstance();
      if (!autoMine.enabled) {
         instance.method_52787(packet);
      } else {
         autoMine.onAbort(this.position);
      }

   }

   @Inject(
      method = {"updateBlockBreakingProgress"},
      at = {@At("HEAD")}
   )
   private void onUpdateProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      this.position = pos;
   }

   @Redirect(
      method = {"updateBlockBreakingProgress"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"
)
   )
   private float calcDelta2(BlockState instance, PlayerEntity playerEntity, BlockView blockView, BlockPos pos) {
      HandMine handMine = HandMine.getInstance();
      float vanilla = instance.method_26165(playerEntity, blockView, pos);
      return handMine.enabled ? handMine.getDelta(pos, vanilla) : vanilla;
   }

   @Redirect(
      method = {"updateBlockBreakingProgress"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
   ordinal = 1
)
   )
   private void onStop(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
      AutoMine autoMine = AutoMine.getInstance();
      if (autoMine.enabled) {
         autoMine.onStop(this.position);
      } else {
         HandMine handMine = HandMine.getInstance();
         if (handMine.enabled) {
            handMine.onEnd(this.position, () -> {
               this.method_41931(world, packetCreator);
            });
         } else {
            this.method_41931(world, packetCreator);
         }
      }

   }

   @Redirect(
      method = {"cancelBlockBreaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
)
   )
   private void cancel(ClientPlayNetworkHandler instance, Packet<?> packet) {
      AutoMine autoMine = AutoMine.getInstance();
      if (!autoMine.enabled) {
         instance.method_52787(packet);
      } else {
         autoMine.onAbort(this.field_3714);
      }

   }

   @Redirect(
      method = {"interactItem"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
)
   )
   private void onRotationSync(ClientPlayNetworkHandler instance, Packet<?> packet) {
      if (!AntiRotationSync.getInstance().enabled) {
         instance.method_52787(packet);
      } else {
         instance.method_52787(new Full(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
      }

   }

   @Redirect(
      method = {"attackEntity"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"
)
   )
   private void onAttack(PlayerEntity instance, Entity target) {
      if (!(target instanceof FakePlayerEntity)) {
         instance.method_7324(target);
      }

   }
}
