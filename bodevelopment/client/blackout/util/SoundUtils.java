package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.Channel.SourceManager;
import net.minecraft.client.sound.SoundEngine.RunMode;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

public class SoundUtils {
   public static void play(float pitch, float volume, String name) {
      play(pitch, volume, 0.0D, 0.0D, 0.0D, false, name);
   }

   public static void play(SoundInstance instance, String name) {
      play(instance.method_4782(), instance.method_4781(), instance.method_4784(), instance.method_4779(), instance.method_4778(), instance.method_4787(), name);
   }

   public static void play(float pitch, float volume, double x, double y, double z, boolean relative, String name) {
      InputStream inputStream = FileUtils.getResourceStream("sounds", name + ".ogg");
      SoundSystem engine = BlackOut.mc.method_1483().field_5590;
      SourceManager sourceManager = createSourceManager(engine, 5);
      if (sourceManager != null) {
         Vec3d vec = new Vec3d(x, y, z);
         sourceManager.method_19735((source) -> {
            source.method_19639(pitch);
            source.method_19647(volume);
            source.method_19657();
            source.method_19645(false);
            source.method_19641(vec);
            source.method_19649(relative);
         });
         CompletableFuture.supplyAsync(() -> {
            try {
               return new OggAudioStream(inputStream);
            } catch (IOException var2) {
               throw new CompletionException(var2);
            }
         }, Util.method_18349()).thenAccept((stream) -> {
            sourceManager.method_19735((source) -> {
               source.method_19643(stream);
               source.method_19650();
            });
         });
      }
   }

   private static SourceManager createSourceManager(SoundSystem engine, int i) {
      SourceManager sourceManager = (SourceManager)engine.field_18949.method_19723(RunMode.field_18353).join();
      SourceManager var10000;
      if (sourceManager == null && i > 0) {
         --i;
         var10000 = createSourceManager(engine, i);
      } else {
         var10000 = sourceManager;
      }

      return var10000;
   }
}
