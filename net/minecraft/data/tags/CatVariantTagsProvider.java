package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTagsProvider extends TagsProvider<CatVariant> {
   public CatVariantTagsProvider(DataGenerator var1) {
      super(var1, Registry.CAT_VARIANT);
   }

   protected void addTags() {
      this.tag(CatVariantTags.DEFAULT_SPAWNS).add((Object[])(CatVariant.TABBY, CatVariant.BLACK, CatVariant.RED, CatVariant.SIAMESE, CatVariant.BRITISH_SHORTHAIR, CatVariant.CALICO, CatVariant.PERSIAN, CatVariant.RAGDOLL, CatVariant.WHITE, CatVariant.JELLIE));
      this.tag(CatVariantTags.FULL_MOON_SPAWNS).addTag(CatVariantTags.DEFAULT_SPAWNS).add((Object)CatVariant.ALL_BLACK);
   }
}
