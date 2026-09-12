package com.bettercontent.threads;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID)
public final class DeathHintEvents {
    private static final java.util.Set<java.util.UUID> FINALIZED = new java.util.HashSet<>();
    public static final DeathHintContext CONTEXT = new DeathHintContext();

    public static String classify(DamageSource source) {
        return DeathHintContext.classify(source.typeHolder().unwrapKey().map(k -> k.location().toString()).orElse(""),
            source.is(DamageTypeTags.IS_FIRE), source.is(DamageTypeTags.IS_FALL), source.is(DamageTypeTags.IS_PROJECTILE),
            source.is(DamageTypeTags.IS_EXPLOSION), source.is(DamageTypeTags.IS_FREEZING), source.getEntity() != null);
    }
    public static void finalDeath(ServerPlayer player, DamageSource source) {
        if (FINALIZED.add(player.getUUID())) ThreadNetwork.deathHint(player, classify(source));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("downed_player_revival")) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            finalDeath(player, event.getSource());
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { CONTEXT.clear(event.getEntity().getUUID()); FINALIZED.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) { CONTEXT.clear(event.getEntity().getUUID()); FINALIZED.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { CONTEXT.clear(); FINALIZED.clear(); }
}
