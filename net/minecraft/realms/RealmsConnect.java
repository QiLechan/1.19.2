package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.slf4j.Logger;

public class RealmsConnect {
   static final Logger LOGGER = LogUtils.getLogger();
   final Screen onlineScreen;
   volatile boolean aborted;
   @Nullable
   Connection connection;

   public RealmsConnect(Screen var1) {
      super();
      this.onlineScreen = var1;
   }

   public void connect(final RealmsServer var1, ServerAddress var2) {
      final Minecraft var3 = Minecraft.getInstance();
      var3.setConnectedToRealms(true);
      var3.prepareForMultiplayer();
      var3.getNarrator().sayNow((Component)Component.translatable("mco.connect.success"));
      final CompletableFuture var4 = var3.getProfileKeyPairManager().preparePublicKey();
      final String var5 = var2.getHost();
      final int var6 = var2.getPort();
      (new Thread("Realms-connect-task") {
         public void run() {
            InetSocketAddress var1x = null;

            try {
               var1x = new InetSocketAddress(var5, var6);
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.this.connection = Connection.connectToServer(var1x, var3.options.useNativeTransport());
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.this.connection.setListener(new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, var3, RealmsConnect.this.onlineScreen, (var0) -> {
               }));
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.this.connection.send(new ClientIntentionPacket(var5, var6, ConnectionProtocol.LOGIN));
               if (RealmsConnect.this.aborted) {
                  return;
               }

               String var2 = var3.getUser().getName();
               UUID var6x = var3.getUser().getProfileId();
               RealmsConnect.this.connection.send(new ServerboundHelloPacket(var2, (Optional)var4.join(), Optional.ofNullable(var6x)));
               var3.setCurrentServer(var1, var5);
            } catch (Exception var5x) {
               var3.getClientPackSource().clearServerPack();
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.LOGGER.error("Couldn't connect to world", var5x);
               String var3x = var5x.toString();
               if (var1x != null) {
                  String var4x = var1x + ":" + var6;
                  var3x = var3x.replaceAll(var4x, "");
               }

               DisconnectedRealmsScreen var7 = new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", var3x));
               var3.execute(() -> {
                  var3.setScreen(var7);
               });
            }

         }
      }).start();
   }

   public void abort() {
      this.aborted = true;
      if (this.connection != null && this.connection.isConnected()) {
         this.connection.disconnect(Component.translatable("disconnect.genericReason"));
         this.connection.handleDisconnection();
      }

   }

   public void tick() {
      if (this.connection != null) {
         if (this.connection.isConnected()) {
            this.connection.tick();
         } else {
            this.connection.handleDisconnection();
         }
      }

   }
}
