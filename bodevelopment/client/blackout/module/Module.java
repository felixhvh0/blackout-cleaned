package bodevelopment.client.blackout.module;

import bodevelopment.client.blackout.BlackOut;
import bodevelopment.client.blackout.enums.BindMode;
import bodevelopment.client.blackout.enums.SwingHand;
import bodevelopment.client.blackout.enums.SwingState;
import bodevelopment.client.blackout.enums.SwingType;
import bodevelopment.client.blackout.helpers.RotationHelper;
import bodevelopment.client.blackout.interfaces.functional.SingleOut;
import bodevelopment.client.blackout.manager.Managers;
import bodevelopment.client.blackout.module.modules.client.Notifications;
import bodevelopment.client.blackout.module.modules.visual.misc.SwingModifier;
import bodevelopment.client.blackout.module.setting.Setting;
import bodevelopment.client.blackout.module.setting.SettingGroup;
import bodevelopment.client.blackout.module.setting.Settings;
import bodevelopment.client.blackout.module.setting.WarningSettingGroup;
import bodevelopment.client.blackout.module.setting.settings.KeyBindSetting;
import bodevelopment.client.blackout.randomstuff.PlaceData;
import bodevelopment.client.blackout.util.ChatUtils;
import bodevelopment.client.blackout.util.SettingUtils;
import bodevelopment.client.blackout.util.SoundUtils;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Module extends RotationHelper {
   public final String name;
   public final String description;
   public final SubCategory category;
   public boolean enabled = false;
   public long toggleTime = 0L;
   public final List<SettingGroup> settingGroups = new ArrayList();
   public final SettingGroup sgModule = this.addGroup("Module");
   private final Setting<String> displayName;
   public final KeyBindSetting bind;
   public final Setting<BindMode> bindMode;
   public boolean wip = false;

   public Module(String name, String description, SubCategory category, boolean subscribe) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.set(this);
      this.displayName = this.sgModule.s("Name", name, "");
      this.bind = (KeyBindSetting)Settings.k("Bind", "This module can be toggled by pressing this key.", (SingleOut)null);
      this.bindMode = this.sgModule.e("Bind Mode", BindMode.Toggle, ".");
      if (subscribe) {
         BlackOut.EVENT_BUS.subscribe(this, this::shouldSkipListeners);
      }

   }

   public boolean toggleable() {
      return true;
   }

   public String getFileName() {
      return this.name.replaceAll(" ", "");
   }

   public String getDisplayName() {
      String dn = (String)this.displayName.get();
      return dn.isEmpty() ? this.name : dn;
   }

   public void toggle() {
      if (this.enabled) {
         this.disable();
      } else {
         this.enable();
      }

   }

   public void silentEnable() {
      this.enable((String)null, 0, false);
   }

   public void enable() {
      this.enable((String)null, 2, true);
   }

   public void enable(String message) {
      this.enable(message, 2, true);
   }

   public void enable(String message, int time, boolean sendNotification) {
      if (!this.enabled) {
         this.onEnable();
         this.enabled = true;
         this.toggleTime = System.currentTimeMillis();
         if (sendNotification) {
            this.sendNotification(message == null ? this.getDisplayName() + Formatting.field_1060 + " Enabled" : " " + message, message == null ? "Enabled " + this.getDisplayName() : message, "Module Toggle", Notifications.Type.Enable, (double)(time == 0 ? 2 : time));
            if ((Boolean)Notifications.getInstance().sound.get()) {
               SoundUtils.play(1.0F, 1.0F, "enable");
            }
         }

      }
   }

   public void silentDisable() {
      this.doDisable((String)null, 0, Notifications.Type.Disable, false);
   }

   public void disable() {
      this.disable((String)null, 2);
   }

   public void disable(String message) {
      this.disable(message, 2);
   }

   public void disable(String message, int time) {
      this.doDisable(message, time, Notifications.Type.Disable, true);
   }

   public void disable(String message, int time, Notifications.Type type) {
      this.doDisable(message, time, type, true);
   }

   private void doDisable(String message, int time, Notifications.Type type, Boolean sendNotification) {
      if (this.enabled) {
         this.onDisable();
         this.enabled = false;
         this.toggleTime = System.currentTimeMillis();
         if (sendNotification) {
            this.sendNotification(message == null ? this.getDisplayName() + Formatting.field_1061 + " OFF" : " " + message, message == null ? "Disabled " + this.getDisplayName() : message, "Module Toggle", type, (double)(time == 0 ? 2 : time));
            if ((Boolean)Notifications.getInstance().sound.get()) {
               SoundUtils.play(1.0F, 1.0F, "disable");
            }
         }

      }
   }

   protected void sendNotification(String chatMessage, String text, String bigText, Notifications.Type type, double time) {
      Notifications notifications = Notifications.getInstance();
      if ((Boolean)notifications.chatNotifications.get()) {
         this.sendMessage(chatMessage);
      }

      Managers.NOTIFICATIONS.addNotification(text, bigText, time, type);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public String getInfo() {
      return null;
   }

   protected void sendMessage(String message) {
      ChatUtils.addMessage(Notifications.getInstance().getClientPrefix() + " " + message, Objects.hash(new Object[]{this.name + "toggle"}));
   }

   protected void sendPacket(Packet<?> packet) {
      Managers.PACKET.sendPacket(packet);
   }

   protected void sendInstantly(Packet<?> packet) {
      Managers.PACKET.sendInstantly(packet);
   }

   protected void sendSequencedInstantly(SequencedPacketCreator packetCreator) {
      if (BlackOut.mc.field_1761 != null && BlackOut.mc.field_1687 != null) {
         PendingUpdateManager sequence = BlackOut.mc.field_1687.method_41925().method_41937();
         Packet<?> packet = packetCreator.predict(sequence.method_41942());
         this.sendInstantly(packet);
         sequence.close();
      }
   }

   protected void sendSequenced(SequencedPacketCreator packetCreator) {
      if (BlackOut.mc.field_1761 != null && BlackOut.mc.field_1687 != null) {
         PendingUpdateManager sequence = BlackOut.mc.field_1687.method_41925().method_41937();
         Packet<?> packet = packetCreator.predict(sequence.method_41942());
         this.sendPacket(packet);
         sequence.close();
      }
   }

   protected void sendSequencedPostGrim(SequencedPacketCreator packetCreator) {
      if (BlackOut.mc.field_1761 != null && BlackOut.mc.field_1687 != null) {
         PendingUpdateManager sequence = BlackOut.mc.field_1687.method_41925().method_41937();
         Packet<?> packet = packetCreator.predict(sequence.method_41942());
         Managers.PACKET.sendPostPacket(packet);
         sequence.close();
      }
   }

   protected void placeBlock(Hand hand, PlaceData data) {
      boolean shouldSneak = data.sneak() && !BlackOut.mc.field_1724.method_5715();
      if (shouldSneak) {
         this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12979));
      }

      this.placeBlock(hand, data.pos().method_46558(), data.dir(), data.pos());
      if (shouldSneak) {
         this.sendPacket(new ClientCommandC2SPacket(BlackOut.mc.field_1724, Mode.field_12984));
      }

   }

   protected void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
      Hand finalHand = (Hand)Objects.requireNonNullElse(hand, Hand.field_5808);
      Vec3d eyes = BlackOut.mc.field_1724.method_33571();
      boolean inside = eyes.field_1352 > (double)pos.method_10263() && eyes.field_1352 < (double)(pos.method_10263() + 1) && eyes.field_1351 > (double)pos.method_10264() && eyes.field_1351 < (double)(pos.method_10264() + 1) && eyes.field_1350 > (double)pos.method_10260() && eyes.field_1350 < (double)(pos.method_10260() + 1);
      SettingUtils.swing(SwingState.Pre, SwingType.Placing, finalHand);
      this.sendSequenced((s) -> {
         return new PlayerInteractBlockC2SPacket(finalHand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s);
      });
      SettingUtils.swing(SwingState.Post, SwingType.Placing, finalHand);
   }

   protected void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
      Hand finalHand = (Hand)Objects.requireNonNullElse(hand, Hand.field_5808);
      Vec3d eyes = BlackOut.mc.field_1724.method_33571();
      boolean inside = eyes.field_1352 > (double)pos.method_10263() && eyes.field_1352 < (double)(pos.method_10263() + 1) && eyes.field_1351 > (double)pos.method_10264() && eyes.field_1351 < (double)(pos.method_10264() + 1) && eyes.field_1350 > (double)pos.method_10260() && eyes.field_1350 < (double)(pos.method_10260() + 1);
      SettingUtils.swing(SwingState.Pre, SwingType.Interact, finalHand);
      this.sendSequenced((s) -> {
         return new PlayerInteractBlockC2SPacket(finalHand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s);
      });
      SettingUtils.swing(SwingState.Post, SwingType.Interact, finalHand);
   }

   protected void useItem(Hand hand) {
      Hand finalHand = (Hand)Objects.requireNonNullElse(hand, Hand.field_5808);
      if (SettingUtils.grimUsing()) {
         this.sendPacket(new Full(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
      }

      SettingUtils.swing(SwingState.Pre, SwingType.Using, finalHand);
      this.sendSequenced((s) -> {
         return new PlayerInteractItemC2SPacket(finalHand, s);
      });
      SettingUtils.swing(SwingState.Post, SwingType.Using, finalHand);
   }

   protected void useItemInstantly(Hand hand) {
      Hand finalHand = (Hand)Objects.requireNonNullElse(hand, Hand.field_5808);
      if (SettingUtils.grimUsing()) {
         this.sendPacket(new Full(BlackOut.mc.field_1724.method_23317(), BlackOut.mc.field_1724.method_23318(), BlackOut.mc.field_1724.method_23321(), Managers.ROTATION.prevYaw, Managers.ROTATION.prevPitch, Managers.PACKET.isOnGround()));
      }

      SettingUtils.swing(SwingState.Pre, SwingType.Using, finalHand);
      this.sendSequencedInstantly((s) -> {
         return new PlayerInteractItemC2SPacket(finalHand, s);
      });
      SettingUtils.swing(SwingState.Post, SwingType.Using, finalHand);
   }

   protected void releaseUseItem() {
      this.sendPacket(new PlayerActionC2SPacket(Action.field_12974, BlockPos.field_10980, Direction.field_11033, 0));
   }

   protected void attackEntity(Entity entity) {
      SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.field_5808);
      this.sendPacket(PlayerInteractEntityC2SPacket.method_34206(entity, BlackOut.mc.field_1724.method_5715()));
      SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.field_5808);
      if (entity instanceof EndCrystalEntity) {
         Managers.ENTITY.setSemiDead(entity.method_5628());
      }

   }

   protected void clientSwing(SwingHand swingHand, Hand realHand) {
      Hand var10000;
      switch(swingHand) {
      case MainHand:
         var10000 = Hand.field_5808;
         break;
      case OffHand:
         var10000 = Hand.field_5810;
         break;
      case RealHand:
         var10000 = (Hand)Objects.requireNonNullElse(realHand, Hand.field_5808);
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      Hand hand = var10000;
      BlackOut.mc.field_1724.method_23667(hand, true);
      SwingModifier.getInstance().startSwing(hand);
   }

   protected void blockPlaceSound(BlockPos pos, ItemStack stack) {
      if (stack != null) {
         this.blockPlaceSound(pos, stack.method_7909());
      }
   }

   protected void blockPlaceSound(BlockPos pos, Item item) {
      if (item instanceof BlockItem) {
         BlockItem blockItem = (BlockItem)item;
         this.blockPlaceSound(pos, blockItem);
      }
   }

   protected void blockPlaceSound(BlockPos pos, BlockItem blockItem) {
      BlackOut.mc.field_1687.method_8486((double)pos.method_10263() + 0.5D, (double)pos.method_10264() + 0.5D, (double)pos.method_10260() + 0.5D, blockItem.method_19260(BlackOut.mc.field_1687.method_8320(pos)), SoundCategory.field_15245, 1.0F, 1.0F, true);
   }

   protected void blockPlaceSound(BlockPos pos, BlockItem blockItem, float volume, float pitch, boolean distance) {
      BlackOut.mc.field_1687.method_8486((double)pos.method_10263() + 0.5D, (double)pos.method_10264() + 0.5D, (double)pos.method_10260() + 0.5D, blockItem.method_19260(BlackOut.mc.field_1687.method_8320(pos)), SoundCategory.field_15245, volume, pitch, distance);
   }

   protected SettingGroup addGroup(String name) {
      SettingGroup group = new SettingGroup(name);
      this.settingGroups.add(group);
      return group;
   }

   protected SettingGroup addGroup(String name, String warning) {
      SettingGroup group = new WarningSettingGroup(name, warning);
      this.settingGroups.add(group);
      return group;
   }

   public void readSettings(JsonObject jsonObject) {
      this.settingGroups.forEach((group) -> {
         group.settings.forEach((s) -> {
            s.read(jsonObject);
         });
      });
   }

   public void writeSettings(JsonObject jsonObject) {
      this.settingGroups.forEach((group) -> {
         group.settings.forEach((s) -> {
            s.write(jsonObject);
         });
      });
   }

   public boolean shouldSkipListeners() {
      return !this.enabled;
   }

   protected void closeInventory() {
      this.sendPacket(new CloseHandledScreenC2SPacket(BlackOut.mc.field_1724.field_7512.field_7763));
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         boolean var10000;
         if (object instanceof Module) {
            Module module = (Module)object;
            if (module.name.equals(this.name)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }
}
