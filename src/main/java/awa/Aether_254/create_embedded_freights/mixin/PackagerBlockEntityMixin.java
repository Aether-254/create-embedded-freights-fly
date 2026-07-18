package awa.Aether_254.create_embedded_freights.mixin;

import awa.Aether_254.create_embedded_freights.PackageSafety;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagingRequest;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackagerBlockEntity.class)
abstract class PackagerBlockEntityMixin {
    @Redirect(
        method = "attemptToSend()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;extractAnyMax()Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack createEmbeddedFreights$extractFirstSafeStack(Container inventory) {
        HolderLookup.Provider registries = createEmbeddedFreights$registries();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !PackageSafety.canPack(candidate, registries))
                continue;

            return inventory.extract(
                stack -> ItemStack.isSameItemSameComponents(stack, candidate),
                candidate.getMaxStackSize()
            );
        }
        return ItemStack.EMPTY;
    }

    @ModifyArg(
        method = "attemptToSend()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/Container;extractMax(Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;"
        ),
        index = 0
    )
    private Predicate<ItemStack> createEmbeddedFreights$filterAdditionalStacks(Predicate<ItemStack> original) {
        HolderLookup.Provider registries = createEmbeddedFreights$registries();
        return stack -> (stack.getItem() instanceof PackageItem || original.test(stack))
            && PackageSafety.canPack(stack, registries);
    }

    @Redirect(
        method = "attemptToSend()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canFitInsideContainerItems()Z")
    )
    private boolean createEmbeddedFreights$allowNestedPackagesInRedstoneMode(Item item) {
        return item instanceof PackageItem || item.canFitInsideContainerItems();
    }

    @Redirect(
        method = "attemptToSend(Ljava/util/Collection;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canFitInsideContainerItems()Z")
    )
    private boolean createEmbeddedFreights$allowNestedPackagesInRequestMode(Item item) {
        return item instanceof PackageItem || item.canFitInsideContainerItems();
    }

    @Inject(method = "attemptToSend(Ljava/util/Collection;)V", at = @At("HEAD"), cancellable = true)
    private void createEmbeddedFreights$rejectUnsafeRequests(Collection<PackagingRequest> requests, CallbackInfo ci) {
        if (requests == null)
            return;

        HolderLookup.Provider registries = createEmbeddedFreights$registries();
        for (PackagingRequest request : requests) {
            if (!PackageSafety.canPack(request.item(), registries)) {
                ci.cancel();
                return;
            }
        }
    }

    private HolderLookup.Provider createEmbeddedFreights$registries() {
        PackagerBlockEntity packager = (PackagerBlockEntity) (Object) this;
        return packager.getLevel().registryAccess();
    }
}
