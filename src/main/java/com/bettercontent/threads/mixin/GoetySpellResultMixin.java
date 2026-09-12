package com.bettercontent.threads.mixin;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.utils.SEHelper;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;
@Pseudo
@Mixin(targets = "com.Polarice3.Goety.common.items.magic.DarkWand", remap = false)
public abstract class GoetySpellResultMixin {
    @Unique private final Map<UUID, Integer> threads$souls = new HashMap<>();
    @Inject(method = "MagicResults(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lcom/Polarice3/Goety/api/magic/ISpell;)V", at = @At("HEAD"), remap = false)
    private void threads$before(ItemStack wand, Level level, LivingEntity caster, ISpell spell, CallbackInfo ci) {
        if (caster instanceof ServerPlayer player) threads$souls.put(player.getUUID(), SEHelper.getSoulAmountInt(player));
    }
    @Inject(method = "MagicResults(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lcom/Polarice3/Goety/api/magic/ISpell;)V", at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/api/magic/ISpell;SpellResult(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/common/magic/SpellStat;)V", shift = At.Shift.AFTER), remap = false)
    private void threads$resolved(ItemStack wand, Level level, LivingEntity caster, ISpell spell, CallbackInfo ci) {
        if (!(caster instanceof ServerPlayer player)) return;
        Integer before = threads$souls.get(player.getUUID());
        if (before != null && SEHelper.getSoulAmountInt(player) < before)
            ThreadSignals.emit(player, "soul_work", "goety", UUID.randomUUID().toString());
    }
    @Inject(method = "MagicResults(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lcom/Polarice3/Goety/api/magic/ISpell;)V", at = @At("RETURN"), remap = false)
    private void threads$clear(ItemStack wand, Level level, LivingEntity caster, ISpell spell, CallbackInfo ci) { threads$souls.remove(caster.getUUID()); }
}
