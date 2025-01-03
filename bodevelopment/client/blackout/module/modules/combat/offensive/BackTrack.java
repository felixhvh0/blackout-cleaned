package bodevelopment.client.blackout.module.modules.combat.offensive;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.RenderShape;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.PacketEvent;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.mixin.accessors.AccessorInteractEntityC2SPacket;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.randomstuff.Pair;
import bodevelopment.client.blackout.randomstuff.timers.TickTimerList;
import bodevelopment.client.blackout.randomstuff.timers.TimerMap;
import bodevelopment.client.blackout.util.render.Render3DUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BackTrack extends Module {
   private static BackTrack INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   public final Setting<Integer> time;
   public final Setting<Integer> maxTime;
   private final Setting<RenderShape> renderShape;
   private final Setting<BlackOutColor> lineColor;
   private final Setting<BlackOutColor> sideColor;
   public final TickTimerList<Pair<OtherClientPlayerEntity, Box>> hit;
   public final TickTimerList<Pair<OtherClientPlayerEntity, Box>> spoofed;
   public final TimerMap<OtherClientPlayerEntity, Vec3d> realPositions;

   public BackTrack() {
      super("Back Track", ".", SubCategory.OFFENSIVE, true);
      this.time = this.sgGeneral.i("Ticks", 5, 0, 20, 1, ".");
      this.maxTime = this.sgGeneral.i("Max Ticks", 50, 0, 100, 1, ".");
      this.renderShape = this.sgGeneral.e("Render Shape", RenderShape.Full, "Which parts of render should be rendered.");
      this.lineColor = this.sgGeneral.c("Line Color", new BlackOutColor(255, 0, 0, 255), "Line color of rendered boxes.");
      this.sideColor = this.sgGeneral.c("Side Color", new BlackOutColor(255, 0, 0, 50), "Side color of rendered boxes.");
      this.hit = new TickTimerList(false);
      this.spoofed = new TickTimerList(false);
      this.realPositions = new TimerMap(false);
      INSTANCE = this;
   }

   public static BackTrack getInstance() {
      return INSTANCE;
   }

   @Event
   public void onSend(PacketEvent.Send event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerInteractEntityC2SPacket) {
         PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)var3;
         AccessorInteractEntityC2SPacket accessor = (AccessorInteractEntityC2SPacket)packet;
         if (accessor.getType().method_34211() == InteractType.field_29172) {
            Entity var5 = BlackOut.mc.field_1687.method_8469(accessor.getId());
            if (var5 instanceof OtherClientPlayerEntity) {
               OtherClientPlayerEntity player = (OtherClientPlayerEntity)var5;
               Box box = player.method_5829();
               Pair<OtherClientPlayerEntity, Box> pair = new Pair(player, box);
               this.hit.remove((timer) -> {
                  return ((OtherClientPlayerEntity)((Pair)timer.value).method_15442()).equals(player);
               });
               this.hit.add(pair, (Integer)this.time.get());
               if (!this.spoofed.contains((timer) -> {
                  return ((OtherClientPlayerEntity)((Pair)timer.value).method_15442()).equals(player);
               })) {
                  this.spoofed.remove((timer) -> {
                     return ((OtherClientPlayerEntity)((Pair)timer.value).method_15442()).equals(player);
                  });
                  this.spoofed.add(pair, (Integer)this.maxTime.get());
               }
            }
         }
      }

   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      this.realPositions.remove((key, value) -> {
         if (System.currentTimeMillis() > value.endTime) {
            return true;
         } else {
            Box box = new Box(((Vec3d)value.value).method_10216() - 0.3D, ((Vec3d)value.value).method_10214(), ((Vec3d)value.value).method_10215() - 0.3D, ((Vec3d)value.value).method_10216() + 0.3D, ((Vec3d)value.value).method_10214() + key.method_5829().method_17940(), ((Vec3d)value.value).method_10215() + 0.3D);
            Render3DUtils.box(box, (BlackOutColor)this.sideColor.get(), (BlackOutColor)this.lineColor.get(), (RenderShape)this.renderShape.get());
            return false;
         }
      });
   }

   @Event
   public void onTick(TickEvent.Pre event) {
      this.hit.timers.removeIf((item) -> {
         if (item.ticks-- <= 0) {
            this.spoofed.remove((timer) -> {
               return ((OtherClientPlayerEntity)((Pair)item.value).method_15442()).equals(((Pair)timer.value).method_15442());
            });
            return true;
         } else {
            return false;
         }
      });
      this.spoofed.update();
   }
}
