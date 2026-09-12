package com.bettercontent.threads;

import com.bettercontent.threads.compat.CreatingSpaceThreads;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import java.util.*;

/** Player lifecycle and committed cross-damage in the player's tracked encounter. */
public final class ThreadEvents {
    private static final Map<UUID, HostileCollision> COLLISIONS = new HashMap<>();
    private static final class HostileCollision {
        final Set<UUID> hostiles = new LinkedHashSet<>();
        long lastTargetTick;
        final String token = UUID.randomUUID().toString();
    }
    @SubscribeEvent public static void reload(AddReloadListenerEvent event) { event.addListener(ThreadDefinitions.INSTANCE); }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean successor = ModList.get().isLoaded("world_lifecycle_manager")
                && com.bettercontent.threads.compat.bettercontent.WorldLifecycleThreads.verifySuccessor(player);
            ThreadSignals.login(player);
            if (successor) {
                ThreadSignals.emit(player, "lineage", "successor", player.getUUID() + ":generation:" + ThreadPlayerState.currentGeneration(player.server));
                if (ThreadPlayerState.get(player).discovered.contains("lineage_successor"))
                    com.bettercontent.threads.compat.bettercontent.WorldLifecycleThreads.successorRecorded(player);
            }
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ThreadPlayerState.get(player).save(player);
            ThreadPlayerState.forget(player);
            COLLISIONS.remove(player.getUUID());
        }
    }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ModList.get().isLoaded("creatingspace"))
            CreatingSpaceThreads.arrived(player, event.getTo().location().toString());
    }
    @SubscribeEvent public static void targetChanged(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)
            || !(event.getNewTarget() instanceof ServerPlayer player)) return;
        var collision = COLLISIONS.computeIfAbsent(player.getUUID(), ignored -> new HostileCollision());
        collision.hostiles.add(mob.getUUID());
        collision.lastTargetTick = player.server.getTickCount();
        while (collision.hostiles.size() > 64) collision.hostiles.remove(collision.hostiles.iterator().next());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damaged(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0) return;
        var attacker = event.getSource().getEntity();
        if (!(attacker instanceof Mob) || !(attacker instanceof Enemy)
            || !(event.getEntity() instanceof Mob) || !(event.getEntity() instanceof Enemy)) return;
        var server = attacker.level().getServer();
        if (server == null) return;
        for (var player : server.getPlayerList().getPlayers()) {
            var collision = COLLISIONS.get(player.getUUID());
            if (collision != null && server.getTickCount() - collision.lastTargetTick <= 20 * 45
                && collision.hostiles.contains(attacker.getUUID()) && collision.hostiles.contains(event.getEntity().getUUID()))
                ThreadSignals.emit(player, "hostile_collision", "cross_damage", collision.token);
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 20 == 0)
            COLLISIONS.values().removeIf(c -> event.getServer().getTickCount() - c.lastTargetTick > 20 * 45);
    }
}
