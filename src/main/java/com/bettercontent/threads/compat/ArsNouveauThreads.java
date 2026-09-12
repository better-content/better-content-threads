package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import com.hollingsworth.arsnouveau.api.event.EffectResolveEvent;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

/** Authorship plus a demonstrated world/target change at native effect resolution. */
public final class ArsNouveauThreads {
    private static final String ROOT = "BetterContentThreadsAuthoredSpells";
    private static final Map<Object, String> BEFORE = new WeakHashMap<>();
    private ArsNouveauThreads() {}
    public static void authored(ServerPlayer player, Spell spell) {
        if (spell == null || !spell.isValid() || spell.getSpellSize() < 2) return;
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var authored = persisted.getCompound(ROOT);
        authored.putBoolean(signature(spell), true);
        persisted.put(ROOT, authored); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void before(EffectResolveEvent.Pre event) {
        if (!event.isCanceled() && event.shooter instanceof ServerPlayer) BEFORE.put(event.resolver, observation(event));
    }
    @SubscribeEvent public static void effectResolved(EffectResolveEvent.Post event) {
        String before = BEFORE.remove(event.resolver);
        if (!(event.shooter instanceof ServerPlayer player) || event.spell == null || before == null
            || before.equals(observation(event))) return;
        if (!player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(ROOT).getBoolean(signature(event.spell))) return;
        String token = UUID.randomUUID().toString();
        ThreadSignals.emit(player, "spell_effect", "ars_nouveau", token);
        boolean elemental = event.resolveEffect.getRegistryName().getNamespace().equals("ars_elemental")
            || event.spellStats.getAugments().stream().anyMatch(a -> a.getRegistryName().getNamespace().equals("ars_elemental"));
        if (elemental) ThreadSignals.emit(player, "spell_element", "triggered", token);
    }
    private static String observation(EffectResolveEvent event) {
        var hit = event.rayTraceResult.getLocation();
        var pos = BlockPos.containing(hit);
        StringBuilder value = new StringBuilder();
        for (var b : BlockPos.betweenClosed(pos.offset(-1,-1,-1), pos.offset(1,1,1)))
            value.append(event.world.getBlockState(b)).append(';');
        if (event.rayTraceResult instanceof EntityHitResult entityHit) append(value, entityHit.getEntity());
        append(value, event.shooter);
        event.world.getEntities((Entity)null, new AABB(pos).inflate(4)).stream()
            .map(Entity::getUUID).sorted().forEach(value::append);
        return value.toString();
    }
    private static void append(StringBuilder value, Entity entity) {
        value.append(entity.getUUID()).append(entity.position()).append(entity.getDeltaMovement())
            .append(entity.getRemainingFireTicks()).append(entity.isRemoved());
        if (entity instanceof LivingEntity living) value.append(living.getHealth()).append(living.getActiveEffects());
    }
    private static String signature(Spell spell) {
        return spell.serializeRecipe().stream().map(Object::toString).reduce((a,b) -> a + "," + b).orElse("");
    }
}
