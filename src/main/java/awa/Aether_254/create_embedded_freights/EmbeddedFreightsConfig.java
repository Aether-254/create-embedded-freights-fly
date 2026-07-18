package awa.Aether_254.create_embedded_freights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class EmbeddedFreightsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("create_embedded_freights.json");

    private static Data data = new Data();

    private EmbeddedFreightsConfig() {
    }

    public static Data get() {
        return data;
    }

    public static void load() {
        try {
            if (Files.isRegularFile(PATH)) {
                Data loaded = GSON.fromJson(Files.readString(PATH), Data.class);
                data = loaded == null ? new Data() : loaded;
            }
        } catch (IOException | RuntimeException ignored) {
            data = new Data();
        }
        sanitize();
        save();
    }

    public static void save() {
        sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException ignored) {
        }
    }

    private static void sanitize() {
        data.maxPackageDepth = Math.clamp(data.maxPackageDepth, 1, 511);
        data.maxNbtDepth = Math.clamp(data.maxNbtDepth, 1, 511);
    }

    public static final class Data {
        public boolean packageDepthLimitEnabled = true;
        public int maxPackageDepth = 30;
        public boolean nbtDepthLimitEnabled = true;
        public int maxNbtDepth = 450;
    }
}
