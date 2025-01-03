package bodevelopment.client.blackout.util;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SimpleEntityLookup;

public class EntityUtils {
   public static boolean intersects(Box box, Predicate<Entity> predicate) {
      return intersects(box, predicate, (Map)null);
   }

   public static boolean intersects(Box box, Predicate<Entity> predicate, Map<Entity, Box> hitboxes) {
      int minX = ChunkSectionPos.method_32204(box.field_1323 - 2.0D);
      int minY = ChunkSectionPos.method_32204(box.field_1322 - 4.0D);
      int minZ = ChunkSectionPos.method_32204(box.field_1321 - 2.0D);
      int maxX = ChunkSectionPos.method_32204(box.field_1320 + 2.0D);
      int maxY = ChunkSectionPos.method_32204(box.field_1325);
      int maxZ = ChunkSectionPos.method_32204(box.field_1324 + 2.0D);
      SimpleEntityLookup<Entity> lookup = (SimpleEntityLookup)BlackOut.mc.field_1687.method_31592();

      label71:
      for(int x = minX; x <= maxX; ++x) {
         LongBidirectionalIterator var11 = lookup.field_27259.field_27253.subSet(ChunkSectionPos.method_18685(x, 0, 0), ChunkSectionPos.method_18685(x, -1, -1) + 1L).iterator();

         while(true) {
            EntityTrackingSection entityTrackingSection;
            do {
               do {
                  do {
                     long chunk;
                     int z;
                     do {
                        int y;
                        do {
                           do {
                              do {
                                 if (!var11.hasNext()) {
                                    continue label71;
                                 }

                                 chunk = (Long)var11.next();
                                 y = ChunkSectionPos.method_18689(chunk);
                                 z = ChunkSectionPos.method_18690(chunk);
                              } while(y < minY);
                           } while(y > maxY);
                        } while(z < minZ);
                     } while(z > maxZ);

                     entityTrackingSection = (EntityTrackingSection)lookup.field_27259.field_27252.get(chunk);
                  } while(entityTrackingSection == null);
               } while(entityTrackingSection.method_31761());
            } while(!entityTrackingSection.method_31768().method_31885());

            Iterator var17 = entityTrackingSection.field_27248.iterator();

            while(var17.hasNext()) {
               Entity entity = (Entity)var17.next();
               if (predicate.test(entity) && !Managers.ENTITY.isDead(entity.method_5628()) && getBox(entity, hitboxes).method_994(box)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static Box getBox(Entity entity, Map<Entity, Box> map) {
      return map != null && map.containsKey(entity) ? (Box)map.get(entity) : entity.method_5829();
   }

   public static boolean intersectsWithSpawningItem(BlockPos crystalPos) {
      return Managers.ENTITY.containsItem(crystalPos) || Managers.ENTITY.containsItem(crystalPos.method_10084());
   }
}
