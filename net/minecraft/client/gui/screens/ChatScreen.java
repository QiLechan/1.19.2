package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.chat.ChatPreviewAnimator;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PreviewableCommand;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

public class ChatScreen extends Screen {
   private static final int CHAT_SIGNING_PENDING_INDICATOR_COLOR = 15118153;
   private static final int CHAT_SIGNING_READY_INDICATOR_COLOR = 7844841;
   private static final int PREVIEW_HIGHLIGHT_COLOR = 10533887;
   public static final double MOUSE_SCROLL_SPEED = 7.0D;
   private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
   private static final int PREVIEW_MARGIN_SIDES = 2;
   private static final int PREVIEW_PADDING = 2;
   private static final int PREVIEW_MARGIN_BOTTOM = 15;
   private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
   private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
   private static final Component PREVIEW_INPUT_HINT;
   private static final int TOOLTIP_MAX_WIDTH = 260;
   private String historyBuffer = "";
   private int historyPos = -1;
   protected EditBox input;
   private String initial;
   CommandSuggestions commandSuggestions;
   private ClientChatPreview chatPreview;
   private ChatPreviewStatus chatPreviewStatus;
   private boolean previewNotRequired;
   private final ChatPreviewAnimator chatPreviewAnimator = new ChatPreviewAnimator();

   public ChatScreen(String var1) {
      super(Component.translatable("chat_screen.title"));
      this.initial = var1;
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
      this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.input.setMaxLength(256);
      this.input.setBordered(false);
      this.input.setValue(this.initial);
      this.input.setResponder(this::onEdited);
      this.addWidget(this.input);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestions.updateCommandInfo();
      this.setInitialFocus(this.input);
      this.chatPreviewAnimator.reset(Util.getMillis());
      this.chatPreview = new ClientChatPreview(this.minecraft);
      this.updateChatPreview(this.input.getValue());
      ServerData var1 = this.minecraft.getCurrentServer();
      this.chatPreviewStatus = var1 != null && !var1.previewsChat() ? ChatPreviewStatus.OFF : (ChatPreviewStatus)this.minecraft.options.chatPreview().get();
      if (var1 != null && this.chatPreviewStatus != ChatPreviewStatus.OFF) {
         ServerData.ChatPreview var2 = var1.getChatPreview();
         if (var2 != null && var1.previewsChat() && var2.showToast()) {
            ServerList.saveSingleServer(var1);
            SystemToast var3 = SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST);
            this.minecraft.getToasts().addToast(var3);
         }
      }

      if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM) {
         this.previewNotRequired = this.initial.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(this.initial.substring(1));
      }

   }

   public void resize(Minecraft var1, int var2, int var3) {
      String var4 = this.input.getValue();
      this.init(var1, var2, var3);
      this.setChatLine(var4);
      this.commandSuggestions.updateCommandInfo();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
      this.minecraft.gui.getChat().resetChatScroll();
   }

   public void tick() {
      this.input.tick();
      this.chatPreview.tick();
   }

   private void onEdited(String var1) {
      String var2 = this.input.getValue();
      this.commandSuggestions.setAllowSuggestions(!var2.equals(this.initial));
      this.commandSuggestions.updateCommandInfo();
      if (this.chatPreviewStatus == ChatPreviewStatus.LIVE) {
         this.updateChatPreview(var2);
      } else if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.chatPreview.queryEquals(var2)) {
         this.previewNotRequired = var2.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(var2.substring(1));
         this.chatPreview.update("");
      }

   }

   private void updateChatPreview(String var1) {
      String var2 = this.normalizeChatMessage(var1);
      if (this.sendsChatPreviewRequests()) {
         this.requestPreview(var2);
      } else {
         this.chatPreview.disable();
      }

   }

   private void requestPreview(String var1) {
      if (var1.startsWith("/")) {
         this.requestCommandArgumentPreview(var1);
      } else {
         this.requestChatMessagePreview(var1);
      }

   }

   private void requestChatMessagePreview(String var1) {
      this.chatPreview.update(var1);
   }

   private void requestCommandArgumentPreview(String var1) {
      ParseResults var2 = this.commandSuggestions.getCurrentContext();
      CommandNode var3 = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
      if (var2 != null && var3 != null && PreviewableCommand.of(var2).isPreviewed(var3)) {
         this.chatPreview.update(var1);
      } else {
         this.chatPreview.disable();
      }

   }

   private boolean sendsChatPreviewRequests() {
      if (this.minecraft.player == null) {
         return false;
      } else if (this.minecraft.isLocalServer()) {
         return true;
      } else if (this.chatPreviewStatus == ChatPreviewStatus.OFF) {
         return false;
      } else {
         ServerData var1 = this.minecraft.getCurrentServer();
         return var1 != null && var1.previewsChat();
      }
   }

   public boolean keyPressed(int var1, int var2, int var3) {
      if (this.commandSuggestions.keyPressed(var1, var2, var3)) {
         return true;
      } else if (super.keyPressed(var1, var2, var3)) {
         return true;
      } else if (var1 == 256) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else if (var1 != 257 && var1 != 335) {
         if (var1 == 265) {
            this.moveInHistory(-1);
            return true;
         } else if (var1 == 264) {
            this.moveInHistory(1);
            return true;
         } else if (var1 == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
         } else if (var1 == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5) {
      var5 = Mth.clamp(var5, -1.0D, 1.0D);
      if (this.commandSuggestions.mouseScrolled(var5)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            var5 *= 7.0D;
         }

         this.minecraft.gui.getChat().scrollChat((int)var5);
         return true;
      }
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.commandSuggestions.mouseClicked((double)((int)var1), (double)((int)var3), var5)) {
         return true;
      } else {
         if (var5 == 0) {
            ChatComponent var6 = this.minecraft.gui.getChat();
            if (var6.handleChatQueueClicked(var1, var3)) {
               return true;
            }

            Style var7 = this.getComponentStyleAt(var1, var3);
            if (var7 != null && this.handleComponentClicked(var7)) {
               this.initial = this.input.getValue();
               return true;
            }
         }

         return this.input.mouseClicked(var1, var3, var5) ? true : super.mouseClicked(var1, var3, var5);
      }
   }

   protected void insertText(String var1, boolean var2) {
      if (var2) {
         this.input.setValue(var1);
      } else {
         this.input.insertText(var1);
      }

   }

   public void moveInHistory(int var1) {
      int var2 = this.historyPos + var1;
      int var3 = this.minecraft.gui.getChat().getRecentChat().size();
      var2 = Mth.clamp((int)var2, (int)0, (int)var3);
      if (var2 != this.historyPos) {
         if (var2 == var3) {
            this.historyPos = var3;
            this.input.setValue(this.historyBuffer);
         } else {
            if (this.historyPos == var3) {
               this.historyBuffer = this.input.getValue();
            }

            this.input.setValue((String)this.minecraft.gui.getChat().getRecentChat().get(var2));
            this.commandSuggestions.setAllowSuggestions(false);
            this.historyPos = var2;
         }
      }
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.setFocused(this.input);
      this.input.setFocus(true);
      fill(var1, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(-2147483648));
      this.input.render(var1, var2, var3, var4);
      super.render(var1, var2, var3, var4);
      boolean var5 = this.minecraft.getProfileKeyPairManager().signer() != null;
      ChatPreviewAnimator.State var6 = this.chatPreviewAnimator.get(Util.getMillis(), this.getDisplayedPreviewText());
      if (var6.preview() != null) {
         this.renderChatPreview(var1, var6.preview(), var6.alpha(), var5);
         this.commandSuggestions.renderSuggestions(var1, var2, var3);
      } else {
         this.commandSuggestions.render(var1, var2, var3);
         if (var5) {
            var1.pushPose();
            fill(var1, 0, this.height - 14, 2, this.height - 2, -8932375);
            var1.popPose();
         }
      }

      Style var7 = this.getComponentStyleAt((double)var2, (double)var3);
      if (var7 != null && var7.getHoverEvent() != null) {
         this.renderComponentHoverEffect(var1, var7, var2, var3);
      } else {
         GuiMessageTag var8 = this.minecraft.gui.getChat().getMessageTagAt((double)var2, (double)var3);
         if (var8 != null && var8.text() != null) {
            this.renderTooltip(var1, this.font.split(var8.text(), 260), var2, var3);
         }
      }

   }

   @Nullable
   protected Component getDisplayedPreviewText() {
      String var1 = this.input.getValue();
      if (var1.isBlank()) {
         return null;
      } else {
         Component var2 = this.peekPreview();
         return this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired ? (Component)Objects.requireNonNullElse(var2, this.chatPreview.queryEquals(var1) && !var1.startsWith("/") ? Component.literal(var1) : PREVIEW_INPUT_HINT) : var2;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setChatLine(String var1) {
      this.input.setValue(var1);
   }

   protected void updateNarrationState(NarrationElementOutput var1) {
      var1.add(NarratedElementType.TITLE, this.getTitle());
      var1.add(NarratedElementType.USAGE, USAGE_TEXT);
      String var2 = this.input.getValue();
      if (!var2.isEmpty()) {
         var1.nest().add(NarratedElementType.TITLE, (Component)Component.translatable("chat_screen.message", var2));
      }

   }

   public void renderChatPreview(PoseStack var1, Component var2, float var3, boolean var4) {
      int var5 = (int)(255.0D * ((Double)this.minecraft.options.chatOpacity().get() * 0.8999999761581421D + 0.10000000149011612D) * (double)var3);
      int var6 = (int)((double)(this.chatPreview.hasScheduledRequest() ? 127 : 255) * (Double)this.minecraft.options.textBackgroundOpacity().get() * (double)var3);
      int var7 = this.chatPreviewWidth();
      List var8 = this.splitChatPreview(var2);
      int var9 = this.chatPreviewHeight(var8);
      int var10 = this.chatPreviewTop(var9);
      RenderSystem.enableBlend();
      var1.pushPose();
      var1.translate((double)this.chatPreviewLeft(), (double)var10, 0.0D);
      fill(var1, 0, 0, var7, var9, var6 << 24);
      int var11;
      if (var5 > 0) {
         var1.translate(2.0D, 2.0D, 0.0D);

         for(var11 = 0; var11 < var8.size(); ++var11) {
            FormattedCharSequence var12 = (FormattedCharSequence)var8.get(var11);
            Objects.requireNonNull(this.font);
            int var13 = var11 * 9;
            this.renderChatPreviewHighlights(var1, var12, var13, var5);
            this.font.drawShadow(var1, var12, 0.0F, (float)var13, var5 << 24 | 16777215);
         }
      }

      var1.popPose();
      RenderSystem.disableBlend();
      if (var4 && this.chatPreview.peek() != null) {
         var11 = this.chatPreview.hasScheduledRequest() ? 15118153 : 7844841;
         int var14 = (int)(255.0F * var3);
         var1.pushPose();
         fill(var1, 0, var10, 2, this.chatPreviewBottom(), var14 << 24 | var11);
         var1.popPose();
      }

   }

   private void renderChatPreviewHighlights(PoseStack var1, FormattedCharSequence var2, int var3, int var4) {
      Objects.requireNonNull(this.font);
      int var5 = var3 + 9;
      int var6 = var4 << 24 | 10533887;
      Predicate var7 = (var0) -> {
         return var0.getHoverEvent() != null || var0.getClickEvent() != null;
      };
      Iterator var8 = this.font.getSplitter().findSpans(var2, var7).iterator();

      while(var8.hasNext()) {
         StringSplitter.Span var9 = (StringSplitter.Span)var8.next();
         int var10 = Mth.floor(var9.left());
         int var11 = Mth.ceil(var9.right());
         fill(var1, var10, var3, var11, var5, var6);
      }

   }

   @Nullable
   private Style getComponentStyleAt(double var1, double var3) {
      Style var5 = this.minecraft.gui.getChat().getClickedComponentStyleAt(var1, var3);
      if (var5 == null) {
         var5 = this.getChatPreviewStyleAt(var1, var3);
      }

      return var5;
   }

   @Nullable
   private Style getChatPreviewStyleAt(double var1, double var3) {
      if (this.minecraft.options.hideGui) {
         return null;
      } else {
         Component var5 = this.peekPreview();
         if (var5 == null) {
            return null;
         } else {
            List var6 = this.splitChatPreview(var5);
            int var7 = this.chatPreviewHeight(var6);
            if (!(var1 < (double)this.chatPreviewLeft()) && !(var1 > (double)this.chatPreviewRight()) && !(var3 < (double)this.chatPreviewTop(var7)) && !(var3 > (double)this.chatPreviewBottom())) {
               int var8 = this.chatPreviewLeft() + 2;
               int var9 = this.chatPreviewTop(var7) + 2;
               int var10000 = Mth.floor(var3) - var9;
               Objects.requireNonNull(this.font);
               int var10 = var10000 / 9;
               if (var10 >= 0 && var10 < var6.size()) {
                  FormattedCharSequence var11 = (FormattedCharSequence)var6.get(var10);
                  return this.minecraft.font.getSplitter().componentStyleAtWidth(var11, (int)(var1 - (double)var8));
               } else {
                  return null;
               }
            } else {
               return null;
            }
         }
      }
   }

   @Nullable
   private Component peekPreview() {
      return (Component)Util.mapNullable(this.chatPreview.peek(), ClientChatPreview.Preview::response);
   }

   private List<FormattedCharSequence> splitChatPreview(Component var1) {
      return this.font.split(var1, this.chatPreviewWidth());
   }

   private int chatPreviewWidth() {
      return this.minecraft.screen.width - 4;
   }

   private int chatPreviewHeight(List<FormattedCharSequence> var1) {
      int var10000 = Math.max(var1.size(), 1);
      Objects.requireNonNull(this.font);
      return var10000 * 9 + 4;
   }

   private int chatPreviewBottom() {
      return this.minecraft.screen.height - 15;
   }

   private int chatPreviewTop(int var1) {
      return this.chatPreviewBottom() - var1;
   }

   private int chatPreviewLeft() {
      return 2;
   }

   private int chatPreviewRight() {
      return this.minecraft.screen.width - 2;
   }

   public boolean handleChatInput(String var1, boolean var2) {
      var1 = this.normalizeChatMessage(var1);
      if (var1.isEmpty()) {
         return true;
      } else {
         if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired) {
            this.commandSuggestions.hide();
            if (!this.chatPreview.queryEquals(var1)) {
               this.updateChatPreview(var1);
               return false;
            }
         }

         if (var2) {
            this.minecraft.gui.getChat().addRecentChat(var1);
         }

         Component var3 = (Component)Util.mapNullable(this.chatPreview.pull(var1), ClientChatPreview.Preview::response);
         if (var1.startsWith("/")) {
            this.minecraft.player.commandSigned(var1.substring(1), var3);
         } else {
            this.minecraft.player.chatSigned(var1, var3);
         }

         return true;
      }
   }

   public String normalizeChatMessage(String var1) {
      return StringUtils.normalizeSpace(var1.trim());
   }

   public ClientChatPreview getChatPreview() {
      return this.chatPreview;
   }

   static {
      PREVIEW_INPUT_HINT = Component.translatable("chat.previewInput", Component.translatable("key.keyboard.enter")).withStyle(ChatFormatting.DARK_GRAY);
   }
}
