package awa.Aether_254.create_embedded_freights.client;

import awa.Aether_254.create_embedded_freights.EmbeddedFreightsConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EmbeddedFreightsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EmbeddedFreightsModMenu::createScreen;
    }

    private static Screen createScreen(Screen parent) {
        EmbeddedFreightsConfig.Data config = EmbeddedFreightsConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Create: Embedded Freights"));
        ConfigCategory safety = builder.getOrCreateCategory(Component.literal("Safety limits"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        safety.addEntry(entries.startBooleanToggle(Component.literal("Limit package nesting depth"), config.packageDepthLimitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Rejects packaging when the resulting package nesting depth would exceed the configured limit."))
            .setSaveConsumer(value -> config.packageDepthLimitEnabled = value)
            .build());
        safety.addEntry(entries.startIntField(Component.literal("Maximum package nesting depth"), config.maxPackageDepth)
            .setDefaultValue(30)
            .setMin(1)
            .setMax(511)
            .setTooltip(Component.literal("Counts nested packages, not raw NBT tags. The default maximum is 30."))
            .setSaveConsumer(value -> config.maxPackageDepth = value)
            .build());
        safety.addEntry(entries.startBooleanToggle(Component.literal("Limit serialized NBT depth"), config.nbtDepthLimitEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Rejects packaging when the resulting serialized ItemStack NBT would exceed the configured depth."))
            .setSaveConsumer(value -> config.nbtDepthLimitEnabled = value)
            .build());
        safety.addEntry(entries.startIntField(Component.literal("Maximum serialized NBT depth"), config.maxNbtDepth)
            .setDefaultValue(450)
            .setMin(1)
            .setMax(511)
            .setTooltip(Component.literal("Minecraft can fail at 512 NBT layers. The default maximum is 450."))
            .setSaveConsumer(value -> config.maxNbtDepth = value)
            .build());

        builder.setSavingRunnable(EmbeddedFreightsConfig::save);
        return builder.build();
    }
}
