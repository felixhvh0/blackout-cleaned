package bodevelopment.client.blackout.hud.elements;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.hud.HudElement;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.rendering.renderer.TextureRenderer;
import bodevelopment.client.blackout.rendering.texture.BOTextures;

public class CatGirl extends HudElement {
   public final SettingGroup sgGeneral = this.addGroup("General");

   public CatGirl() {
      super("Cat Girl", "Literally a cat girl.");
      TextureRenderer t = BOTextures.getCat2Renderer();
      if (t != null) {
         this.setSize((float)t.getWidth() / 4.0F, (float)t.getHeight() / 4.0F);
      } else {
         this.setSize(120.0F, 175.0F);
      }

   }

   public void render() {
      if (BlackOut.mc.field_1724 != null && BlackOut.mc.field_1687 != null) {
         TextureRenderer t = BOTextures.getCat2Renderer();
         float width = (float)t.getWidth() / 4.0F;
         float height = (float)t.getHeight() / 4.0F;
         this.setSize(width, height);
         t.quad(this.stack, 0.0F, 0.0F, width, height);
      }
   }

   public static enum Side {
      Left,
      Right;

      // $FF: synthetic method
      private static CatGirl.Side[] $values() {
         return new CatGirl.Side[]{Left, Right};
      }
   }
}
