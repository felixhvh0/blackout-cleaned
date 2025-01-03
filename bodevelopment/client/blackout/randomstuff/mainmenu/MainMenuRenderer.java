package bodevelopment.client.blackout.randomstuff.mainmenu;

import net.minecraft.client.util.math.MatrixStack;

public interface MainMenuRenderer {
   void render(MatrixStack var1, float var2, float var3, float var4, String var5);

   void renderBackground(MatrixStack var1, float var2, float var3, float var4, float var5);

   int onClick(float var1, float var2);
}
