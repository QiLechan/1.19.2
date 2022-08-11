package net.minecraft.client.multiplayer;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class ProfileKeyPairManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys");
   private final UserApiService userApiService;
   private final Path profileKeyPairPath;
   private CompletableFuture<Optional<ProfileKeyPairManager.Result>> keyPair;

   public ProfileKeyPairManager(UserApiService var1, UUID var2, Path var3) {
      super();
      this.userApiService = var1;
      this.profileKeyPairPath = var3.resolve(PROFILE_KEY_PAIR_DIR).resolve(var2 + ".json");
      this.keyPair = CompletableFuture.supplyAsync(() -> {
         return this.readProfileKeyPair().filter((var0) -> {
            return !var0.publicKey().data().hasExpired();
         });
      }, Util.backgroundExecutor()).thenCompose(this::readOrFetchProfileKeyPair);
   }

   public CompletableFuture<Optional<ProfilePublicKey.Data>> preparePublicKey() {
      this.keyPair = this.keyPair.thenCompose((var1) -> {
         Optional var2 = var1.map(ProfileKeyPairManager.Result::keyPair);
         return this.readOrFetchProfileKeyPair(var2);
      });
      return this.keyPair.thenApply((var0) -> {
         return var0.map((var0x) -> {
            return var0x.keyPair().publicKey().data();
         });
      });
   }

   private CompletableFuture<Optional<ProfileKeyPairManager.Result>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> var1) {
      return CompletableFuture.supplyAsync(() -> {
         if (var1.isPresent() && !((ProfileKeyPair)var1.get()).dueRefresh()) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
               return var1;
            }

            this.writeProfileKeyPair((ProfileKeyPair)null);
         }

         try {
            ProfileKeyPair var2 = this.fetchProfileKeyPair(this.userApiService);
            this.writeProfileKeyPair(var2);
            return Optional.of(var2);
         } catch (CryptException | MinecraftClientException | IOException var3) {
            LOGGER.error("Failed to retrieve profile key pair", var3);
            this.writeProfileKeyPair((ProfileKeyPair)null);
            return var1;
         }
      }, Util.backgroundExecutor()).thenApply((var0) -> {
         return var0.map(ProfileKeyPairManager.Result::new);
      });
   }

   private Optional<ProfileKeyPair> readProfileKeyPair() {
      if (Files.notExists(this.profileKeyPairPath, new LinkOption[0])) {
         return Optional.empty();
      } else {
         try {
            BufferedReader var1 = Files.newBufferedReader(this.profileKeyPairPath);

            Optional var2;
            try {
               var2 = ProfileKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(var1)).result();
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
         } catch (Exception var6) {
            LOGGER.error("Failed to read profile key pair file {}", this.profileKeyPairPath, var6);
            return Optional.empty();
         }
      }
   }

   private void writeProfileKeyPair(@Nullable ProfileKeyPair var1) {
      try {
         Files.deleteIfExists(this.profileKeyPairPath);
      } catch (IOException var3) {
         LOGGER.error("Failed to delete profile key pair file {}", this.profileKeyPairPath, var3);
      }

      if (var1 != null) {
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, var1).result().ifPresent((var1x) -> {
               try {
                  Files.createDirectories(this.profileKeyPairPath.getParent());
                  Files.writeString(this.profileKeyPairPath, var1x.toString());
               } catch (Exception var3) {
                  LOGGER.error("Failed to write profile key pair file {}", this.profileKeyPairPath, var3);
               }

            });
         }
      }
   }

   private ProfileKeyPair fetchProfileKeyPair(UserApiService var1) throws CryptException, IOException {
      KeyPairResponse var2 = var1.getKeyPair();
      if (var2 != null) {
         ProfilePublicKey.Data var3 = parsePublicKey(var2);
         return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(var2.getPrivateKey()), new ProfilePublicKey(var3), Instant.parse(var2.getRefreshedAfter()));
      } else {
         throw new IOException("Could not retrieve profile key pair");
      }
   }

   private static ProfilePublicKey.Data parsePublicKey(KeyPairResponse var0) throws CryptException {
      if (!Strings.isNullOrEmpty(var0.getPublicKey()) && var0.getPublicKeySignature() != null && var0.getPublicKeySignature().array().length != 0) {
         try {
            Instant var1 = Instant.parse(var0.getExpiresAt());
            PublicKey var2 = Crypt.stringToRsaPublicKey(var0.getPublicKey());
            ByteBuffer var3 = var0.getPublicKeySignature();
            return new ProfilePublicKey.Data(var1, var2, var3.array());
         } catch (IllegalArgumentException | DateTimeException var4) {
            throw new CryptException(var4);
         }
      } else {
         throw new CryptException(new MissingException());
      }
   }

   @Nullable
   public Signer signer() {
      return (Signer)((Optional)this.keyPair.join()).map(ProfileKeyPairManager.Result::signer).orElse((Object)null);
   }

   public Optional<ProfilePublicKey> profilePublicKey() {
      return ((Optional)this.keyPair.join()).map((var0) -> {
         return var0.keyPair().publicKey();
      });
   }

   private static record Result(ProfileKeyPair a, Signer b) {
      private final ProfileKeyPair keyPair;
      private final Signer signer;

      public Result(ProfileKeyPair var1) {
         this(var1, Signer.from(var1.privateKey(), "SHA256withRSA"));
      }

      private Result(ProfileKeyPair var1, Signer var2) {
         super();
         this.keyPair = var1;
         this.signer = var2;
      }

      public ProfileKeyPair keyPair() {
         return this.keyPair;
      }

      public Signer signer() {
         return this.signer;
      }
   }
}
