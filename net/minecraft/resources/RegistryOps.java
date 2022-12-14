package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
   private final Optional<RegistryLoader.Bound> loader;
   private final RegistryAccess registryAccess;
   private final DynamicOps<JsonElement> asJson;

   public static <T> RegistryOps<T> create(DynamicOps<T> var0, RegistryAccess var1) {
      return new RegistryOps(var0, var1, Optional.empty());
   }

   public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> var0, RegistryAccess.Writable var1, ResourceManager var2) {
      return createAndLoad(var0, var1, RegistryResourceAccess.forResourceManager(var2));
   }

   public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> var0, RegistryAccess.Writable var1, RegistryResourceAccess var2) {
      RegistryLoader var3 = new RegistryLoader(var2);
      RegistryOps var4 = new RegistryOps(var0, var1, Optional.of(var3.bind(var1)));
      RegistryAccess.load(var1, var4.getAsJson(), var3);
      return var4;
   }

   private RegistryOps(DynamicOps<T> var1, RegistryAccess var2, Optional<RegistryLoader.Bound> var3) {
      super(var1);
      this.loader = var3;
      this.registryAccess = var2;
      this.asJson = var1 == JsonOps.INSTANCE ? this : new RegistryOps(JsonOps.INSTANCE, var2, var3);
   }

   public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> var1) {
      return this.registryAccess.registry(var1);
   }

   public Optional<RegistryLoader.Bound> registryLoader() {
      return this.loader;
   }

   public DynamicOps<JsonElement> getAsJson() {
      return this.asJson;
   }

   public static <E> MapCodec<Registry<E>> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> var0) {
      return ExtraCodecs.retrieveContext((var1) -> {
         if (var1 instanceof RegistryOps) {
            RegistryOps var2 = (RegistryOps)var1;
            return (DataResult)var2.registry(var0).map((var0x) -> {
               return DataResult.success(var0x, var0x.elementsLifecycle());
            }).orElseGet(() -> {
               return DataResult.error("Unknown registry: " + var0);
            });
         } else {
            return DataResult.error("Not a registry ops");
         }
      });
   }
}
