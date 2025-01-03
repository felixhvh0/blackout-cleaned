package bodevelopment.client.blackout.randomstuff;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.manager.managers.FakePlayerManager;
import bodevelopment.client.blackout.module.modules.client.settings.FakeplayerSettings;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class FakePlayerEntity extends AbstractClientPlayerEntity {
   public int progress;
   private final List<FakePlayerEntity.PlayerPos> positions = new ArrayList();
   private FakePlayerEntity.PlayerPos currentPlayerPos = null;
   private final String name;
   public int popped = 0;
   public int sinceSwap = 0;
   private int sinceEat = 0;

   public FakePlayerEntity(String name) {
      super(BlackOut.mc.field_1687, new GameProfile(Uuids.method_43344(name), name));
      this.method_49477(1.0F);
      this.field_5960 = true;
      this.name = name;
      FakePlayerEntity.PlayerPos ownPos = FakePlayerManager.getPlayerPos(BlackOut.mc.field_1724);
      this.updatePosition(ownPos);
      this.updatePosition(ownPos);
      Byte playerModel = (Byte)BlackOut.mc.field_1724.method_5841().method_12789(PlayerEntity.field_7518);
      this.field_6011.method_12778(PlayerEntity.field_7518, playerModel);
      this.method_6127().method_26846(BlackOut.mc.field_1724.method_6127());
      this.cloneInv(BlackOut.mc.field_1724.method_31548());
      this.method_6033(20.0F);
      this.method_6073(16.0F);
      this.method_31482();
      BlackOut.mc.field_1687.method_53875(this);
      this.progress = 0;
   }

   private void cloneInv(PlayerInventory inventory) {
      PlayerInventory ownInventory = this.method_31548();

      for(int i = 0; i < ownInventory.method_5439(); ++i) {
         ownInventory.method_5447(i, inventory.method_5438(i).method_7972());
      }

      ownInventory.field_7545 = inventory.field_7545;
   }

   public boolean method_5643(DamageSource source, float amount) {
      try {
         return this.innerDamage(source, amount);
      } catch (Exception var4) {
         return false;
      }
   }

   private boolean innerDamage(DamageSource source, float amount) {
      if (this.method_5679(source)) {
         return false;
      } else if (this.method_31549().field_7480 && !source.method_48789(DamageTypeTags.field_42242)) {
         return false;
      } else {
         amount *= ((Double)FakeplayerSettings.getInstance().damageMultiplier.get()).floatValue();
         this.field_6278 = 0;
         if (!this.method_29504() && !(amount < 0.0F)) {
            if (source.method_5514()) {
               if (this.method_37908().method_8407() == Difficulty.field_5805) {
                  amount = Math.min(amount / 2.0F + 1.0F, amount);
               }

               if (this.method_37908().method_8407() == Difficulty.field_5807) {
                  amount = amount * 3.0F / 2.0F;
               }
            }

            if (amount == 0.0F) {
               return false;
            } else if (this.method_5679(source)) {
               return false;
            } else if (this.method_29504()) {
               return false;
            } else if (source.method_48789(DamageTypeTags.field_42246) && this.method_6059(StatusEffects.field_5918)) {
               return false;
            } else {
               this.field_6278 = 0;
               boolean bl = false;
               Entity attacker;
               if (amount > 0.0F && this.method_6061(source)) {
                  this.method_6056(amount);
                  amount = 0.0F;
                  if (!source.method_48789(DamageTypeTags.field_42247)) {
                     attacker = source.method_5526();
                     if (attacker instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity)attacker;
                        this.method_6090(livingEntity);
                     }
                  }

                  bl = true;
               }

               if (source.method_48789(DamageTypeTags.field_42252) && this.method_5864().method_20210(EntityTypeTags.field_29826)) {
                  amount *= 5.0F;
               }

               this.field_42108.method_48567(1.5F);
               boolean bl2 = true;
               if (this.field_6008 > 10 && !source.method_48789(DamageTypeTags.field_42969)) {
                  if (amount <= this.field_6253) {
                     return false;
                  }

                  this.method_6074(source, amount - this.field_6253);
                  this.field_6253 = amount;
                  bl2 = false;
               } else {
                  this.field_6253 = amount;
                  this.field_6008 = 20;
                  this.method_6074(source, amount);
                  this.field_6254 = 10;
                  this.field_6235 = this.field_6254;
               }

               if (source.method_48789(DamageTypeTags.field_42240) && !this.method_6118(EquipmentSlot.field_6169).method_7960()) {
                  this.method_36977(source, amount);
                  amount *= 0.75F;
               }

               attacker = source.method_5529();
               if (attacker instanceof LivingEntity) {
                  LivingEntity livingEntity = (LivingEntity)attacker;
                  if (!source.method_48789(DamageTypeTags.field_42254)) {
                     this.method_6015(livingEntity);
                  }
               }

               if (attacker instanceof PlayerEntity) {
                  PlayerEntity player = (PlayerEntity)attacker;
                  this.field_6238 = 100;
                  this.field_6258 = player;
               } else if (attacker instanceof WolfEntity) {
                  WolfEntity wolf = (WolfEntity)attacker;
                  if (wolf.method_6181()) {
                     this.field_6238 = 100;
                     LivingEntity var9 = wolf.method_35057();
                     PlayerEntity var10001;
                     if (var9 instanceof PlayerEntity) {
                        PlayerEntity owner = (PlayerEntity)var9;
                        var10001 = owner;
                     } else {
                        var10001 = null;
                     }

                     this.field_6258 = var10001;
                  }
               }

               SoundEvent soundEvent;
               if (this.method_29504()) {
                  if (!this.method_6095(source)) {
                     soundEvent = this.method_6002();
                     if (bl2 && soundEvent != null) {
                        this.method_5783(soundEvent, this.method_6107(), this.method_6017());
                     }
                  } else {
                     BlackOut.mc.field_1713.method_3051(this, ParticleTypes.field_11220, 30);
                     BlackOut.mc.field_1687.method_8486(this.method_23317(), this.method_23318(), this.method_23321(), SoundEvents.field_14931, this.method_5634(), 1.0F, 1.0F, true);
                  }
               } else if (bl2) {
                  soundEvent = this.method_6011(source);
                  BlackOut.mc.field_1687.method_8486(this.method_23317(), this.method_23318(), this.method_23321(), soundEvent, this.method_5634(), 1.0F, 1.0F, true);
               }

               boolean bl3 = !bl || amount > 0.0F;
               if (bl3) {
                  this.field_6276 = source;
                  this.field_6226 = this.method_37908().method_8510();
               }

               return bl3;
            }
         } else {
            return false;
         }
      }
   }

   public boolean method_5640(double distance) {
      return true;
   }

   public void method_5773() {
      ++this.sinceEat;
      FakeplayerSettings fakeplayerSettings = FakeplayerSettings.getInstance();
      if (!this.method_6079().method_31574(Items.field_8288)) {
         if (++this.sinceSwap > (Integer)fakeplayerSettings.swapDelay.get() && (this.popped < (Integer)fakeplayerSettings.totems.get() || (Boolean)fakeplayerSettings.unlimitedTotems.get())) {
            this.swapToOffhand();
         }
      } else {
         this.sinceSwap = 0;
      }

      if ((Boolean)fakeplayerSettings.eating.get()) {
         if (!this.method_6047().method_31574(Items.field_8367)) {
            this.method_6122(Hand.field_5808, new ItemStack(Items.field_8367, 64));
         }

         this.method_6040();
      }

      super.method_5773();
   }

   public void method_6040() {
      this.method_6098(this.method_6047(), 16);
      ItemStack itemStack = this.method_6047().method_7910(this.method_37908(), this);
      if (itemStack != this.method_6047()) {
         this.method_6122(Hand.field_5808, itemStack);
      }

   }

   public ItemStack method_18866(World world, ItemStack stack) {
      if (stack.method_19267()) {
         world.method_43128((PlayerEntity)null, this.method_23317(), this.method_23318(), this.method_23321(), this.method_18869(stack), SoundCategory.field_15254, 1.0F, 1.0F + (world.field_9229.method_43057() - world.field_9229.method_43057()) * 0.4F);
         this.applyFoodEffects(stack, world, this);
         if (!this.method_31549().field_7477) {
            stack.method_7934(1);
         }
      }

      return stack;
   }

   public boolean method_6012() {
      Iterator var1 = this.method_6088().values().iterator();

      while(var1.hasNext()) {
         StatusEffectInstance instance = (StatusEffectInstance)var1.next();
         this.method_6129(instance);
      }

      return true;
   }

   protected void method_6129(StatusEffectInstance effect) {
      super.method_6129(effect);
      effect.method_5579().method_5562(this.method_6127());
      this.method_52543();
   }

   private void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity) {
      Item item = stack.method_7909();
      if (item.method_19263()) {
         Iterator var5 = item.method_19264().method_19235().iterator();

         while(var5.hasNext()) {
            com.mojang.datafixers.util.Pair<StatusEffectInstance, Float> pair = (com.mojang.datafixers.util.Pair)var5.next();
            if (pair.getFirst() != null && world.field_9229.method_43057() < (Float)pair.getSecond()) {
               targetEntity.method_6092(new StatusEffectInstance((StatusEffectInstance)pair.getFirst()));
            }
         }

      }
   }

   private void swapToOffhand() {
      this.method_6122(Hand.field_5810, Items.field_8288.method_7854());
      this.sinceSwap = 0;
      ++this.popped;
   }

   public void method_6007() {
      this.tickRecord();
      this.field_7505 = this.field_7483;
      this.method_29242(false);
      this.method_6119();
      float f = this.method_24828() && !this.method_29504() ? (float)Math.min(0.1D, this.method_18798().method_37267()) : 0.0F;
      this.field_7483 += (f - this.field_7483) * 0.4F;
   }

   public GameProfile method_7334() {
      return new GameProfile(Uuids.method_43344(this.name), this.name);
   }

   private void updatePosition(FakePlayerEntity.PlayerPos playerPos) {
      this.currentPlayerPos = playerPos;
      this.method_33574(this.currentPlayerPos.vec());
      this.method_5710(this.currentPlayerPos.yaw(), this.currentPlayerPos.pitch());
      this.method_18799(this.currentPlayerPos.velocity());
      this.field_6241 = this.currentPlayerPos.headYaw();
      this.field_6283 = this.currentPlayerPos.bodyYaw();
   }

   public void tickRecord() {
      if (this.progress >= this.positions.size() || this.method_29504()) {
         this.progress = -1;
      }

      if (this.progress >= 0) {
         FakePlayerEntity.PlayerPos playerPos = (FakePlayerEntity.PlayerPos)this.positions.get(this.progress);
         this.updatePosition(playerPos);
         ++this.progress;
      }

      Managers.EXTRAPOLATION.tick(this, this.method_19538().method_1023(this.field_6014, this.field_6036, this.field_5969));
   }

   public void set(List<FakePlayerEntity.PlayerPos> positions) {
      this.positions.addAll(positions);
   }

   public static record PlayerPos(Vec3d vec, Vec3d velocity, EntityPose pose, float pitch, float yaw, float headYaw, float bodyYaw) {
      public PlayerPos(Vec3d vec, Vec3d velocity, EntityPose pose, float pitch, float yaw, float headYaw, float bodyYaw) {
         this.vec = vec;
         this.velocity = velocity;
         this.pose = pose;
         this.pitch = pitch;
         this.yaw = yaw;
         this.headYaw = headYaw;
         this.bodyYaw = bodyYaw;
      }

      public Vec3d vec() {
         return this.vec;
      }

      public Vec3d velocity() {
         return this.velocity;
      }

      public EntityPose pose() {
         return this.pose;
      }

      public float pitch() {
         return this.pitch;
      }

      public float yaw() {
         return this.yaw;
      }

      public float headYaw() {
         return this.headYaw;
      }

      public float bodyYaw() {
         return this.bodyYaw;
      }
   }
}
