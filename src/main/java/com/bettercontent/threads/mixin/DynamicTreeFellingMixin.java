package com.bettercontent.threads.mixin;
import com.bettercontent.threads.compat.DynamicTreeThreads;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "com.ferreusveritas.dynamictrees.block.branch.BranchBlock", remap = false)
public abstract class DynamicTreeFellingMixin {
    @Inject(method = "futureBreak", at = @At(value = "INVOKE", target = "Lcom/ferreusveritas/dynamictrees/entity/FallingTreeEntity;dropTree(Lnet/minecraft/world/level/Level;Lcom/ferreusveritas/dynamictrees/util/BranchDestructionData;Ljava/util/List;Lcom/ferreusveritas/dynamictrees/entity/FallingTreeEntity$DestroyType;)Lcom/ferreusveritas/dynamictrees/entity/FallingTreeEntity;", shift = At.Shift.AFTER), remap = false)
    private void threads$felled(BlockState state, Level level, BlockPos pos, LivingEntity breaker, CallbackInfo ci) {
        if (breaker instanceof ServerPlayer player) DynamicTreeThreads.felled(player, state);
    }
}
