package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class OrePlacements {
   public static final Holder<PlacedFeature> ORE_MAGMA;
   public static final Holder<PlacedFeature> ORE_SOUL_SAND;
   public static final Holder<PlacedFeature> ORE_GOLD_DELTAS;
   public static final Holder<PlacedFeature> ORE_QUARTZ_DELTAS;
   public static final Holder<PlacedFeature> ORE_GOLD_NETHER;
   public static final Holder<PlacedFeature> ORE_QUARTZ_NETHER;
   public static final Holder<PlacedFeature> ORE_GRAVEL_NETHER;
   public static final Holder<PlacedFeature> ORE_BLACKSTONE;
   public static final Holder<PlacedFeature> ORE_DIRT;
   public static final Holder<PlacedFeature> ORE_GRAVEL;
   public static final Holder<PlacedFeature> ORE_GRANITE_UPPER;
   public static final Holder<PlacedFeature> ORE_GRANITE_LOWER;
   public static final Holder<PlacedFeature> ORE_DIORITE_UPPER;
   public static final Holder<PlacedFeature> ORE_DIORITE_LOWER;
   public static final Holder<PlacedFeature> ORE_ANDESITE_UPPER;
   public static final Holder<PlacedFeature> ORE_ANDESITE_LOWER;
   public static final Holder<PlacedFeature> ORE_TUFF;
   public static final Holder<PlacedFeature> ORE_COAL_UPPER;
   public static final Holder<PlacedFeature> ORE_COAL_LOWER;
   public static final Holder<PlacedFeature> ORE_IRON_UPPER;
   public static final Holder<PlacedFeature> ORE_IRON_MIDDLE;
   public static final Holder<PlacedFeature> ORE_IRON_SMALL;
   public static final Holder<PlacedFeature> ORE_GOLD_EXTRA;
   public static final Holder<PlacedFeature> ORE_GOLD;
   public static final Holder<PlacedFeature> ORE_GOLD_LOWER;
   public static final Holder<PlacedFeature> ORE_REDSTONE;
   public static final Holder<PlacedFeature> ORE_REDSTONE_LOWER;
   public static final Holder<PlacedFeature> ORE_DIAMOND;
   public static final Holder<PlacedFeature> ORE_DIAMOND_LARGE;
   public static final Holder<PlacedFeature> ORE_DIAMOND_BURIED;
   public static final Holder<PlacedFeature> ORE_LAPIS;
   public static final Holder<PlacedFeature> ORE_LAPIS_BURIED;
   public static final Holder<PlacedFeature> ORE_INFESTED;
   public static final Holder<PlacedFeature> ORE_EMERALD;
   public static final Holder<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE;
   public static final Holder<PlacedFeature> ORE_ANCIENT_DEBRIS_SMALL;
   public static final Holder<PlacedFeature> ORE_COPPER;
   public static final Holder<PlacedFeature> ORE_COPPER_LARGE;
   public static final Holder<PlacedFeature> ORE_CLAY;

   public OrePlacements() {
      super();
   }

   private static List<PlacementModifier> orePlacement(PlacementModifier var0, PlacementModifier var1) {
      return List.of(var0, InSquarePlacement.spread(), var1, BiomeFilter.biome());
   }

   private static List<PlacementModifier> commonOrePlacement(int var0, PlacementModifier var1) {
      return orePlacement(CountPlacement.of(var0), var1);
   }

   private static List<PlacementModifier> rareOrePlacement(int var0, PlacementModifier var1) {
      return orePlacement(RarityFilter.onAverageOnceEvery(var0), var1);
   }

   static {
      ORE_MAGMA = PlacementUtils.register("ore_magma", OreFeatures.ORE_MAGMA, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))));
      ORE_SOUL_SAND = PlacementUtils.register("ore_soul_sand", OreFeatures.ORE_SOUL_SAND, commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))));
      ORE_GOLD_DELTAS = PlacementUtils.register("ore_gold_deltas", OreFeatures.ORE_NETHER_GOLD, commonOrePlacement(20, PlacementUtils.RANGE_10_10));
      ORE_QUARTZ_DELTAS = PlacementUtils.register("ore_quartz_deltas", OreFeatures.ORE_QUARTZ, commonOrePlacement(32, PlacementUtils.RANGE_10_10));
      ORE_GOLD_NETHER = PlacementUtils.register("ore_gold_nether", OreFeatures.ORE_NETHER_GOLD, commonOrePlacement(10, PlacementUtils.RANGE_10_10));
      ORE_QUARTZ_NETHER = PlacementUtils.register("ore_quartz_nether", OreFeatures.ORE_QUARTZ, commonOrePlacement(16, PlacementUtils.RANGE_10_10));
      ORE_GRAVEL_NETHER = PlacementUtils.register("ore_gravel_nether", OreFeatures.ORE_GRAVEL_NETHER, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))));
      ORE_BLACKSTONE = PlacementUtils.register("ore_blackstone", OreFeatures.ORE_BLACKSTONE, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))));
      ORE_DIRT = PlacementUtils.register("ore_dirt", OreFeatures.ORE_DIRT, commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160))));
      ORE_GRAVEL = PlacementUtils.register("ore_gravel", OreFeatures.ORE_GRAVEL, commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));
      ORE_GRANITE_UPPER = PlacementUtils.register("ore_granite_upper", OreFeatures.ORE_GRANITE, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
      ORE_GRANITE_LOWER = PlacementUtils.register("ore_granite_lower", OreFeatures.ORE_GRANITE, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
      ORE_DIORITE_UPPER = PlacementUtils.register("ore_diorite_upper", OreFeatures.ORE_DIORITE, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
      ORE_DIORITE_LOWER = PlacementUtils.register("ore_diorite_lower", OreFeatures.ORE_DIORITE, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
      ORE_ANDESITE_UPPER = PlacementUtils.register("ore_andesite_upper", OreFeatures.ORE_ANDESITE, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
      ORE_ANDESITE_LOWER = PlacementUtils.register("ore_andesite_lower", OreFeatures.ORE_ANDESITE, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
      ORE_TUFF = PlacementUtils.register("ore_tuff", OreFeatures.ORE_TUFF, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))));
      ORE_COAL_UPPER = PlacementUtils.register("ore_coal_upper", OreFeatures.ORE_COAL, commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top())));
      ORE_COAL_LOWER = PlacementUtils.register("ore_coal_lower", OreFeatures.ORE_COAL_BURIED, commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))));
      ORE_IRON_UPPER = PlacementUtils.register("ore_iron_upper", OreFeatures.ORE_IRON, commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384))));
      ORE_IRON_MIDDLE = PlacementUtils.register("ore_iron_middle", OreFeatures.ORE_IRON, commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))));
      ORE_IRON_SMALL = PlacementUtils.register("ore_iron_small", OreFeatures.ORE_IRON_SMALL, commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));
      ORE_GOLD_EXTRA = PlacementUtils.register("ore_gold_extra", OreFeatures.ORE_GOLD, commonOrePlacement(50, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256))));
      ORE_GOLD = PlacementUtils.register("ore_gold", OreFeatures.ORE_GOLD_BURIED, commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))));
      ORE_GOLD_LOWER = PlacementUtils.register("ore_gold_lower", OreFeatures.ORE_GOLD_BURIED, orePlacement(CountPlacement.of(UniformInt.of(0, 1)), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48))));
      ORE_REDSTONE = PlacementUtils.register("ore_redstone", OreFeatures.ORE_REDSTONE, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15))));
      ORE_REDSTONE_LOWER = PlacementUtils.register("ore_redstone_lower", OreFeatures.ORE_REDSTONE, commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32))));
      ORE_DIAMOND = PlacementUtils.register("ore_diamond", OreFeatures.ORE_DIAMOND_SMALL, commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
      ORE_DIAMOND_LARGE = PlacementUtils.register("ore_diamond_large", OreFeatures.ORE_DIAMOND_LARGE, rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
      ORE_DIAMOND_BURIED = PlacementUtils.register("ore_diamond_buried", OreFeatures.ORE_DIAMOND_BURIED, commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
      ORE_LAPIS = PlacementUtils.register("ore_lapis", OreFeatures.ORE_LAPIS, commonOrePlacement(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))));
      ORE_LAPIS_BURIED = PlacementUtils.register("ore_lapis_buried", OreFeatures.ORE_LAPIS_BURIED, commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))));
      ORE_INFESTED = PlacementUtils.register("ore_infested", OreFeatures.ORE_INFESTED, commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))));
      ORE_EMERALD = PlacementUtils.register("ore_emerald", OreFeatures.ORE_EMERALD, commonOrePlacement(100, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480))));
      ORE_ANCIENT_DEBRIS_LARGE = PlacementUtils.register("ore_ancient_debris_large", OreFeatures.ORE_ANCIENT_DEBRIS_LARGE, InSquarePlacement.spread(), HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24)), BiomeFilter.biome());
      ORE_ANCIENT_DEBRIS_SMALL = PlacementUtils.register("ore_debris_small", OreFeatures.ORE_ANCIENT_DEBRIS_SMALL, InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome());
      ORE_COPPER = PlacementUtils.register("ore_copper", OreFeatures.ORE_COPPPER_SMALL, commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
      ORE_COPPER_LARGE = PlacementUtils.register("ore_copper_large", OreFeatures.ORE_COPPER_LARGE, commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
      ORE_CLAY = PlacementUtils.register("ore_clay", OreFeatures.ORE_CLAY, commonOrePlacement(46, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT));
   }
}
