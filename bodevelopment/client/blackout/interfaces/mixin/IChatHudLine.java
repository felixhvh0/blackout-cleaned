package bodevelopment.client.blackout.interfaces.mixin;

import net.minecraft.text.Text;

public interface IChatHudLine {
   void blackout_Client$setId(int var1);

   boolean blackout_Client$idEquals(int var1);

   Text blackout_Client$getMessage();

   void blackout_Client$setMessage(Text var1);

   void blackout_Client$setSpam(int var1);

   int blackout_Client$getSpam();
}
