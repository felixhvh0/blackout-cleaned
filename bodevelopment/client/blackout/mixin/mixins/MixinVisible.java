package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Visible.class})
public class MixinVisible implements IVisible {
   @Unique
   private int id;
   @Unique
   private ChatHudLine line;

   public void blackout_Client$set(int id) {
      this.id = id;
   }

   public boolean blackout_Client$idEquals(int id) {
      return this.id == id;
   }

   public boolean blackout_Client$messageEquals(ChatHudLine line) {
      return this.line.equals(line);
   }

   public void blackout_Client$setLine(ChatHudLine line) {
      this.line = line;
   }
}
