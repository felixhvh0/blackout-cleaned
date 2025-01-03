package bodevelopment.client.blackout.interfaces.mixin;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;

public interface IMinecraftClient {
   void blackout_Client$setSession(String var1, UUID var2, String var3, Optional<String> var4, Optional<String> var5, AccountType var6);

   void blackout_Client$setSession(Session var1);

   void blackout_Client$useItem();
}
