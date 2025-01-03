package bodevelopment.client.blackout.mixin.accessors;

import net.minecraft.client.sound.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AbstractSoundInstance.class})
public interface AccessorAbstractSoundInstance {
   @Accessor("volume")
   void setVolume(float var1);

   @Accessor("pitch")
   void setPitch(float var1);
}
