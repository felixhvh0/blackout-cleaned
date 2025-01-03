package bodevelopment.client.blackout.randomstuff;

public class Pair<A, B> extends net.minecraft.util.Pair<A, B> {
   public Pair(A left, B right) {
      super(left, right);
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Pair)) {
         return false;
      } else {
         Pair<?, ?> pair = (Pair)obj;
         return this.method_15442().equals(pair.method_15442()) && this.method_15441().equals(pair.method_15441());
      }
   }
}
