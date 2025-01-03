package bodevelopment.client.blackout.mixin.mixins;

import bodevelopment.client.blackout.interfaces.mixin.IChatHud;
import bodevelopment.client.blackout.interfaces.mixin.IChatHudLine;
import bodevelopment.client.blackout.interfaces.mixin.IVisible;
import bodevelopment.client.blackout.module.modules.misc.AntiSpam;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatHud.class})
public abstract class MixinChatHud implements IChatHud {
   @Shadow
   @Final
   private List<ChatHudLine> field_2061;
   @Shadow
   @Final
   private List<Visible> field_2064;
   @Unique
   private int addedId = -1;
   @Unique
   private ChatHudLine currentLine = null;

   @Shadow
   public abstract void method_1812(Text var1);

   public void blackout_Client$addMessageToChat(Text text, int id) {
      if (id != -1) {
         this.field_2061.removeIf((line) -> {
            return ((IChatHudLine)line).blackout_Client$idEquals(id);
         });
         this.field_2064.removeIf((visible) -> {
            return ((IVisible)visible).blackout_Client$idEquals(id);
         });
      }

      this.addedId = id;
      this.method_1812(text);
      this.addedId = -1;
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At("HEAD")}
   )
   private void onAddMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
      AntiSpam antiSpam = AntiSpam.getInstance();
      MutableInt highest = new MutableInt(0);
      this.currentLine = new ChatHudLine(ticks, message, signature, indicator);
      if (antiSpam.enabled) {
         AtomicBoolean b = new AtomicBoolean(false);
         this.field_2061.removeIf((line) -> {
            if (antiSpam.isSimilar(((IChatHudLine)line).blackout_Client$getMessage().getString(), message.getString())) {
               highest.setValue(Math.max(((IChatHudLine)line).blackout_Client$getSpam(), highest.getValue()));
               b.set(true);
               this.field_2064.removeIf((visible) -> {
                  return ((IVisible)visible).blackout_Client$messageEquals(line);
               });
               return true;
            } else {
               return false;
            }
         });
         if (b.get()) {
            MutableText var10004 = message.method_27661();
            Formatting var10005 = Formatting.field_1075;
            this.currentLine = new ChatHudLine(ticks, var10004.method_27693(var10005 + " (" + (highest.getValue() + 1) + ")"), signature, indicator);
         }
      }

      ((IChatHudLine)this.currentLine).blackout_Client$setSpam(highest.getValue() + 1);
      ((IChatHudLine)this.currentLine).blackout_Client$setMessage(message);
   }

   @Redirect(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"
)
   )
   private List<OrderedText> breakIntoLines(StringVisitable message, int width, TextRenderer textRenderer) {
      return ChatMessages.method_1850(this.currentLine.comp_893(), width, textRenderer);
   }

   @Redirect(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;add(ILjava/lang/Object;)V"
)
   )
   private <E> void addLineToList(List<E> instance, int index, E e) {
      if (e instanceof ChatHudLine) {
         ((IChatHudLine)this.currentLine).blackout_Client$setId(this.addedId);
         instance.add(index, this.currentLine);
      } else if (e instanceof Visible) {
         Visible line = (Visible)e;
         ((IVisible)line).blackout_Client$set(this.addedId);
         ((IVisible)line).blackout_Client$setLine(this.currentLine);
         instance.add(index, e);
      }

   }
}
