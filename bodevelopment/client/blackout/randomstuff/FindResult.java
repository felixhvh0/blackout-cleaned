package bodevelopment.client.blackout.randomstuff;

import net.minecraft.item.ItemStack;

public record FindResult(int slot, int amount, ItemStack stack) {
   public FindResult(int slot, int amount, ItemStack stack) {
      this.slot = slot;
      this.amount = amount;
      this.stack = stack;
   }

   public boolean wasFound() {
      return this.slot > -1;
   }

   public int slot() {
      return this.slot;
   }

   public int amount() {
      return this.amount;
   }

   public ItemStack stack() {
      return this.stack;
   }
}
