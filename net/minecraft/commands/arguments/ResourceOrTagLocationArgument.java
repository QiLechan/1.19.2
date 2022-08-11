package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagLocationArgument<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagLocationArgument(ResourceKey<? extends Registry<T>> var1) {
      super();
      this.registryKey = var1;
   }

   public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> var0) {
      return new ResourceOrTagLocationArgument(var0);
   }

   public static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(CommandContext<CommandSourceStack> var0, String var1, ResourceKey<Registry<T>> var2, DynamicCommandExceptionType var3) throws CommandSyntaxException {
      ResourceOrTagLocationArgument.Result var4 = (ResourceOrTagLocationArgument.Result)var0.getArgument(var1, ResourceOrTagLocationArgument.Result.class);
      Optional var5 = var4.cast(var2);
      return (ResourceOrTagLocationArgument.Result)var5.orElseThrow(() -> {
         return var3.create(var4);
      });
   }

   public ResourceOrTagLocationArgument.Result<T> parse(StringReader var1) throws CommandSyntaxException {
      if (var1.canRead() && var1.peek() == '#') {
         int var5 = var1.getCursor();

         try {
            var1.skip();
            ResourceLocation var3 = ResourceLocation.read(var1);
            return new ResourceOrTagLocationArgument.TagResult(TagKey.create(this.registryKey, var3));
         } catch (CommandSyntaxException var4) {
            var1.setCursor(var5);
            throw var4;
         }
      } else {
         ResourceLocation var2 = ResourceLocation.read(var1);
         return new ResourceOrTagLocationArgument.ResourceResult(ResourceKey.create(this.registryKey, var2));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> var1, SuggestionsBuilder var2) {
      Object var4 = var1.getSource();
      if (var4 instanceof SharedSuggestionProvider) {
         SharedSuggestionProvider var3 = (SharedSuggestionProvider)var4;
         return var3.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, var2, var1);
      } else {
         return var2.buildFuture();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader var1) throws CommandSyntaxException {
      return this.parse(var1);
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<ResourceKey<T>, TagKey<T>> unwrap();

      <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

      String asPrintable();
   }

   static record TagResult<T>(TagKey<T> a) implements ResourceOrTagLocationArgument.Result<T> {
      private final TagKey<T> key;

      TagResult(TagKey<T> var1) {
         super();
         this.key = var1;
      }

      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.right(this.key);
      }

      public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1) {
         return this.key.cast(var1).map(ResourceOrTagLocationArgument.TagResult::new);
      }

      public boolean test(Holder<T> var1) {
         return var1.is(this.key);
      }

      public String asPrintable() {
         return "#" + this.key.location();
      }

      public TagKey<T> key() {
         return this.key;
      }

      // $FF: synthetic method
      public boolean test(Object var1) {
         return this.test((Holder)var1);
      }
   }

   static record ResourceResult<T>(ResourceKey<T> a) implements ResourceOrTagLocationArgument.Result<T> {
      private final ResourceKey<T> key;

      ResourceResult(ResourceKey<T> var1) {
         super();
         this.key = var1;
      }

      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.left(this.key);
      }

      public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1) {
         return this.key.cast(var1).map(ResourceOrTagLocationArgument.ResourceResult::new);
      }

      public boolean test(Holder<T> var1) {
         return var1.is(this.key);
      }

      public String asPrintable() {
         return this.key.location().toString();
      }

      public ResourceKey<T> key() {
         return this.key;
      }

      // $FF: synthetic method
      public boolean test(Object var1) {
         return this.test((Holder)var1);
      }
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ResourceOrTagLocationArgument.Info<T>.Template> {
      public Info() {
         super();
      }

      public void serializeToNetwork(ResourceOrTagLocationArgument.Info<T>.Template var1, FriendlyByteBuf var2) {
         var2.writeResourceLocation(var1.registryKey.location());
      }

      public ResourceOrTagLocationArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf var1) {
         ResourceLocation var2 = var1.readResourceLocation();
         return new ResourceOrTagLocationArgument.Info.Template(ResourceKey.createRegistryKey(var2));
      }

      public void serializeToJson(ResourceOrTagLocationArgument.Info<T>.Template var1, JsonObject var2) {
         var2.addProperty("registry", var1.registryKey.location().toString());
      }

      public ResourceOrTagLocationArgument.Info<T>.Template unpack(ResourceOrTagLocationArgument<T> var1) {
         return new ResourceOrTagLocationArgument.Info.Template(var1.registryKey);
      }

      // $FF: synthetic method
      public ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf var1) {
         return this.deserializeFromNetwork(var1);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagLocationArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> var2) {
            super();
            this.registryKey = var2;
         }

         public ResourceOrTagLocationArgument<T> instantiate(CommandBuildContext var1) {
            return new ResourceOrTagLocationArgument(this.registryKey);
         }

         public ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ?> type() {
            return Info.this;
         }

         // $FF: synthetic method
         public ArgumentType instantiate(CommandBuildContext var1) {
            return this.instantiate(var1);
         }
      }
   }
}
