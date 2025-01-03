package bodevelopment.client.blackout.mixin.accessors;

import net.minecraft.block.AbstractBlock.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Settings.class})
public interface AccessorBlockSettings {
   @Accessor("replaceable")
   boolean replaceable();
}
