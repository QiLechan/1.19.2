package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;

public class EntityVariantFix extends NamedEntityFix {
   private final String fieldName;
   private final IntFunction<String> idConversions;

   public EntityVariantFix(Schema var1, String var2, TypeReference var3, String var4, String var5, IntFunction<String> var6) {
      super(var1, false, var2, var3, var4);
      this.fieldName = var5;
      this.idConversions = var6;
   }

   private static <T> Dynamic<T> updateAndRename(Dynamic<T> var0, String var1, String var2, Function<Dynamic<T>, Dynamic<T>> var3) {
      return var0.map((var4) -> {
         DynamicOps var5 = var0.getOps();
         Function var6 = (var2x) -> {
            return ((Dynamic)var3.apply(new Dynamic(var5, var2x))).getValue();
         };
         return var5.get(var4, var1).map((var4x) -> {
            return var5.set(var4, var2, var6.apply(var4x));
         }).result().orElse(var4);
      });
   }

   protected Typed<?> fix(Typed<?> var1) {
      return var1.update(DSL.remainderFinder(), (var1x) -> {
         return updateAndRename(var1x, this.fieldName, "variant", (var1) -> {
            return (Dynamic)DataFixUtils.orElse(var1.asNumber().map((var2) -> {
               return var1.createString((String)this.idConversions.apply(var2.intValue()));
            }).result(), var1);
         });
      });
   }
}
