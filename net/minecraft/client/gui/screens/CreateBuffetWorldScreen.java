package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class CreateBuffetWorldScreen extends Screen {
   private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome");
   private final Screen parent;
   private final Consumer<Holder<Biome>> applySettings;
   final Registry<Biome> biomes;
   private CreateBuffetWorldScreen.BiomeList list;
   Holder<Biome> biome;
   private Button doneButton;

   public CreateBuffetWorldScreen(Screen var1, WorldCreationContext var2, Consumer<Holder<Biome>> var3) {
      super(Component.translatable("createWorld.customize.buffet.title"));
      this.parent = var1;
      this.applySettings = var3;
      this.biomes = var2.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
      Holder var4 = (Holder)this.biomes.getHolder(Biomes.PLAINS).or(() -> {
         return this.biomes.holders().findAny();
      }).orElseThrow();
      this.biome = (Holder)var2.worldGenSettings().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(var4);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.list = new CreateBuffetWorldScreen.BiomeList();
      this.addWidget(this.list);
      this.doneButton = (Button)this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, (var1) -> {
         this.applySettings.accept(this.biome);
         this.minecraft.setScreen(this.parent);
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (var1) -> {
         this.minecraft.setScreen(this.parent);
      }));
      this.list.setSelected((CreateBuffetWorldScreen.BiomeList.Entry)this.list.children().stream().filter((var1) -> {
         return Objects.equals(var1.biome, this.biome);
      }).findFirst().orElse((Object)null));
   }

   void updateButtonValidity() {
      this.doneButton.active = this.list.getSelected() != null;
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.renderDirtBackground(0);
      this.list.render(var1, var2, var3, var4);
      drawCenteredString(var1, this.font, this.title, this.width / 2, 8, 16777215);
      drawCenteredString(var1, this.font, BIOME_SELECT_INFO, this.width / 2, 28, 10526880);
      super.render(var1, var2, var3, var4);
   }

   class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
      BiomeList() {
         super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
         Collator var2 = Collator.getInstance(Locale.getDefault());
         CreateBuffetWorldScreen.this.biomes.holders().map((var1x) -> {
            return new CreateBuffetWorldScreen.BiomeList.Entry(var1x);
         }).sorted(Comparator.comparing((var0) -> {
            return var0.name.getString();
         }, var2)).forEach((var1x) -> {
            this.addEntry(var1x);
         });
      }

      protected boolean isFocused() {
         return CreateBuffetWorldScreen.this.getFocused() == this;
      }

      public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry var1) {
         super.setSelected(var1);
         if (var1 != null) {
            CreateBuffetWorldScreen.this.biome = var1.biome;
         }

         CreateBuffetWorldScreen.this.updateButtonValidity();
      }

      private class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
         final Holder.Reference<Biome> biome;
         final Component name;

         public Entry(Holder.Reference<Biome> var2) {
            super();
            this.biome = var2;
            ResourceLocation var3 = var2.key().location();
            String var4 = var3.toLanguageKey("biome");
            if (Language.getInstance().has(var4)) {
               this.name = Component.translatable(var4);
            } else {
               this.name = Component.literal(var3.toString());
            }

         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.name);
         }

         public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
            GuiComponent.drawString(var1, CreateBuffetWorldScreen.this.font, this.name, var4 + 5, var3 + 2, 16777215);
         }

         public boolean mouseClicked(double var1, double var3, int var5) {
            if (var5 == 0) {
               BiomeList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}
