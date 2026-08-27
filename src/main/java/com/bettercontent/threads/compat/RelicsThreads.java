package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import it.hurts.sskirillss.relics.api.events.leveling.ExperienceAddEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public final class RelicsThreads {
    private static final String RELIC_TOKEN = "BetterContentThreadsRelicToken";
    private static final String RELIC_CONTEXT = "BetterContentThreadsRelicContext";

    private RelicsThreads() {}

    @SubscribeEvent
    public static void relicExperience(ExperienceAddEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        ItemStack stack = event.getStack();
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !itemId.getNamespace().equals("relics")) return;

        CompoundTag tag = stack.getOrCreateTag();
        String context = context(player);
        String active = ThreadSignals.activeCorrelation(player, "relics_remember_wearers");
        if (active == null) {
            // The token lives on the same physical relic, not merely the same item type.
            String token = player.getUUID() + ":relic:" + UUID.randomUUID();
            tag.putString(RELIC_TOKEN, token);
            tag.putString(RELIC_CONTEXT, context);
            ThreadSignals.emit(player, "relic_activation", "first", token);
        } else if (active.equals(tag.getString(RELIC_TOKEN)) && !context.equals(tag.getString(RELIC_CONTEXT))) {
            ThreadSignals.emit(player, "relic_activation", "correlated_second", active);
            tag.putString(RELIC_CONTEXT, context);
        }
    }

    private static String context(ServerPlayer player) {
        if (player.isUnderWater()) return "underwater";
        if (player.getLastHurtByMob() != null || player.getLastHurtMob() != null) return "combat";
        if (player.isFallFlying()) return "flight";
        if (player.isSprinting()) return "sprinting";
        return player.level().dimension().location().toString();
    }
}
