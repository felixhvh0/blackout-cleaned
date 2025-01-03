package bodevelopment.client.blackout.util.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

public class ModelVertexConsumer implements VertexConsumer {
   private double prevX;
   private double prevY;
   private double prevZ;
   private Vec3d[] currentArray;
   public final List<Vec3d[]> positions = new ArrayList();
   private int i = 0;

   public void start() {
      this.positions.clear();
      this.startArray();
   }

   private void startArray() {
      this.currentArray = new Vec3d[4];
      this.i = 0;
   }

   public VertexConsumer method_22912(double x, double y, double z) {
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
      return this;
   }

   public VertexConsumer method_1336(int red, int green, int blue, int alpha) {
      return this;
   }

   public VertexConsumer method_22913(float u, float v) {
      return this;
   }

   public VertexConsumer method_22917(int u, int v) {
      return this;
   }

   public VertexConsumer method_22921(int u, int v) {
      return this;
   }

   public VertexConsumer method_22914(float x, float y, float z) {
      return this;
   }

   public void method_1344() {
      this.currentArray[this.i++] = new Vec3d(this.prevX, this.prevY, this.prevZ);
      if (this.i >= 4) {
         this.positions.add(this.currentArray);
         this.startArray();
      }

   }

   public void method_22901(int red, int green, int blue, int alpha) {
   }

   public void method_35666() {
   }
}
