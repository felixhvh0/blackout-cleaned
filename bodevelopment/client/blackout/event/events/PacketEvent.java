package bodevelopment.client.blackout.event.events;

import bodevelopment.client.blackout.event.Cancellable;
import net.minecraft.network.packet.Packet;

public class PacketEvent {
   public static class Received {
      private static final PacketEvent.Received INSTANCE = new PacketEvent.Received();
      public Packet<?> packet = null;

      public static PacketEvent.Received get(Packet<?> packet) {
         INSTANCE.packet = packet;
         return INSTANCE;
      }
   }

   public static class Receive {
      public static class Post extends Cancellable {
         private static final PacketEvent.Receive.Post INSTANCE = new PacketEvent.Receive.Post();
         public Packet<?> packet = null;

         public static PacketEvent.Receive.Post get(Packet<?> packet) {
            INSTANCE.packet = packet;
            INSTANCE.setCancelled(false);
            return INSTANCE;
         }
      }

      public static class Pre {
         private static final PacketEvent.Receive.Pre INSTANCE = new PacketEvent.Receive.Pre();
         public Packet<?> packet = null;

         public static PacketEvent.Receive.Pre get(Packet<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
         }
      }
   }

   public static class Sent {
      private static final PacketEvent.Sent INSTANCE = new PacketEvent.Sent();
      public Packet<?> packet = null;

      public static PacketEvent.Sent get(Packet<?> packet) {
         INSTANCE.packet = packet;
         return INSTANCE;
      }
   }

   public static class Send extends Cancellable {
      private static final PacketEvent.Send INSTANCE = new PacketEvent.Send();
      public Packet<?> packet = null;

      public static PacketEvent.Send get(Packet<?> packet) {
         INSTANCE.packet = packet;
         INSTANCE.setCancelled(false);
         return INSTANCE;
      }
   }
}
