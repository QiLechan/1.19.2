package net.minecraft.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataGenerator.PathProvider pathProvider;
   private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders;

   public LootTableProvider(DataGenerator var1) {
      super();
      this.subProviders = ImmutableList.of(Pair.of(FishingLoot::new, LootContextParamSets.FISHING), Pair.of(ChestLoot::new, LootContextParamSets.CHEST), Pair.of(EntityLoot::new, LootContextParamSets.ENTITY), Pair.of(BlockLoot::new, LootContextParamSets.BLOCK), Pair.of(PiglinBarterLoot::new, LootContextParamSets.PIGLIN_BARTER), Pair.of(GiftLoot::new, LootContextParamSets.GIFT));
      this.pathProvider = var1.createPathProvider(DataGenerator.Target.DATA_PACK, "loot_tables");
   }

   public void run(CachedOutput var1) {
      HashMap var2 = Maps.newHashMap();
      this.subProviders.forEach((var1x) -> {
         ((Consumer)((Supplier)var1x.getFirst()).get()).accept((var2x, var3) -> {
            if (var2.put(var2x, var3.setParamSet((LootContextParamSet)var1x.getSecond()).build()) != null) {
               throw new IllegalStateException("Duplicate loot table " + var2x);
            }
         });
      });
      LootContextParamSet var10002 = LootContextParamSets.ALL_PARAMS;
      Function var10003 = (var0) -> {
         return null;
      };
      Objects.requireNonNull(var2);
      ValidationContext var3 = new ValidationContext(var10002, var10003, var2::get);
      SetView var4 = Sets.difference(BuiltInLootTables.all(), var2.keySet());
      Iterator var5 = var4.iterator();

      while(var5.hasNext()) {
         ResourceLocation var6 = (ResourceLocation)var5.next();
         var3.reportProblem("Missing built-in table: " + var6);
      }

      var2.forEach((var1x, var2x) -> {
         LootTables.validate(var3, var1x, var2x);
      });
      Multimap var7 = var3.getProblems();
      if (!var7.isEmpty()) {
         var7.forEach((var0, var1x) -> {
            LOGGER.warn("Found validation problem in {}: {}", var0, var1x);
         });
         throw new IllegalStateException("Failed to validate loot tables, see logs");
      } else {
         var2.forEach((var2x, var3x) -> {
            Path var4 = this.pathProvider.json(var2x);

            try {
               DataProvider.saveStable(var1, LootTables.serialize(var3x), var4);
            } catch (IOException var6) {
               LOGGER.error("Couldn't save loot table {}", var4, var6);
            }

         });
      }
   }

   public String getName() {
      return "LootTables";
   }
}
