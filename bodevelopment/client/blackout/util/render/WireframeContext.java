package bodevelopment.client.blackout.util.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public class WireframeContext {
   public final List<Vec3d[]> lines = new ArrayList();
   public final List<Vec3d[]> quads = new ArrayList();

   public static WireframeContext of(List<Vec3d[]> positions) {
      WireframeContext context = new WireframeContext();
      context.quads.addAll(positions);
      Iterator var2 = positions.iterator();

      while(var2.hasNext()) {
         Vec3d[] arr = (Vec3d[])var2.next();

         for(int i = 0; i < 4; ++i) {
            Vec3d[] line = new Vec3d[]{arr[i], arr[i + 1]};
            if (!contains(context.lines, line)) {
               context.lines.add(line);
            }
         }
      }

      return context;
   }

   private static boolean contains(List<Vec3d[]> list, Vec3d[] line) {
      Iterator var2 = list.iterator();

      Vec3d[] arr;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         arr = (Vec3d[])var2.next();
         if (arr[0].equals(line[0]) && arr[1].equals(line[1])) {
            return true;
         }
      } while(!arr[0].equals(line[1]) || !arr[1].equals(line[0]));

      return true;
   }
}
