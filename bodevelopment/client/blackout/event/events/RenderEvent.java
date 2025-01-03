package bodevelopment.client.blackout.event.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class RenderEvent {
   public double frameTime = 0.0D;
   public float tickDelta = 0.0F;
   private long prevEvent = 0L;

   protected void setFrameTime() {
      if (this.prevEvent > 0L) {
         this.frameTime = (double)(System.currentTimeMillis() - this.prevEvent) / 1000.0D;
      }

      this.prevEvent = System.currentTimeMillis();
   }

   public static class Hud extends RenderEvent {
      public DrawContext context;

      public static class Post extends RenderEvent.Hud {
         private static final RenderEvent.Hud.Post INSTANCE = new RenderEvent.Hud.Post();

         public static RenderEvent.Hud.Post get(DrawContext context, float tickDelta) {
            INSTANCE.context = context;
            INSTANCE.tickDelta = tickDelta;
            INSTANCE.setFrameTime();
            return INSTANCE;
         }
      }

      public static class Pre extends RenderEvent.Hud {
         private static final RenderEvent.Hud.Pre INSTANCE = new RenderEvent.Hud.Pre();

         public static RenderEvent.Hud.Pre get(DrawContext context, float tickDelta) {
            INSTANCE.context = context;
            INSTANCE.tickDelta = tickDelta;
            INSTANCE.setFrameTime();
            return INSTANCE;
         }
      }
   }

   public static class World extends RenderEvent {
      public MatrixStack stack = null;

      public static class Post extends RenderEvent.World {
         private static final RenderEvent.World.Post INSTANCE = new RenderEvent.World.Post();

         public static RenderEvent.World.Post get(MatrixStack stack, float tickDelta) {
            INSTANCE.stack = stack;
            INSTANCE.tickDelta = tickDelta;
            INSTANCE.setFrameTime();
            return INSTANCE;
         }
      }

      public static class Pre extends RenderEvent.World {
         private static final RenderEvent.World.Pre INSTANCE = new RenderEvent.World.Pre();

         public static RenderEvent.World.Pre get(MatrixStack stack, float tickDelta) {
            INSTANCE.stack = stack;
            INSTANCE.tickDelta = tickDelta;
            INSTANCE.setFrameTime();
            return INSTANCE;
         }
      }
   }
}
