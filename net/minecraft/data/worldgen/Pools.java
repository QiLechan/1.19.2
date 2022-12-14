package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
   public static final ResourceKey<StructureTemplatePool> EMPTY;
   private static final Holder<StructureTemplatePool> BUILTIN_EMPTY;

   public Pools() {
      super();
   }

   public static Holder<StructureTemplatePool> register(StructureTemplatePool var0) {
      return BuiltinRegistries.register(BuiltinRegistries.TEMPLATE_POOL, (ResourceLocation)var0.getName(), var0);
   }

   /** @deprecated */
   @Deprecated
   public static void forceBootstrap() {
      bootstrap(BuiltinRegistries.TEMPLATE_POOL);
   }

   public static Holder<StructureTemplatePool> bootstrap(Registry<StructureTemplatePool> var0) {
      BastionPieces.bootstrap();
      PillagerOutpostPools.bootstrap();
      VillagePools.bootstrap();
      AncientCityStructurePieces.bootstrap();
      return BUILTIN_EMPTY;
   }

   static {
      EMPTY = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("empty"));
      BUILTIN_EMPTY = register(new StructureTemplatePool(EMPTY.location(), EMPTY.location(), ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
   }
}
