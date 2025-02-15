package bodevelopment.client.blackout.module.modules.misc;

import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.randomstuff.Pair;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.Packet;

public class Pause extends Module {
   private static Pause INSTANCE;
   public final List<Pair<ChannelHandlerContext, Packet<?>>> packets = new ArrayList();
   public boolean emptying = false;

   public Pause() {
      super("Pause", "Pauses receiving packets.", SubCategory.MISC, false);
      INSTANCE = this;
   }

   public static Pause getInstance() {
      return INSTANCE;
   }
}
