package bodevelopment.client.blackout.interfaces.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;

public interface IVisible {
   void blackout_Client$set(int var1);

   boolean blackout_Client$idEquals(int var1);

   boolean blackout_Client$messageEquals(ChatHudLine var1);

   void blackout_Client$setLine(ChatHudLine var1);
}
