package com.bettercontent.learningsurfaces.compat.bettercontent;
import com.bettercontent.economy.api.event.SpiritReleasedEvent;
import com.bettercontent.economy.api.event.SpiritAcquiredEvent;
import com.bettercontent.learningsurfaces.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class EconomyThreads {
    private EconomyThreads() {}
    @SubscribeEvent public static void spiritAcquired(SpiritReleasedEvent event) {
        if (com.bettercontent.economy.spirit.SpiritKind.fromId(event.getSpiritId().toString()) == null) return;
        ThreadSignals.emit(event.getPlayer(), "spirit_acquired", event.getSpiritId().toString(), event.getSpiritEntityUuid().toString());
    }
    @SubscribeEvent public static void pickedUp(SpiritAcquiredEvent event) {
        if (com.bettercontent.economy.spirit.SpiritKind.fromId(event.getSpiritId().toString()) == null) return;
        ThreadSignals.emit(event.getPlayer(), "spirit_acquired", "first", event.getSpiritEntityUuid().toString());
    }
    @SubscribeEvent public static void marketContact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof AbstractVillager villager)) return;
        boolean spiritOffer = villager.getOffers().stream().anyMatch(offer -> {
            var currency = ForgeRegistries.ITEMS.getKey(offer.getCostA().getItem());
            return currency != null && currency.getNamespace().equals("malum") && currency.getPath().contains("spirit");
        });
        if (spiritOffer) ThreadSignals.emit(player, "spirit_market_contact", "first", player.getUUID() + ":market:" + villager.getUUID());
    }
}
