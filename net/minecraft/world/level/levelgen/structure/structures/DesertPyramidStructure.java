package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class DesertPyramidStructure extends SinglePieceStructure {
   public static final Codec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

   public DesertPyramidStructure(Structure.StructureSettings var1) {
      super(DesertPyramidPiece::new, 21, 21, var1);
   }

   public StructureType<?> type() {
      return StructureType.DESERT_PYRAMID;
   }
}
