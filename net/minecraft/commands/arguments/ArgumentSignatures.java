package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PreviewableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> b) {
   private final List<ArgumentSignatures.Entry> entries;
   public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
   private static final int MAX_ARGUMENT_COUNT = 8;
   private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

   public ArgumentSignatures(FriendlyByteBuf var1) {
      this((List)var1.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
   }

   public ArgumentSignatures(List<ArgumentSignatures.Entry> var1) {
      super();
      this.entries = var1;
   }

   public MessageSignature get(String var1) {
      Iterator var2 = this.entries.iterator();

      ArgumentSignatures.Entry var3;
      do {
         if (!var2.hasNext()) {
            return MessageSignature.EMPTY;
         }

         var3 = (ArgumentSignatures.Entry)var2.next();
      } while(!var3.name.equals(var1));

      return var3.signature;
   }

   public void write(FriendlyByteBuf var1) {
      var1.writeCollection(this.entries, (var0, var1x) -> {
         var1x.write(var0);
      });
   }

   public static boolean hasSignableArguments(PreviewableCommand<?> var0) {
      return var0.arguments().stream().anyMatch((var0x) -> {
         return var0x.previewType() instanceof SignedArgument;
      });
   }

   public static ArgumentSignatures signCommand(PreviewableCommand<?> var0, ArgumentSignatures.Signer var1) {
      List var2 = collectPlainSignableArguments(var0).stream().map((var1x) -> {
         MessageSignature var2 = var1.sign((String)var1x.getFirst(), (String)var1x.getSecond());
         return new ArgumentSignatures.Entry((String)var1x.getFirst(), var2);
      }).toList();
      return new ArgumentSignatures(var2);
   }

   public static List<Pair<String, String>> collectPlainSignableArguments(PreviewableCommand<?> var0) {
      ArrayList var1 = new ArrayList();
      Iterator var2 = var0.arguments().iterator();

      while(var2.hasNext()) {
         PreviewableCommand.Argument var3 = (PreviewableCommand.Argument)var2.next();
         PreviewedArgument var5 = var3.previewType();
         if (var5 instanceof SignedArgument) {
            SignedArgument var4 = (SignedArgument)var5;
            String var6 = getSignableText(var4, var3.parsedValue());
            var1.add(Pair.of(var3.name(), var6));
         }
      }

      return var1;
   }

   private static <T> String getSignableText(SignedArgument<T> var0, ParsedArgument<?, ?> var1) {
      return var0.getSignableText(var1.getResult());
   }

   public List<ArgumentSignatures.Entry> entries() {
      return this.entries;
   }

   public static record Entry(String a, MessageSignature b) {
      final String name;
      final MessageSignature signature;

      public Entry(FriendlyByteBuf var1) {
         this(var1.readUtf(16), new MessageSignature(var1));
      }

      public Entry(String var1, MessageSignature var2) {
         super();
         this.name = var1;
         this.signature = var2;
      }

      public void write(FriendlyByteBuf var1) {
         var1.writeUtf(this.name, 16);
         this.signature.write(var1);
      }

      public String name() {
         return this.name;
      }

      public MessageSignature signature() {
         return this.signature;
      }
   }

   @FunctionalInterface
   public interface Signer {
      MessageSignature sign(String var1, String var2);
   }
}
