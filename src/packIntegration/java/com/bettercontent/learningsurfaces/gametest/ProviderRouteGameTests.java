package com.bettercontent.learningsurfaces.gametest;

import com.bettercontent.bumblezonecultivars.api.event.CultivarHarvestEvent;
import com.bettercontent.bumblezonecultivars.api.event.WildVegetableDropEvent;
import com.bettercontent.depthdirector.api.event.CavePressureStartedEvent;
import com.bettercontent.depthdirector.api.event.CaveWarningEvent;
import com.bettercontent.dimensiondrink.api.event.FontEnterEvent;
import com.bettercontent.economy.api.event.SpiritAcquiredEvent;
import com.bettercontent.heatsync.api.event.FoodThermalEpisodeEvent;
import com.bettercontent.learningsurfaces.ThreadPlayerState;
import com.bettercontent.systemicsalience.api.event.MetabolicDiscoveryEvent;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Real provider events traverse the Forge bus into the registered adapters. */
@GameTestHolder("learning_surfaces")
@PrefixGameTestTemplate(false)
public final class ProviderRouteGameTests {
    @GameTest(template = "empty") public static void depth_pressure_and_warning_routes(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new CavePressureStartedEvent(p, 21));
            MinecraftForge.EVENT_BUS.post(new CaveWarningEvent(p, UUID.randomUUID(), BlockPos.ZERO));
            known(h, p, "underground_pressure_builds", "cave_warning");
        });
    }

    @GameTest(template = "empty") public static void wild_cultivar_and_owned_harvest_routes(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new WildVegetableDropEvent(p, BlockPos.ZERO));
            MinecraftForge.EVENT_BUS.post(new CultivarHarvestEvent(p.serverLevel(), p.getUUID(), BlockPos.ZERO,
                "test_cultivar", UUID.randomUUID()));
            known(h, p, "bring_planting_stock_home", "transplanted_cultivar");
        });
    }

    @GameTest(template = "empty") public static void thermal_food_route(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new FoodThermalEpisodeEvent(p,
                new ResourceLocation("minecraft", "apple"), "gametest:frozen", FoodThermalEpisodeEvent.Stage.FROZEN_USE_REJECTED));
            known(h, p, "frozen_food");
        });
    }

    @GameTest(template = "empty") public static void metabolic_episode_route(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new MetabolicDiscoveryEvent(p,
                MetabolicDiscoveryEvent.Kind.SUGAR_CRASH, "Confirmed tempo crash"));
            known(h, p, "sugar_crash");
        });
    }

    @GameTest(template = "empty") public static void spirit_acquisition_route(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new SpiritAcquiredEvent(p,
                new ResourceLocation("malum", "arcane_spirit"), 1, UUID.randomUUID()));
            known(h, p, "spirits_are_materials");
        });
    }

    @GameTest(template = "empty") public static void font_transit_route(GameTestHelper h) {
        withPlayer(h, p -> {
            MinecraftForge.EVENT_BUS.post(new FontEnterEvent(p, UUID.randomUUID(),
                new ResourceLocation("dimension_drink", "test_font"), p.serverLevel().dimension(),
                new ResourceLocation("dimension_drink", "test_aggregate")));
            known(h, p, "font_travel");
        });
    }

    private static void known(GameTestHelper h, ServerPlayer p, String... ids) {
        for (String id : ids) h.assertTrue(ThreadPlayerState.get(p).known.contains(id), "Expected provider route " + id);
    }

    private static void withPlayer(GameTestHelper h, Consumer<ServerPlayer> scenario) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "route-test"));
        try { scenario.accept(player); h.succeed(); }
        finally { ThreadPlayerState.forget(player); }
    }
}
