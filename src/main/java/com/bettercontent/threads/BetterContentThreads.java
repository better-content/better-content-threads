package com.bettercontent.threads;

import com.bettercontent.threads.compat.ArsNouveauThreads;
import com.bettercontent.threads.compat.Ae2Threads;
import com.bettercontent.threads.compat.BloodMagicThreads;
import com.bettercontent.threads.compat.CreatingSpaceThreads;
import com.bettercontent.threads.compat.DynamicTreeThreads;
import com.bettercontent.threads.compat.GoetyThreads;
import com.bettercontent.threads.compat.PneumaticThreads;
import com.bettercontent.threads.compat.PowerGridThreads;
import com.bettercontent.threads.compat.RelicsThreads;
import com.bettercontent.threads.compat.SereneSeasonsThreads;
import com.bettercontent.threads.compat.TConstructThreads;
import com.bettercontent.threads.compat.WeatherTwoThreads;
import com.bettercontent.threads.compat.bettercontent.DimensionDrinkThreads;
import com.bettercontent.threads.compat.bettercontent.ArcaneChunkLoaderThreads;
import com.bettercontent.threads.compat.bettercontent.EconomyThreads;
import com.bettercontent.threads.compat.bettercontent.HeatSyncThreads;
import com.bettercontent.threads.compat.bettercontent.RpgStatsThreads;
import com.bettercontent.threads.compat.bettercontent.RealisticOresThreads;
import com.bettercontent.threads.compat.bettercontent.SettlementRoadThreads;
import com.bettercontent.threads.compat.bettercontent.WaterSurvivalThreads;
import com.bettercontent.threads.compat.bettercontent.WorldLifecycleThreads;
import com.bettercontent.threads.compat.bettercontent.BetterContentFixesThreads;
import com.bettercontent.threads.compat.bettercontent.PlayerTraceThreads;
import com.bettercontent.threads.compat.bettercontent.SystemicSalienceThreads;
import com.bettercontent.threads.compat.bettercontent.DownedRevivalThreads;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

@Mod(BetterContentThreads.MOD_ID)
public final class BetterContentThreads {
    public static final String MOD_ID = "better_content_threads";

    public BetterContentThreads() {
        ThreadRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ThreadNetwork.register();
        MinecraftForge.EVENT_BUS.register(ThreadEvents.class);
        MinecraftForge.EVENT_BUS.register(NativeAdvancementThreads.class);
        MinecraftForge.EVENT_BUS.register(PackActionThreads.class);
        if (ModList.get().isLoaded("ae2")) {
            MinecraftForge.EVENT_BUS.register(Ae2Threads.class);
        }
        if (ModList.get().isLoaded("arcane_chunk_loaders")) {
            MinecraftForge.EVENT_BUS.register(ArcaneChunkLoaderThreads.class);
        }
        if (ModList.get().isLoaded("better_content_economy")) {
            MinecraftForge.EVENT_BUS.register(EconomyThreads.class);
        }
        if (ModList.get().isLoaded("heat_sync")) {
            MinecraftForge.EVENT_BUS.register(HeatSyncThreads.class);
        }
        if (ModList.get().isLoaded("settlement_roads")) {
            MinecraftForge.EVENT_BUS.register(SettlementRoadThreads.class);
        }
        if (ModList.get().isLoaded("water_survival")) {
            MinecraftForge.EVENT_BUS.register(WaterSurvivalThreads.class);
        }
        if (ModList.get().isLoaded("world_lifecycle_manager")) {
            MinecraftForge.EVENT_BUS.register(WorldLifecycleThreads.class);
        }
        if (ModList.get().isLoaded("better_content_fixes")) {
            MinecraftForge.EVENT_BUS.register(BetterContentFixesThreads.class);
        }
        if (ModList.get().isLoaded("player_traces")) {
            MinecraftForge.EVENT_BUS.register(PlayerTraceThreads.class);
        }
        if (ModList.get().isLoaded("systemic_salience")) {
            MinecraftForge.EVENT_BUS.register(SystemicSalienceThreads.class);
        }
        if (ModList.get().isLoaded("downed_player_revival")) {
            MinecraftForge.EVENT_BUS.register(DownedRevivalThreads.class);
        }
        if (ModList.get().isLoaded("dimension_drink")) {
            MinecraftForge.EVENT_BUS.register(DimensionDrinkThreads.class);
        }
        if (ModList.get().isLoaded("rpg_stats")) {
            MinecraftForge.EVENT_BUS.register(RpgStatsThreads.class);
        }
        if (ModList.get().isLoaded("realistic_ores")) {
            MinecraftForge.EVENT_BUS.register(RealisticOresThreads.class);
        }
        if (ModList.get().isLoaded("dynamictrees")) {
            MinecraftForge.EVENT_BUS.register(DynamicTreeThreads.class);
        }
        if (ModList.get().isLoaded("sereneseasons")) {
            MinecraftForge.EVENT_BUS.register(SereneSeasonsThreads.class);
        }
        if (ModList.get().isLoaded("weather2")) {
            MinecraftForge.EVENT_BUS.register(WeatherTwoThreads.class);
        }
        if (ModList.get().isLoaded("pneumaticcraft")) {
            MinecraftForge.EVENT_BUS.register(PneumaticThreads.class);
        }
        if (ModList.get().isLoaded("powergrid")) {
            MinecraftForge.EVENT_BUS.register(PowerGridThreads.class);
        }
        if (ModList.get().isLoaded("creatingspace")) {
            MinecraftForge.EVENT_BUS.register(CreatingSpaceThreads.class);
        }
        if (ModList.get().isLoaded("ars_nouveau")) {
            MinecraftForge.EVENT_BUS.register(ArsNouveauThreads.class);
        }
        if (ModList.get().isLoaded("bloodmagic")) {
            MinecraftForge.EVENT_BUS.register(BloodMagicThreads.class);
        }
        if (ModList.get().isLoaded("goety")) {
            MinecraftForge.EVENT_BUS.register(GoetyThreads.class);
        }
        if (ModList.get().isLoaded("tconstruct")) {
            MinecraftForge.EVENT_BUS.register(TConstructThreads.class);
        }
        if (ModList.get().isLoaded("relics")) {
            MinecraftForge.EVENT_BUS.register(RelicsThreads.class);
        }
    }
}
