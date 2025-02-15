package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.misc.Pause;
import bodevelopment.client.blackout.module.modules.movement.Blink;
import bodevelopment.client.blackout.randomstuff.Pair;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public abstract class MixinClientConnection {
   @Shadow
   @Final
   private static Logger field_11642;
   @Shadow
   @Final
   private NetworkSide field_11643;
   @Shadow
   private Channel field_11651;
   @Shadow
   private int field_11656;
   @Unique
   private Packet<?> currentPacket = null;
   @Unique
   private boolean cancelled = false;

   @Shadow
   public abstract void method_36942(Packet<?> var1, @Nullable PacketCallbacks var2, boolean var3);

   @Shadow
   protected abstract void method_10770(ChannelHandlerContext var1, Packet<?> var2);

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")}
   )
   private void onException(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
      field_11642.warn("Crashed on packet event ", ex);
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preSendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
      this.cancelled = ((PacketEvent.Send)BlackOut.EVENT_BUS.post(PacketEvent.Send.get(packet))).isCancelled();
      if (this.cancelled) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"sendImmediately"},
      at = {@At("HEAD")}
   )
   public void sendHead(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
      this.currentPacket = packet;
   }

   @Inject(
      method = {"sendImmediately"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sendPing(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
      ++this.field_11656;
      if (this.field_11651.eventLoop().inEventLoop()) {
         if (Managers.PING.shouldDelay(packet) && this.field_11643 == NetworkSide.field_11942) {
            Managers.PING.addSend(() -> {
               this.method_36942(packet, callbacks, flush);
            });
         } else {
            this.method_36942(packet, callbacks, flush);
         }
      } else {
         Runnable runnable = () -> {
            this.method_36942(packet, callbacks, flush);
         };
         if (Managers.PING.shouldDelay(this.currentPacket) && this.field_11643 == NetworkSide.field_11942) {
            Managers.PING.addSend(() -> {
               this.field_11651.eventLoop().execute(runnable);
            });
         } else {
            this.field_11651.eventLoop().execute(runnable);
         }
      }

      ci.cancel();
   }

   @Redirect(
      method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/network/ClientConnection;isOpen()Z"
)
   )
   private boolean isOpenSend(ClientConnection instance) {
      Blink blink = Blink.getInstance();
      return blink.enabled && blink.onSend() ? false : instance.method_10758();
   }

   @Inject(
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
      Pause pause = Pause.getInstance();
      List<Pair<ChannelHandlerContext, Packet<?>>> packets = pause.packets;
      if (pause.enabled) {
         packets.add(new Pair(channelHandlerContext, packet));
         ci.cancel();
      } else if (!pause.emptying && !packets.isEmpty()) {
         pause.emptying = true;
         packets.forEach((pair) -> {
            this.method_10770((ChannelHandlerContext)pair.method_15442(), (Packet)pair.method_15441());
         });
         packets.clear();
         pause.emptying = false;
      }

   }

   @Redirect(
      method = {"flush"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/network/ClientConnection;isOpen()Z"
)
   )
   private boolean isOpenFlush(ClientConnection instance) {
      Blink blink = Blink.getInstance();
      return blink.enabled && blink.shouldDelay() ? false : instance.method_10758();
   }

   @Redirect(
      method = {"handleQueuedTasks"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/network/ClientConnection;channel:Lio/netty/channel/Channel;"
)
   )
   private Channel isChannelOpen(ClientConnection instance) {
      Blink blink = Blink.getInstance();
      return blink.enabled && blink.shouldDelay() ? null : this.field_11651;
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"},
      at = {@At("TAIL")}
   )
   private void postSendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
      if (!this.cancelled) {
         BlackOut.EVENT_BUS.post(PacketEvent.Sent.get(packet));
      }

      this.cancelled = false;
   }

   @Inject(
      method = {"handlePacket"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void preReceivePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(PacketEvent.Receive.Pre.get(packet));
      if (((PacketEvent.Receive.Post)BlackOut.EVENT_BUS.post(PacketEvent.Receive.Post.get(packet))).isCancelled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handlePacket"},
      at = {@At("TAIL")}
   )
   private static void postReceivePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
      BlackOut.EVENT_BUS.post(PacketEvent.Received.get(packet));
   }
}
