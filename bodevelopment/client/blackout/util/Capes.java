package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.rendering.texture.BOTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class Capes {
   private static final Map<String, Identifier> capes = new HashMap();
   private static final List<Identifier> loaded = new ArrayList();
   private static final List<Pair<String, Identifier>> toLoad = new ArrayList();
   private static boolean loading = false;

   public static Identifier getCape(AbstractClientPlayerEntity player) {
      String UUID = player.method_5845();
      if (!capes.containsKey(UUID)) {
         return null;
      } else {
         Identifier identifier = (Identifier)capes.get(UUID);
         if (!loaded.contains(identifier)) {
            if (!loading) {
               startLoad();
            }

            return null;
         } else {
            return (Identifier)capes.get(UUID);
         }
      }
   }

   public static void requestCapes() {
      CompletableFuture.runAsync(() -> {
         try {
            Map<String, Identifier> identifiers = new HashMap();
            InputStream stream = (new URL("https://raw.githubusercontent.com/KassuK1/BlackoutCapes/main/capes")).openStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(stream));
            read.lines().forEach((line) -> {
               readLine(line, identifiers);
            });
            read.close();
         } catch (IOException var3) {
         }

      });
   }

   private static void loadCape(String name, Identifier identifier) {
      CompletableFuture.runAsync(() -> {
         loading = true;
         new Capes.CapeTexture(name, identifier);
         loading = false;
      });
   }

   private static void startLoad() {
      if (!toLoad.isEmpty()) {
         Pair<String, Identifier> pair = (Pair)toLoad.get(0);
         toLoad.remove(0);
         String name = (String)pair.method_15442();
         Identifier identifier = (Identifier)pair.method_15441();
         loadCape(name, identifier);
      }
   }

   private static void readLine(String line, Map<String, Identifier> identifiers) {
      String[] string = line.replace(" ", "").split(":");
      if (string.length >= 3) {
         String UUID = string[1];
         String capeName = string[2];
         capes.put(UUID, (Identifier)identifiers.computeIfAbsent(capeName, (name) -> {
            Identifier identifier = new Identifier("blackout", "textures/capes/" + name + ".png");
            toLoad.add(new Pair(name, identifier));
            return identifier;
         }));
      }
   }

   private static class CapeTexture extends AbstractTexture {
      public CapeTexture(String name, Identifier identifier) {
         try {
            BufferedImage image = ImageIO.read(new URL("https://raw.githubusercontent.com/KassuK1/BlackoutCapes/main/textures/" + name + ".png"));
            if (!RenderSystem.isOnRenderThread()) {
               RenderSystem.recordRenderCall(() -> {
                  try {
                     this.setId(name, identifier, image);
                  } catch (IOException var5) {
                  }

               });
            } else {
               this.setId(name, identifier, image);
            }
         } catch (IOException var4) {
         }

      }

      private void setId(String name, Identifier identifier, BufferedImage image) throws IOException {
         this.field_5204 = BOTextures.upload(image).id();
         BlackOut.mc.method_1531().method_4616(identifier, this);
         Capes.loaded.add(identifier);
         System.out.println("Loaded cape: " + name);
      }

      public void method_4625(ResourceManager manager) {
      }
   }
}
