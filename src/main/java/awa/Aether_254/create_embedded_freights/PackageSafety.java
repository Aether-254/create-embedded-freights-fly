package awa.Aether_254.create_embedded_freights;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

public final class PackageSafety {
    private PackageSafety() {
    }

    public static boolean canPack(ItemStack stack, HolderLookup.Provider registries) {
        EmbeddedFreightsConfig.Data config = EmbeddedFreightsConfig.get();
        if (config.packageDepthLimitEnabled && exceedsPackageDepth(stack, config.maxPackageDepth))
            return false;
        return !config.nbtDepthLimitEnabled || !exceedsNbtDepth(stack, registries, config.maxNbtDepth);
    }

    private static boolean exceedsPackageDepth(ItemStack stack, int limit) {
        if (!(stack.getItem() instanceof PackageItem))
            return 1 > limit;

        ArrayDeque<PackageNode> pending = new ArrayDeque<>();
        pending.push(new PackageNode(stack, 1));
        int deepest = 1;

        while (!pending.isEmpty()) {
            PackageNode node = pending.pop();
            deepest = Math.max(deepest, node.depth());
            if (deepest + 1 > limit)
                return true;

            ItemStackHandler contents = PackageItem.getContents(node.stack());
            for (int slot = 0; slot < contents.getContainerSize(); slot++) {
                ItemStack nested = contents.getItem(slot);
                if (nested.getItem() instanceof PackageItem)
                    pending.push(new PackageNode(nested, node.depth() + 1));
            }
        }
        return false;
    }

    private static boolean exceedsNbtDepth(ItemStack stack, HolderLookup.Provider registries, int limit) {
        try {
            ItemStack outerPackage = PackageItem.containing(List.of(stack.copy()));
            Tag encoded = ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), outerPackage).getOrThrow();
            return tagDepthExceeds(encoded, limit);
        } catch (RuntimeException | StackOverflowError ignored) {
            return true;
        }
    }

    private static boolean tagDepthExceeds(Tag root, int limit) {
        ArrayDeque<TagNode> pending = new ArrayDeque<>();
        pending.push(new TagNode(root, 1));

        while (!pending.isEmpty()) {
            TagNode node = pending.pop();
            if (node.depth() > limit)
                return true;

            if (node.tag() instanceof CompoundTag compound) {
                for (Tag child : compound.values())
                    pending.push(new TagNode(child, node.depth() + 1));
            } else if (node.tag() instanceof ListTag list) {
                for (Tag child : list)
                    pending.push(new TagNode(child, node.depth() + 1));
            }
        }
        return false;
    }

    private record PackageNode(ItemStack stack, int depth) {
    }

    private record TagNode(Tag tag, int depth) {
    }
}
