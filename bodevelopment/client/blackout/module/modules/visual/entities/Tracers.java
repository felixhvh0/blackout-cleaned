package bodevelopment.client.blackout.module.modules.visual.entities;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.event.Event;
import bodevelopment.client.blackout.event.events.RenderEvent;
import bodevelopment.client.blackout.event.events.TickEvent;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.modules.combat.misc.AntiBot;
import bodevelopment.client.blackout.module.modules.visual.misc.Freecam;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.randomstuff.BlackOutColor;
import bodevelopment.client.blackout.util.render.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class Tracers extends Module {
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final Setting<List<EntityType<?>>> entityTypes;
   private final Setting<BlackOutColor> line;
   private final Setting<BlackOutColor> friendLine;
   private final MatrixStack stack;
   private final List<Entity> entities;

   public Tracers() {
      super("Tracers", "Traces to other entities", SubCategory.ENTITIES, true);
      this.entityTypes = this.sgGeneral.el("Entities", ".", EntityType.field_6097);
      this.line = this.sgGeneral.c("Line Color", new BlackOutColor(255, 255, 255, 100), ".");
      this.friendLine = this.sgGeneral.c("Friend Line Color", new BlackOutColor(150, 150, 255, 100), ".");
      this.stack = new MatrixStack();
      this.entities = new ArrayList();
   }

   @Event
   public void onTick(TickEvent.Post event) {
      if (BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724 != null) {
         this.entities.clear();
         BlackOut.mc.field_1687.field_27733.method_31791((entity) -> {
            if (this.shouldRender(entity)) {
               this.entities.add(entity);
            }

         });
         this.entities.sort(Comparator.comparingDouble((entity) -> {
            return -BlackOut.mc.field_1773.method_19418().method_19326().method_1022(entity.method_19538());
         }));
      }
   }

   @Event
   public void onRender(RenderEvent.Hud.Post event) {
      if (BlackOut.mc.field_1687 != null && BlackOut.mc.field_1724 != null) {
         this.stack.method_22903();
         RenderUtils.unGuiScale(this.stack);
         this.entities.forEach((entity) -> {
            this.renderTracer((double)event.tickDelta, entity);
         });
         this.stack.method_22909();
      }
   }

   public void renderTracer(double tickDelta, Entity entity) {
      double x = MathHelper.method_16436(tickDelta, entity.field_6014, entity.method_23317());
      double y = MathHelper.method_16436(tickDelta, entity.field_6036, entity.method_23318());
      double z = MathHelper.method_16436(tickDelta, entity.field_5969, entity.method_23321());
      this.stack.method_22903();
      Color color;
      if (entity instanceof PlayerEntity && Managers.FRIENDS.isFriend((PlayerEntity)entity)) {
         color = ((BlackOutColor)this.friendLine.get()).getColor();
      } else {
         color = ((BlackOutColor)this.line.get()).getColor();
      }

      Vec2f f = RenderUtils.getCoords(x, y + entity.method_5829().method_17940() / 2.0D, z, false);
      if (f == null) {
         this.stack.method_22909();
      } else {
         RenderUtils.line(this.stack, (float)BlackOut.mc.method_22683().method_4480() / 2.0F, (float)BlackOut.mc.method_22683().method_4507() / 2.0F, f.field_1343, f.field_1342, color.getRGB());
         this.stack.method_22909();
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

      if (!((List)this.entityTypes.get()).contains(entity.method_5864())) {
         return false;
      } else {
         return entity != BlackOut.mc.field_1724 ? true : Freecam.getInstance().enabled;
      }
   }
}
