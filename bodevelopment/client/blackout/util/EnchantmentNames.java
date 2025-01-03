package bodevelopment.client.blackout.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.text.Text;

public class EnchantmentNames {
   public static final List<Enchantment> enchantments = new ArrayList();
   private static final Map<Enchantment, String[]> map = new HashMap();

   public static String getName(Enchantment enchantment, boolean shortName) {
      return shortName ? getShortName(enchantment) : getLongName(enchantment);
   }

   public static String getLongName(Enchantment enchantment) {
      return ((String[])map.get(enchantment))[0];
   }

   public static String getShortName(Enchantment enchantment) {
      return ((String[])map.get(enchantment))[1];
   }

   public static void init() {
      Map<Enchantment, String> shortNames = getShortNames();
      Field[] var1 = Enchantments.class.getDeclaredFields();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Field field = var1[var3];
         if (Modifier.isPublic(field.getModifiers()) && field.getType() == Enchantment.class) {
            Object object;
            try {
               object = field.get(field);
            } catch (IllegalAccessException var9) {
               throw new RuntimeException(var9);
            }

            if (!(object instanceof Enchantment)) {
               return;
            }

            Enchantment enchantment = (Enchantment)object;
            String name = Text.method_43471(enchantment.method_8184()).getString();
            String shortName = (String)shortNames.get(enchantment);
            map.put(enchantment, new String[]{name, shortName});
         }
      }

   }

   private static Map<Enchantment, String> getShortNames() {
      Map<Enchantment, String> map = new HashMap();
      put(map, Enchantments.field_9111, "prot");
      put(map, Enchantments.field_9095, "fire");
      put(map, Enchantments.field_9129, "feat");
      put(map, Enchantments.field_9107, "bla");
      put(map, Enchantments.field_9096, "proj");
      put(map, Enchantments.field_9127, "resp");
      put(map, Enchantments.field_9105, "aqua");
      put(map, Enchantments.field_9097, "tho");
      put(map, Enchantments.field_9128, "dep");
      put(map, Enchantments.field_9122, "frost");
      put(map, Enchantments.field_9113, "bind");
      put(map, Enchantments.field_23071, "soul");
      put(map, Enchantments.field_38223, "swi");
      put(map, Enchantments.field_9118, "sha");
      put(map, Enchantments.field_9123, "smi");
      put(map, Enchantments.field_9112, "bane");
      put(map, Enchantments.field_9121, "kno");
      put(map, Enchantments.field_9124, "asp");
      put(map, Enchantments.field_9110, "loot");
      put(map, Enchantments.field_9115, "swe");
      put(map, Enchantments.field_9131, "eff");
      put(map, Enchantments.field_9099, "silk");
      put(map, Enchantments.field_9119, "unb");
      put(map, Enchantments.field_9130, "for");
      put(map, Enchantments.field_9103, "pow");
      put(map, Enchantments.field_9116, "pun");
      put(map, Enchantments.field_9126, "fla");
      put(map, Enchantments.field_9125, "inf");
      put(map, Enchantments.field_9114, "luck");
      put(map, Enchantments.field_9100, "lure");
      put(map, Enchantments.field_9120, "loy");
      put(map, Enchantments.field_9106, "imp");
      put(map, Enchantments.field_9104, "rip");
      put(map, Enchantments.field_9117, "cha");
      put(map, Enchantments.field_9108, "mul");
      put(map, Enchantments.field_9098, "qui");
      put(map, Enchantments.field_9132, "pie");
      put(map, Enchantments.field_9101, "men");
      put(map, Enchantments.field_9109, "van");
      return map;
   }

   private static void put(Map<Enchantment, String> map, Enchantment enchantment, String name) {
      enchantments.add(enchantment);
      map.put(enchantment, name);
   }
}
