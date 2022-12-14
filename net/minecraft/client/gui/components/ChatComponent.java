package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;

public class ChatComponent extends GuiComponent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_CHAT_HISTORY = 100;
   private static final int MESSAGE_NOT_FOUND = -1;
   private static final int MESSAGE_INDENT = 4;
   private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
   private final Minecraft minecraft;
   private final List<String> recentChat = Lists.newArrayList();
   private final List<GuiMessage> allMessages = Lists.newArrayList();
   private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
   private int chatScrollbarPos;
   private boolean newMessageSinceScroll;

   public ChatComponent(Minecraft var1) {
      super();
      this.minecraft = var1;
   }

   public void render(PoseStack var1, int var2) {
      if (!this.isChatHidden()) {
         int var3 = this.getLinesPerPage();
         int var4 = this.trimmedMessages.size();
         if (var4 > 0) {
            boolean var5 = this.isChatFocused();
            float var6 = (float)this.getScale();
            int var7 = Mth.ceil((float)this.getWidth() / var6);
            var1.pushPose();
            var1.translate(4.0D, 8.0D, 0.0D);
            var1.scale(var6, var6, 1.0F);
            double var8 = (Double)this.minecraft.options.chatOpacity().get() * 0.8999999761581421D + 0.10000000149011612D;
            double var10 = (Double)this.minecraft.options.textBackgroundOpacity().get();
            double var12 = (Double)this.minecraft.options.chatLineSpacing().get();
            int var14 = this.getLineHeight();
            double var15 = -8.0D * (var12 + 1.0D) + 4.0D * var12;
            int var17 = 0;

            int var20;
            int var23;
            int var24;
            int var26;
            int var27;
            for(int var18 = 0; var18 + this.chatScrollbarPos < this.trimmedMessages.size() && var18 < var3; ++var18) {
               GuiMessage.Line var19 = (GuiMessage.Line)this.trimmedMessages.get(var18 + this.chatScrollbarPos);
               if (var19 != null) {
                  var20 = var2 - var19.addedTime();
                  if (var20 < 200 || var5) {
                     double var21 = var5 ? 1.0D : getTimeFactor(var20);
                     var23 = (int)(255.0D * var21 * var8);
                     var24 = (int)(255.0D * var21 * var10);
                     ++var17;
                     if (var23 > 3) {
                        boolean var25 = false;
                        var26 = -var18 * var14;
                        var27 = (int)((double)var26 + var15);
                        var1.pushPose();
                        var1.translate(0.0D, 0.0D, 50.0D);
                        fill(var1, -4, var26 - var14, 0 + var7 + 4 + 4, var26, var24 << 24);
                        GuiMessageTag var28 = var19.tag();
                        if (var28 != null) {
                           int var29 = var28.indicatorColor() | var23 << 24;
                           fill(var1, -4, var26 - var14, -2, var26, var29);
                           if (var5 && var19.endOfEntry() && var28.icon() != null) {
                              int var30 = this.getTagIconLeft(var19);
                              Objects.requireNonNull(this.minecraft.font);
                              int var31 = var27 + 9;
                              this.drawTagIcon(var1, var30, var31, var28.icon());
                           }
                        }

                        RenderSystem.enableBlend();
                        var1.translate(0.0D, 0.0D, 50.0D);
                        this.minecraft.font.drawShadow(var1, var19.content(), 0.0F, (float)var27, 16777215 + (var23 << 24));
                        RenderSystem.disableBlend();
                        var1.popPose();
                     }
                  }
               }
            }

            long var32 = this.minecraft.getChatListener().queueSize();
            int var33;
            if (var32 > 0L) {
               var20 = (int)(128.0D * var8);
               var33 = (int)(255.0D * var10);
               var1.pushPose();
               var1.translate(0.0D, 0.0D, 50.0D);
               fill(var1, -2, 0, var7 + 4, 9, var33 << 24);
               RenderSystem.enableBlend();
               var1.translate(0.0D, 0.0D, 50.0D);
               this.minecraft.font.drawShadow(var1, (Component)Component.translatable("chat.queue", var32), 0.0F, 1.0F, 16777215 + (var20 << 24));
               var1.popPose();
               RenderSystem.disableBlend();
            }

            if (var5) {
               var20 = this.getLineHeight();
               var33 = var4 * var20;
               int var22 = var17 * var20;
               var23 = this.chatScrollbarPos * var22 / var4;
               var24 = var22 * var22 / var33;
               if (var33 != var22) {
                  int var34 = var23 > 0 ? 170 : 96;
                  var26 = this.newMessageSinceScroll ? 13382451 : 3355562;
                  var27 = var7 + 4;
                  fill(var1, var27, -var23, var27 + 2, -var23 - var24, var26 + (var34 << 24));
                  fill(var1, var27 + 2, -var23, var27 + 1, -var23 - var24, 13421772 + (var34 << 24));
               }
            }

            var1.popPose();
         }
      }
   }

   private void drawTagIcon(PoseStack var1, int var2, int var3, GuiMessageTag.Icon var4) {
      int var5 = var3 - var4.height - 1;
      var4.draw(var1, var2, var5);
   }

   private int getTagIconLeft(GuiMessage.Line var1) {
      return this.minecraft.font.width(var1.content()) + 4;
   }

   private boolean isChatHidden() {
      return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
   }

   private static double getTimeFactor(int var0) {
      double var1 = (double)var0 / 200.0D;
      var1 = 1.0D - var1;
      var1 *= 10.0D;
      var1 = Mth.clamp(var1, 0.0D, 1.0D);
      var1 *= var1;
      return var1;
   }

   public void clearMessages(boolean var1) {
      this.minecraft.getChatListener().clearQueue();
      this.trimmedMessages.clear();
      this.allMessages.clear();
      if (var1) {
         this.recentChat.clear();
      }

   }

   public void addMessage(Component var1) {
      this.addMessage(var1, (MessageSignature)null, GuiMessageTag.system());
   }

   public void addMessage(Component var1, @Nullable MessageSignature var2, @Nullable GuiMessageTag var3) {
      this.addMessage(var1, var2, this.minecraft.gui.getGuiTicks(), var3, false);
   }

   private void logChatMessage(Component var1, @Nullable GuiMessageTag var2) {
      String var3 = var1.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
      String var4 = (String)Util.mapNullable(var2, GuiMessageTag::logTag);
      if (var4 != null) {
         LOGGER.info("[{}] [CHAT] {}", var4, var3);
      } else {
         LOGGER.info("[CHAT] {}", var3);
      }

   }

   private void addMessage(Component var1, @Nullable MessageSignature var2, int var3, @Nullable GuiMessageTag var4, boolean var5) {
      this.logChatMessage(var1, var4);
      int var6 = Mth.floor((double)this.getWidth() / this.getScale());
      if (var4 != null && var4.icon() != null) {
         var6 -= var4.icon().width + 4 + 2;
      }

      List var7 = ComponentRenderUtils.wrapComponents(var1, var6, this.minecraft.font);
      boolean var8 = this.isChatFocused();

      for(int var9 = 0; var9 < var7.size(); ++var9) {
         FormattedCharSequence var10 = (FormattedCharSequence)var7.get(var9);
         if (var8 && this.chatScrollbarPos > 0) {
            this.newMessageSinceScroll = true;
            this.scrollChat(1);
         }

         boolean var11 = var9 == var7.size() - 1;
         this.trimmedMessages.add(0, new GuiMessage.Line(var3, var10, var4, var11));
      }

      while(this.trimmedMessages.size() > 100) {
         this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
      }

      if (!var5) {
         this.allMessages.add(0, new GuiMessage(var3, var1, var2, var4));

         while(this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
         }
      }

   }

   public void deleteMessage(MessageSignature var1) {
      Iterator var2 = this.allMessages.iterator();

      while(var2.hasNext()) {
         MessageSignature var3 = ((GuiMessage)var2.next()).headerSignature();
         if (var3 != null && var3.equals(var1)) {
            var2.remove();
            break;
         }
      }

      this.refreshTrimmedMessage();
   }

   public void rescaleChat() {
      this.resetChatScroll();
      this.refreshTrimmedMessage();
   }

   private void refreshTrimmedMessage() {
      this.trimmedMessages.clear();

      for(int var1 = this.allMessages.size() - 1; var1 >= 0; --var1) {
         GuiMessage var2 = (GuiMessage)this.allMessages.get(var1);
         this.addMessage(var2.content(), var2.headerSignature(), var2.addedTime(), var2.tag(), true);
      }

   }

   public List<String> getRecentChat() {
      return this.recentChat;
   }

   public void addRecentChat(String var1) {
      if (this.recentChat.isEmpty() || !((String)this.recentChat.get(this.recentChat.size() - 1)).equals(var1)) {
         this.recentChat.add(var1);
      }

   }

   public void resetChatScroll() {
      this.chatScrollbarPos = 0;
      this.newMessageSinceScroll = false;
   }

   public void scrollChat(int var1) {
      this.chatScrollbarPos += var1;
      int var2 = this.trimmedMessages.size();
      if (this.chatScrollbarPos > var2 - this.getLinesPerPage()) {
         this.chatScrollbarPos = var2 - this.getLinesPerPage();
      }

      if (this.chatScrollbarPos <= 0) {
         this.chatScrollbarPos = 0;
         this.newMessageSinceScroll = false;
      }

   }

   public boolean handleChatQueueClicked(double var1, double var3) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         ChatListener var5 = this.minecraft.getChatListener();
         if (var5.queueSize() == 0L) {
            return false;
         } else {
            double var6 = var1 - 2.0D;
            double var8 = (double)this.minecraft.getWindow().getGuiScaledHeight() - var3 - 40.0D;
            if (var6 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && var8 < 0.0D && var8 > (double)Mth.floor(-9.0D * this.getScale())) {
               var5.acceptNextDelayedMessage();
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double var1, double var3) {
      double var5 = this.screenToChatX(var1);
      if (!(var5 < 0.0D) && !(var5 > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
         double var7 = this.screenToChatY(var3);
         int var9 = this.getMessageIndexAt(var7);
         if (var9 >= 0 && var9 < this.trimmedMessages.size()) {
            GuiMessage.Line var10 = (GuiMessage.Line)this.trimmedMessages.get(var9);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(var10.content(), Mth.floor(var5));
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public GuiMessageTag getMessageTagAt(double var1, double var3) {
      double var5 = this.screenToChatX(var1);
      double var7 = this.screenToChatY(var3);
      int var9 = this.getMessageIndexAt(var7);
      if (var9 >= 0 && var9 < this.trimmedMessages.size()) {
         GuiMessage.Line var10 = (GuiMessage.Line)this.trimmedMessages.get(var9);
         GuiMessageTag var11 = var10.tag();
         if (var11 != null && this.hasSelectedMessageTag(var5, var10, var11)) {
            return var11;
         }
      }

      return null;
   }

   private boolean hasSelectedMessageTag(double var1, GuiMessage.Line var3, GuiMessageTag var4) {
      if (var1 < 0.0D) {
         return true;
      } else {
         GuiMessageTag.Icon var5 = var4.icon();
         if (var5 == null) {
            return false;
         } else {
            int var6 = this.getTagIconLeft(var3);
            int var7 = var6 + var5.width;
            return var1 >= (double)var6 && var1 <= (double)var7;
         }
      }
   }

   private double screenToChatX(double var1) {
      return (var1 - 4.0D) / this.getScale();
   }

   private double screenToChatY(double var1) {
      double var3 = (double)this.minecraft.getWindow().getGuiScaledHeight() - var1 - 40.0D;
      return var3 / (this.getScale() * ((Double)this.minecraft.options.chatLineSpacing().get() + 1.0D));
   }

   private int getMessageIndexAt(double var1) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         int var3 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
         if (var1 >= 0.0D) {
            Objects.requireNonNull(this.minecraft.font);
            if (var1 < (double)(9 * var3 + var3)) {
               Objects.requireNonNull(this.minecraft.font);
               int var4 = Mth.floor(var1 / 9.0D + (double)this.chatScrollbarPos);
               if (var4 >= 0 && var4 < this.trimmedMessages.size()) {
                  return var4;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   @Nullable
   public ChatScreen getFocusedChat() {
      Screen var2 = this.minecraft.screen;
      if (var2 instanceof ChatScreen) {
         ChatScreen var1 = (ChatScreen)var2;
         return var1;
      } else {
         return null;
      }
   }

   private boolean isChatFocused() {
      return this.getFocusedChat() != null;
   }

   public int getWidth() {
      return getWidth((Double)this.minecraft.options.chatWidth().get());
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? (Double)this.minecraft.options.chatHeightFocused().get() : (Double)this.minecraft.options.chatHeightUnfocused().get());
   }

   public double getScale() {
      return (Double)this.minecraft.options.chatScale().get();
   }

   public static int getWidth(double var0) {
      boolean var2 = true;
      boolean var3 = true;
      return Mth.floor(var0 * 280.0D + 40.0D);
   }

   public static int getHeight(double var0) {
      boolean var2 = true;
      boolean var3 = true;
      return Mth.floor(var0 * 160.0D + 20.0D);
   }

   public static double defaultUnfocusedPct() {
      boolean var0 = true;
      boolean var1 = true;
      return 70.0D / (double)(getHeight(1.0D) - 20);
   }

   public int getLinesPerPage() {
      return this.getHeight() / this.getLineHeight();
   }

   private int getLineHeight() {
      Objects.requireNonNull(this.minecraft.font);
      return (int)(9.0D * ((Double)this.minecraft.options.chatLineSpacing().get() + 1.0D));
   }
}
