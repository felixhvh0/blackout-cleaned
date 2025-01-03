package bodevelopment.client.blackout.event.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

public class RemoveEvent {
   private static final RemoveEvent INSTANCE = new RemoveEvent();
   public Entity entity;
   public RemovalReason removalReason;

   public static RemoveEvent get(Entity entity, RemovalReason removalReason) {
      INSTANCE.entity = entity;
      INSTANCE.removalReason = removalReason;
      return INSTANCE;
   }
}
