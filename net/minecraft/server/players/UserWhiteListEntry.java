package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserWhiteListEntry extends StoredUserEntry<GameProfile> {
   public UserWhiteListEntry(GameProfile var1) {
      super(var1);
   }

   public UserWhiteListEntry(JsonObject var1) {
      super(createGameProfile(var1));
   }

   protected void serialize(JsonObject var1) {
      if (this.getUser() != null) {
         var1.addProperty("uuid", ((GameProfile)this.getUser()).getId() == null ? "" : ((GameProfile)this.getUser()).getId().toString());
         var1.addProperty("name", ((GameProfile)this.getUser()).getName());
      }
   }

   private static GameProfile createGameProfile(JsonObject var0) {
      if (var0.has("uuid") && var0.has("name")) {
         String var1 = var0.get("uuid").getAsString();

         UUID var2;
         try {
            var2 = UUID.fromString(var1);
         } catch (Throwable var4) {
            return null;
         }

         return new GameProfile(var2, var0.get("name").getAsString());
      } else {
         return null;
      }
   }
}
