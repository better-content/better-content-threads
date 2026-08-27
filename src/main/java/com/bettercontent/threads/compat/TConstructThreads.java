package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.events.TinkerToolEvent;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.smeltery.block.entity.controller.SmelteryBlockEntity;

/** Correlates an actually used worn tool with its actual station repair output. */
public final class TConstructThreads {
    private static final String TOKEN = "BetterContentThreadsTinkerRepairToken";
    private static final String DAMAGE = "BetterContentThreadsTinkerRepairDamage";
    private static final String ALLOY = "BetterContentThreadsAlloyEpisode";
    private TConstructThreads() {}

    @SubscribeEvent
    public static void wornToolUsed(TinkerToolEvent.ToolHarvestEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        int damage = event.getTool().getDamage();
        int total = damage + event.getTool().getCurrentDurability();
        if (damage <= 0 || total <= 0 || damage * 2 < total) return;
        String token = player.getUUID() + ":tinker-repair:" + player.server.getTickCount();
        event.getStack().getOrCreateTag().putString(TOKEN, token);
        event.getStack().getOrCreateTag().putInt(DAMAGE, damage);
        ThreadSignals.emit(player, "tool_worn", "tconstruct", token);
    }

    public static void repaired(ServerPlayer player, ItemStack output) {
        if (!output.hasTag()) return;
        String token = output.getTag().getString(TOKEN);
        int before = output.getTag().getInt(DAMAGE);
        if (token.isBlank() || token.length() > 128 || before <= 0 || ToolStack.from(output).getDamage() >= before) return;
        if (!token.equals(ThreadSignals.activeCorrelation(player, "hands_learn_repair"))) return;
        ThreadSignals.emit(player, "tool_repaired", "correlated", token);
        output.getTag().remove(TOKEN);
        output.getTag().remove(DAMAGE);
    }

    @SubscribeEvent
    public static void inspectAlloyableSmeltery(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level().getBlockEntity(event.getPos()) instanceof SmelteryBlockEntity smeltery)
            || !smeltery.getAlloyingModule().canAlloy()) return;
        String token = player.getUUID() + ":alloy:" + event.getPos().asLong() + ":" + player.server.getTickCount();
        var state = new net.minecraft.nbt.CompoundTag();
        state.putString("dimension", player.level().dimension().location().toString());
        state.putLong("position", event.getPos().asLong());
        state.putString("token", token);
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ALLOY, state);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        ThreadSignals.emit(player, "smeltery_alloyable", "tconstruct", token);
    }

    public static void alloyed(BlockEntity smeltery) {
        if (smeltery.getLevel() == null || smeltery.getLevel().isClientSide || smeltery.getLevel().getServer() == null) return;
        String dimension = smeltery.getLevel().dimension().location().toString();
        long position = smeltery.getBlockPos().asLong();
        for (ServerPlayer player : smeltery.getLevel().getServer().getPlayerList().getPlayers()) {
            var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            var state = persisted.getCompound(ALLOY);
            String token = state.getString("token");
            if (!dimension.equals(state.getString("dimension")) || position != state.getLong("position")
                || !token.equals(ThreadSignals.activeCorrelation(player, "materials_temperaments"))) continue;
            ThreadSignals.emit(player, "alloy_cast", "tconstruct", token);
            persisted.remove(ALLOY);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }
}
