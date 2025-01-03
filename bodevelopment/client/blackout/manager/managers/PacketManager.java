package bodevelopment.client.blackout.manager.managers;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.EntityAddEvent;
import bodevelopment.client.blackout.event.events.MoveEvent;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.interfaces.mixin.IEndCrystalEntity;
import bodevelopment.client.blackout.manager.Manager;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.mixin.accessors.AccessorInteractEntityC2SPacket;
import bodevelopment.client.blackout.mixin.accessors.AccessorPlayerMoveC2SPacket;
import bodevelopment.client.blackout.module.modules.misc.Simulation;
import bodevelopment.client.blackout.randomstuff.FakePlayerEntity;
import bodevelopment.client.blackout.randomstuff.timers.TimerList;
import bodevelopment.client.blackout.randomstuff.timers.TimerMap;
import bodevelopment.client.blackout.util.SettingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PacketManager extends Manager {
   private boolean onGround;
   public int slot = 0;
   public Vec3d pos;
   public final TimerList<Integer> ids;
   private final List<Consumer<? super ClientPlayNetworkHandler>> grimQueue;
   private final List<Consumer<? super ClientPlayNetworkHandler>> postGrimQueue;
   private boolean spoofOG;
   private boolean spoofedOG;
   public int teleportId;
   public int receivedId;
   public int prevReceived;
   public final TimerMap<Integer, Vec3d> validPos;
   public final TimerList<Integer> ignoreSetSlot;
   public final TimerList<ScreenHandlerSlotUpdateS2CPacket> ignoredInventory;
   private final TimerList<BlockPos> own;

   public PacketManager() {
      this.pos = Vec3d.field_1353;
      this.ids = new TimerList(true);
      this.grimQueue = new ArrayList();
      this.postGrimQueue = new ArrayList();
      this.spoofOG = false;
      this.spoofedOG = false;
      this.teleportId = 0;
      this.receivedId = 0;
      this.prevReceived = 0;
      this.validPos = new TimerMap(true);
      this.ignoreSetSlot = new TimerList(true);
      this.ignoredInventory = new TimerList(true);
      this.own = new TimerList(true);
   }

   public void init() {
      BlackOut.EVENT_BUS.subscribe(this, () -> {
         return false;
      });
      this.onGround = false;
   }

   @Event
   public void onSent(PacketEvent.Sent event) {
      Packet var3 = event.packet;
      if (var3 instanceof UpdateSelectedSlotC2SPacket) {
         UpdateSelectedSlotC2SPacket packet = (UpdateSelectedSlotC2SPacket)var3;
         if (packet.method_12442() >= 0) {
            this.slot = packet.method_12442();
            BlackOut.mc.field_1761.field_3721 = packet.method_12442();
         }
      }

      var3 = event.packet;
      if (var3 instanceof PlayerMoveC2SPacket) {
         PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket)var3;
         this.onGround = packet.method_12273();
         if (packet.method_36171()) {
            this.pos = new Vec3d(packet.method_12269(0.0D), packet.method_12268(0.0D), packet.method_12274(0.0D));
         }
      }

      var3 = event.packet;
      if (var3 instanceof TeleportConfirmC2SPacket) {
         TeleportConfirmC2SPacket packet = (TeleportConfirmC2SPacket)var3;
         this.teleportId = packet.method_12086();
      }

      var3 = event.packet;
      if (var3 instanceof PlayerInteractEntityC2SPacket) {
         PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)var3;
         if (((AccessorInteractEntityC2SPacket)packet).getType().method_34211() == InteractType.field_29172) {
            Entity var4 = BlackOut.mc.field_1687.method_8469(((AccessorInteractEntityC2SPacket)packet).getId());
            if (var4 instanceof FakePlayerEntity) {
               FakePlayerEntity player = (FakePlayerEntity)var4;
               Managers.FAKE_PLAYER.onAttack(player);
            }

            if (Simulation.getInstance().hitReset()) {
               BlackOut.mc.field_1724.method_7350();
            }

            if (Simulation.getInstance().stopSprint()) {
               BlackOut.mc.field_1724.method_5728(false);
            }
         }
      }

      var3 = event.packet;
      if (var3 instanceof PlayerInteractBlockC2SPacket) {
         PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket)var3;
         if (this.handStack(packet.method_12546()).method_31574(Items.field_8301)) {
            this.own.replace(packet.method_12543().method_17777().method_10084(), 1.0D);
         }
      }

   }

   @Event
   public void onEntityAdd(EntityAddEvent.Post event) {
      Entity var3 = event.entity;
      if (var3 instanceof EndCrystalEntity) {
         EndCrystalEntity entity = (EndCrystalEntity)var3;
         if (this.own.contains((Object)entity.method_24515())) {
            ((IEndCrystalEntity)entity).blackout_Client$markOwn();
         }
      }

   }

   @Event
   public void onSend(PacketEvent.Send event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerMoveC2SPacket) {
         PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket)var3;
         if (this.spoofOG) {
            ((AccessorPlayerMoveC2SPacket)packet).setOnGround(this.spoofedOG);
            this.spoofOG = false;
         }
      }

   }

   @Event
   public void onReceive(PacketEvent.Receive.Post e) {
      Packet var3 = e.packet;
      if (var3 instanceof PlayerPositionLookS2CPacket) {
         PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket)var3;
         Vec3d vec = new Vec3d(packet.method_11734(), packet.method_11735(), packet.method_11738());
         int id = packet.method_11737();
         if (this.validPos.containsKey(id) && ((Vec3d)this.validPos.get(id)).equals(vec)) {
            e.setCancelled(true);
            this.validPos.removeKey(packet.method_11737());
         }

         this.prevReceived = this.receivedId;
         this.receivedId = packet.method_11737();
         if (!this.ids.contains((Object)id)) {
            this.teleportId = id;
         }
      }

      var3 = e.packet;
      if (var3 instanceof UpdateSelectedSlotS2CPacket) {
         UpdateSelectedSlotS2CPacket packet = (UpdateSelectedSlotS2CPacket)var3;
         if (this.ignoreSetSlot.contains((Object)packet.method_11803())) {
            e.setCancelled(true);
         }
      }

      var3 = e.packet;
      if (var3 instanceof ScreenHandlerSlotUpdateS2CPacket) {
         ScreenHandlerSlotUpdateS2CPacket packet = (ScreenHandlerSlotUpdateS2CPacket)var3;
         if (this.ignoredInventory.contains((timer) -> {
            return this.inventoryEquals(packet, (ScreenHandlerSlotUpdateS2CPacket)timer.value);
         }) && !this.isItemEquals(packet)) {
            e.setCancelled(true);
         }
      }

   }

   @Event
   public void onMove(MoveEvent.Pre event) {
      this.sendPackets();
   }

   private boolean isItemEquals(ScreenHandlerSlotUpdateS2CPacket packet) {
      return this.getStackInSlot(packet).method_31574(packet.method_11449().method_7909());
   }

   private ItemStack getStackInSlot(ScreenHandlerSlotUpdateS2CPacket packet) {
      if (packet.method_11452() == -1) {
         return null;
      } else if (packet.method_11452() == -2) {
         return BlackOut.mc.field_1724.method_31548().method_5438(packet.method_11450());
      } else if (packet.method_11452() == 0 && PlayerScreenHandler.method_36211(packet.method_11450())) {
         return BlackOut.mc.field_1724.field_7498.method_7611(packet.method_11450()).method_7677();
      } else {
         return packet.method_11452() == BlackOut.mc.field_1724.field_7512.field_7763 ? BlackOut.mc.field_1724.field_7512.method_7611(packet.method_11450()).method_7677() : null;
      }
   }

   public void sendPackets() {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         this.sendList(this.grimQueue);
         this.sendList(this.postGrimQueue);
      }
   }

   private void sendList(List<Consumer<? super ClientPlayNetworkHandler>> list) {
      list.forEach((consumer) -> {
         this.sendPacket(BlackOut.mc.method_1562(), consumer);
      });
      list.clear();
   }

   private void sendPacket(ClientPlayNetworkHandler handler, Consumer<? super ClientPlayNetworkHandler> consumer) {
      if (handler != null) {
         consumer.accept(handler);
      }

   }

   public void sendPacket(Packet<?> packet) {
      this.sendPacketToList(packet, this.grimQueue);
   }

   public void sendPostPacket(Packet<?> packet) {
      this.sendPacketToList(packet, this.postGrimQueue);
   }

   public void sendInstantly(Packet<?> packet) {
      this.sendPacket(BlackOut.mc.method_1562(), (handler) -> {
         handler.method_52787(packet);
      });
   }

   private void sendPacketToList(Packet<?> packet, List<Consumer<? super ClientPlayNetworkHandler>> list) {
      if (this.shouldBeDelayed(packet)) {
         this.addToQueue((handler) -> {
            handler.method_52787(packet);
         }, list);
      } else {
         BlackOut.mc.method_1562().method_52787(packet);
      }

   }

   public void addToQueue(Consumer<? super ClientPlayNetworkHandler> consumer) {
      this.addToQueue(consumer, this.grimQueue);
   }

   public void addToPostQueue(Consumer<? super ClientPlayNetworkHandler> consumer) {
      this.addToQueue(consumer, this.postGrimQueue);
   }

   private void addToQueue(Consumer<? super ClientPlayNetworkHandler> consumer, List<Consumer<? super ClientPlayNetworkHandler>> list) {
      if (SettingUtils.grimPackets()) {
         list.add(consumer);
      } else {
         consumer.accept(BlackOut.mc.method_1562());
      }

   }

   private boolean shouldBeDelayed(Packet<?> packet) {
      if (!SettingUtils.grimPackets()) {
         return false;
      } else if (packet instanceof PlayerInteractEntityC2SPacket) {
         return true;
      } else if (packet instanceof PlayerInteractBlockC2SPacket) {
         return true;
      } else if (packet instanceof PlayerInteractItemC2SPacket) {
         return true;
      } else if (packet instanceof PlayerActionC2SPacket) {
         return true;
      } else if (packet instanceof HandSwingC2SPacket) {
         return true;
      } else if (packet instanceof UpdateSelectedSlotC2SPacket) {
         return true;
      } else {
         return packet instanceof ClickSlotC2SPacket ? true : packet instanceof PickFromInventoryC2SPacket;
      }
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public ItemStack getStack() {
      return BlackOut.mc.field_1724.method_31548().method_5438(this.slot);
   }

   public ItemStack stackInHand(Hand hand) {
      ItemStack var10000;
      switch(hand) {
      case field_5808:
         var10000 = this.getStack();
         break;
      case field_5810:
         var10000 = BlackOut.mc.field_1724.method_6079();
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isHolding(Item... items) {
      ItemStack stack = this.getStack();
      if (stack == null) {
         return false;
      } else {
         Item[] var3 = items;
         int var4 = items.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Item item = var3[var5];
            if (item.equals(stack.method_7909())) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isHolding(Item item) {
      ItemStack stack = this.getStack();
      return stack == null ? false : stack.method_7909().equals(item);
   }

   public void spoofOG(boolean state) {
      this.spoofOG = true;
      this.spoofedOG = state;
   }

   public ItemStack handStack(Hand hand) {
      return hand == Hand.field_5808 ? this.getStack() : BlackOut.mc.field_1724.method_6079();
   }

   public TeleportConfirmC2SPacket incrementedPacket(Vec3d vec3d) {
      int id = this.teleportId + 1;
      this.ids.add(id, 1.0D);
      this.validPos.add(id, vec3d, 1.0D);
      return new TeleportConfirmC2SPacket(id);
   }

   public TeleportConfirmC2SPacket incrementedPacket2(Vec3d vec3d) {
      int id = this.receivedId + 1;
      this.ids.replace(id, 1.0D);
      this.validPos.add(id, vec3d, 1.0D);
      return new TeleportConfirmC2SPacket(id);
   }

   public void preApply(ScreenHandlerSlotUpdateS2CPacket packet) {
      packet.method_11451(BlackOut.mc.method_1562());
      this.addInvIgnore(packet);
   }

   public void addInvIgnore(ScreenHandlerSlotUpdateS2CPacket packet) {
      this.ignoredInventory.remove((timer) -> {
         return this.inventoryEquals((ScreenHandlerSlotUpdateS2CPacket)timer.value, packet);
      });
      this.ignoredInventory.add(packet, 0.3D);
   }

   private boolean inventoryEquals(ScreenHandlerSlotUpdateS2CPacket packet1, ScreenHandlerSlotUpdateS2CPacket packet2) {
      return packet1.method_11450() == packet2.method_11450() && packet1.method_11449().method_31574(packet2.method_11449().method_7909());
   }

   public void sendPreUse() {
      this.sendInstantly(new Full(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, this.isOnGround()));
   }

   public void sendPositionSync(Vec3d pos, float yaw, float pitch) {
      yaw = MathHelper.method_15393(yaw);
      if (yaw >= 0.0F) {
         yaw = -180.0F - (180.0F - yaw);
      }

      Managers.PACKET.sendInstantly(new Full(pos.field_1352, pos.field_1351, pos.field_1350, yaw, pitch, false));
   }
}
