package bodevelopment.client.blackout.module.setting;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class RegistryNames {
   private static final Map<ParticleType<?>, String> particles = new HashMap();

   public static void init() {
      Registries.field_41180.forEach((particleType) -> {
         if (particleType instanceof ParticleEffect) {
            ParticleEffect effect = (ParticleEffect)particleType;
            particles.put(particleType, capitalize(effect.method_10293()));
         }

      });
   }

   public static String get(ParticleType<?> particleType) {
      return (String)particles.getOrDefault(particleType, "null");
   }

   private static String capitalize(String string) {
      String var10000 = String.valueOf(string.charAt(0)).toUpperCase();
      return var10000 + string.substring(1);
   }
}
