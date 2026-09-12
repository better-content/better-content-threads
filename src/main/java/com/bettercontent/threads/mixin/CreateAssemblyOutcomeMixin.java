package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;
@Pseudo
@Mixin(targets = "com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks", remap = false)
public abstract class CreateAssemblyOutcomeMixin {
    @Unique private static final ThreadLocal<Boolean> threads$precision = ThreadLocal.withInitial(() -> false);
    @Redirect(method = "activate", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/recipe/RecipeApplier;applyRecipeOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/Recipe;Z)Ljava/util/List;"), remap = false)
    private static List<ItemStack> threads$result(Level level, ItemStack input, Recipe<?> recipe, boolean bulk) {
        var outputs = RecipeApplier.applyRecipeOn(level, input, recipe, bulk);
        threads$precision.set(input.hasTag() && input.getTag().contains("SequencedAssembly") && outputs.stream().anyMatch(s -> !s.isEmpty()
            && net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(s.getItem()).toString().equals("create:precision_mechanism")));
        return outputs;
    }
    @Inject(method = "activate", at = @At("TAIL"), remap = false)
    private static void threads$finished(TransportedItemStack input, TransportedItemStackHandlerBehaviour handler, DeployerBlockEntity deployer, Recipe<?> recipe, CallbackInfo ci) {
        if (threads$precision.get()) OperationOwners.completed(deployer, "precision_complete", "create:precision_mechanism", "The complete sequence produced a Precision Mechanism");
        threads$precision.remove();
    }
}
