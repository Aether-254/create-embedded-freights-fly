package awa.Aether_254.create_embedded_freights;

import net.fabricmc.api.ModInitializer;

public final class CreateEmbeddedFreights implements ModInitializer {
    @Override
    public void onInitialize() {
        EmbeddedFreightsConfig.load();
    }
}
