package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public class WorldGenSettingsComponent implements Widget {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");
   private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
   private static final Component MAP_FEATURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
   private static final Component SELECT_FILE_PROMPT = Component.translatable("selectWorld.import_worldgen_settings.select_file");
   private MultiLineLabel amplifiedWorldInfo;
   private Font font;
   private int width;
   private EditBox seedEdit;
   private CycleButton<Boolean> featuresButton;
   private CycleButton<Boolean> bonusItemsButton;
   private CycleButton<Holder<WorldPreset>> typeButton;
   private Button customWorldDummyButton;
   private Button customizeTypeButton;
   private Button importSettingsButton;
   private WorldCreationContext settings;
   private Optional<Holder<WorldPreset>> preset;
   private OptionalLong seed;

   public WorldGenSettingsComponent(WorldCreationContext var1, Optional<ResourceKey<WorldPreset>> var2, OptionalLong var3) {
      super();
      this.amplifiedWorldInfo = MultiLineLabel.EMPTY;
      this.settings = var1;
      this.preset = findPreset(var1, var2);
      this.seed = var3;
   }

   private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext var0, Optional<ResourceKey<WorldPreset>> var1) {
      return var1.flatMap((var1x) -> {
         return var0.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolder(var1x);
      });
   }

   public void init(CreateWorldScreen var1, Minecraft var2, Font var3) {
      this.font = var3;
      this.width = var1.width;
      this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("selectWorld.enterSeed"));
      this.seedEdit.setValue(toString(this.seed));
      this.seedEdit.setResponder((var1x) -> {
         this.seed = WorldGenSettings.parseSeed(this.seedEdit.getValue());
      });
      var1.addWidget(this.seedEdit);
      int var4 = this.width / 2 - 155;
      int var5 = this.width / 2 + 5;
      this.featuresButton = (CycleButton)var1.addRenderableWidget(CycleButton.onOffBuilder(this.settings.worldGenSettings().generateStructures()).withCustomNarration((var0) -> {
         return CommonComponents.joinForNarration(var0.createDefaultNarrationMessage(), Component.translatable("selectWorld.mapFeatures.info"));
      }).create(var4, 100, 150, 20, Component.translatable("selectWorld.mapFeatures"), (var1x, var2x) -> {
         this.updateSettings(WorldGenSettings::withStructuresToggled);
      }));
      this.featuresButton.visible = false;
      Registry var6 = this.settings.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY);
      List var7 = (List)getNonEmptyList(var6, WorldPresetTags.NORMAL).orElseGet(() -> {
         return (List)var6.holders().collect(Collectors.toUnmodifiableList());
      });
      List var8 = (List)getNonEmptyList(var6, WorldPresetTags.EXTENDED).orElse(var7);
      this.typeButton = (CycleButton)var1.addRenderableWidget(CycleButton.builder(WorldGenSettingsComponent::describePreset).withValues(var7, var8).withCustomNarration((var0) -> {
         return isAmplified((Holder)var0.getValue()) ? CommonComponents.joinForNarration(var0.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT) : var0.createDefaultNarrationMessage();
      }).create(var5, 100, 150, 20, Component.translatable("selectWorld.mapType"), (var2x, var3x) -> {
         this.preset = Optional.of(var3x);
         this.updateSettings((var1x) -> {
            return ((WorldPreset)var3x.value()).recreateWorldGenSettings(var1x);
         });
         var1.refreshWorldGenSettingsVisibility();
      }));
      Optional var10000 = this.preset;
      CycleButton var10001 = this.typeButton;
      Objects.requireNonNull(var10001);
      var10000.ifPresent(var10001::setValue);
      this.typeButton.visible = false;
      this.customWorldDummyButton = (Button)var1.addRenderableWidget(new Button(var5, 100, 150, 20, CommonComponents.optionNameValue(Component.translatable("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), (var0) -> {
      }));
      this.customWorldDummyButton.active = false;
      this.customWorldDummyButton.visible = false;
      this.customizeTypeButton = (Button)var1.addRenderableWidget(new Button(var5, 120, 150, 20, Component.translatable("selectWorld.customizeType"), (var3x) -> {
         PresetEditor var4 = (PresetEditor)PresetEditor.EDITORS.get(this.preset.flatMap(Holder::unwrapKey));
         if (var4 != null) {
            var2.setScreen(var4.createEditScreen(var1, this.settings));
         }

      }));
      this.customizeTypeButton.visible = false;
      this.bonusItemsButton = (CycleButton)var1.addRenderableWidget(CycleButton.onOffBuilder(this.settings.worldGenSettings().generateBonusChest() && !var1.hardCore).create(var4, 151, 150, 20, Component.translatable("selectWorld.bonusItems"), (var1x, var2x) -> {
         this.updateSettings(WorldGenSettings::withBonusChestToggled);
      }));
      this.bonusItemsButton.visible = false;
      this.importSettingsButton = (Button)var1.addRenderableWidget(new Button(var4, 185, 150, 20, Component.translatable("selectWorld.import_worldgen_settings"), (var3x) -> {
         String var4 = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), (CharSequence)null, (PointerBuffer)null, (CharSequence)null, false);
         if (var4 != null) {
            RegistryOps var5 = RegistryOps.create(JsonOps.INSTANCE, this.settings.registryAccess());

            DataResult var6;
            try {
               BufferedReader var7 = Files.newBufferedReader(Paths.get(var4));

               try {
                  JsonElement var8 = JsonParser.parseReader(var7);
                  var6 = WorldGenSettings.CODEC.parse(var5, var8);
               } catch (Throwable var11) {
                  if (var7 != null) {
                     try {
                        var7.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (var7 != null) {
                  var7.close();
               }
            } catch (Exception var12) {
               var6 = DataResult.error("Failed to parse file: " + var12.getMessage());
            }

            if (var6.error().isPresent()) {
               MutableComponent var14 = Component.translatable("selectWorld.import_worldgen_settings.failure");
               String var15 = ((PartialResult)var6.error().get()).message();
               LOGGER.error("Error parsing world settings: {}", var15);
               MutableComponent var9 = Component.literal(var15);
               var2.getToasts().addToast(SystemToast.multiline(var2, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var14, var9));
            } else {
               Lifecycle var13 = var6.lifecycle();
               Logger var10001 = LOGGER;
               Objects.requireNonNull(var10001);
               var6.resultOrPartial(var10001::error).ifPresent((var4x) -> {
                  WorldOpenFlows.confirmWorldCreation(var2, var1, var13, () -> {
                     this.importSettings(var4x);
                  });
               });
            }
         }
      }));
      this.importSettingsButton.visible = false;
      this.amplifiedWorldInfo = MultiLineLabel.create(var3, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
   }

   private static Optional<List<Holder<WorldPreset>>> getNonEmptyList(Registry<WorldPreset> var0, TagKey<WorldPreset> var1) {
      return var0.getTag(var1).map((var0x) -> {
         return var0x.stream().toList();
      }).filter((var0x) -> {
         return !var0x.isEmpty();
      });
   }

   private static boolean isAmplified(Holder<WorldPreset> var0) {
      return var0.unwrapKey().filter((var0x) -> {
         return var0x.equals(WorldPresets.AMPLIFIED);
      }).isPresent();
   }

   private static Component describePreset(Holder<WorldPreset> var0) {
      return (Component)var0.unwrapKey().map((var0x) -> {
         return Component.translatable(var0x.location().toLanguageKey("generator"));
      }).orElse(CUSTOM_WORLD_DESCRIPTION);
   }

   private void importSettings(WorldGenSettings var1) {
      this.settings = this.settings.withSettings(var1);
      this.preset = findPreset(this.settings, WorldPresets.fromSettings(var1));
      this.selectWorldTypeButton(true);
      this.seed = OptionalLong.of(var1.seed());
      this.seedEdit.setValue(toString(this.seed));
   }

   public void tick() {
      this.seedEdit.tick();
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      if (this.featuresButton.visible) {
         this.font.drawShadow(var1, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
      }

      this.seedEdit.render(var1, var2, var3, var4);
      if (this.preset.filter(WorldGenSettingsComponent::isAmplified).isPresent()) {
         MultiLineLabel var10000 = this.amplifiedWorldInfo;
         int var10002 = this.typeButton.x + 2;
         int var10003 = this.typeButton.y + 22;
         Objects.requireNonNull(this.font);
         var10000.renderLeftAligned(var1, var10002, var10003, 9, 10526880);
      }

   }

   void updateSettings(WorldCreationContext.SimpleUpdater var1) {
      this.settings = this.settings.withSettings(var1);
   }

   void updateSettings(WorldCreationContext.Updater var1) {
      this.settings = this.settings.withSettings(var1);
   }

   void updateSettings(WorldCreationContext var1) {
      this.settings = var1;
   }

   private static String toString(OptionalLong var0) {
      return var0.isPresent() ? Long.toString(var0.getAsLong()) : "";
   }

   public WorldCreationContext createFinalSettings(boolean var1) {
      OptionalLong var2 = WorldGenSettings.parseSeed(this.seedEdit.getValue());
      return this.settings.withSettings((var2x) -> {
         return var2x.withSeed(var1, var2);
      });
   }

   public boolean isDebug() {
      return this.settings.worldGenSettings().isDebug();
   }

   public void setVisibility(boolean var1) {
      this.selectWorldTypeButton(var1);
      if (this.isDebug()) {
         this.featuresButton.visible = false;
         this.bonusItemsButton.visible = false;
         this.customizeTypeButton.visible = false;
         this.importSettingsButton.visible = false;
      } else {
         this.featuresButton.visible = var1;
         this.bonusItemsButton.visible = var1;
         this.customizeTypeButton.visible = var1 && PresetEditor.EDITORS.containsKey(this.preset.flatMap(Holder::unwrapKey));
         this.importSettingsButton.visible = var1;
      }

      this.seedEdit.setVisible(var1);
   }

   private void selectWorldTypeButton(boolean var1) {
      if (this.preset.isPresent()) {
         this.typeButton.visible = var1;
         this.customWorldDummyButton.visible = false;
      } else {
         this.typeButton.visible = false;
         this.customWorldDummyButton.visible = var1;
      }

   }

   public WorldCreationContext settings() {
      return this.settings;
   }

   public RegistryAccess registryHolder() {
      return this.settings.registryAccess();
   }

   public void switchToHardcore() {
      this.bonusItemsButton.active = false;
      this.bonusItemsButton.setValue(false);
   }

   public void switchOutOfHardcode() {
      this.bonusItemsButton.active = true;
      this.bonusItemsButton.setValue(this.settings.worldGenSettings().generateBonusChest());
   }
}
