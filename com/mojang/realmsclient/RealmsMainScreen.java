package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsNewsManager;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class RealmsMainScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
   private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
   private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
   static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
   private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
   private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
   private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
   private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
   private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
   static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
   private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
   static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
   static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
   static final Component PENDING_INVITES_TEXT = Component.translatable("mco.invites.pending");
   static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2"));
   static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
   static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
   static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
   static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
   static final Component SUBSCRIPTION_CREATE_TEXT = Component.translatable("mco.selectServer.expiredSubscribe");
   static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(" ");
   private static final Component POPUP_TEXT = Component.translatable("mco.selectServer.popup");
   private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
   private static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
   private static final Component LEAVE_SERVER_TOOLTIP = Component.translatable("mco.selectServer.leave");
   private static final Component CONFIGURE_SERVER_TOOLTIP = Component.translatable("mco.selectServer.configure");
   private static final Component NEWS_TOOLTIP = Component.translatable("mco.news");
   static final Component UNITIALIZED_WORLD_NARRATION;
   static final Component TRIAL_TEXT;
   private static List<ResourceLocation> teaserImages;
   @Nullable
   private DataFetcher.Subscription dataSubscription;
   private RealmsServerList serverList;
   static boolean overrideConfigure;
   private static int lastScrollYPosition;
   static volatile boolean hasParentalConsent;
   static volatile boolean checkedParentalConsent;
   static volatile boolean checkedClientCompatability;
   @Nullable
   static Screen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private final RateLimiter inviteNarrationLimiter;
   private boolean dontSetConnectedToRealms;
   final Screen lastScreen;
   RealmsMainScreen.RealmSelectionList realmSelectionList;
   private boolean realmsSelectionListAdded;
   private Button playButton;
   private Button backButton;
   private Button renewButton;
   private Button configureButton;
   private Button leaveButton;
   @Nullable
   private List<Component> toolTip;
   private List<RealmsServer> realmsServers = ImmutableList.of();
   volatile int numberOfPendingInvites;
   int animTick;
   private boolean hasFetchedServers;
   boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   volatile boolean hasUnreadNews;
   @Nullable
   volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private List<KeyCombo> keyCombos;
   long lastClickTime;
   private ReentrantLock connectLock = new ReentrantLock();
   private MultiLineLabel formattedPopup;
   RealmsMainScreen.HoveredElement hoveredElement;
   private Button showPopupButton;
   private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
   private Button newsButton;
   private Button createTrialButton;
   private Button buyARealmButton;
   private Button closeButton;

   public RealmsMainScreen(Screen var1) {
      super(GameNarrator.NO_TITLE);
      this.formattedPopup = MultiLineLabel.EMPTY;
      this.lastScreen = var1;
      this.inviteNarrationLimiter = RateLimiter.create(0.01666666753590107D);
   }

   private boolean shouldShowMessageInList() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialsAvailable && !this.createdTrial) {
            return true;
         } else {
            Iterator var1 = this.realmsServers.iterator();

            RealmsServer var2;
            do {
               if (!var1.hasNext()) {
                  return true;
               }

               var2 = (RealmsServer)var1.next();
            } while(!var2.ownerUUID.equals(this.minecraft.getUser().getUuid()));

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         return this.popupOpenedByUser ? true : this.realmsServers.isEmpty();
      } else {
         return false;
      }
   }

   public void init() {
      this.keyCombos = Lists.newArrayList(new KeyCombo[]{new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         overrideConfigure = !overrideConfigure;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.switchToProd();
         } else {
            this.switchToStage();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.switchToProd();
         } else {
            this.switchToLocal();
         }

      })});
      if (realmsGenericErrorScreen != null) {
         this.minecraft.setScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatability && !hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatability();
         if (!this.dontSetConnectedToRealms) {
            this.minecraft.setConnectedToRealms(false);
         }

         this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
         this.showingPopup = false;
         this.addButtons();
         this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
         if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
         }

         this.addWidget(this.realmSelectionList);
         this.realmsSelectionListAdded = true;
         this.magicalSpecialHackyFocus(this.realmSelectionList);
         this.formattedPopup = MultiLineLabel.create(this.font, POPUP_TEXT, 100);
         RealmsNewsManager var1 = this.minecraft.realmsDataFetcher().newsManager;
         this.hasUnreadNews = var1.hasUnreadNews();
         this.newsLink = var1.newsLink();
         if (this.serverList == null) {
            this.serverList = new RealmsServerList(this.minecraft);
         }

         if (this.dataSubscription != null) {
            this.dataSubscription.forceUpdate();
         }

      }
   }

   private static boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addButtons() {
      this.leaveButton = (Button)this.addRenderableWidget(new Button(this.width / 2 - 202, this.height - 32, 90, 20, Component.translatable("mco.selectServer.leave"), (var1) -> {
         this.leaveClicked(this.getSelectedServer());
      }));
      this.configureButton = (Button)this.addRenderableWidget(new Button(this.width / 2 - 190, this.height - 32, 90, 20, Component.translatable("mco.selectServer.configure"), (var1) -> {
         this.configureClicked(this.getSelectedServer());
      }));
      this.playButton = (Button)this.addRenderableWidget(new Button(this.width / 2 - 93, this.height - 32, 90, 20, Component.translatable("mco.selectServer.play"), (var1) -> {
         this.play(this.getSelectedServer(), this);
      }));
      this.backButton = (Button)this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 32, 90, 20, CommonComponents.GUI_BACK, (var1) -> {
         if (!this.justClosedPopup) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }));
      this.renewButton = (Button)this.addRenderableWidget(new Button(this.width / 2 + 100, this.height - 32, 90, 20, Component.translatable("mco.selectServer.expiredRenew"), (var1) -> {
         this.onRenew(this.getSelectedServer());
      }));
      this.newsButton = (Button)this.addRenderableWidget(new RealmsMainScreen.NewsButton());
      this.showPopupButton = (Button)this.addRenderableWidget(new Button(this.width - 90, 6, 80, 20, Component.translatable("mco.selectServer.purchase"), (var1) -> {
         this.popupOpenedByUser = !this.popupOpenedByUser;
      }));
      this.pendingInvitesButton = (RealmsMainScreen.PendingInvitesButton)this.addRenderableWidget(new RealmsMainScreen.PendingInvitesButton());
      this.closeButton = (Button)this.addRenderableWidget(new RealmsMainScreen.CloseButton());
      this.createTrialButton = (Button)this.addRenderableWidget(new Button(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, Component.translatable("mco.selectServer.trial"), (var1) -> {
         if (this.trialsAvailable && !this.createdTrial) {
            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
            this.minecraft.setScreen(this.lastScreen);
         }
      }));
      this.buyARealmButton = (Button)this.addRenderableWidget(new Button(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20, Component.translatable("mco.selectServer.buy"), (var0) -> {
         Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms");
      }));
      this.updateButtonStates((RealmsServer)null);
   }

   void updateButtonStates(@Nullable RealmsServer var1) {
      this.backButton.active = true;
      if (hasParentalConsent() && this.hasFetchedServers) {
         this.playButton.visible = true;
         this.playButton.active = this.shouldPlayButtonBeActive(var1) && !this.shouldShowPopup();
         this.renewButton.visible = this.shouldRenewButtonBeActive(var1);
         this.configureButton.visible = this.shouldConfigureButtonBeVisible(var1);
         this.leaveButton.visible = this.shouldLeaveButtonBeVisible(var1);
         boolean var2 = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
         this.createTrialButton.visible = var2;
         this.createTrialButton.active = var2;
         this.buyARealmButton.visible = this.shouldShowPopup();
         this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
         this.renewButton.active = !this.shouldShowPopup();
         this.configureButton.active = !this.shouldShowPopup();
         this.leaveButton.active = !this.shouldShowPopup();
         this.newsButton.active = true;
         this.newsButton.visible = this.newsLink != null;
         this.pendingInvitesButton.active = true;
         this.pendingInvitesButton.visible = true;
         this.showPopupButton.active = !this.shouldShowPopup();
      } else {
         hideWidgets(new AbstractWidget[]{this.playButton, this.renewButton, this.configureButton, this.createTrialButton, this.buyARealmButton, this.closeButton, this.newsButton, this.pendingInvitesButton, this.showPopupButton, this.leaveButton});
      }
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
   }

   boolean shouldPlayButtonBeActive(@Nullable RealmsServer var1) {
      return var1 != null && !var1.expired && var1.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer var1) {
      return var1 != null && var1.expired && this.isSelfOwnedServer(var1);
   }

   private boolean shouldConfigureButtonBeVisible(@Nullable RealmsServer var1) {
      return var1 != null && this.isSelfOwnedServer(var1);
   }

   private boolean shouldLeaveButtonBeVisible(@Nullable RealmsServer var1) {
      return var1 != null && !this.isSelfOwnedServer(var1);
   }

   public void tick() {
      super.tick();
      if (this.pendingInvitesButton != null) {
         this.pendingInvitesButton.tick();
      }

      this.justClosedPopup = false;
      ++this.animTick;
      boolean var1 = hasParentalConsent();
      if (this.dataSubscription == null && var1) {
         this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
      } else if (this.dataSubscription != null && !var1) {
         this.dataSubscription = null;
      }

      if (this.dataSubscription != null) {
         this.dataSubscription.tick();
      }

      if (this.shouldShowPopup()) {
         ++this.carouselTick;
      }

      if (this.showPopupButton != null) {
         this.showPopupButton.visible = this.shouldShowPopupButton();
         this.showPopupButton.active = this.showPopupButton.visible;
      }

   }

   private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher var1) {
      DataFetcher.Subscription var2 = var1.dataFetcher.createSubscription();
      var2.subscribe(var1.serverListUpdateTask, (var1x) -> {
         List var2 = this.serverList.updateServersList(var1x);
         RealmsServer var3 = this.getSelectedServer();
         RealmsMainScreen.ServerEntry var4 = null;
         this.realmSelectionList.clear();
         boolean var5 = !this.hasFetchedServers;
         if (var5) {
            this.hasFetchedServers = true;
         }

         boolean var6 = false;
         Iterator var7 = var2.iterator();

         RealmsServer var8;
         while(var7.hasNext()) {
            var8 = (RealmsServer)var7.next();
            if (this.isSelfOwnedNonExpiredServer(var8)) {
               var6 = true;
            }
         }

         this.realmsServers = var2;
         if (this.shouldShowMessageInList()) {
            this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
         }

         var7 = this.realmsServers.iterator();

         while(var7.hasNext()) {
            var8 = (RealmsServer)var7.next();
            RealmsMainScreen.ServerEntry var9 = new RealmsMainScreen.ServerEntry(var8);
            this.realmSelectionList.addEntry(var9);
            if (var3 != null && var3.id == var8.id) {
               var4 = var9;
            }
         }

         if (!regionsPinged && var6) {
            regionsPinged = true;
            this.pingRegions();
         }

         if (var5) {
            this.updateButtonStates((RealmsServer)null);
         } else {
            this.realmSelectionList.setSelected((RealmsMainScreen.Entry)var4);
         }

      });
      var2.subscribe(var1.pendingInvitesTask, (var1x) -> {
         this.numberOfPendingInvites = var1x;
         if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
            this.minecraft.getNarrator().sayNow((Component)Component.translatable("mco.configure.world.invite.narration", this.numberOfPendingInvites));
         }

      });
      var2.subscribe(var1.trialAvailabilityTask, (var1x) -> {
         if (!this.createdTrial) {
            if (var1x != this.trialsAvailable && this.shouldShowPopup()) {
               this.trialsAvailable = var1x;
               this.showingPopup = false;
            } else {
               this.trialsAvailable = var1x;
            }

         }
      });
      var2.subscribe(var1.liveStatsTask, (var1x) -> {
         Iterator var2 = var1x.servers.iterator();

         while(true) {
            while(var2.hasNext()) {
               RealmsServerPlayerList var3 = (RealmsServerPlayerList)var2.next();
               Iterator var4 = this.realmsServers.iterator();

               while(var4.hasNext()) {
                  RealmsServer var5 = (RealmsServer)var4.next();
                  if (var5.id == var3.serverId) {
                     var5.updateServerPing(var3);
                     break;
                  }
               }
            }

            return;
         }
      });
      var2.subscribe(var1.newsTask, (var2x) -> {
         var1.newsManager.updateUnreadNews(var2x);
         this.hasUnreadNews = var1.newsManager.hasUnreadNews();
         this.newsLink = var1.newsManager.newsLink();
         this.updateButtonStates((RealmsServer)null);
      });
      return var2;
   }

   void refreshFetcher() {
      if (this.dataSubscription != null) {
         this.dataSubscription.reset();
      }

   }

   private void pingRegions() {
      (new Thread(() -> {
         List var1 = Ping.pingAllRegions();
         RealmsClient var2 = RealmsClient.create();
         PingResult var3 = new PingResult();
         var3.pingResults = var1;
         var3.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            var2.sendPingResults(var3);
         } catch (Throwable var5) {
            LOGGER.warn("Could not send ping result to Realms: ", var5);
         }

      })).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      ArrayList var1 = Lists.newArrayList();
      Iterator var2 = this.realmsServers.iterator();

      while(var2.hasNext()) {
         RealmsServer var3 = (RealmsServer)var2.next();
         if (this.isSelfOwnedNonExpiredServer(var3)) {
            var1.add(var3.id);
         }
      }

      return var1;
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public void setCreatedTrial(boolean var1) {
      this.createdTrial = var1;
   }

   void onRenew(@Nullable RealmsServer var1) {
      if (var1 != null) {
         String var10000 = var1.remoteSubscriptionId;
         String var2 = "https://aka.ms/ExtendJavaRealms?subscriptionId=" + var10000 + "&profileId=" + this.minecraft.getUser().getUuid() + "&ref=" + (var1.expiredTrial ? "expiredTrial" : "expiredRealm");
         this.minecraft.keyboardHandler.setClipboard(var2);
         Util.getPlatform().openUri(var2);
      }

   }

   private void checkClientCompatability() {
      if (!checkedClientCompatability) {
         checkedClientCompatability = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse var2 = var1.clientCompatible();
                  if (var2 != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  RealmsMainScreen.this.checkParentalConsent();
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.checkedClientCompatability = false;
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
                  if (var3.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                  } else {
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
                     });
                  }
               }

            }
         }).start();
      }

   }

   void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient var1 = RealmsClient.create();

            try {
               Boolean var2 = var1.mcoEnabled();
               if (var2) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                  });
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
               RealmsMainScreen.this.minecraft.execute(() -> {
                  RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
               });
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  Boolean var2 = var1.stageAvailable();
                  if (var2) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.this.refreshFetcher();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  Boolean var2 = var1.stageAvailable();
                  if (var2) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.this.refreshFetcher();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      this.refreshFetcher();
   }

   void configureClicked(@Nullable RealmsServer var1) {
      if (var1 != null && (this.minecraft.getUser().getUuid().equals(var1.ownerUUID) || overrideConfigure)) {
         this.saveListScrollPosition();
         this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, var1.id));
      }

   }

   void leaveClicked(@Nullable RealmsServer var1) {
      if (var1 != null && !this.minecraft.getUser().getUuid().equals(var1.ownerUUID)) {
         this.saveListScrollPosition();
         MutableComponent var2 = Component.translatable("mco.configure.world.leave.question.line1");
         MutableComponent var3 = Component.translatable("mco.configure.world.leave.question.line2");
         this.minecraft.setScreen(new RealmsLongConfirmationScreen((var2x) -> {
            this.leaveServer(var2x, var1);
         }, RealmsLongConfirmationScreen.Type.Info, var2, var3, true));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer getSelectedServer() {
      if (this.realmSelectionList == null) {
         return null;
      } else {
         RealmsMainScreen.Entry var1 = (RealmsMainScreen.Entry)this.realmSelectionList.getSelected();
         return var1 != null ? var1.getServer() : null;
      }
   }

   private void leaveServer(boolean var1, final RealmsServer var2) {
      if (var1) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsClient var1 = RealmsClient.create();
                  var1.uninviteMyselfFrom(var2.id);
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.removeServer(var2);
                  });
               } catch (RealmsServiceException var2x) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var2x, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.minecraft.setScreen(this);
   }

   void removeServer(RealmsServer var1) {
      this.realmsServers = this.serverList.removeItem(var1);
      this.realmSelectionList.children().removeIf((var1x) -> {
         RealmsServer var2 = var1x.getServer();
         return var2 != null && var2.id == var1.id;
      });
      this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      this.updateButtonStates((RealmsServer)null);
      this.playButton.active = false;
   }

   public void resetScreen() {
      if (this.realmSelectionList != null) {
         this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      }

   }

   public boolean keyPressed(int var1, int var2, int var3) {
      if (var1 == 256) {
         this.keyCombos.forEach(KeyCombo::reset);
         this.onClosePopup();
         return true;
      } else {
         return super.keyPressed(var1, var2, var3);
      }
   }

   void onClosePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      } else {
         this.minecraft.setScreen(this.lastScreen);
      }

   }

   public boolean charTyped(char var1, int var2) {
      this.keyCombos.forEach((var1x) -> {
         var1x.keyPressed(var1);
      });
      return true;
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.hoveredElement = RealmsMainScreen.HoveredElement.NONE;
      this.toolTip = null;
      this.renderBackground(var1);
      this.realmSelectionList.render(var1, var2, var3, var4);
      this.drawRealmsLogo(var1, this.width / 2 - 50, 7);
      if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
         this.renderStage(var1);
      }

      if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
         this.renderLocal(var1);
      }

      if (this.shouldShowPopup()) {
         this.drawPopup(var1);
      } else {
         if (this.showingPopup) {
            this.updateButtonStates((RealmsServer)null);
            if (!this.realmsSelectionListAdded) {
               this.addWidget(this.realmSelectionList);
               this.realmsSelectionListAdded = true;
            }

            this.playButton.active = this.shouldPlayButtonBeActive(this.getSelectedServer());
         }

         this.showingPopup = false;
      }

      super.render(var1, var2, var3, var4);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(var1, this.toolTip, var2, var3);
      }

      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         boolean var5 = true;
         boolean var6 = true;
         byte var7 = 0;
         if ((Util.getMillis() / 800L & 1L) == 1L) {
            var7 = 8;
         }

         GuiComponent.blit(var1, this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4, 0.0F, (float)var7, 8, 8, 8, 16);
      }

   }

   private void drawRealmsLogo(PoseStack var1, int var2, int var3) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, LOGO_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      var1.pushPose();
      var1.scale(0.5F, 0.5F, 0.5F);
      GuiComponent.blit(var1, var2 * 2, var3 * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
      var1.popPose();
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isOutsidePopup(var1, var3) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(var1, var3, var5);
      }
   }

   private boolean isOutsidePopup(double var1, double var3) {
      int var5 = this.popupX0();
      int var6 = this.popupY0();
      return var1 < (double)(var5 - 5) || var1 > (double)(var5 + 315) || var3 < (double)(var6 - 5) || var3 > (double)(var6 + 171);
   }

   private void drawPopup(PoseStack var1) {
      int var2 = this.popupX0();
      int var3 = this.popupY0();
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates((RealmsServer)null);
         if (this.realmsSelectionListAdded) {
            this.removeWidget(this.realmSelectionList);
            this.realmsSelectionListAdded = false;
         }

         this.minecraft.getNarrator().sayNow(POPUP_TEXT);
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, DARKEN_LOCATION);
      boolean var4 = false;
      boolean var5 = true;
      GuiComponent.blit(var1, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, POPUP_LOCATION);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!teaserImages.isEmpty()) {
         RenderSystem.setShaderTexture(0, (ResourceLocation)teaserImages.get(this.carouselIndex));
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         GuiComponent.blit(var1, var2 + 7, var3 + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.carouselTick % 95 < 5) {
            if (!this.hasSwitchedCarouselImage) {
               this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
               this.hasSwitchedCarouselImage = true;
            }
         } else {
            this.hasSwitchedCarouselImage = false;
         }
      }

      this.formattedPopup.renderLeftAlignedNoShadow(var1, this.width / 2 + 52, var3 + 7, 10, 5000268);
   }

   int popupX0() {
      return (this.width - 310) / 2;
   }

   int popupY0() {
      return this.height / 2 - 80;
   }

   void drawInvitationPendingIcon(PoseStack var1, int var2, int var3, int var4, int var5, boolean var6, boolean var7) {
      int var8 = this.numberOfPendingInvites;
      boolean var9 = this.inPendingInvitationArea((double)var2, (double)var3);
      boolean var10 = var7 && var6;
      if (var10) {
         float var11 = 0.25F + (1.0F + Mth.sin((float)this.animTick * 0.5F)) * 0.25F;
         int var12 = -16777216 | (int)(var11 * 64.0F) << 16 | (int)(var11 * 64.0F) << 8 | (int)(var11 * 64.0F) << 0;
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 + 18, var5 + 18, var12, var12);
         var12 = -16777216 | (int)(var11 * 255.0F) << 16 | (int)(var11 * 255.0F) << 8 | (int)(var11 * 255.0F) << 0;
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 + 18, var5 - 1, var12, var12);
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 - 1, var5 + 18, var12, var12);
         this.fillGradient(var1, var4 + 17, var5 - 2, var4 + 18, var5 + 18, var12, var12);
         this.fillGradient(var1, var4 - 2, var5 + 17, var4 + 18, var5 + 18, var12, var12);
      }

      RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      boolean var19 = var7 && var6;
      float var20 = var19 ? 16.0F : 0.0F;
      GuiComponent.blit(var1, var4, var5 - 6, var20, 0.0F, 15, 25, 31, 25);
      boolean var13 = var7 && var8 != 0;
      int var14;
      if (var13) {
         var14 = (Math.min(var8, 6) - 1) * 8;
         int var15 = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         float var16 = var9 ? 8.0F : 0.0F;
         GuiComponent.blit(var1, var4 + 4, var5 + 4 + var15, (float)var14, var16, 8, 8, 48, 16);
      }

      var14 = var2 + 12;
      boolean var21 = var7 && var9;
      if (var21) {
         Component var17 = var8 == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT;
         int var18 = this.font.width((FormattedText)var17);
         this.fillGradient(var1, var14 - 3, var3 - 3, var14 + var18 + 3, var3 + 8 + 3, -1073741824, -1073741824);
         this.font.drawShadow(var1, (Component)var17, (float)var14, (float)var3, -1);
      }

   }

   private boolean inPendingInvitationArea(double var1, double var3) {
      int var5 = this.width / 2 + 50;
      int var6 = this.width / 2 + 66;
      int var7 = 11;
      int var8 = 23;
      if (this.numberOfPendingInvites != 0) {
         var5 -= 3;
         var6 += 3;
         var7 -= 5;
         var8 += 5;
      }

      return (double)var5 <= var1 && var1 <= (double)var6 && (double)var7 <= var3 && var3 <= (double)var8;
   }

   public void play(@Nullable RealmsServer var1, Screen var2) {
      if (var1 != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(var2, new GetServerDetailsTask(this, var2, var1, this.connectLock)));
      }

   }

   boolean isSelfOwnedServer(RealmsServer var1) {
      return var1.ownerUUID != null && var1.ownerUUID.equals(this.minecraft.getUser().getUuid());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer var1) {
      return this.isSelfOwnedServer(var1) && !var1.expired;
   }

   void drawExpired(PoseStack var1, int var2, int var3, int var4, int var5) {
      RenderSystem.setShaderTexture(0, EXPIRED_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_EXPIRED_TOOLTIP);
      }

   }

   void drawExpiring(PoseStack var1, int var2, int var3, int var4, int var5, int var6) {
      RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.animTick % 20 < 10) {
         GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         GuiComponent.blit(var1, var2, var3, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         if (var6 <= 0) {
            this.setTooltip(SERVER_EXPIRES_SOON_TOOLTIP);
         } else if (var6 == 1) {
            this.setTooltip(SERVER_EXPIRES_IN_DAY_TOOLTIP);
         } else {
            this.setTooltip(Component.translatable("mco.selectServer.expires.days", var6));
         }
      }

   }

   void drawOpen(PoseStack var1, int var2, int var3, int var4, int var5) {
      RenderSystem.setShaderTexture(0, ON_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_OPEN_TOOLTIP);
      }

   }

   void drawClose(PoseStack var1, int var2, int var3, int var4, int var5) {
      RenderSystem.setShaderTexture(0, OFF_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_CLOSED_TOOLTIP);
      }

   }

   void drawLeave(PoseStack var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (var4 >= var2 && var4 <= var2 + 28 && var5 >= var3 && var5 <= var3 + 28 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         var6 = true;
      }

      RenderSystem.setShaderTexture(0, LEAVE_ICON_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      float var7 = var6 ? 28.0F : 0.0F;
      GuiComponent.blit(var1, var2, var3, var7, 0.0F, 28, 28, 56, 28);
      if (var6) {
         this.setTooltip(LEAVE_SERVER_TOOLTIP);
         this.hoveredElement = RealmsMainScreen.HoveredElement.LEAVE;
      }

   }

   void drawConfigure(PoseStack var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (var4 >= var2 && var4 <= var2 + 28 && var5 >= var3 && var5 <= var3 + 28 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         var6 = true;
      }

      RenderSystem.setShaderTexture(0, CONFIGURE_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      float var7 = var6 ? 28.0F : 0.0F;
      GuiComponent.blit(var1, var2, var3, var7, 0.0F, 28, 28, 56, 28);
      if (var6) {
         this.setTooltip(CONFIGURE_SERVER_TOOLTIP);
         this.hoveredElement = RealmsMainScreen.HoveredElement.CONFIGURE;
      }

   }

   protected void renderMousehoverTooltip(PoseStack var1, List<Component> var2, int var3, int var4) {
      if (!var2.isEmpty()) {
         int var5 = 0;
         int var6 = 0;
         Iterator var7 = var2.iterator();

         while(var7.hasNext()) {
            Component var8 = (Component)var7.next();
            int var9 = this.font.width((FormattedText)var8);
            if (var9 > var6) {
               var6 = var9;
            }
         }

         int var12 = var3 - var6 - 5;
         int var13 = var4;
         if (var12 < 0) {
            var12 = var3 + 12;
         }

         for(Iterator var14 = var2.iterator(); var14.hasNext(); var5 += 10) {
            Component var10 = (Component)var14.next();
            int var11 = var13 - (var5 == 0 ? 3 : 0) + var5;
            this.fillGradient(var1, var12 - 3, var11, var12 + var6 + 3, var13 + 8 + 3 + var5, -1073741824, -1073741824);
            this.font.drawShadow(var1, var10, (float)var12, (float)(var13 + var5), 16777215);
         }

      }
   }

   void renderNews(PoseStack var1, int var2, int var3, boolean var4, int var5, int var6, boolean var7, boolean var8) {
      boolean var9 = false;
      if (var2 >= var5 && var2 <= var5 + 20 && var3 >= var6 && var3 <= var6 + 20) {
         var9 = true;
      }

      RenderSystem.setShaderTexture(0, NEWS_LOCATION);
      if (var8) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean var10 = var8 && var7;
      float var11 = var10 ? 20.0F : 0.0F;
      GuiComponent.blit(var1, var5, var6, var11, 0.0F, 20, 20, 40, 20);
      if (var9 && var8) {
         this.setTooltip(NEWS_TOOLTIP);
      }

      if (var4 && var8) {
         int var12 = var9 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
         RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         GuiComponent.blit(var1, var5 + 10, var6 + 2 + var12, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void renderLocal(PoseStack var1) {
      String var2 = "LOCAL!";
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      var1.pushPose();
      var1.translate((double)(this.width / 2 - 25), 20.0D, 0.0D);
      var1.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
      var1.scale(1.5F, 1.5F, 1.5F);
      this.font.draw(var1, "LOCAL!", 0.0F, 0.0F, 8388479);
      var1.popPose();
   }

   private void renderStage(PoseStack var1) {
      String var2 = "STAGE!";
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      var1.pushPose();
      var1.translate((double)(this.width / 2 - 25), 20.0D, 0.0D);
      var1.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
      var1.scale(1.5F, 1.5F, 1.5F);
      this.font.draw(var1, (String)"STAGE!", 0.0F, 0.0F, -256);
      var1.popPose();
   }

   public RealmsMainScreen newScreen() {
      RealmsMainScreen var1 = new RealmsMainScreen(this.lastScreen);
      var1.init(this.minecraft, this.width, this.height);
      return var1;
   }

   public static void updateTeaserImages(ResourceManager var0) {
      Set var1 = var0.listResources("textures/gui/images", (var0x) -> {
         return var0x.getPath().endsWith(".png");
      }).keySet();
      teaserImages = var1.stream().filter((var0x) -> {
         return var0x.getNamespace().equals("realms");
      }).toList();
   }

   void setTooltip(Component... var1) {
      this.toolTip = Arrays.asList(var1);
   }

   private void pendingButtonPress(Button var1) {
      this.minecraft.setScreen(new RealmsPendingInvitesScreen(this.lastScreen));
   }

   static {
      UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
      TRIAL_TEXT = CommonComponents.joinLines((Collection)TRIAL_MESSAGE_LINES);
      teaserImages = ImmutableList.of();
      lastScrollYPosition = -1;
   }

   class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
      }

      public boolean isFocused() {
         return RealmsMainScreen.this.getFocused() == this;
      }

      public boolean keyPressed(int var1, int var2, int var3) {
         if (var1 != 257 && var1 != 32 && var1 != 335) {
            return super.keyPressed(var1, var2, var3);
         } else {
            RealmsMainScreen.Entry var4 = (RealmsMainScreen.Entry)this.getSelected();
            return var4 == null ? super.keyPressed(var1, var2, var3) : var4.mouseClicked(0.0D, 0.0D, 0);
         }
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         if (var5 == 0 && var1 < (double)this.getScrollbarPosition() && var3 >= (double)this.y0 && var3 <= (double)this.y1) {
            int var6 = RealmsMainScreen.this.realmSelectionList.getRowLeft();
            int var7 = this.getScrollbarPosition();
            int var8 = (int)Math.floor(var3 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int var9 = var8 / this.itemHeight;
            if (var1 >= (double)var6 && var1 <= (double)var7 && var9 >= 0 && var8 >= 0 && var9 < this.getItemCount()) {
               this.itemClicked(var8, var9, var1, var3, this.width);
               this.selectItem(var9);
            }

            return true;
         } else {
            return super.mouseClicked(var1, var3, var5);
         }
      }

      public void setSelected(@Nullable RealmsMainScreen.Entry var1) {
         super.setSelected(var1);
         if (var1 != null) {
            RealmsMainScreen.this.updateButtonStates(var1.getServer());
         } else {
            RealmsMainScreen.this.updateButtonStates((RealmsServer)null);
         }

      }

      public void itemClicked(int var1, int var2, double var3, double var5, int var7) {
         RealmsMainScreen.Entry var8 = (RealmsMainScreen.Entry)this.getEntry(var2);
         if (var8 instanceof RealmsMainScreen.TrialEntry) {
            RealmsMainScreen.this.popupOpenedByUser = true;
         } else {
            RealmsServer var9 = var8.getServer();
            if (var9 != null) {
               if (var9.state == RealmsServer.State.UNINITIALIZED) {
                  Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(var9, RealmsMainScreen.this));
               } else {
                  if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.CONFIGURE) {
                     RealmsMainScreen.this.configureClicked(var9);
                  } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.LEAVE) {
                     RealmsMainScreen.this.leaveClicked(var9);
                  } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.EXPIRED) {
                     RealmsMainScreen.this.onRenew(var9);
                  } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(var9)) {
                     if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isSelectedItem(var2)) {
                        RealmsMainScreen.this.play(var9, RealmsMainScreen.this);
                     }

                     RealmsMainScreen.this.lastClickTime = Util.getMillis();
                  }

               }
            }
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   private class NewsButton extends Button {
      public NewsButton() {
         super(RealmsMainScreen.this.width - 115, 6, 20, 20, Component.translatable("mco.news"), (var1x) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               Util.getPlatform().openUri(RealmsMainScreen.this.newsLink);
               if (RealmsMainScreen.this.hasUnreadNews) {
                  RealmsPersistence.RealmsPersistenceData var2 = RealmsPersistence.readFile();
                  var2.hasUnreadNews = false;
                  RealmsMainScreen.this.hasUnreadNews = false;
                  RealmsPersistence.writeFile(var2);
               }

            }
         });
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.renderNews(var1, var2, var3, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHoveredOrFocused(), this.active);
      }
   }

   private class PendingInvitesButton extends Button {
      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, CommonComponents.EMPTY, RealmsMainScreen.this::pendingButtonPress);
      }

      public void tick() {
         this.setMessage(RealmsMainScreen.this.numberOfPendingInvites == 0 ? RealmsMainScreen.NO_PENDING_INVITES_TEXT : RealmsMainScreen.PENDING_INVITES_TEXT);
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.drawInvitationPendingIcon(var1, var2, var3, this.x, this.y, this.isHoveredOrFocused(), this.active);
      }
   }

   private class CloseButton extends Button {
      public CloseButton() {
         super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, Component.translatable("mco.selectServer.close"), (var1x) -> {
            RealmsMainScreen.this.onClosePopup();
         });
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RenderSystem.setShaderTexture(0, RealmsMainScreen.CROSS_ICON_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         float var5 = this.isHoveredOrFocused() ? 12.0F : 0.0F;
         blit(var1, this.x, this.y, 0.0F, var5, 12, 12, 12, 24);
         if (this.isMouseOver((double)var2, (double)var3)) {
            RealmsMainScreen.this.setTooltip(this.getMessage());
         }

      }
   }

   abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
      Entry() {
         super();
      }

      @Nullable
      public abstract RealmsServer getServer();
   }

   private static enum HoveredElement {
      NONE,
      EXPIRED,
      LEAVE,
      CONFIGURE;

      private HoveredElement() {
      }

      // $FF: synthetic method
      private static RealmsMainScreen.HoveredElement[] $values() {
         return new RealmsMainScreen.HoveredElement[]{NONE, EXPIRED, LEAVE, CONFIGURE};
      }
   }

   private class TrialEntry extends RealmsMainScreen.Entry {
      TrialEntry() {
         super();
      }

      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         this.renderTrialItem(var1, var2, var4, var3, var7, var8);
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         RealmsMainScreen.this.popupOpenedByUser = true;
         return true;
      }

      private void renderTrialItem(PoseStack var1, int var2, int var3, int var4, int var5, int var6) {
         int var7 = var4 + 8;
         int var8 = 0;
         boolean var9 = false;
         if (var3 <= var5 && var5 <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && var4 <= var6 && var6 <= var4 + 32) {
            var9 = true;
         }

         int var10 = 8388479;
         if (var9 && !RealmsMainScreen.this.shouldShowPopup()) {
            var10 = 6077788;
         }

         for(Iterator var11 = RealmsMainScreen.TRIAL_MESSAGE_LINES.iterator(); var11.hasNext(); var8 += 10) {
            Component var12 = (Component)var11.next();
            GuiComponent.drawCenteredString(var1, RealmsMainScreen.this.font, var12, RealmsMainScreen.this.width / 2, var7 + var8, var10);
         }

      }

      public Component getNarration() {
         return RealmsMainScreen.TRIAL_TEXT;
      }

      @Nullable
      public RealmsServer getServer() {
         return null;
      }
   }

   private class ServerEntry extends RealmsMainScreen.Entry {
      private static final int SKIN_HEAD_LARGE_WIDTH = 36;
      private final RealmsServer serverData;

      public ServerEntry(RealmsServer var2) {
         super();
         this.serverData = var2;
      }

      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         this.renderMcoServerItem(this.serverData, var1, var4, var3, var7, var8);
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
         }

         return true;
      }

      private void renderMcoServerItem(RealmsServer var1, PoseStack var2, int var3, int var4, int var5, int var6) {
         this.renderLegacy(var1, var2, var3 + 36, var4, var5, var6);
      }

      private void renderLegacy(RealmsServer var1, PoseStack var2, int var3, int var4, int var5, int var6) {
         if (var1.state == RealmsServer.State.UNINITIALIZED) {
            RenderSystem.setShaderTexture(0, RealmsMainScreen.WORLDICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(var2, var3 + 10, var4 + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float var19 = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int var20 = -16777216 | (int)(127.0F * var19) << 16 | (int)(255.0F * var19) << 8 | (int)(127.0F * var19);
            GuiComponent.drawCenteredString(var2, RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, var3 + 10 + 40 + 75, var4 + 12, var20);
         } else {
            boolean var7 = true;
            boolean var8 = true;
            if (var1.expired) {
               RealmsMainScreen.this.drawExpired(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            } else if (var1.state == RealmsServer.State.CLOSED) {
               RealmsMainScreen.this.drawClose(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(var1) && var1.daysLeft < 7) {
               RealmsMainScreen.this.drawExpiring(var2, var3 + 225 - 14, var4 + 2, var5, var6, var1.daysLeft);
            } else if (var1.state == RealmsServer.State.OPEN) {
               RealmsMainScreen.this.drawOpen(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            }

            if (!RealmsMainScreen.this.isSelfOwnedServer(var1) && !RealmsMainScreen.overrideConfigure) {
               RealmsMainScreen.this.drawLeave(var2, var3 + 225, var4 + 2, var5, var6);
            } else {
               RealmsMainScreen.this.drawConfigure(var2, var3 + 225, var4 + 2, var5, var6);
            }

            if (!"0".equals(var1.serverPing.nrOfPlayers)) {
               String var9 = ChatFormatting.GRAY + var1.serverPing.nrOfPlayers;
               RealmsMainScreen.this.font.draw(var2, var9, (float)(var3 + 207 - RealmsMainScreen.this.font.width(var9)), (float)(var4 + 3), 8421504);
               if (var5 >= var3 + 207 - RealmsMainScreen.this.font.width(var9) && var5 <= var3 + 207 && var6 >= var4 + 1 && var6 <= var4 + 10 && var6 < RealmsMainScreen.this.height - 40 && var6 > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.setTooltip(Component.literal(var1.serverPing.playerList));
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(var1) && var1.expired) {
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.enableBlend();
               RenderSystem.setShaderTexture(0, RealmsMainScreen.BUTTON_LOCATION);
               RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
               Component var22;
               Component var23;
               if (var1.expiredTrial) {
                  var22 = RealmsMainScreen.TRIAL_EXPIRED_TEXT;
                  var23 = RealmsMainScreen.SUBSCRIPTION_CREATE_TEXT;
               } else {
                  var22 = RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                  var23 = RealmsMainScreen.SUBSCRIPTION_RENEW_TEXT;
               }

               int var11 = RealmsMainScreen.this.font.width((FormattedText)var23) + 17;
               boolean var12 = true;
               int var13 = var3 + RealmsMainScreen.this.font.width((FormattedText)var22) + 8;
               int var14 = var4 + 13;
               boolean var15 = false;
               if (var5 >= var13 && var5 < var13 + var11 && var6 > var14 && var6 <= var14 + 16 && var6 < RealmsMainScreen.this.height - 40 && var6 > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  var15 = true;
                  RealmsMainScreen.this.hoveredElement = RealmsMainScreen.HoveredElement.EXPIRED;
               }

               int var16 = var15 ? 2 : 1;
               GuiComponent.blit(var2, var13, var14, 0.0F, (float)(46 + var16 * 20), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13 + var11 / 2, var14, (float)(200 - var11 / 2), (float)(46 + var16 * 20), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13, var14 + 8, 0.0F, (float)(46 + var16 * 20 + 12), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13 + var11 / 2, var14 + 8, (float)(200 - var11 / 2), (float)(46 + var16 * 20 + 12), var11 / 2, 8, 256, 256);
               RenderSystem.disableBlend();
               int var17 = var4 + 11 + 5;
               int var18 = var15 ? 16777120 : 16777215;
               RealmsMainScreen.this.font.draw(var2, var22, (float)(var3 + 2), (float)(var17 + 1), 15553363);
               GuiComponent.drawCenteredString(var2, RealmsMainScreen.this.font, var23, var13 + var11 / 2, var17 + 1, var18);
            } else {
               if (var1.worldType == RealmsServer.WorldType.MINIGAME) {
                  int var21 = 13413468;
                  int var10 = RealmsMainScreen.this.font.width((FormattedText)RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                  RealmsMainScreen.this.font.draw(var2, RealmsMainScreen.SELECT_MINIGAME_PREFIX, (float)(var3 + 2), (float)(var4 + 12), 13413468);
                  RealmsMainScreen.this.font.draw(var2, var1.getMinigameName(), (float)(var3 + 2 + var10), (float)(var4 + 12), 7105644);
               } else {
                  RealmsMainScreen.this.font.draw(var2, var1.getDescription(), (float)(var3 + 2), (float)(var4 + 12), 7105644);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(var1)) {
                  RealmsMainScreen.this.font.draw(var2, var1.owner, (float)(var3 + 2), (float)(var4 + 12 + 11), 5000268);
               }
            }

            RealmsMainScreen.this.font.draw(var2, var1.getName(), (float)(var3 + 2), (float)(var4 + 1), 16777215);
            RealmsTextureManager.withBoundFace(var1.ownerUUID, () -> {
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               PlayerFaceRenderer.draw(var2, var3 - 36, var4, 32);
            });
         }
      }

      public Component getNarration() {
         return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION : Component.translatable("narrator.select", this.serverData.name));
      }

      @Nullable
      public RealmsServer getServer() {
         return this.serverData;
      }
   }
}
