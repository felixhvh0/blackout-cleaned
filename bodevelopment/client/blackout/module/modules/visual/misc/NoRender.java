package bodevelopment.client.blackout.module.modules.visual.misc;

import bodevelopment.client.blackout.module.Module;
import bodevelopment.client.blackout.module.SubCategory;
import bodevelopment.client.blackout.module.setting.RegistryNames;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;

public class NoRender extends Module {
   private static NoRender INSTANCE;
   private final SettingGroup sgGeneral = this.addGroup("General");
   private final SettingGroup sgItems = this.addGroup("Items");
   private final Setting<List<ParticleType<?>>> particles;
   public final Setting<Boolean> wallOverlay;
   public final Setting<Boolean> waterOverlay;
   public final Setting<Boolean> fireOverlay;
   public final Setting<Boolean> effectOverlay;
   public final Setting<Boolean> totem;
   public final Setting<Boolean> pumpkin;
   public final Setting<Boolean> crystalBase;
   public final Setting<Boolean> helmet;
   public final Setting<Boolean> chestplate;
   public final Setting<Boolean> leggings;
   public final Setting<Boolean> boots;
   public final Setting<Boolean> left;
   public final Setting<Boolean> right;

   public NoRender() {
      super("No Render", "Doesn't render some stuff.", SubCategory.MISC_VISUAL, true);
      this.particles = this.sgGeneral.r("Particles", ".", Registries.field_41180, RegistryNames::get);
      this.wallOverlay = this.sgGeneral.b("Wall Overlay", true, "Doesn't cover your whole screen while inside a wall.");
      this.waterOverlay = this.sgGeneral.b("Water Overlay", true, "Doesn't render water overlay.");
      this.fireOverlay = this.sgGeneral.b("Fire Overlay", true, "Doesn't render fire overlay.");
      this.effectOverlay = this.sgGeneral.b("Effect Overlay", true, "Doesn't render effect overlay.");
      this.totem = this.sgGeneral.b("Totem", true, "Doesn't render totem of undying after popping.");
      this.pumpkin = this.sgGeneral.b("Pumpkin Overlay", true, "Doesn't render the pumpkin overlay.");
      this.crystalBase = this.sgGeneral.b("Crystal Base", true, "Doesn't render the bedrock slab under end crystals.");
      this.helmet = this.sgItems.b("Helmet", false, ".");
      this.chestplate = this.sgItems.b("Chestplate", false, ".");
      this.leggings = this.sgItems.b("Leggings", false, ".");
      this.boots = this.sgItems.b("Boots", false, ".");
      this.left = this.sgItems.b("Left Hand", false, ".");
      this.right = this.sgItems.b("Right Hand", false, ".");
      INSTANCE = this;
   }

   public static NoRender getInstance() {
      return INSTANCE;
   }

   public boolean shouldNoRender(ParticleType<?> particleType) {
      return ((List)this.particles.get()).contains(particleType);
   }

   public boolean ignoreArmor(EquipmentSlot slot) {
      Setting var10000;
      switch(slot) {
      case field_6166:
         var10000 = this.boots;
         break;
      case field_6172:
         var10000 = this.leggings;
         break;
      case field_6174:
         var10000 = this.chestplate;
         break;
      default:
         var10000 = this.helmet;
      }

      return (Boolean)var10000.get();
   }

   public boolean ignoreHand(Arm arm) {
      return (Boolean)(arm == Arm.field_6183 ? this.right : this.left).get();
   }
}
