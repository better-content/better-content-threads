package com.bettercontent.threads;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Correlates Create's own action-backed advancement criteria. These are not
 * inventory milestones: each mapped advancement is awarded by a native Create
 * trigger at the moment the represented mechanism actually operates.
 */
public final class NativeAdvancementThreads {
    private NativeAdvancementThreads() {}

    @SubscribeEvent
    public static void earned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        switch (event.getAdvancement().getId().toString()) {
            case "create:hand_crank_000" -> reveal(player, "create_manual_start", "hand_crank");
            case "create:mechanical_press" -> complete(player, "motion_becomes_industry", "create_manual_complete", "mechanical_press");
            case "create:water_wheel" -> reveal(player, "kinetic_source", "water");
            case "create:mechanical_pump_0" -> complete(player, "rivers_turn_work", "kinetic_complete", "water");
            case "create:deployer" -> reveal(player, "precision_start", "create:precision_mechanism");
            case "create:precision_mechanism" -> complete(player, "precision_has_rhythm", "precision_complete", "create:precision_mechanism");
            case "create:train" -> reveal(player, "train_stations", "linked");
            case "create:long_travel" -> complete(player, "rails_turn_distance", "train_ride", "128");
            case "pneumaticcraft:pressure_chamber" -> complete(player, "pressure_changes_matter", "pressure_complete", "pressure_chamber");
            default -> { }
        }
    }

    private static void reveal(ServerPlayer player, String type, String value) {
        ThreadSignals.emit(player, type, value,
            player.getUUID() + ":create:" + value + ":" + player.server.getTickCount());
    }

    private static void complete(ServerPlayer player, String card, String type, String value) {
        String token = ThreadSignals.activeCorrelation(player, card);
        if (token != null) ThreadSignals.emit(player, type, value, token);
    }
}
