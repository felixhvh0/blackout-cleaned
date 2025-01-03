package bodevelopment.client.blackout.module.modules.visual.entities;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.combat.misc.AntiBot;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.module.setting.multisettings.BoxMultiSetting;
import bodevelopment.client.blackout.util.OLEPOSSUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BoxESP extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<List<EntityType<?>>> entityTypes;
   private final BoxMultiSetting rendering;
   private final List<Entity> entities;

   public BoxESP() {
      super("Box ESP", "Extra Sensory Perception with boxes!", SubCategory.ENTITIES, true);
      this.entityTypes = this.sgGeneral.el("Entities", ".", EntityType.field_6097);
      this.rendering = BoxMultiSetting.of(this.sgGeneral);
      this.entities = new ArrayList();
   }

   @Event
   public void onTickPost(TickEvent.Post event) {
      if (BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724 != null) {
         this.entities.clear();
         BlackOut.mc.field_1687.field_27733.method_31791((entity) -> {
            if (this.shouldRender(entity)) {
               this.entities.add(entity);
            }

         });
         this.entities.sort(Comparator.comparingDouble((entity) -> {
            return (double)(-BlackOut.mc.field_1724.method_5739(entity));
         }));
      }
   }

   public boolean shouldRender(Entity entity) {
      AntiBot antiBot = AntiBot.getInstance();
      if (antiBot.enabled && antiBot.mode.get() == AntiBot.HandlingMode.Ignore && entity instanceof AbstractClientPlayerEntity) {
         AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
         if (antiBot.getBots().contains(player)) {
            return false;
         }
      }

      return entity != BlackOut.mc.field_1724 && ((List)this.entityTypes.get()).contains(entity.method_5864());
   }

   @Event
   public void onRender(RenderEvent.World.Post event) {
      if (BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724 != null) {
         GlStateManager._disableDepthTest();
         GlStateManager._enableBlend();
         GlStateManager._disableCull();
         this.entities.forEach((entity) -> {
            this.renderBox(entity, (double)event.tickDelta);
         });
      }
   }

   private void renderBox(Entity entity, double tickDelta) {
      Vec3d pos = OLEPOSSUtils.getLerpedPos(entity, tickDelta);
      this.rendering.render(new Box(pos.method_10216() - entity.method_5829().method_17939() / 2.0D, pos.method_10214(), pos.method_10215() - entity.method_5829().method_17941() / 2.0D, pos.method_10216() + entity.method_5829().method_17939() / 2.0D, pos.method_10214() + entity.method_5829().method_17940(), pos.method_10215() + entity.method_5829().method_17941() / 2.0D));
   }
}
