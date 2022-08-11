package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

public class TitleScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEMO_LEVEL_ID = "Demo_World";
   public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
   public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
   private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
   private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
   private final boolean minceraftEasterEgg;
   @Nullable
   private String splash;
   private Button resetDemoButton;
   private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
   @Nullable
   private RealmsNotificationsScreen realmsNotificationsScreen;
   private final PanoramaRenderer panorama;
   private final boolean fading;
   private long fadeInStart;
   @Nullable
   private TitleScreen.WarningLabel warningLabel;

   public TitleScreen() {
      this(false);
   }

   public TitleScreen(boolean var1) {
      super(Component.translatable("narrator.screen.title"));
      this.panorama = new PanoramaRenderer(CUBE_MAP);
      this.fading = var1;
      this.minceraftEasterEgg = (double)RandomSource.create().nextFloat() < 1.0E-4D;
   }

   private boolean realmsNotificationsEnabled() {
      return (Boolean)this.minecraft.options.realmsNotifications().get() && this.realmsNotificationsScreen != null;
   }

   public void tick() {
      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.tick();
      }

      this.minecraft.getRealms32BitWarningStatus().showRealms32BitWarningIfNeeded(this);
   }

   public static CompletableFuture<Void> preloadResources(TextureManager var0, Executor var1) {
      return CompletableFuture.allOf(var0.preload(MINECRAFT_LOGO, var1), var0.preload(MINECRAFT_EDITION, var1), var0.preload(PANORAMA_OVERLAY, var1), CUBE_MAP.preload(var0, var1));
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      if (this.splash == null) {
         this.splash = this.minecraft.getSplashManager().getSplash();
      }

      int var1 = this.font.width((FormattedText)COPYRIGHT_TEXT);
      int var2 = this.width - var1 - 2;
      boolean var3 = true;
      int var4 = this.height / 4 + 48;
      if (this.minecraft.isDemo()) {
         this.createDemoMenuOptions(var4, 24);
      } else {
         this.createNormalMenuOptions(var4, 24);
      }

      this.addRenderableWidget(new ImageButton(this.width / 2 - 124, var4 + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (var1x) -> {
         this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
      }, Component.translatable("narrator.button.language")));
      this.addRenderableWidget(new Button(this.width / 2 - 100, var4 + 72 + 12, 98, 20, Component.translatable("menu.options"), (var1x) -> {
         this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 2, var4 + 72 + 12, 98, 20, Component.translatable("menu.quit"), (var1x) -> {
         this.minecraft.stop();
      }));
      this.addRenderableWidget(new ImageButton(this.width / 2 + 104, var4 + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64, (var1x) -> {
         this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options));
      }, Component.translatable("narrator.button.accessibility")));
      this.addRenderableWidget(new PlainTextButton(var2, this.height - 10, var1, 10, COPYRIGHT_TEXT, (var1x) -> {
         this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
      }, this.font));
      this.minecraft.setConnectedToRealms(false);
      if ((Boolean)this.minecraft.options.realmsNotifications().get() && this.realmsNotificationsScreen == null) {
         this.realmsNotificationsScreen = new RealmsNotificationsScreen();
      }

      if (this.realmsNotificationsEnabled()) {
         this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
      }

      if (!this.minecraft.is64Bit()) {
         this.warningLabel = new TitleScreen.WarningLabel(this.font, MultiLineLabel.create(this.font, Component.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, var4 - 24);
      }

   }

   private void createNormalMenuOptions(int var1, int var2) {
      this.addRenderableWidget(new Button(this.width / 2 - 100, var1, 200, 20, Component.translatable("menu.singleplayer"), (var1x) -> {
         this.minecraft.setScreen(new SelectWorldScreen(this));
      }));
      final Component var3 = this.getMultiplayerDisabledReason();
      boolean var4 = var3 == null;
      Button.OnTooltip var5 = var3 == null ? Button.NO_TOOLTIP : new Button.OnTooltip() {
         public void onTooltip(Button var1, PoseStack var2, int var3x, int var4) {
            TitleScreen.this.renderTooltip(var2, TitleScreen.this.minecraft.font.split(var3, Math.max(TitleScreen.this.width / 2 - 43, 170)), var3x, var4);
         }

         public void narrateTooltip(Consumer<Component> var1) {
            var1.accept(var3);
         }
      };
      ((Button)this.addRenderableWidget(new Button(this.width / 2 - 100, var1 + var2 * 1, 200, 20, Component.translatable("menu.multiplayer"), (var1x) -> {
         Object var2 = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
         this.minecraft.setScreen((Screen)var2);
      }, var5))).active = var4;
      ((Button)this.addRenderableWidget(new Button(this.width / 2 - 100, var1 + var2 * 2, 200, 20, Component.translatable("menu.online"), (var1x) -> {
         this.realmsButtonClicked();
      }, var5))).active = var4;
   }

   @Nullable
   private Component getMultiplayerDisabledReason() {
      if (this.minecraft.allowsMultiplayer()) {
         return null;
      } else {
         BanDetails var1 = this.minecraft.multiplayerBan();
         if (var1 != null) {
            return var1.expires() != null ? Component.translatable("title.multiplayer.disabled.banned.temporary") : Component.translatable("title.multiplayer.disabled.banned.permanent");
         } else {
            return Component.translatable("title.multiplayer.disabled");
         }
      }
   }

   private void createDemoMenuOptions(int var1, int var2) {
      boolean var3 = this.checkDemoWorldPresence();
      this.addRenderableWidget(new Button(this.width / 2 - 100, var1, 200, 20, Component.translatable("menu.playdemo"), (var2x) -> {
         if (var3) {
            this.minecraft.createWorldOpenFlows().loadLevel(this, "Demo_World");
         } else {
            RegistryAccess.Frozen var3x = RegistryAccess.builtinCopy().freeze();
            this.minecraft.createWorldOpenFlows().createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, var3x, WorldPresets.demoSettings(var3x));
         }

      }));
      this.resetDemoButton = (Button)this.addRenderableWidget(new Button(this.width / 2 - 100, var1 + var2 * 1, 200, 20, Component.translatable("menu.resetdemo"), (var1x) -> {
         LevelStorageSource var2 = this.minecraft.getLevelSource();

         try {
            LevelStorageSource.LevelStorageAccess var3 = var2.createAccess("Demo_World");

            try {
               LevelSummary var4 = var3.getSummary();
               if (var4 != null) {
                  this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", var4.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
               }
            } catch (Throwable var7) {
               if (var3 != null) {
                  try {
                     var3.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (var3 != null) {
               var3.close();
            }
         } catch (IOException var8) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to access demo world", var8);
         }

      }));
      this.resetDemoButton.active = var3;
   }

   private boolean checkDemoWorldPresence() {
      try {
         LevelStorageSource.LevelStorageAccess var1 = this.minecraft.getLevelSource().createAccess("Demo_World");

         boolean var2;
         try {
            var2 = var1.getSummary() != null;
         } catch (Throwable var5) {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (var1 != null) {
            var1.close();
         }

         return var2;
      } catch (IOException var6) {
         SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
         LOGGER.warn("Failed to read demo world data", var6);
         return false;
      }
   }

   private void realmsButtonClicked() {
      this.minecraft.setScreen(new RealmsMainScreen(this));
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      if (this.fadeInStart == 0L && this.fading) {
         this.fadeInStart = Util.getMillis();
      }

      float var5 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
      this.panorama.render(var4, Mth.clamp(var5, 0.0F, 1.0F));
      boolean var6 = true;
      int var7 = this.width / 2 - 137;
      boolean var8 = true;
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(var5, 0.0F, 1.0F)) : 1.0F);
      blit(var1, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
      float var9 = this.fading ? Mth.clamp(var5 - 1.0F, 0.0F, 1.0F) : 1.0F;
      int var10 = Mth.ceil(var9 * 255.0F) << 24;
      if ((var10 & -67108864) != 0) {
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, var9);
         if (this.minceraftEasterEgg) {
            this.blitOutlineBlack(var7, 30, (var2x, var3x) -> {
               this.blit(var1, var2x + 0, var3x, 0, 0, 99, 44);
               this.blit(var1, var2x + 99, var3x, 129, 0, 27, 44);
               this.blit(var1, var2x + 99 + 26, var3x, 126, 0, 3, 44);
               this.blit(var1, var2x + 99 + 26 + 3, var3x, 99, 0, 26, 44);
               this.blit(var1, var2x + 155, var3x, 0, 45, 155, 44);
            });
         } else {
            this.blitOutlineBlack(var7, 30, (var2x, var3x) -> {
               this.blit(var1, var2x + 0, var3x, 0, 0, 155, 44);
               this.blit(var1, var2x + 155, var3x, 0, 45, 155, 44);
            });
         }

         RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
         blit(var1, var7 + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
         if (this.warningLabel != null) {
            this.warningLabel.render(var1, var10);
         }

         if (this.splash != null) {
            var1.pushPose();
            var1.translate((double)(this.width / 2 + 90), 70.0D, 0.0D);
            var1.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
            float var11 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
            var11 = var11 * 100.0F / (float)(this.font.width(this.splash) + 32);
            var1.scale(var11, var11, var11);
            drawCenteredString(var1, this.font, this.splash, 0, -8, 16776960 | var10);
            var1.popPose();
         }

         String var14 = "Minecraft " + SharedConstants.getCurrentVersion().getName();
         if (this.minecraft.isDemo()) {
            var14 = var14 + " Demo";
         } else {
            var14 = var14 + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
         }

         if (Minecraft.checkModStatus().shouldReportAsModified()) {
            var14 = var14 + I18n.get("menu.modded");
         }

         drawString(var1, this.font, var14, 2, this.height - 10, 16777215 | var10);
         Iterator var12 = this.children().iterator();

         while(var12.hasNext()) {
            GuiEventListener var13 = (GuiEventListener)var12.next();
            if (var13 instanceof AbstractWidget) {
               ((AbstractWidget)var13).setAlpha(var9);
            }
         }

         super.render(var1, var2, var3, var4);
         if (this.realmsNotificationsEnabled() && var9 >= 1.0F) {
            RenderSystem.enableDepthTest();
            this.realmsNotificationsScreen.render(var1, var2, var3, var4);
         }

      }
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (super.mouseClicked(var1, var3, var5)) {
         return true;
      } else {
         return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(var1, var3, var5);
      }
   }

   public void removed() {
      if (this.realmsNotificationsScreen != null) {
         this.realmsNotificationsScreen.removed();
      }

   }

   private void confirmDemo(boolean var1) {
      if (var1) {
         try {
            LevelStorageSource.LevelStorageAccess var2 = this.minecraft.getLevelSource().createAccess("Demo_World");

            try {
               var2.deleteLevel();
            } catch (Throwable var6) {
               if (var2 != null) {
                  try {
                     var2.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (var2 != null) {
               var2.close();
            }
         } catch (IOException var7) {
            SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to delete demo world", var7);
         }
      }

      this.minecraft.setScreen(this);
   }

   private static record WarningLabel(Font a, MultiLineLabel b, int c, int d) {
      private final Font font;
      private final MultiLineLabel label;
      private final int x;
      private final int y;

      WarningLabel(Font var1, MultiLineLabel var2, int var3, int var4) {
         super();
         this.font = var1;
         this.label = var2;
         this.x = var3;
         this.y = var4;
      }

      public void render(PoseStack var1, int var2) {
         MultiLineLabel var10000 = this.label;
         int var10002 = this.x;
         int var10003 = this.y;
         Objects.requireNonNull(this.font);
         var10000.renderBackgroundCentered(var1, var10002, var10003, 9, 2, 1428160512);
         var10000 = this.label;
         var10002 = this.x;
         var10003 = this.y;
         Objects.requireNonNull(this.font);
         var10000.renderCentered(var1, var10002, var10003, 9, 16777215 | var2);
      }

      public Font font() {
         return this.font;
      }

      public MultiLineLabel label() {
         return this.label;
      }

      public int x() {
         return this.x;
      }

      public int y() {
         return this.y;
      }
   }
}
